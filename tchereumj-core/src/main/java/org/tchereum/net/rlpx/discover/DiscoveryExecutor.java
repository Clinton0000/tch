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
package org.tcheum.net.rlpx.discover;

import org.tcheum.net.rlpx.discover.table.KademliaOptions;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscoveryExecutor {

    ScheduledExecutorService discoverer = Executors.newSingleThreadScheduledExecutor();
    ScheduledExecutorService refresher = Executors.newSingleThreadScheduledExecutor();

    NodeManager nodeManager;

    public DiscoveryExecutor(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    public void start() {
        discoverer.scheduleWithFixedDelay(
                new DiscoverTask(nodeManager),
                1, KademliaOptions.DISCOVER_CYCLE, TimeUnit.SECONDS);

        refresher.scheduleWithFixedDelay(
                new RefreshTask(nodeManager),
                1, KademliaOptions.BUCKET_REFRESH, TimeUnit.MILLISECONDS);

    }

    public void close() {
        discoverer.shutdownNow();
        refresher.shutdownNow();
    }
}
