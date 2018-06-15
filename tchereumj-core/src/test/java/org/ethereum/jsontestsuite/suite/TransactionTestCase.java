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
package org.tcheum.jsontestsuite.suite;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.tcheum.jsontestsuite.suite.model.TransactionTck;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionTestCase {

    private String blocknumber;
    private TransactionTck transaction;
    private String hash;
    private String rlp;
    private String sender;
    private String senderExpect;


    public TransactionTestCase() {
    }

    public String getBlocknumber() {
        return blocknumber;
    }

    public void setBlocknumber(String blocknumber) {
        this.blocknumber = blocknumber;
    }

    public TransactionTck getTransaction() {
        return transaction;
    }

    public void setTransaction(TransactionTck transaction) {
        this.transaction = transaction;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getRlp() {
        return rlp;
    }

    public void setRlp(String rlp) {
        this.rlp = rlp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderExpect() {
        return senderExpect;
    }

    public void setSenderExpect(String senderExpect) {
        this.senderExpect = senderExpect;
    }
}
