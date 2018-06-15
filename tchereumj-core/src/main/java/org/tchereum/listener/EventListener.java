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
package org.tcheum.listener;

import org.tcheum.core.Block;
import org.tcheum.core.BlockSummary;
import org.tcheum.core.Bloom;
import org.tcheum.core.CallTransaction;
import org.tcheum.core.PendingStateImpl;
import org.tcheum.core.TransactionReceipt;
import org.tcheum.db.ByteArrayWrapper;
import org.tcheum.listener.tcheumListener.PendingTransactionState;
import org.tcheum.util.ByteArrayMap;
import org.tcheum.util.Utils;
import org.tcheum.vm.LogInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.tcheum.sync.BlockDownloader.MAX_IN_REQUEST;
import static org.tcheum.util.ByteUtil.toHexString;

/**
 * The base class for tracking events generated by a Solidity contract
 * For initializing the contract with its address use the [initContractAddress] method
 * For initializing the contract with some topic and any address use the [initContractTopic] method
 *
 * Alternatively you could override initialization and [onLogMatch] method for very specific parsing
 *
 * For example of usage, look at {@link org.tcheum.samples.EventListenerSample}
 *
 * @param <EventData> this is the data generated by the implementing class for
 *                   a Solidity Event. It is created by the [onEvent] implementation
 */
public abstract class EventListener<EventData> {
    private static final Logger logger = LoggerFactory.getLogger("events");

    /**
     * The container class for [EventData] instance(s) with the respect
     * to the transaction which generated events.
     * The transaction pending state is tracked via
     * - onPendingTransactionUpdate callback: to handle Tx state changes on block inclusion, rebranches and rejects
     * - onBlock callback: to handle confirming blocks after the Tx is included
     */
    protected class PendingEvent {
        // The latest transaction receipt either pending/rejected or best branch included
        public TransactionReceipt receipt;

        // The Solidity Events (represented as EventData) generated by transaction
        public List<EventData> eventData;
        // null if pending/rejected
        public Block includedTo;
        // the latest block from the main branch
        public Block bestConfirmingBlock;
        // if came from a block we take block time, it pending we take current time
        public long created;
        // status of the tcheum Tx
        public TxStatus txStatus;

        public PendingEvent(long created) {
            this.created = created;
        }

        public void update(TransactionReceipt receipt, List<EventData> txs, PendingTransactionState state, Block includedTo) {
            this.receipt = receipt;
            this.eventData = txs;
            this.bestConfirmingBlock = state == PendingTransactionState.INCLUDED ? includedTo : null;
            this.includedTo = state == PendingTransactionState.INCLUDED ? includedTo : null;
            txStatus = state.isPending() ? TxStatus.PENDING :
                    (state == PendingTransactionState.DROPPED ? TxStatus.REJECTED : TxStatus.getConfirmed(1));
        }

        public boolean setBestConfirmingBlock(Block bestConfirmingBlock) {
            if (txStatus == TxStatus.REJECTED || txStatus == TxStatus.PENDING) return false;
            if (this.bestConfirmingBlock.isEqual(bestConfirmingBlock)) return false;
            this.bestConfirmingBlock = bestConfirmingBlock;
            txStatus = TxStatus.getConfirmed((int) (bestConfirmingBlock.getNumber() - includedTo.getNumber() + 1));
            return true;
        }

        @Override
        public String toString() {
            return "PendingEvent{" +
                    "eventData=" + eventData +
                    ", includedTo=" + (includedTo == null ? "null" : includedTo.getShortDescr()) +
                    ", bestConfirmingBlock=" + (bestConfirmingBlock == null ? "null" : bestConfirmingBlock.getShortDescr()) +
                    ", created=" + created +
                    ", txStatus=" + txStatus +
                    ", tx=" + receipt.getTransaction() +
                    '}';
        }
    }

    protected LogFilter logFilter;
    protected CallTransaction.Contract contract;
    protected PendingStateImpl pendingState;
    private boolean initialized = false;

    // txHash => PendingEvent
    protected ByteArrayMap<PendingEvent> pendings = new ByteArrayMap<>(new LinkedHashMap<ByteArrayWrapper, PendingEvent>());
    protected Block bestBlock;
    BigInteger lastTotDiff = BigInteger.ZERO;
    // executing tcheumListener callbacks on a separate thread to avoid long running
    // handlers (die to DB access) messing up core
    ExecutorService executor = Executors.newSingleThreadExecutor(r -> new Thread(r, EventListener.this.getClass().getSimpleName() + "-exec"));

    public final tcheumListener listener =  new tcheumListenerAdapter() {
        @Override
        public void onBlock(BlockSummary blockSummary) {
            executor.submit(() -> onBlockImpl(blockSummary));
        }

        @Override
        public void onPendingTransactionUpdate(TransactionReceipt txReceipt, PendingTransactionState state, Block block) {
            executor.submit(() -> onPendingTransactionUpdateImpl(txReceipt, state, block));
        }
    };

    public EventListener(PendingStateImpl pendingState) {
        this.pendingState = pendingState;
    }

    public void onBlockImpl(BlockSummary blockSummary) {
        if (!initialized) throw new RuntimeException("Event listener should be initialized");
        try {
            logger.debug("onBlock: " + blockSummary.getBlock().getShortDescr());

            // ignoring spurious old blocks
            if (bestBlock != null && blockSummary.getBlock().getNumber() < bestBlock.getNumber() - MAX_IN_REQUEST) {
                logger.debug("Ignoring block as too old: " + blockSummary.getBlock().getShortDescr());
                return;
            }

            if (logFilter.matchBloom(new Bloom(blockSummary.getBlock().getLogBloom()))) {
                for (int i = 0; i < blockSummary.getReceipts().size(); i++) {
                    TransactionReceipt receipt = blockSummary.getReceipts().get(i);
                    if (logFilter.matchBloom(receipt.getBloomFilter())) {
                        if (!pendings.containsKey(receipt.getTransaction().getHash())) {
                            // ask PendingState to track candidate transactions from blocks since there is
                            // no guarantee the transaction of interest received as a pending
                            // on included transaction PendingState should call onPendingTransactionUpdateImpl
                            // with INCLUDED state
                            // if Tx was included into fork block PendingState should call onPendingTransactionUpdateImpl
                            // with PENDING state
                            pendingState.trackTransaction(receipt.getTransaction());
                        }
                    }
                }
            }

            if (blockSummary.betterThan(lastTotDiff)) {
                lastTotDiff = blockSummary.getTotalDifficulty();
                bestBlock = blockSummary.getBlock();
                // we need to update pendings bestConfirmingBlock
                newBestBlock(blockSummary.getBlock());
            }
        } catch (Exception e) {
            logger.error("Unexpected error while processing onBlock", e);
        }
    }

    public void onPendingTransactionUpdateImpl(TransactionReceipt txReceipt, PendingTransactionState state, Block block) {
        try {
            if (state != PendingTransactionState.DROPPED || pendings.containsKey(txReceipt.getTransaction().getHash())) {
                logger.debug("onPendingTransactionUpdate: " + toHexString(txReceipt.getTransaction().getHash()) + ", " + state);
            }
            onReceipt(txReceipt, block, state);
        } catch (Exception e) {
            logger.error("Unexpected error while processing onPendingTransactionUpdate", e);
        }
    }

    /**
     *  Initialize listener with the contract of interest and its address
     */
    public synchronized void initContractAddress(String abi, byte[] contractAddress) {
        if (initialized) throw new RuntimeException("Already initialized");
        contract = new CallTransaction.Contract(abi);
        logFilter = new LogFilter().withContractAddress(contractAddress);
        initialized = true;
    }

    /**
     *  Initialize listener with the contract of interest and topic to search for
     */
    public synchronized void initContractTopic(String abi, byte[] topic) {
        if (initialized) throw new RuntimeException("Already initialized");
        contract = new CallTransaction.Contract(abi);
        logFilter = new LogFilter().withTopic(topic);
        initialized = true;
    }

    // checks the Tx receipt for the contract LogEvents
    // initiated on [onPendingTransactionUpdateImpl] callback only
    private synchronized void onReceipt(TransactionReceipt receipt, Block block, PendingTransactionState state) {
        if (!initialized) throw new RuntimeException("Event listener should be initialized");
        byte[] txHash = receipt.getTransaction().getHash();
        if (logFilter.matchBloom(receipt.getBloomFilter())) {
            int txCount = 0; // several transactions are possible withing a single tcheum Tx
            List<EventData> matchedTxs = new ArrayList<>();
            for (LogInfo logInfo : receipt.getLogInfoList()) {
                if (logFilter.matchBloom(logInfo.getBloom()) &&
                        logFilter.matchesExactly(logInfo)) {
                    // This is our contract event, asking implementing class to process it
                    EventData matchedTx = onLogMatch(logInfo, block, receipt, txCount, state);
                    // implementing class may return null if the event is not interesting
                    if (matchedTx != null) {
                        txCount++;
                        matchedTxs.add(matchedTx);
                    }
                }
            }
            if (!matchedTxs.isEmpty()) {
                // cool, we've got some Events from this Tx, let's track further Tx lifecycle
                onEventData(receipt, block, state, matchedTxs);
            }
        } else if (state == PendingTransactionState.DROPPED && pendings.containsKey(txHash)) {
            PendingEvent event = pendings.get(txHash);
            onEventData(receipt, block, state, event.eventData);
        }
    }

    // process the list of [EventData] generated by the Tx
    // initiated on [onPendingTransactionUpdateImpl] callback only
    private void onEventData(TransactionReceipt receipt, Block block,
                             PendingTransactionState state, List<EventData> matchedTxs) {
        byte[] txHash = receipt.getTransaction().getHash();
        PendingEvent event = pendings.get(txHash);
        boolean newEvent = false;
        if (event == null) {
            // new Tx
            event = new PendingEvent(state.isPending() ? Utils.toUnixTime(System.currentTimeMillis()) : block.getTimestamp());
            pendings.put(txHash, event);
            newEvent = true;
        }
        // If the Tx is not new, then update its data with the latest results
        // Tx may change its state (and thus results) several times if it was included
        // to block(s) from different fork branches
        event.update(receipt, matchedTxs, state, block);
        logger.debug("Event " + (newEvent ? "created" : "updated") + ": " + event);

        if (pendingTransactionUpdated(event)) {
            pendings.remove(txHash);
        }
        pendingTransactionsUpdated();
    }

    protected EventData onLogMatch(LogInfo logInfo, Block block, TransactionReceipt receipt, int txCount, PendingTransactionState state) {
        CallTransaction.Invocation event = contract.parseEvent(logInfo);

        if (event == null) {
            logger.error("Can't parse log: " + logInfo);
            return null;
        }

        return onEvent(event, block, receipt, txCount, state);
    }

    /**
     * The implementing subclass should create an EventData instance with the data extracted from
     * Solidity [event]
     * @param event  Representation of the Solidity event which contains ABI to parse Event arguments and the
     *               actual Event arguments
     * @param block  Either block where Tx was included, or PENDING block
     * @param receipt
     * @param txCount The sequence number of this event generated by this Tx. A single Tx might produce
     *                several Events of interest, so the unique key of an Event is TxHash + SeqNumber
     * @param state   The state of Transaction (Pending/Rejected/Included)
     * @return  Either null if this [event] is not interesting for implementation class, or [event] representation
     */
    protected abstract EventData onEvent(CallTransaction.Invocation event, Block block, TransactionReceipt receipt,
                                         int txCount, PendingTransactionState state);

    /**
     * Called after one or more transactions updated
     * (normally on a new best block all the included Txs are updated with confirmed block)
     */
    protected abstract void pendingTransactionsUpdated();

    /**
     * Called on a single transaction update
     * @return true if the implementation is not interested in further tracking of this Tx
     *         (i.e. the transaction is assumed 100% confirmed or it was rejected)
     */
    protected abstract boolean pendingTransactionUpdated(PendingEvent evt);

    // Updates included [pendings] with a new confirming block
    // and removes those we are not interested in anymore
    private synchronized void newBestBlock(Block newBlock) {
        List<byte[]> toRemove = new ArrayList<>();

        boolean updated = false;
        for (PendingEvent event : pendings.values()) {
            if (event.setBestConfirmingBlock(newBlock)) {
                boolean remove = pendingTransactionUpdated(event);
                if (remove) {
                    logger.info("Removing event from pending: " + event);
                    toRemove.add(event.receipt.getTransaction().getHash());
                }
                updated = true;
            }
        }

        pendings.keySet().removeAll(toRemove);

        if (updated) {
            pendingTransactionsUpdated();
        }
    }

}
