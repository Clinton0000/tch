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
package org.tcheum.net.eth.message;

import org.tcheum.util.RLP;
import org.tcheum.util.RLPList;
import org.tcheum.util.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.tcheum.util.ByteUtil.toHexString;

/**
 * Wrapper around an tcheum GetNodeData message on the network
 * Could contain:
 * - state roots
 * - accounts state roots
 * - accounts code hashes
 *
 * @see EthMessageCodes#GET_NODE_DATA
 */
public class GetNodeDataMessage extends EthMessage {

    /**
     * List of node hashes for which is state requested
     */
    private List<byte[]> nodeKeys;

    public GetNodeDataMessage(byte[] encoded) {
        super(encoded);
    }

    public GetNodeDataMessage(List<byte[]> nodeKeys) {
        this.nodeKeys = nodeKeys;
        this.parsed = true;
    }

    private synchronized void parse() {
        if (parsed) return;
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        this.nodeKeys = new ArrayList<>();
        for (int i = 0; i < paramsList.size(); ++i) {
            nodeKeys.add(paramsList.get(i).getRLPData());
        }

        this.parsed = true;
    }

    private void encode() {
        List<byte[]> encodedElements = new ArrayList<>();
        for (byte[] hash : nodeKeys)
            encodedElements.add(RLP.encodeElement(hash));
        byte[][] encodedElementArray = encodedElements.toArray(new byte[encodedElements.size()][]);

        this.encoded = RLP.encodeList(encodedElementArray);
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }


    @Override
    public Class<NodeDataMessage> getAnswerMessage() {
        return NodeDataMessage.class;
    }

    public List<byte[]> getNodeKeys() {
        parse();
        return nodeKeys;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.GET_NODE_DATA;
    }

    public String toString() {
        parse();

        StringBuilder payload = new StringBuilder();

        payload.append("count( ").append(nodeKeys.size()).append(" ) ");

        if (logger.isDebugEnabled()) {
            for (byte[] hash : nodeKeys) {
                payload.append(toHexString(hash).substring(0, 6)).append(" | ");
            }
            if (!nodeKeys.isEmpty()) {
                payload.delete(payload.length() - 3, payload.length());
            }
        } else {
            payload.append(Utils.getHashListShort(nodeKeys));
        }

        return "[" + getCommand().name() + " " + payload + "]";
    }
}
