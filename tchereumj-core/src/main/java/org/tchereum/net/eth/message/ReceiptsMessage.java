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

import org.tcheum.core.Bloom;
import org.tcheum.core.Transaction;
import org.tcheum.core.TransactionReceipt;
import org.tcheum.util.RLP;
import org.tcheum.util.RLPElement;
import org.tcheum.util.RLPItem;
import org.tcheum.util.RLPList;
import org.tcheum.vm.LogInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around an tcheum Receipts message on the network
 * Tx Receipts grouped by blocks
 *
 * @see EthMessageCodes#RECEIPTS
 */
public class ReceiptsMessage extends EthMessage {

    private List<List<TransactionReceipt>> receipts;

    public ReceiptsMessage(byte[] encoded) {
        super(encoded);
    }

    public ReceiptsMessage(List<List<TransactionReceipt>> receiptList) {
        this.receipts = receiptList;
        parsed = true;
    }

    private synchronized void parse() {
        if (parsed) return;
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        this.receipts = new ArrayList<>();
        for (int i = 0; i < paramsList.size(); ++i) {
            RLPList blockRLP = (RLPList) paramsList.get(i);

            List<TransactionReceipt> blockReceipts = new ArrayList<>();
            for (RLPElement txReceipt : blockRLP) {
                RLPList receiptRLP = (RLPList) txReceipt;
                if (receiptRLP.size() != 4) {
                    continue;
                }
                TransactionReceipt receipt = new TransactionReceipt(receiptRLP);
                blockReceipts.add(receipt);
            }
            this.receipts.add(blockReceipts);
        }
        this.parsed = true;
    }

    private void encode() {
        List<byte[]> blocks = new ArrayList<>();

        for (List<TransactionReceipt> blockReceipts : receipts) {

            List<byte[]> encodedBlockReceipts = new ArrayList<>();
            for (TransactionReceipt txReceipt : blockReceipts) {
                encodedBlockReceipts.add(txReceipt.getEncoded(true));
            }
            byte[][] encodedElementArray = encodedBlockReceipts.toArray(new byte[encodedBlockReceipts.size()][]);
            byte[] blockReceiptsEncoded = RLP.encodeList(encodedElementArray);

            blocks.add(blockReceiptsEncoded);
        }

        byte[][] encodedElementArray = blocks.toArray(new byte[blocks.size()][]);
        this.encoded = RLP.encodeList(encodedElementArray);
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }


    public List<List<TransactionReceipt>> getReceipts() {
        parse();
        return receipts;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.RECEIPTS;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    public String toString() {
        parse();
        final StringBuilder sb = new StringBuilder();
        if (receipts.size() < 4) {
            for (List<TransactionReceipt> blockReceipts : receipts)
                sb.append("\n   ").append(blockReceipts.size()).append(" receipts in block");
        } else {
            for (int i = 0; i < 3; i++) {
                sb.append("\n   ").append(receipts.get(i).size()).append(" receipts in block");
            }
            sb.append("\n   ").append("[Skipped ").append(receipts.size() - 3).append(" blocks]");
        }
        return "[" + getCommand().name() + " num:"
                + receipts.size() + " " + sb.toString() + "]";
    }
}