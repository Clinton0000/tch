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
package org.tcheum.net.swarm.bzz;

import org.tcheum.net.message.Message;
import org.tcheum.net.message.MessageFactory;

/**
 * @author Mikhail Kalinin
 * @since 20.08.2015
 */
public class BzzMessageFactory implements MessageFactory {

    @Override
    public Message create(byte code, byte[] encoded) {

        BzzMessageCodes receivedCommand = BzzMessageCodes.fromByte(code);
        switch (receivedCommand) {
            case STATUS:
                return new BzzStatusMessage(encoded);
            case STORE_REQUEST:
                return new BzzStoreReqMessage(encoded);
            case RETRIEVE_REQUEST:
                return new BzzRetrieveReqMessage(encoded);
            case PEERS:
                return new BzzPeersMessage(encoded);
            default:
                throw new IllegalArgumentException("No such message");
        }
    }
}
