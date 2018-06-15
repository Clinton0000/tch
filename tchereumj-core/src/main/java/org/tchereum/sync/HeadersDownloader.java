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
package org.tcheum.sync;

import org.tcheum.core.BlockHeaderWrapper;
import org.tcheum.core.BlockWrapper;
import org.tcheum.core.Blockchain;
import org.tcheum.db.DbFlushManager;
import org.tcheum.db.HeaderStore;
import org.tcheum.db.IndexedBlockStore;
import org.tcheum.net.server.Channel;
import org.tcheum.net.server.ChannelManager;
import org.tcheum.validator.BlockHeaderValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.tcheum.util.ByteUtil.toHexString;

/**
 * Created by Anton Nashatyrev on 27.10.2016.
 */
@Component
@Lazy
public class HeadersDownloader extends BlockDownloader {
    private final static Logger logger = LoggerFactory.getLogger("sync");

    @Autowired
    SyncPool syncPool;

    @Autowired
    ChannelManager channelManager;

    @Autowired
    IndexedBlockStore blockStore;

    @Autowired
    HeaderStore headerStore;

    @Autowired
    DbFlushManager dbFlushManager;

    @Autowired
    Blockchain blockchain;

    byte[] genesisHash;

    int headersLoaded  = 0;

    @Autowired
    public HeadersDownloader(BlockHeaderValidator headerValidator) {
        super(headerValidator);
        setHeaderQueueLimit(200000);
        setBlockBodiesDownload(false);
        logger.info("HeaderDownloader created.");
    }

    public void init(byte[] startFromBlockHash) {
        logger.info("HeaderDownloader init: startHash = " + toHexString(startFromBlockHash));
        SyncQueueReverseImpl syncQueue = new SyncQueueReverseImpl(startFromBlockHash, true);
        super.init(syncQueue, syncPool, "HeadersDownloader");
        syncPool.init(channelManager, blockchain);
    }

    @Override
    protected synchronized void pushBlocks(List<BlockWrapper> blockWrappers) {}

    @Override
    protected void pushHeaders(List<BlockHeaderWrapper> headers) {
        if (headers.get(headers.size() - 1).getNumber() == 0) {
            genesisHash = headers.get(headers.size() - 1).getHash();
        }
        if (headers.get(headers.size() - 1).getNumber() == 1) {
            genesisHash = headers.get(headers.size() - 1).getHeader().getParentHash();
        }
        logger.info(name + ": " + headers.size() + " headers loaded: " + headers.get(0).getNumber() + " - " + headers.get(headers.size() - 1).getNumber());
        for (BlockHeaderWrapper header : headers) {
            headerStore.saveHeader(header.getHeader());
            headersLoaded++;
        }
        dbFlushManager.commit();
    }

    /**
     * Headers download could block chain synchronization occupying all peers
     * Prevents this by leaving one peer without work
     * Fallbacks to any peer when low number of active peers available
     */
    @Override
    Channel getAnyPeer() {
        return syncPool.getActivePeersCount() > 2 ? syncPool.getNotLastIdle() : syncPool.getAnyIdle();
    }

    @Override
    protected int getBlockQueueFreeSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected int getMaxHeadersInQueue() {
        return getHeaderQueueLimit();
    }

    public int getHeadersLoaded() {
        return headersLoaded;
    }

    @Override
    protected void finishDownload() {
        stop();
    }

    public byte[] getGenesisHash() {
        return genesisHash;
    }
}
