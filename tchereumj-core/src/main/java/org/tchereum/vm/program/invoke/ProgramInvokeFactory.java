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
package org.tcheum.vm.program.invoke;

import org.tcheum.core.Block;
import org.tcheum.core.Repository;
import org.tcheum.core.Transaction;
import org.tcheum.db.BlockStore;
import org.tcheum.vm.DataWord;
import org.tcheum.vm.program.Program;

import java.math.BigInteger;

/**
 * @author Roman Mandeleil
 * @since 19.12.2014
 */
public interface ProgramInvokeFactory {

    ProgramInvoke createProgramInvoke(Transaction tx, Block block,
                                      Repository repository, BlockStore blockStore);

    ProgramInvoke createProgramInvoke(Program program, DataWord toAddress, DataWord callerAddress,
                                             DataWord inValue, DataWord inGas,
                                             BigInteger balanceInt, byte[] dataIn,
                                             Repository repository, BlockStore blockStore,
                                            boolean staticCall, boolean byTestingSuite);


}
