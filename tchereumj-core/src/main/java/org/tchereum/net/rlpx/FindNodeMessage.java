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

import org.tcheum.crypto.ECKey;
import org.tcheum.util.ByteUtil;
import org.tcheum.util.RLP;
import org.tcheum.util.RLPItem;
import org.tcheum.util.RLPList;

import static org.tcheum.util.ByteUtil.longToBytesNoLeadZeroes;
import static org.tcheum.util.ByteUtil.toHexString;

public class FindNodeMessage extends Message {

    byte[] target;
    long expires;

    @Override
    public void parse(byte[] data) {

        RLPList list = (RLPList) RLP.decode2OneItem(data, 0);

        RLPItem target = (RLPItem) list.get(0);
        RLPItem expires = (RLPItem) list.get(1);

        this.target = target.getRLPData();
        this.expires = ByteUtil.byteArrayToLong(expires.getRLPData());
    }


    public static FindNodeMessage create(byte[] target, ECKey privKey) {

        long expiration = 90 * 60 + System.currentTimeMillis() / 1000;

        /* RLP Encode data */
        byte[] rlpToken = RLP.encodeElement(target);

        byte[] rlpExp = longToBytesNoLeadZeroes(expiration);
        rlpExp = RLP.encodeElement(rlpExp);

        byte[] type = new byte[]{3};
        byte[] data = RLP.encodeList(rlpToken, rlpExp);

        FindNodeMessage findNode = new FindNodeMessage();
        findNode.encode(type, data, privKey);
        findNode.target = target;
        findNode.expires = expiration;

        return findNode;
    }

    public byte[] getTarget() {
        return target;
    }

    public long getExpires() {
        return expires;
    }

    @Override
    public String toString() {

        long currTime = System.currentTimeMillis() / 1000;

        return String.format("[FindNodeMessage] \n target: %s \n expires in %d seconds \n %s\n",
                toHexString(target), (expires - currTime), super.toString());
    }

}
