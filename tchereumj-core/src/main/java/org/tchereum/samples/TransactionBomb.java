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
package org.tcheum.samples;

import org.tcheum.core.Block;
import org.tcheum.core.Transaction;
import org.tcheum.core.TransactionReceipt;
import org.tcheum.facade.tcheum;
import org.tcheum.facade.tcheumFactory;
import org.tcheum.listener.tcheumListenerAdapter;
import org.spongycastle.util.encoders.Hex;

import java.util.Collections;
import java.util.List;

import static org.tcheum.crypto.HashUtil.sha3;
import static org.tcheum.util.ByteUtil.longToBytesNoLeadZeroes;
import static org.tcheum.util.ByteUtil.toHexString;

public class TransactionBomb extends tcheumListenerAdapter {


    tcheum tcheum = null;
    boolean startedTxBomb = false;

    public TransactionBomb(tcheum tcheum) {
        this.tcheum = tcheum;
    }

    public static void main(String[] args) {

        tcheum tcheum = tcheumFactory.createtcheum();
        tcheum.addListener(new TransactionBomb(tcheum));
    }


    @Override
    public void onSyncDone(SyncState state) {

        // We will send transactions only
        // after we have the full chain syncs
        // - in order to prevent old nonce usage
        startedTxBomb = true;
        System.err.println(" ~~~ SYNC DONE ~~~ ");
    }

    @Override
    public void onBlock(Block block, List<TransactionReceipt> receipts) {

        if (startedTxBomb){
            byte[] sender = Hex.decode("cd2a3d9f938e13cd947ec05abc7fe734df8dd826");
            long nonce = tcheum.getRepository().getNonce(sender).longValue();;

            for (int i=0; i < 20; ++i){
                sendTx(nonce);
                ++nonce;
                sleep(10);
            }
        }
    }

    private void sendTx(long nonce){

        byte[] gasPrice = longToBytesNoLeadZeroes(1_000_000_000_000L);
        byte[] gasLimit = longToBytesNoLeadZeroes(21000);

        byte[] toAddress = Hex.decode("9f598824ffa7068c1f2543f04efb58b6993db933");
        byte[] value = longToBytesNoLeadZeroes(10_000);

        Transaction tx = new Transaction(longToBytesNoLeadZeroes(nonce),
                gasPrice,
                gasLimit,
                toAddress,
                value,
                null,
                tcheum.getChainIdForNextBlock());

        byte[] privKey = sha3("cow".getBytes());
        tx.sign(privKey);

        tcheum.getChannelManager().sendTransaction(Collections.singletonList(tx), null);
        System.err.println("Sending tx: " + toHexString(tx.getHash()));
    }

    private void sleep(int millis){
        try {Thread.sleep(millis);}
        catch (InterruptedException e) {e.printStackTrace();}
    }
}
