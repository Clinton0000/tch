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
package org.tcheum.datasource.inmem;

import org.tcheum.datasource.DbSettings;
import org.tcheum.datasource.DbSource;
import org.tcheum.util.ByteArrayMap;
import org.tcheum.util.FastByteComparisons;

import java.util.Map;
import java.util.Set;

/**
 * Created by Anton Nashatyrev on 12.10.2016.
 */
public class HashMapDBSimple<V> implements DbSource<V> {

    protected final Map<byte[], V> storage;

    public HashMapDBSimple() {
        this(new ByteArrayMap<V>());
    }

    public HashMapDBSimple(ByteArrayMap<V> storage) {
        this.storage = storage;
    }

    @Override
    public void put(byte[] key, V val) {
        if (val == null) {
            delete(key);
        } else {
            storage.put(key, val);
        }
    }

    @Override
    public V get(byte[] key) {
        return storage.get(key);
    }

    @Override
    public void delete(byte[] key) {
        storage.remove(key);
    }

    @Override
    public boolean flush() {
        return true;
    }

    @Override
    public void setName(String name) {}

    @Override
    public String getName() {
        return "in-memory";
    }

    @Override
    public void init() {}

    @Override
    public void init(DbSettings settings) {}

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public void close() {}

    @Override
    public Set<byte[]> keys() {
        return getStorage().keySet();
    }

    @Override
    public void reset() {
        storage.clear();
    }

    @Override
    public V prefixLookup(byte[] key, int prefixBytes) {

        for (Map.Entry<byte[], V> e : storage.entrySet())
            if (FastByteComparisons.compareTo(key, 0, prefixBytes, e.getKey(), 0, prefixBytes) == 0) {
                return e.getValue();
            }

        return null;
    }

    @Override
    public void updateBatch(Map<byte[], V> rows) {
        for (Map.Entry<byte[], V> entry : rows.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public Map<byte[], V> getStorage() {
        return storage;
    }
}
