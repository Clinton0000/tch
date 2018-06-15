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
package org.tcheum.net.client;

import org.tcheum.config.SystemProperties;
import org.tcheum.net.eth.EthVersion;
import org.tcheum.net.shh.ShhHandler;
import org.tcheum.net.swarm.bzz.BzzHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.tcheum.net.eth.EthVersion.fromCode;
import static org.tcheum.net.client.Capability.*;

/**
 * Created by Anton Nashatyrev on 13.10.2015.
 */
@Component
public class ConfigCapabilities {

    SystemProperties config;

    private SortedSet<Capability> AllCaps = new TreeSet<>();

    @Autowired
    public ConfigCapabilities(final SystemProperties config) {
        this.config = config;
        if (config.syncVersion() != null) {
            EthVersion eth = fromCode(config.syncVersion());
            if (eth != null) AllCaps.add(new Capability(ETH, eth.getCode()));
        } else {
            for (EthVersion v : EthVersion.supported())
                AllCaps.add(new Capability(ETH, v.getCode()));
        }

        AllCaps.add(new Capability(SHH, ShhHandler.VERSION));
        AllCaps.add(new Capability(BZZ, BzzHandler.VERSION));
    }

    /**
     * Gets the capabilities listed in 'peer.capabilities' config property
     * sorted by their names.
     */
    public List<Capability> getConfigCapabilities() {
        List<Capability> ret = new ArrayList<>();
        List<String> caps = config.peerCapabilities();
        for (Capability capability : AllCaps) {
            if (caps.contains(capability.getName())) {
                ret.add(capability);
            }
        }
        return ret;
    }

}
