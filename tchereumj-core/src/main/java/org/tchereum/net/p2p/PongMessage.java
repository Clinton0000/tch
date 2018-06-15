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
package org.tcheum.net.p2p;

import org.spongycastle.util.encoders.Hex;

/**
 * Wrapper around an tcheum Pong message on the network
 *
 * @see org.tcheum.net.p2p.P2pMessageCodes#PONG
 */
public class PongMessage extends P2pMessage {

    /**
     * Pong message is always a the same single command payload
     */
    private final static byte[] FIXED_PAYLOAD = Hex.decode("C0");

    @Override
    public byte[] getEncoded() {
        return FIXED_PAYLOAD;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    @Override
    public P2pMessageCodes getCommand() {
        return P2pMessageCodes.PONG;
    }

    @Override
    public String toString() {
        return "[" + this.getCommand().name() + "]";
    }
}