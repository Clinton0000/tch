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
package org.tcheum.samples;

import org.tcheum.config.CommonConfig;
import org.tcheum.config.SystemProperties;
import org.tcheum.core.Block;
import org.tcheum.datasource.Source;
import org.tcheum.db.IndexedBlockStore;

import java.util.List;

/**
 * Created by Anton Nashatyrev on 21.07.2016.
 */
public class CheckFork {
    public static void main(String[] args) throws Exception {
        SystemProperties.getDefault().overrideParams("database.dir", "");
        Source<byte[], byte[]> index = CommonConfig.getDefault().cachedDbSource("index");
        Source<byte[], byte[]> blockDS = CommonConfig.getDefault().cachedDbSource("block");

        IndexedBlockStore indexedBlockStore = new IndexedBlockStore();
        indexedBlockStore.init(index, blockDS);

        for (int i = 1_919_990; i < 1_921_000; i++) {
            Block chainBlock = indexedBlockStore.getChainBlockByNumber(i);
            List<Block> blocks = indexedBlockStore.getBlocksByNumber(i);
            String s = chainBlock.getShortDescr() + " (";
            for (Block block : blocks) {
                if (!block.isEqual(chainBlock)) {
                    s += block.getShortDescr() + " ";
                }
            }
            System.out.println(s);
        }
    }
}
