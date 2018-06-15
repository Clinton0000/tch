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
package org.tcheum.config.blockchain;

import org.apache.commons.lang3.ArrayUtils;
import org.tcheum.config.Constants;
import org.tcheum.core.BlockHeader;
import org.tcheum.core.Transaction;

import java.math.BigInteger;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class OlympicConfig extends AbstractConfig {

    public OlympicConfig() {
    }

    public OlympicConfig(Constants constants) {
        super(constants);
    }

    @Override
    public BigInteger getCalcDifficultyMultiplier(BlockHeader curBlock, BlockHeader parent) {
        return BigInteger.valueOf(curBlock.getTimestamp() >= parent.getTimestamp() +
                getConstants().getDURATION_LIMIT() ? -1 : 1);
    }

    @Override
    public long getTransactionCost(Transaction tx) {
        long nonZeroes = tx.nonZeroDataBytes();
        long zeroVals  = ArrayUtils.getLength(tx.getData()) - nonZeroes;

        return getGasCost().getTRANSACTION() + zeroVals * getGasCost().getTX_ZERO_DATA() +
                nonZeroes * getGasCost().getTX_NO_ZERO_DATA();
    }
}
