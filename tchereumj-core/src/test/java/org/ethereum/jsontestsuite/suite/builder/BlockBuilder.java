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
package org.tcheum.jsontestsuite.suite.builder;

import org.tcheum.core.Block;
import org.tcheum.core.BlockHeader;
import org.tcheum.core.Transaction;
import org.tcheum.jsontestsuite.suite.Env;
import org.tcheum.jsontestsuite.suite.model.BlockHeaderTck;
import org.tcheum.jsontestsuite.suite.model.TransactionTck;
import org.tcheum.util.ByteUtil;

import java.util.ArrayList;
import java.util.List;

import static org.tcheum.util.BIUtil.toBI;
import static org.tcheum.util.ByteUtil.byteArrayToLong;

public class BlockBuilder {


    public static Block build(BlockHeaderTck header,
                              List<TransactionTck> transactionsTck,
                              List<BlockHeaderTck> unclesTck) {

        if (header == null) return null;

        List<BlockHeader> uncles = new ArrayList<>();
        if (unclesTck != null) for (BlockHeaderTck uncle : unclesTck)
            uncles.add(BlockHeaderBuilder.build(uncle));

        List<Transaction> transactions = new ArrayList<>();
        if (transactionsTck != null) for (TransactionTck tx : transactionsTck)
            transactions.add(TransactionBuilder.build(tx));

        BlockHeader blockHeader = BlockHeaderBuilder.build(header);
        Block block = new Block(
                blockHeader,
                transactions, uncles);

        return block;
    }


    public static Block build(Env env){

        Block block = new Block(
                ByteUtil.EMPTY_BYTE_ARRAY,
                ByteUtil.EMPTY_BYTE_ARRAY,
                env.getCurrentCoinbase(),
                ByteUtil.EMPTY_BYTE_ARRAY,
                env.getCurrentDifficulty(),

                byteArrayToLong(env.getCurrentNumber()),
                env.getCurrentGasLimit(),
                0L,
                byteArrayToLong(env.getCurrentTimestamp()),
                new byte[32],
                ByteUtil.ZERO_BYTE_ARRAY,
                ByteUtil.ZERO_BYTE_ARRAY,
                null, null);

        return block;
    }
}
