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
import org.tcheum.core.TransactionReceipt;
import org.tcheum.facade.tcheum;
import org.tcheum.facade.tcheumFactory;
import org.tcheum.facade.Repository;
import org.tcheum.listener.tcheumListenerAdapter;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.List;

public class FollowAccount extends tcheumListenerAdapter {


    tcheum tcheum = null;

    public FollowAccount(tcheum tcheum) {
        this.tcheum = tcheum;
    }

    public static void main(String[] args) {

        tcheum tcheum = tcheumFactory.createtcheum();
        tcheum.addListener(new FollowAccount(tcheum));
    }

    @Override
    public void onBlock(Block block, List<TransactionReceipt> receipts) {

        byte[] cow = Hex.decode("cd2a3d9f938e13cd947ec05abc7fe734df8dd826");

        // Get snapshot some time ago - 10% blocks ago
        long bestNumber = tcheum.getBlockchain().getBestBlock().getNumber();
        long oldNumber = (long) (bestNumber * 0.9);

        Block oldBlock = tcheum.getBlockchain().getBlockByNumber(oldNumber);

        Repository repository = tcheum.getRepository();
        Repository snapshot = tcheum.getSnapshotTo(oldBlock.getStateRoot());

        BigInteger nonce_ = snapshot.getNonce(cow);
        BigInteger nonce = repository.getNonce(cow);

        System.err.println(" #" + block.getNumber() + " [cd2a3d9] => snapshot_nonce:" +  nonce_ + " latest_nonce:" + nonce);
    }
}
