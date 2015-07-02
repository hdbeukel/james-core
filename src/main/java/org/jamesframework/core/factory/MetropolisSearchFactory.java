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

package org.jamesframework.core.factory;

import org.jamesframework.core.problems.Problem;
import org.jamesframework.core.problems.sol.Solution;
import org.jamesframework.core.search.algo.MetropolisSearch;
import org.jamesframework.core.search.neigh.Neighbourhood;

/**
 * Factory used to create a Metropolis search, given the problem to be solved, the applied
 * neighbourhood and the desired temperature.
 * 
 * @param <SolutionType> solution type of created Metropolis searches, required to extend {@link Solution}
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
@FunctionalInterface
public interface MetropolisSearchFactory<SolutionType extends Solution>{
        
    /**
     * Create a Metropolis search, given the problem to solve,
     * the applied neighbourhood and the desired temperature.
     * 
     * @param problem problem to solve
     * @param neighbourhood applied neighbourhood
     * @param temperature temperature of the search
     * @return created Metropolis search with the requested parameters
     */
    public MetropolisSearch<SolutionType> create(Problem<SolutionType> problem,
                                            Neighbourhood<? super SolutionType> neighbourhood,
                                            double temperature);

}
