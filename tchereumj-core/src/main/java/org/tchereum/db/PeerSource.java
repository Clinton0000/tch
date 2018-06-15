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
package org.tcheum.db;

import org.apache.commons.lang3.tuple.Pair;
import org.tcheum.datasource.DataSourceArray;
import org.tcheum.datasource.DbSource;
import org.tcheum.datasource.ObjectDataSource;
import org.tcheum.datasource.Serializer;
import org.tcheum.datasource.Source;
import org.tcheum.net.rlpx.Node;
import org.tcheum.util.ByteUtil;
import org.tcheum.util.RLP;
import org.tcheum.util.RLPList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigInteger;

/**
 * Source for {@link org.tcheum.net.rlpx.Node} also known as Peers
 */
public class PeerSource {
    private static final Logger logger = LoggerFactory.getLogger("db");

    // for debug purposes
    public static PeerSource INST;

    private Source<byte[], byte[]> src;

    DataSourceArray<Pair<Node, Integer>> nodes;

    public static final Serializer<Pair<Node, Integer>, byte[]> NODE_SERIALIZER = new Serializer<Pair<Node, Integer>, byte[]>(){

        @Override
        public byte[] serialize(Pair<Node, Integer> value) {
            byte[] nodeRlp = value.getLeft().getRLP();
            byte[] nodeIsDiscovery = RLP.encodeByte(value.getLeft().isDiscoveryNode() ? (byte) 1 : 0);
            byte[] savedReputation = RLP.encodeBigInteger(BigInteger.valueOf(value.getRight()));

            return RLP.encodeList(nodeRlp, nodeIsDiscovery, savedReputation);
        }

        @Override
        public Pair<Node, Integer> deserialize(byte[] bytes) {
            if (bytes == null) return null;

            RLPList nodeElement = (RLPList) RLP.decode2(bytes).get(0);
            byte[] nodeRlp = nodeElement.get(0).getRLPData();
            byte[] nodeIsDiscovery = nodeElement.get(1).getRLPData();
            byte[] savedReputation = nodeElement.get(2).getRLPData();
            Node node = new Node(nodeRlp);
            node.setDiscoveryNode(nodeIsDiscovery != null);

            return Pair.of(node, ByteUtil.byteArrayToInt(savedReputation));
        }
    };

    public PeerSource(Source<byte[], byte[]> src) {
        this.src = src;
        INST = this;
        this.nodes = new DataSourceArray<>(
                new ObjectDataSource<>(src, NODE_SERIALIZER, 512));
    }

    public DataSourceArray<Pair<Node, Integer>> getNodes() {
        return nodes;
    }

    public void clear() {
        if (src instanceof DbSource) {
            ((DbSource) src).reset();
            this.nodes = new DataSourceArray<>(
                    new ObjectDataSource<>(src, NODE_SERIALIZER, 512));
        } else {
            throw new RuntimeException("Not supported");
        }
    }
}
