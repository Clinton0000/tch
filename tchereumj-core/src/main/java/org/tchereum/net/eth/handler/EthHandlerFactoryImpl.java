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

import org.tcheum.net.eth.EthVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Default factory implementation
 *
 * @author Mikhail Kalinin
 * @since 20.08.2015
 */
@Component
public class EthHandlerFactoryImpl implements EthHandlerFactory {

    @Autowired
    private ApplicationContext ctx;

    @Override
    public EthHandler create(EthVersion version) {
        switch (version) {
            case V62:   return (EthHandler) ctx.getBean("Eth62");
            case V63:   return (EthHandler) ctx.getBean("Eth63");
            default:    throw new IllegalArgumentException("Eth " + version + " is not supported");
        }
    }
}
