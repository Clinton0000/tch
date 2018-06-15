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
package org.tcheum.jsontestsuite.suite.builder;

import org.tcheum.core.AccountState;
import org.tcheum.core.Repository;
import org.tcheum.datasource.inmem.HashMapDB;
import org.tcheum.datasource.NoDeleteSource;
import org.tcheum.jsontestsuite.suite.IterableTestRepository;
import org.tcheum.db.RepositoryRoot;
import org.tcheum.db.ByteArrayWrapper;
import org.tcheum.db.ContractDetails;
import org.tcheum.jsontestsuite.suite.ContractDetailsCacheImpl;
import org.tcheum.jsontestsuite.suite.model.AccountTck;

import java.util.HashMap;
import java.util.Map;

import static org.tcheum.jsontestsuite.suite.Utils.parseData;
import static org.tcheum.util.ByteUtil.wrap;

public class RepositoryBuilder {

    public static Repository build(Map<String, AccountTck> accounts){
        HashMap<ByteArrayWrapper, AccountState> stateBatch = new HashMap<>();
        HashMap<ByteArrayWrapper, ContractDetails> detailsBatch = new HashMap<>();

        for (String address : accounts.keySet()) {

            AccountTck accountTCK = accounts.get(address);
            AccountBuilder.StateWrap stateWrap = AccountBuilder.build(accountTCK);

            AccountState state = stateWrap.getAccountState();
            ContractDetails details = stateWrap.getContractDetails();

            stateBatch.put(wrap(parseData(address)), state);

            ContractDetailsCacheImpl detailsCache = new ContractDetailsCacheImpl(details);
            detailsCache.setDirty(true);

            detailsBatch.put(wrap(parseData(address)), detailsCache);
        }

        Repository repositoryDummy = new IterableTestRepository(new RepositoryRoot(new NoDeleteSource<>(new HashMapDB<byte[]>())));
        Repository track = repositoryDummy.startTracking();

        track.updateBatch(stateBatch, detailsBatch);
        track.commit();
        repositoryDummy.commit();

        return repositoryDummy;
    }
}
