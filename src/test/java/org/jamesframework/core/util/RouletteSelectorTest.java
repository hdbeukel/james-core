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

package org.jamesframework.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test RouletteSelector.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class RouletteSelectorTest {
    
    private static final Random RG = new Random();
    
    /**
     * Set up test class.
     */
    @BeforeClass
    public static void setUpClass() {
        System.out.println("# Testing RouletteSelector ...");
    }

    /**
     * Print message when tests are complete.
     */
    @AfterClass
    public static void tearDownClass() {
        System.out.println("# Done testing RouletteSelector!");
    }
    
    @Test
    public void testExceptions(){
        System.out.println(" - test exceptions");
        
        boolean thrown;
        
        thrown = false;
        try {
            RouletteSelector.select(null, Arrays.asList(1.0), RG);
        } catch (NullPointerException ex) {
            thrown = true;
        }
        assertTrue(thrown);
        
        thrown = false;
        try {
            RouletteSelector.select(Arrays.asList("abc"), null, RG);
        } catch (NullPointerException ex) {
            thrown = true;
        }
        assertTrue(thrown);
        
        thrown = false;
        try {
            RouletteSelector.select(Arrays.asList("abc", "xyz", "qtl"),
                                    Arrays.asList(1.0, null, 5.0),
                                    RG);
        } catch (NullPointerException ex) {
            thrown = true;
        }
        assertTrue(thrown);
        
        thrown = false;
        try {
            RouletteSelector.select(Arrays.asList("a", "b", "c"),
                                    Arrays.asList(0.3, 0.7),
                                    RG);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        assertTrue(thrown);
        
        thrown = false;
        try {
            RouletteSelector.select(Arrays.asList("a", "c"),
                                    Arrays.asList(0.3, 0.5, 0.2),
                                    RG);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        assertTrue(thrown);
        
        thrown = false;
        try {
            RouletteSelector.select(Arrays.asList("a", "b", "c"),
                                    Arrays.asList(0.3, 0.3, -0.4),
                                    RG);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        assertTrue(thrown);
        
        for(int i=0; i<100; i++){
            assertNull(RouletteSelector.select(Arrays.asList(null, null),
                                               Arrays.asList(1.0, 0.6),
                                               RG));
            assertNull(RouletteSelector.select(Arrays.asList("a", "b", "c"),
                                               Arrays.asList(0.0, 0.0, 0.0),
                                               RG));
            assertEquals("b", RouletteSelector.select(Arrays.asList("a", "b", "c"),
                                                      Arrays.asList(0.0, 123.0, 0.0),
                                                      RG));
        }
        
        assertNull(RouletteSelector.select(Collections.emptyList(),
                                           Collections.emptyList(),
                                           RG));
    }

    /**
     * Test roulette selector.
     */
    @Test
    public void testSelect() {
        
        System.out.println(" - testing select");
        
        // empty lists
        assertNull(RouletteSelector.select(new ArrayList<>(), new ArrayList<>(), RG));
        
        // create item list
        List<String> items = Arrays.asList("Banana", "Peach", "Strawberry", "Mango");
        
        // all weights zero
        assertNull(RouletteSelector.select(items,
                                           Arrays.asList(0.0, 0.0, 0.0, 0.0),
                                           RG));
        
        // all but one weight zero
        assertEquals("Strawberry", RouletteSelector.select(items,
                                                           Arrays.asList(0.0, 0.0, 123.4, 0.0),
                                                           RG));
        
        // all but one weight not zero
        List<Double> weights = Arrays.asList(1.2, 0.0, 3.2, 4.0);
        for(int i=0; i<100; i++){
            assertNotEquals("Peach", RouletteSelector.select(items, weights, RG));
        }
        
        // no weights zero
        weights = Arrays.asList(1.2, 0.3, 3.2, 4.0);
        for(int i=0; i<100; i++){
            String selected = RouletteSelector.select(items, weights, RG);
            assertNotNull(selected);
            assertTrue(items.contains(selected));
        }
        
        // check: item list can contain null elements
        items = Arrays.asList(null, null, null, null);
        for(int i=0; i<100; i++){
            assertNull(RouletteSelector.select(items, weights, RG));
        }
        
    }

}