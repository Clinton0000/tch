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
package org.tcheum.net.eth.handler;

import com.google.common.util.concurrent.SettableFuture;
import org.tcheum.core.BlockHeader;
import org.tcheum.net.eth.message.GetBlockHeadersMessage;

import java.util.List;

/**
 * Wraps {@link GetBlockHeadersMessage},
 * adds some additional info required by get headers queue
 *
 * @author Mikhail Kalinin
 * @since 16.02.2016
 */
public class GetBlockHeadersMessageWrapper {

    private GetBlockHeadersMessage message;
    private boolean newHashesHandling = false;
    private boolean sent = false;
    private SettableFuture<List<BlockHeader>> futureHeaders = SettableFuture.create();

    public GetBlockHeadersMessageWrapper(GetBlockHeadersMessage message) {
        this.message = message;
    }

    public GetBlockHeadersMessageWrapper(GetBlockHeadersMessage message, boolean newHashesHandling) {
        this.message = message;
        this.newHashesHandling = newHashesHandling;
    }

    public GetBlockHeadersMessage getMessage() {
        return message;
    }

    public boolean isNewHashesHandling() {
        return newHashesHandling;
    }

    public boolean isSent() {
        return sent;
    }

    public void send() {
        this.sent = true;
    }

    public SettableFuture<List<BlockHeader>> getFutureHeaders() {
        return futureHeaders;
    }
}
