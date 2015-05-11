/*
 * Copyright 2015 Ghent University, Bayer CropScience.
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

package org.jamesframework.core.util;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test randomization tools.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class RandomizationTest {

    /**
     * Set up test class.
     */
    @BeforeClass
    public static void setUpClass() {
        System.out.println("# Testing Randomization ...");
    }

    /**
     * Print message when tests are complete.
     */
    @AfterClass
    public static void tearDownClass() {
        System.out.println("# Done testing Randomization!");
    }
    
    @Test
    public void test() {
        
        System.out.println(" - test randomization tools");
        
        // test default
        Random tlrg = ThreadLocalRandom.current();
        assertEquals(tlrg, Randomization.getRandom());
        
        // set custom
        Random crg = new Random(123);
        Randomization.setRandom(crg);
        assertEquals(crg, Randomization.getRandom());
        
        // reset default
        Randomization.resetDefault();
        assertEquals(tlrg, Randomization.getRandom());
        
        // test reset (2)
        Randomization.setRandom(crg);
        Randomization.setRandom(null);
        assertEquals(tlrg, Randomization.getRandom());
        
    }

}