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

package org.jamesframework.core.search.neigh;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.jamesframework.core.problems.sol.Solution;

/**
 * Interface of a neighbourhood that generates moves to transform a given solution into a neighbouring solution.
 * 
 * @param <SolutionType> solution type for which this neighbourhood can be applied, required to extend {@link Solution}
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public interface Neighbourhood<SolutionType extends Solution> {

    /**
     * Generates a move that transforms the given solution into a random neighbour
     * contained in this specific neighbourhood. If the neighbourhood does not
     * contain any neighbours for the given solution, <code>null</code> is returned.
     * 
     * @param solution solution to which the move is to be applied
     * @param rnd source of randomness used to generate a random move
     * @return random move transforming the given solution into a random neighbour,
     *         <code>null</code> if the given solution does not have any neighbours
     */
    public Move<? super SolutionType> getRandomMove(SolutionType solution, Random rnd);
    
    /**
     * Generates a random move, using a default source of randomness. The default implementation
     * delegates to <code>getRandomMove(solution, ThreadLocalRandom.current())</code> where a thread
     * local random generator is used to avoid overhead and contention for parallel calls.
     * 
     * @param solution solution to which the move is to be applied
     * @return random move transforming the given solution into a random neighbour,
     *         <code>null</code> if the given solution does not have any neighbours
     */
    default public Move<? super SolutionType> getRandomMove(SolutionType solution){
        return getRandomMove(solution, ThreadLocalRandom.current());
    }
    
    /**
     * Get a list of all moves that can be applied to the given solution to transform
     * it into each of the neighbouring solutions contained in this specific neighbourhood.
     * The returned list may be empty, in case the given solution does not have any neighbours.
     * 
     * @param solution solution to which the moves are to be applied
     * @return list of all moves for this neighbourhood, may be empty if the
     *         given solution does not have any neighbours
     */
    public List<? extends Move<? super SolutionType>> getAllMoves(SolutionType solution);
    
}
