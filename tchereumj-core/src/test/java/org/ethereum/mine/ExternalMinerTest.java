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
package org.tcheum.mine;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.tcheum.config.SystemProperties;
import org.tcheum.config.blockchain.FrontierConfig;
import org.tcheum.core.Block;
import org.tcheum.core.BlockHeader;
import org.tcheum.core.BlockchainImpl;
import org.tcheum.core.ImportResult;
import org.tcheum.db.PruneManager;
import org.tcheum.facade.tcheum;
import org.tcheum.facade.tcheumImpl;
import org.tcheum.listener.CompositetcheumListener;
import org.tcheum.util.ByteUtil;
import org.tcheum.util.blockchain.LocalBlockchain;
import org.tcheum.util.blockchain.StandaloneBlockchain;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.Collection;

import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;


/**
 * Creates an instance
 */
public class ExternalMinerTest {

    private StandaloneBlockchain bc = new StandaloneBlockchain().withAutoblock(false);

    private CompositetcheumListener listener = new CompositetcheumListener();

    @Mock
    private tcheumImpl tcheum;

    @InjectMocks
    @Resource
    private BlockMiner blockMiner = new BlockMiner(SystemProperties.getDefault(), listener, bc.getBlockchain(),
            bc.getBlockchain().getBlockStore(), bc.getPendingState());;

    @Before
    public void setup() {
        SystemProperties.getDefault().setBlockchainConfig(new FrontierConfig(new FrontierConfig.FrontierConstants() {
            @Override
            public BigInteger getMINIMUM_DIFFICULTY() {
                return BigInteger.ONE;
            }
        }));

        // Initialize mocks created above
        MockitoAnnotations.initMocks(this);

        when(tcheum.addNewMinedBlock(any(Block.class))).thenAnswer(new Answer<ImportResult>() {
            @Override
            public ImportResult answer(InvocationOnMock invocation) throws Throwable {
                Block block = (Block) invocation.getArguments()[0];
                return bc.getBlockchain().tryToConnect(block);
            }
        });
    }

    @Test
    public void externalMiner_shouldWork() throws Exception {

        final Block startBestBlock = bc.getBlockchain().getBestBlock();

        final SettableFuture<MinerIfc.MiningResult> futureBlock = SettableFuture.create();

        blockMiner.setExternalMiner(new MinerIfc() {
            @Override
            public ListenableFuture<MiningResult> mine(Block block) {
//                System.out.print("Mining requested");
                return futureBlock;
            }

            @Override
            public boolean validate(BlockHeader blockHeader) {
                return true;
            }

            @Override
            public void setListeners(Collection<MinerListener> listeners) {}
        });
        Block b = bc.getBlockchain().createNewBlock(startBestBlock, EMPTY_LIST, EMPTY_LIST);
        Ethash.getForBlock(SystemProperties.getDefault(), b.getNumber()).mineLight(b).get();
        futureBlock.set(new MinerIfc.MiningResult(ByteUtil.byteArrayToLong(b.getNonce()), b.getMixHash(), b));

        assertThat(bc.getBlockchain().getBestBlock().getNumber(), is(startBestBlock.getNumber() + 1));
    }
}
