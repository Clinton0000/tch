/*
 * Copyright (c) [2016] [ <tch.camp> ]
 * This file is part of the tcheumJ library.
 *
 * The tcheumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The tcheumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the tcheumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.tcheum.facade;

import org.apache.commons.lang3.ArrayUtils;
import org.tcheum.config.BlockchainConfig;
import org.tcheum.config.CommonConfig;
import org.tcheum.config.SystemProperties;
import org.tcheum.core.*;
import org.tcheum.core.PendingState;
import org.tcheum.core.Repository;
import org.tcheum.crypto.ECKey;
import org.tcheum.listener.CompositetcheumListener;
import org.tcheum.listener.tcheumListener;
import org.tcheum.listener.tcheumListenerAdapter;
import org.tcheum.listener.GasPriceTracker;
import org.tcheum.manager.AdminInfo;
import org.tcheum.manager.BlockLoader;
import org.tcheum.manager.WorldManager;
import org.tcheum.mine.BlockMiner;
import org.tcheum.net.client.PeerClient;
import org.tcheum.net.rlpx.Node;
import org.tcheum.net.server.ChannelManager;
import org.tcheum.net.shh.Whisper;
import org.tcheum.net.submit.TransactionExecutor;
import org.tcheum.net.submit.TransactionTask;
import org.tcheum.sync.SyncManager;
import org.tcheum.util.ByteUtil;
import org.tcheum.vm.program.ProgramResult;
import org.tcheum.vm.program.invoke.ProgramInvokeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.FutureAdapter;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.tcheum.util.ByteUtil.toHexString;

/**
 * @author Roman Mandeleil
 * @since 27.07.2014
 */
@Component
public class tcheumImpl implements tcheum, SmartLifecycle {

    private static final Logger logger = LoggerFactory.getLogger("facade");
    private static final Logger gLogger = LoggerFactory.getLogger("general");

    @Autowired
    WorldManager worldManager;

    @Autowired
    AdminInfo adminInfo;

    @Autowired
    ChannelManager channelManager;

    @Autowired
    ApplicationContext ctx;

    @Autowired
    BlockLoader blockLoader;

    @Autowired
    ProgramInvokeFactory programInvokeFactory;

    @Autowired
    Whisper whisper;

    @Autowired
    PendingState pendingState;

    @Autowired
    SyncManager syncManager;

    @Autowired
    CommonConfig commonConfig = CommonConfig.getDefault();

    private SystemProperties config;

    private CompositetcheumListener compositetcheumListener;


    private GasPriceTracker gasPriceTracker = new GasPriceTracker();

    @Autowired
    public tcheumImpl(final SystemProperties config, final CompositetcheumListener compositetcheumListener) {
        this.compositetcheumListener = compositetcheumListener;
        this.config = config;
        System.out.println();
        this.compositetcheumListener.addListener(gasPriceTracker);
        gLogger.info("tcheumJ node started: enode://" + toHexString(config.nodeId()) + "@" + config.externalIp() + ":" + config.listenPort());
    }

    @Override
    public void startPeerDiscovery() {
        worldManager.startPeerDiscovery();
    }

    @Override
    public void stopPeerDiscovery() {
        worldManager.stopPeerDiscovery();
    }

    @Override
    public void connect(InetAddress addr, int port, String remoteId) {
        connect(addr.getHostName(), port, remoteId);
    }

    @Override
    public void connect(final String ip, final int port, final String remoteId) {
        logger.debug("Connecting to: {}:{}", ip, port);
        worldManager.getActivePeer().connectAsync(ip, port, remoteId, false);
    }

    @Override
    public void connect(Node node) {
        connect(node.getHost(), node.getPort(), Hex.toHexString(node.getId()));
    }

    @Override
    public org.tcheum.facade.Blockchain getBlockchain() {
        return (org.tcheum.facade.Blockchain) worldManager.getBlockchain();
    }

    public ImportResult addNewMinedBlock(Block block) {
        ImportResult importResult = worldManager.getBlockchain().tryToConnect(block);
        if (importResult == ImportResult.IMPORTED_BEST) {
            channelManager.sendNewBlock(block);
        }
        return importResult;
    }

    @Override
    public BlockMiner getBlockMiner() {
        return ctx.getBean(BlockMiner.class);
    }

    @Override
    public void addListener(tcheumListener listener) {
        worldManager.addListener(listener);
    }

    @Override
    public void close() {
        logger.info("### Shutdown initiated ### ");
        ((AbstractApplicationContext) getApplicationContext()).close();
    }

    @Override
    public SyncStatus getSyncStatus() {
        return syncManager.getSyncStatus();
    }

    @Override
    public PeerClient getDefaultPeer() {
        return worldManager.getActivePeer();
    }

    @Override
    public boolean isConnected() {
        return worldManager.getActivePeer() != null;
    }

    @Override
    public Transaction createTransaction(BigInteger nonce,
                                         BigInteger gasPrice,
                                         BigInteger gas,
                                         byte[] receiveAddress,
                                         BigInteger value, byte[] data) {

        byte[] nonceBytes = ByteUtil.bigIntegerToBytes(nonce);
        byte[] gasPriceBytes = ByteUtil.bigIntegerToBytes(gasPrice);
        byte[] gasBytes = ByteUtil.bigIntegerToBytes(gas);
        byte[] valueBytes = ByteUtil.bigIntegerToBytes(value);

        return new Transaction(nonceBytes, gasPriceBytes, gasBytes,
                receiveAddress, valueBytes, data, getChainIdForNextBlock());
    }


    @Override
    public Future<Transaction> submitTransaction(Transaction transaction) {

        TransactionTask transactionTask = new TransactionTask(transaction, channelManager);

        final Future<List<Transaction>> listFuture =
                TransactionExecutor.instance.submitTransaction(transactionTask);

        pendingState.addPendingTransaction(transaction);

        return new FutureAdapter<Transaction, List<Transaction>>(listFuture) {
            @Override
            protected Transaction adapt(List<Transaction> adapteeResult) throws ExecutionException {
                return adapteeResult.get(0);
            }
        };
    }

    @Override
    public TransactionReceipt callConstant(Transaction tx, Block block) {
        if (tx.getSignature() == null) {
            tx.sign(ECKey.DUMMY);
        }
        return callConstantImpl(tx, block).getReceipt();
    }

    @Override
    public BlockSummary replayBlock(Block block) {
        List<TransactionReceipt> receipts = new ArrayList<>();
        List<TransactionExecutionSummary> summaries = new ArrayList<>();

        Block parent = worldManager.getBlockchain().getBlockByHash(block.getParentHash());

        if (parent == null) {
            logger.info("Failed to replay block #{}, its ancestor is not presented in the db", block.getNumber());
            return new BlockSummary(block, new HashMap<byte[], BigInteger>(), receipts, summaries);
        }

        Repository track = ((Repository) worldManager.getRepository())
                .getSnapshotTo(parent.getStateRoot());

        try {
            for (Transaction tx : block.getTransactionsList()) {

                Repository txTrack = track.startTracking();
                org.tcheum.core.TransactionExecutor executor = new org.tcheum.core.TransactionExecutor(
                        tx, block.getCoinbase(), txTrack, worldManager.getBlockStore(),
                        programInvokeFactory, block, worldManager.getListener(), 0)
                        .withCommonConfig(commonConfig);

                executor.init();
                executor.execute();
                executor.go();

                TransactionExecutionSummary summary = executor.finalization();

                txTrack.commit();

                TransactionReceipt receipt = executor.getReceipt();
                receipt.setPostTxState(track.getRoot());
                receipts.add(receipt);
                summaries.add(summary);
            }
        } finally {
            track.rollback();
        }

        return new BlockSummary(block, new HashMap<byte[], BigInteger>(), receipts, summaries);
    }

    private org.tcheum.core.TransactionExecutor callConstantImpl(Transaction tx, Block block) {

        Repository repository = ((Repository) worldManager.getRepository())
                .getSnapshotTo(block.getStateRoot())
                .startTracking();

        try {
            org.tcheum.core.TransactionExecutor executor = new org.tcheum.core.TransactionExecutor
                    (tx, block.getCoinbase(), repository, worldManager.getBlockStore(),
                            programInvokeFactory, block, new tcheumListenerAdapter(), 0)
                    .withCommonConfig(commonConfig)
                    .setLocalCall(true);

            executor.init();
            executor.execute();
            executor.go();
            executor.finalization();

            return executor;
        } finally {
            repository.rollback();
        }
    }

    @Override
    public ProgramResult callConstantFunction(String receiveAddress,
                                              CallTransaction.Function function, Object... funcArgs) {
        return callConstantFunction(receiveAddress, ECKey.DUMMY, function, funcArgs);
    }

    @Override
    public ProgramResult callConstantFunction(String receiveAddress, ECKey senderPrivateKey,
                                              CallTransaction.Function function, Object... funcArgs) {
        Transaction tx = CallTransaction.createCallTransaction(0, 0, 100000000000000L,
                receiveAddress, 0, function, funcArgs);
        tx.sign(senderPrivateKey);
        Block bestBlock = worldManager.getBlockchain().getBestBlock();

        return callConstantImpl(tx, bestBlock).getResult();
    }

    @Override
    public org.tcheum.facade.Repository getRepository() {
        return worldManager.getRepository();
    }

    @Override
    public org.tcheum.facade.Repository getLastRepositorySnapshot() {
        return getSnapshotTo(getBlockchain().getBestBlock().getStateRoot());
    }

    @Override
    public org.tcheum.facade.Repository getPendingState() {
        return worldManager.getPendingState().getRepository();
    }

    @Override
    public org.tcheum.facade.Repository getSnapshotTo(byte[] root) {

        Repository repository = (Repository) worldManager.getRepository();
        org.tcheum.facade.Repository snapshot = repository.getSnapshotTo(root);

        return snapshot;
    }

    @Override
    public AdminInfo getAdminInfo() {
        return adminInfo;
    }

    @Override
    public ChannelManager getChannelManager() {
        return channelManager;
    }


    @Override
    public List<Transaction> getWireTransactions() {
        return worldManager.getPendingState().getPendingTransactions();
    }

    @Override
    public List<Transaction> getPendingStateTransactions() {
        return worldManager.getPendingState().getPendingTransactions();
    }

    @Override
    public BlockLoader getBlockLoader() {
        return blockLoader;
    }

    @Override
    public Whisper getWhisper() {
        return whisper;
    }

    @Override
    public long getGasPrice() {
        return gasPriceTracker.getGasPrice();
    }

    @Override
    public Integer getChainIdForNextBlock() {
        BlockchainConfig nextBlockConfig = config.getBlockchainConfig().getConfigForBlock(getBlockchain()
                .getBestBlock().getNumber() + 1);
        return nextBlockConfig.getChainId();
    }

    public CompletableFuture<Void> switchToShortSync() {
        return syncManager.switchToShortSync();
    }

    @Override
    public void exitOn(long number) {
        worldManager.getBlockchain().setExitOn(number);
    }

    @Override
    public void initSyncing() {
        worldManager.initSyncing();
    }


    /**
     * For testing purposes and 'hackers'
     */
    public ApplicationContext getApplicationContext() {
        return ctx;
    }

    @Override
    public boolean isAutoStartup() {
        return false;
    }

    /**
     * Shutting down all app beans
     */
    @Override
    public void stop(Runnable callback) {
        logger.info("Shutting down tcheum instance...");
        worldManager.close();
        callback.run();
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public boolean isRunning() {
        return true;
    }

    /**
     * Called first on shutdown lifecycle
     */
    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
