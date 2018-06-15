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
import org.tcheum.config.SystemProperties;
import org.tcheum.core.Block;
import org.tcheum.core.BlockHeader;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * The adapter of Ethash for MinerIfc
 *
 * Created by Anton Nashatyrev on 26.02.2016.
 */
public class EthashMiner implements MinerIfc {

    SystemProperties config;

    private int cpuThreads;
    private boolean fullMining = true;
    private Set<EthashListener> listeners = new CopyOnWriteArraySet<>();

    public EthashMiner(SystemProperties config) {
        this.config = config;
        cpuThreads = config.getMineCpuThreads();
        fullMining = config.isMineFullDataset();
    }

    @Override
    public ListenableFuture<MiningResult> mine(Block block) {
        return fullMining ?
                Ethash.getForBlock(config, block.getNumber(), listeners).mine(block, cpuThreads) :
                Ethash.getForBlock(config, block.getNumber(), listeners).mineLight(block, cpuThreads);
    }

    @Override
    public boolean validate(BlockHeader blockHeader) {
        return Ethash.getForBlock(config, blockHeader.getNumber(), listeners).validate(blockHeader);
    }

    /**
     * Listeners changes affects only future {@link #mine(Block)} and
     * {@link #validate(BlockHeader)} calls
     * Only instances of {@link EthashListener} are used, because EthashMiner
     * produces only events compatible with it
     */
    @Override
    public void setListeners(Collection<MinerListener> listeners) {
        this.listeners.clear();
        listeners.stream()
                .filter(listener -> listener instanceof EthashListener)
                .map(listener -> (EthashListener) listener)
                .forEach(this.listeners::add);
    }
}
