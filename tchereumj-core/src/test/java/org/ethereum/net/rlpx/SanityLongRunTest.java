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
package org.tcheum.net.rlpx;

import com.typesafe.config.ConfigFactory;
import org.tcheum.config.NoAutoscan;
import org.tcheum.config.SystemProperties;
import org.tcheum.core.Block;
import org.tcheum.core.TransactionReceipt;
import org.tcheum.crypto.ECKey;
import org.tcheum.facade.tcheum;
import org.tcheum.facade.tcheumFactory;
import org.tcheum.listener.tcheumListenerAdapter;
import org.tcheum.net.eth.message.StatusMessage;
import org.tcheum.net.message.Message;
import org.tcheum.net.server.Channel;
import org.tcheum.net.shh.MessageWatcher;
import org.tcheum.net.shh.WhisperMessage;
import org.junit.Ignore;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Anton Nashatyrev on 13.10.2015.
 */
@Ignore
public class SanityLongRunTest {

    @Configuration
    @NoAutoscan
    public static class SysPropConfig1 {
        static SystemProperties props;
        @Bean
        public SystemProperties systemProperties() {
            return props;
        }
    }
    @Configuration
    @NoAutoscan
    public static class SysPropConfig2 {
        static SystemProperties props;
        @Bean
        public SystemProperties systemProperties() {
            return props;
        }
    }

    String config1 =
            "peer.discovery.enabled = true \n" +
//            "peer.discovery.enabled = false \n" +
            "database.dir = testDB-1 \n" +
            "database.reset = true \n" +
            "sync.enabled = true \n" +
//            "sync.enabled = false \n" +
            "peer.capabilities = [eth, shh] \n" +
            "peer.listen.port = 60300 \n" +
            " # derived nodeId = deadbeea2250b3efb9e6268451e74bdbdc5632a1a03a0f5b626f59150ff772ac287e122531b5e8d55ff10cb541bbc8abf5def6bcbfa31cf5923ca3c3d783d312\n" +
            "peer.privateKey = d3a4a240b107ab443d46187306d0b947ce3d6b6ed95aead8c4941afcebde43d2\n" +
            "peer.p2p.version = 4 \n" +
            "peer.p2p.framing.maxSize = 1024 \n";

    ECKey config2Key = new ECKey();
    String config2 =
            "peer.discovery.enabled = false \n" +
            "database.dir = testDB-2 \n" +
            "database.reset = true \n" +
            "sync.enabled = true \n" +
            "peer.capabilities = [eth, shh] \n" +
//            "peer.listen.port = 60300 \n" +
            "peer.privateKey = " + Hex.toHexString(config2Key.getPrivKeyBytes()) + "\n" +
            "peer { active = [" +
            "   { url = \"enode://deadbeea2250b3efb9e6268451e74bdbdc5632a1a03a0f5b626f59150ff772ac287e122531b5e8d55ff10cb541bbc8abf5def6bcbfa31cf5923ca3c3d783d312" +
                "@localhost:60300\" }" +
            "] } \n" +
            "peer.p2p.version = 5 \n" +
            "peer.p2p.framing.maxSize = 1024 \n";


    @Test
    public void testTest() throws FileNotFoundException, InterruptedException {
        SysPropConfig1.props = new SystemProperties(ConfigFactory.parseString(config1));
        SysPropConfig2.props = new SystemProperties(ConfigFactory.parseString(config2));

//        tcheum tcheum1 = tcheumFactory.createtcheum(SysPropConfig1.props, SysPropConfig1.class);
        tcheum tcheum1 = null;

//        Thread.sleep(1000000000);

        tcheum tcheum2 = tcheumFactory.createtcheum(SysPropConfig2.props, SysPropConfig2.class);

        final CountDownLatch semaphore = new CountDownLatch(1);

        tcheum2.addListener(new tcheumListenerAdapter() {
            @Override
            public void onRecvMessage(Channel channel, Message message) {
                if (message instanceof StatusMessage) {
                    System.out.println("=== Status received: " + message);
                    semaphore.countDown();
                }
            }

        });

        semaphore.await(60, TimeUnit.SECONDS);
        if(semaphore.getCount() > 0) {
            throw new RuntimeException("StatusMessage was not received in 60 sec: " + semaphore.getCount());
        }

        final CountDownLatch semaphoreBlocks = new CountDownLatch(1);
        final CountDownLatch semaphoreFirstBlock = new CountDownLatch(1);

        tcheum2.addListener(new tcheumListenerAdapter() {
            int blocksCnt = 0;

            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {
                blocksCnt++;
                if (blocksCnt % 1000 == 0 || blocksCnt == 1) {
                    System.out.println("=== Blocks imported: " + blocksCnt);
                    if (blocksCnt == 1) {
                        semaphoreFirstBlock.countDown();
                    }
                }
                if (blocksCnt >= 10_000) {
                    semaphoreBlocks.countDown();
                    System.out.println("=== Blocks task done.");
                }
            }
        });

        semaphoreFirstBlock.await(180, TimeUnit.SECONDS);
        if(semaphoreFirstBlock.getCount() > 0) {
            throw new RuntimeException("No blocks were received in 60 sec: " + semaphore.getCount());
        }

        // SHH messages exchange
        String identity1 = tcheum1.getWhisper().newIdentity();
        String identity2 = tcheum2.getWhisper().newIdentity();

        final int[] counter1 = new int[1];
        final int[] counter2 = new int[1];

        tcheum1.getWhisper().watch(new MessageWatcher(identity1, null, null) {
            @Override
            protected void newMessage(WhisperMessage msg) {
                System.out.println("=== You have a new message to 1: " + msg);
                counter1[0]++;
            }
        });
        tcheum2.getWhisper().watch(new MessageWatcher(identity2, null, null) {
            @Override
            protected void newMessage(WhisperMessage msg) {
                System.out.println("=== You have a new message to 2: " + msg);
                counter2[0]++;
            }
        });

        System.out.println("=== Sending messages ... ");
        int cnt = 0;
        long end = System.currentTimeMillis() + 60 * 60 * 1000;
        while (semaphoreBlocks.getCount() > 0) {
            tcheum1.getWhisper().send(identity2, "Hello Eth2!".getBytes(), null);
            tcheum2.getWhisper().send(identity1, "Hello Eth1!".getBytes(), null);
            cnt++;
            Thread.sleep(10 * 1000);
            if (counter1[0] != cnt || counter2[0] != cnt) {
                throw new RuntimeException("Message was not delivered in 10 sec: " + cnt);
            }
            if (System.currentTimeMillis() > end) {
                throw new RuntimeException("Wanted blocks amount was not received in a hour");
            }
        }

        tcheum1.close();
        tcheum2.close();

        System.out.println("Passed.");
    }
}
