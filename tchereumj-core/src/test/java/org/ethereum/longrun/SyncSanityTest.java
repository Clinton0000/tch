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
package org.tcheum.longrun;

import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.mutable.MutableObject;
import org.tcheum.config.CommonConfig;
import org.tcheum.config.SystemProperties;
import org.tcheum.facade.tcheum;
import org.tcheum.facade.tcheumFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

/**
 * Sync with sanity check
 *
 * Runs sync with defined config
 * - checks State Trie is not broken
 * - checks whtch all blocks are in blockstore, validates parent connection and bodies
 * - checks and validate transaction receipts
 * Stopped, than restarts in 1 minute, syncs and pass all checks again
 *
 * Run with '-Dlogback.configurationFile=longrun/logback.xml' for proper logging
 * Also following flags are available:
 *     -Dreset.db.onFirstRun=true
 *     -Doverride.config.res=longrun/conf/live.conf
 */
@Ignore
public class SyncSanityTest {

    private tcheum regularNode;
    private static AtomicBoolean firstRun = new AtomicBoolean(true);
    private static final Logger testLogger = LoggerFactory.getLogger("TestLogger");
    private static final MutableObject<String> configPath = new MutableObject<>("longrun/conf/ropsten.conf");
    private static final MutableObject<Boolean> resetDBOnFirstRun = new MutableObject<>(null);
    private static final AtomicBoolean allChecksAreOver =  new AtomicBoolean(false);

    public SyncSanityTest() throws Exception {

        String resetDb = System.getProperty("reset.db.onFirstRun");
        String overrideConfigPath = System.getProperty("override.config.res");
        if (Boolean.parseBoolean(resetDb)) {
            resetDBOnFirstRun.setValue(true);
        } else if (resetDb != null && resetDb.equalsIgnoreCase("false")) {
            resetDBOnFirstRun.setValue(false);
        }
        if (overrideConfigPath != null) configPath.setValue(overrideConfigPath);

        statTimer.scheduleAtFixedRate(() -> {
            try {
                if (fatalErrors.get() > 0) {
                    statTimer.shutdownNow();
                }
            } catch (Throwable t) {
                SyncSanityTest.testLogger.error("Unhandled exception", t);
            }
        }, 0, 15, TimeUnit.SECONDS);
    }

    /**
     * Spring configuration class for the Regular peer
     */
    private static class RegularConfig {

        @Bean
        public RegularNode node() {
            return new RegularNode();
        }

        /**
         * Instead of supplying properties via config file for the peer
         * we are substituting the corresponding bean which returns required
         * config for this instance.
         */
        @Bean
        public SystemProperties systemProperties() {
            SystemProperties props = new SystemProperties();
            props.overrideParams(ConfigFactory.parseResources(configPath.getValue()));
            if (firstRun.get() && resetDBOnFirstRun.getValue() != null) {
                props.setDatabaseReset(resetDBOnFirstRun.getValue());
            }
            return props;
        }
    }

    /**
     * Just regular tcheumJ node
     */
    static class RegularNode extends BasicNode {
        public RegularNode() {
            super("sampleNode");
        }

        @Override
        public void waitForSync() throws Exception {
            testLogger.info("Waiting for the whole blockchain sync (will take up to an hour on fast sync for the whole chain)...");
            while(true) {
                sleep(10000);

                if (syncComplete) {
                    testLogger.info("[v] Sync complete! The best block: " + bestBlock.getShortDescr());

                    // Stop syncing
                    config.setSyncEnabled(false);
                    config.setDiscoveryEnabled(false);
                    tcheum.getChannelManager().close();
                    syncPool.close();

                    return;
                }
            }
        }

        @Override
        public void onSyncDone() throws Exception {
            // Full sanity check
            fullSanityCheck(tcheum, commonConfig);
        }
    }

    private final static AtomicInteger fatalErrors = new AtomicInteger(0);

    private final static long MAX_RUN_MINUTES = 180L;

    private static ScheduledExecutorService statTimer =
            Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "StatTimer"));

    private static boolean logStats() {
        testLogger.info("---------====---------");
        testLogger.info("fatalErrors: {}", fatalErrors);
        testLogger.info("---------====---------");

        return fatalErrors.get() == 0;
    }

    private static void fullSanityCheck(tcheum tcheum, CommonConfig commonConfig) {

        BlockchainValidation.fullCheck(tcheum, commonConfig, fatalErrors);
        logStats();

        if (!firstRun.get()) {
            allChecksAreOver.set(true);
            statTimer.shutdownNow();
        }

        firstRun.set(false);
    }

    @Test
    public void testDoubleCheck() throws Exception {

        runtcheum();

        new Thread(() -> {
            try {
                while(firstRun.get()) {
                    sleep(1000);
                }
                testLogger.info("Stopping first run");
                regularNode.close();
                testLogger.info("First run stopped");
                sleep(60_000);
                testLogger.info("Starting second run");
                runtcheum();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }).start();

        if(statTimer.awaitTermination(MAX_RUN_MINUTES, TimeUnit.MINUTES)) {
            logStats();
            // Checking for errors
            assert allChecksAreOver.get();
            if (!logStats()) assert false;
        }
    }

    public void runtcheum() throws Exception {
        testLogger.info("Starting tcheumJ regular instance!");
        this.regularNode = tcheumFactory.createtcheum(RegularConfig.class);
    }
}
