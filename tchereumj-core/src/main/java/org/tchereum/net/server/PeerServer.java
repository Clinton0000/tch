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
package org.tcheum.net.server;

import org.tcheum.config.SystemProperties;
import org.tcheum.listener.tcheumListener;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import static org.tcheum.util.ByteUtil.toHexString;

/**
 * This class establishes a listener for incoming connections.
 * See <a href="http://netty.io">http://netty.io</a>.
 *
 * @author Roman Mandeleil
 * @since 01.11.2014
 */
@Component
public class PeerServer {

    private static final Logger logger = LoggerFactory.getLogger("net");

    private SystemProperties config;

    private ApplicationContext ctx;

    private tcheumListener tcheumListener;

    public tcheumChannelInitializer tcheumChannelInitializer;

    private boolean listening;

    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
    ChannelFuture channelFuture;

    @Autowired
    public PeerServer(final SystemProperties config, final ApplicationContext ctx,
                      final tcheumListener tcheumListener) {
        this.ctx = ctx;
        this.config = config;
        this.tcheumListener = tcheumListener;
    }

    public void start(int port) {

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        tcheumChannelInitializer = ctx.getBean(tcheumChannelInitializer.class, "");

        tcheumListener.trace("Listening on port " + port);


        try {
            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);

            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.peerConnectionTimeout());

            b.handler(new LoggingHandler());
            b.childHandler(tcheumChannelInitializer);

            // Start the client.
            logger.info("Listening for incoming connections, port: [{}] ", port);
            logger.info("NodeId: [{}] ", toHexString(config.nodeId()));

            channelFuture = b.bind(port).sync();

            listening = true;
            // Wait until the connection is closed.
            channelFuture.channel().closeFuture().sync();
            logger.debug("Connection is closed");

        } catch (Exception e) {
            logger.error("Peer server error: {} ({})", e.getMessage(), e.getClass().getName());
            throw new Error("Server Disconnected");
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            listening = false;
        }
    }

    public void close() {
        if (listening && channelFuture != null && channelFuture.channel().isOpen()) {
            try {
                logger.info("Closing PeerServer...");
                channelFuture.channel().close().sync();
                logger.info("PeerServer closed.");
            } catch (Exception e) {
                logger.warn("Problems closing server channel", e);
            }
        }
    }

    public boolean isListening() {
        return listening;
    }
}
