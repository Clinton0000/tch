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
package org.tcheum.jsontestsuite;

import org.tcheum.config.blockchain.*;
import org.tcheum.config.net.MainNetConfig;
import org.tcheum.config.net.RopstenNetConfig;
import org.json.simple.parser.ParseException;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;

import static org.tcheum.jsontestsuite.GitHubJSONTestSuite.runCryptoTest;
import static org.tcheum.jsontestsuite.GitHubJSONTestSuite.runDifficultyTest;

/**
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubBasicTest {

    String commitSHA = "7f638829311dfc1d341c1db85d8a891f57fa4da7";

    @Test
    public void btCrypto() throws IOException {
        runCryptoTest("BasicTests/crypto.json", commitSHA);
    }

    @Test
    public void btDifficulty() throws IOException, ParseException {
        runDifficultyTest(MainNetConfig.INSTANCE, "BasicTests/difficulty.json", commitSHA);
    }

    @Test
    public void btDifficultyByzantium() throws IOException, ParseException {
        runDifficultyTest(new ByzantiumConfig(new DaoHFConfig()), "BasicTests/difficultyByzantium.json", commitSHA);
    }

    @Test
    @Ignore // due to CPP minimumDifficulty issue
    public void btDifficultyCustomHomestead() throws IOException, ParseException {
        runDifficultyTest(new HomesteadConfig(), "BasicTests/difficultyCustomHomestead.json", commitSHA);
    }

    @Test
    @Ignore // due to CPP minimumDifficulty issue
    public void btDifficultyCustomMainNetwork() throws IOException, ParseException {
        runDifficultyTest(MainNetConfig.INSTANCE, "BasicTests/difficultyCustomMainNetwork.json", commitSHA);
    }

    @Test
    public void btDifficultyFrontier() throws IOException, ParseException {
        runDifficultyTest(new FrontierConfig(), "BasicTests/difficultyFrontier.json", commitSHA);
    }

    @Test
    public void btDifficultyHomestead() throws IOException, ParseException {
        runDifficultyTest(new HomesteadConfig(), "BasicTests/difficultyHomestead.json", commitSHA);
    }

    @Test
    public void btDifficultyMainNetwork() throws IOException, ParseException {
        runDifficultyTest(MainNetConfig.INSTANCE, "BasicTests/difficultyMainNetwork.json", commitSHA);
    }

    @Test
    public void btDifficultyRopsten() throws IOException, ParseException {
        runDifficultyTest(new RopstenNetConfig(), "BasicTests/difficultyRopsten.json", commitSHA);
    }
}
