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

package org.jamesframework.core.problems.sol;

import java.util.Random;

/**
 * A random solution generator creates random solutions of a specific type, given some data.
 * It is required that every request generates a new solution instance which is independent
 * of any previously generated solutions.
 * 
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 * @param <SolutionType> type of the generated solutions, required to extend {@link Solution}
 * @param <DataType> type of data used to generate solutions
 */
@FunctionalInterface
public interface RandomSolutionGenerator<SolutionType extends Solution, DataType> {

    /**
     * Generate a random solution.
     * 
     * @param rnd source of randomness
     * @param data given data
     * @return random solution
     */
    public SolutionType create(Random rnd, DataType data);
    
}
