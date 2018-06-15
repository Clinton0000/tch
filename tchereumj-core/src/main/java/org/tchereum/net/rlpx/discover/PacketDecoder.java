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
package org.tcheum.net.rlpx.discover;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.tcheum.crypto.ECKey;
import org.tcheum.net.rlpx.Message;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.tcheum.util.ByteUtil.toHexString;

public class PacketDecoder extends MessageToMessageDecoder<DatagramPacket> {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("discover");

    @Override
    public void decode(ChannelHandlerContext ctx, DatagramPacket packet, List<Object> out) throws Exception {
        ByteBuf buf = packet.content();
        byte[] encoded = new byte[buf.readableBytes()];
        buf.readBytes(encoded);
        try {
            Message msg = Message.decode(encoded);
            DiscoveryEvent event = new DiscoveryEvent(msg, packet.sender());
            out.add(event);
        } catch (Exception e) {
            throw new RuntimeException("Exception processing inbound message from " + ctx.channel().remoteAddress() + ": " + toHexString(encoded), e);
        }
    }
}
