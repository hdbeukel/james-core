/*
 * Copyright 2014 Ghent University, Bayer CropScience.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jamesframework.core.search.algo.tabu;

import org.jamesframework.core.search.SearchTestTemplate;
import org.jamesframework.core.subset.SubsetSolution;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test FirstBestAdmissibleTabuSearch when all moves are declared tabu. Always accepts the first found improvement
 * (first descent strategy) because of the built-in aspiration criterion that overrides tabu for moves that yield
 * a new best solution.
 *
 * @author <a href="mailto:chenhuanfa@gmail.com">Huanfa Chen</a>
 */
public class FirstBestAdmissibleTabuSearchTest2 extends SearchTestTemplate {

    // tabu search
    private FirstBestAdmissibleTabuSearch<SubsetSolution> search;

    // maximum runtime
    private static final long SINGLE_RUN_RUNTIME = 1000;
    private static final TimeUnit MAX_RUNTIME_TIME_UNIT = TimeUnit.MILLISECONDS;

    /**
     * Print message when starting tests.
     */
    @BeforeClass
    public static void setUpClass() {
        System.out.println("# Testing FirstBestAdmissibleTabuSearch (2) ...");
        SearchTestTemplate.setUpClass();
    }

    /**
     * Print message when tests are complete.
     */
    @AfterClass
    public static void tearDownClass() {
        System.out.println("# Done testing FirstBestAdmissibleTabuSearch (2)!");
    }

    @Override
    @Before
    public void setUp(){
        // call super
        super.setUp();
        // create tabu search with all moves declared tabu
        search = new FirstBestAdmissibleTabuSearch<>(problem, neigh, new RejectAllTabuMemory<>());
        // set and log random seed
        setRandomSeed(search);
    }

    @After
    public void tearDown(){
        // dispose search
        search.dispose();
    }

    /**
     * Test with all moves declared tabu.
     */
    @Test
    public void testWithAllMovesTabu() {
        System.out.println(" - test with all moves tabu (~ first descent)");
        // single run
        singleRunWithMaxRuntime(search, SINGLE_RUN_RUNTIME, MAX_RUNTIME_TIME_UNIT);
    }

    /**
     * Test minimizing with all moves declared tabu.
     */
    @Test
    public void testMinimizingWithAllMovesTabu() {
        System.out.println(" - test with all moves tabu, minimizing (~ first descent)");
        // set minimizing
        obj.setMinimizing();
        // single run
        singleRunWithMaxRuntime(search, SINGLE_RUN_RUNTIME, MAX_RUNTIME_TIME_UNIT);
    }

}