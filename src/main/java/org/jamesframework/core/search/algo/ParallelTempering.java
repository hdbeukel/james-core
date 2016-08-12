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

package org.jamesframework.core.search.algo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.jamesframework.core.exceptions.JamesRuntimeException;
import org.jamesframework.core.exceptions.SearchException;
import org.jamesframework.core.factory.MetropolisSearchFactory;
import org.jamesframework.core.problems.Problem;
import org.jamesframework.core.problems.sol.Solution;
import org.jamesframework.core.problems.constraints.validations.Validation;
import org.jamesframework.core.problems.objectives.evaluations.Evaluation;
import org.jamesframework.core.search.LocalSearch;
import org.jamesframework.core.search.Search;
import org.jamesframework.core.search.SingleNeighbourhoodSearch;
import org.jamesframework.core.search.listeners.SearchListener;
import org.jamesframework.core.search.neigh.Neighbourhood;
import org.jamesframework.core.search.stopcriteria.MaxSteps;

/**
 * <p>
 * The parallel tempering algorithm uses several Metropolis search replicas with different
 * temperatures in a given range, where good solutions are pushed towards cool replicas for
 * the sake of convergence, while bad solutions are pushed towards hot replicas in an attempt
 * to find further improvements. Each step of parallel tempering consists of the following
 * two actions:
 * </p>
 * <ol>
 *  <li>
 *      Every replica performs a fixed number of steps (defaults to 500) in an attempt to
 *      improve its own solution.
 *  </li>
 *  <li>
 *      Solutions of adjacent replica (ordered by temperature) are considered to be swapped.
 *      Solutions of replicas \(R_1\) and \(R_2\) with temperatures \(T_1\) and \(T_2\) (\(T_1 \lt T_2\))
 *      and current solution evaluation \(E_1\) and \(E_2\), respectively, are always swapped if
 *      \(\Delta E \ge 0\), where \(\Delta E\) is defined as the improvement of \(E_2\) over \(E_1\)
 *      (see {@link #computeDelta(Evaluation, Evaluation)}).
 *      If \(\Delta E \lt 0\), solutions are swapped with probability
 *      \[
 *          e^{(\frac{1}{T_1}-\frac{1}{T_2})\Delta E}.
 *      \]
 *  </li>
 * </ol>
 * <p>
 * All replicas use the same neighbourhood, which is specified when creating the parallel tempering
 * search. By default, each replica starts from an independently generated random solution. A custom
 * initial solution can be set by calling {@link #setCurrentSolution(Solution)} on the parallel tempering
 * algorithm before starting the search; in this case, a copy of this solution is set as initial solution
 * in each replica. Note that this cancels the built-in multi-start of the parallel tempering algorithm.
 * Setting an independent custom initial solution in each replica can be achieved by providing a custom
 * Metropolis search factory at construction.
 * </p>
 * <p>
 * The overall best solution found by all replicas is tracked and eventually returned by the parallel
 * tempering algorithm. The main algorithm does not actively generate nor apply any moves to its current
 * solution, but simply updates it when a replica has found a new global improvement, in which case the
 * best solution is also updated.
 * </p>
 * <p>
 * The reported number of accepted and rejected moves (see {@link #getNumAcceptedMoves()} and
 * {@link #getNumRejectedMoves()}) correspond to the sum of the number of accepted and rejected
 * moves in all replicas, during the current run of the parallel tempering search. These values
 * are updated with some delay, whenever a Metropolis replica has completed its current run.
 * </p>
 * <p>
 * When creating the parallel tempering algorithm, the number of replicas and a minimum and maximum
 * temperature have to be specified. Temperatures assigned to the replicas are unique and equally
 * spaced in the desired interval. The number of replica steps defaults to 500 but it is strongly
 * advised to fine tune this parameter for specific problems, e.g. in case of a computationally
 * expensive objective function, a lower number of steps may be more appropriate.
 * </p>
 * <p>
 * Note that every replica runs in a separate thread so that they will be executed in parallel on
 * multi-core processors or multi-processor machines. Therefore, it is important that the problem
 * (including all of its components such as the objective, constraints, etc.) and neighbourhood
 * specified at construction are thread-safe.
 * </p>
 * 
 * @param <SolutionType> solution type of the problems that may be solved using this search,
 *                       required to extend {@link Solution}
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class ParallelTempering<SolutionType extends Solution> extends SingleNeighbourhoodSearch<SolutionType> {

    // logger
    // private static final Logger logger = LoggerFactory.getLogger(ParallelTempering.class);
    
    // Metropolis replicas
    private final List<MetropolisSearch<SolutionType>> replicas;
    
    // number of steps performed by each replica
    private long replicaSteps;
    
    // thread pool for replica execution and corresponding queue of futures of submitted tasks
    private final ExecutorService pool;
    private final Queue<Future<Integer>> futures;
    
    // swap base: flipped (0/1) after every step for fair solution swaps
    private int swapBase;
    
    /**
     * <p>
     * Creates a new parallel tempering algorithm, specifying the problem to solve,
     * the neighbourhood used in each replica, the number of replicas, and the minimum
     * and maximum temperature. The problem and neighbourhood can not be <code>null</code>,
     * the number of replicas and both temperature bounds should be strictly positive, and
     * the minimum temperature should be smaller than the maximum temperature. The default
     * name "ParallelTempering" is assigned to the search.
     * </p>
     * <p>
     * Note that it is important that the given problem (including all of its components
     * such as the objective, constraints, etc.) and neighbourhood are thread-safe, because
     * they will be accessed concurrently from several Metropolis searches running in separate
     * threads.
     * </p>
     * 
     * @param problem problem to solve
     * @param neighbourhood neighbourhood used inside Metropolis search replicas
     * @param numReplicas number of Metropolis replicas
     * @param minTemperature minimum temperature of Metropolis replicas
     * @param maxTemperature maximum temperature of Metropolis replicas
     * @throws NullPointerException if <code>problem</code> or <code>neighbourhood</code> are
     *                              <code>null</code>
     * @throws IllegalArgumentException if <code>numReplicas</code>, <code>minTemperature</code>
     *                                  or <code>maxTemperature</code> are not strictly positive,
     *                                  or if <code>minTemperature &ge; maxTemperature</code>
     */
    public ParallelTempering(Problem<SolutionType> problem, Neighbourhood<? super SolutionType> neighbourhood,
                                int numReplicas, double minTemperature, double maxTemperature){
        this(problem, neighbourhood, numReplicas,
            minTemperature, maxTemperature,
            (p, n, t) -> new MetropolisSearch<>(p, n, t));
    }
    
    /**
     * <p>
     * Creates a new parallel tempering algorithm, specifying the problem to solve,
     * the neighbourhood used in each replica, the number of replicas, the minimum
     * and maximum temperature, and a custom Metropolis search factory. The Metropolis
     * search factory can be used to customize the replicas, e.g. to set a custom initial
     * solution per replica. The factory should always respect the contract as defined in
     * the interface {@link MetropolisSearchFactory}. Else, the algorithm may not function
     * correctly and exceptions might be thrown during search.
     * </p>
     * <p>
     * The problem, neighbourhood and Metropolis factory can not be <code>null</code>,
     * the number of replicas and both temperature bounds should be strictly positive,
     * and the minimum temperature should be smaller than the maximum temperature.
     * </p>
     * <p>
     * Note that it is important that the given problem (including all of its components such
     * as the objective, constraints, etc.) and neighbourhood are thread-safe, because they
     * will be accessed concurrently from several Metropolis searches running in separate threads.
     * </p>
     * 
     * @param problem problem to solve
     * @param neighbourhood neighbourhood used inside Metropolis search replicas
     * @param numReplicas number of Metropolis replicas
     * @param minTemperature minimum temperature of Metropolis replicas
     * @param maxTemperature maximum temperature of Metropolis replicas
     * @param metropolisFactory custom factory used to create Metropolis searches
     * @throws NullPointerException if <code>problem</code> or <code>neighbourhood</code> are
     *                              <code>null</code>
     * @throws IllegalArgumentException if <code>numReplicas</code>, <code>minTemperature</code>
     *                                  or <code>maxTemperature</code> are not strictly positive,
     *                                  or if <code>minTemperature &ge; maxTemperature</code>
     */
    public ParallelTempering(Problem<SolutionType> problem,
                             Neighbourhood<? super SolutionType> neighbourhood,
                             int numReplicas, double minTemperature, double maxTemperature,
                             MetropolisSearchFactory<SolutionType> metropolisFactory){
        this(null, problem, neighbourhood, numReplicas, minTemperature, maxTemperature, metropolisFactory);
    }
    
    /**
     * <p>
     * Creates a new parallel tempering algorithm, specifying the problem to solve,
     * the neighbourhood used in each replica, the number of replicas, the minimum
     * and maximum temperature, a custom search name, and a custom Metropolis search
     * factory. The Metropolis search factory can be used to customize the replicas,
     * e.g. to set a custom initial solution per replica. The factory should always
     * respect the contract as defined in the interface {@link MetropolisSearchFactory}.
     * Else, the algorithm may not function correctly and exceptions might be thrown
     * during search.
     * </p>
     * <p>
     * The problem, neighbourhood and Metropolis factory can not be <code>null</code>,
     * the number of replicas and both temperature bounds should be strictly positive,
     * and the minimum temperature should be smaller than the maximum temperature. The
     * search name can be <code>null</code> in which case the default name "ParallelTempering"
     * is assigned.
     * </p>
     * <p>
     * Note that it is important that the given problem (including all of its components such
     * as the objective, constraints, etc.) and neighbourhood are thread-safe, because they
     * will be accessed concurrently from several Metropolis searches running in separate threads.
     * </p>
     * 
     * @param name custom search name
     * @param problem problem to solve
     * @param neighbourhood neighbourhood used inside Metropolis search replicas
     * @param numReplicas number of Metropolis replicas
     * @param minTemperature minimum temperature of Metropolis replicas
     * @param maxTemperature maximum temperature of Metropolis replicas
     * @param metropolisFactory custom factory used to create Metropolis searches
     * @throws NullPointerException if <code>problem</code> or <code>neighbourhood</code> are
     *                              <code>null</code>
     * @throws IllegalArgumentException if <code>numReplicas</code>, <code>minTemperature</code>
     *                                  or <code>maxTemperature</code> are not strictly positive,
     *                                  or if <code>minTemperature &ge; maxTemperature</code>
     */
    public ParallelTempering(String name,
                             Problem<SolutionType> problem,
                             Neighbourhood<? super SolutionType> neighbourhood,
                             int numReplicas, double minTemperature, double maxTemperature,
                             MetropolisSearchFactory<SolutionType> metropolisFactory){
        super(name != null ? name : "ParallelTempering", problem, neighbourhood);
        // check number of replicas
        if(numReplicas <= 0){
            throw new IllegalArgumentException("Error while creating parallel tempering algorithm: "
                                                + "number of replicas should be > 0.");
        }
        // check minimum and maximum temperature
        if(minTemperature <= 0.0){
            throw new IllegalArgumentException("Error while creating parallel tempering algorithm: "
                                                + "minimum temperature should be > 0.0.");
        }
        if(maxTemperature <= 0.0){
            throw new IllegalArgumentException("Error while creating parallel tempering algorithm: "
                                                + "maximum temperature should be > 0.0.");
        }
        if(minTemperature >= maxTemperature){
            throw new IllegalArgumentException("Error while creating parallel tempering algorithm: "
                                                + "minimum temperature should be smaller than "
                                                + "maximum temperature.");
        }
        if(metropolisFactory == null){
            throw new NullPointerException("Error while creating parallel tempering algorithm: "
                                                + "metropolis search factory cannot be null.");
        }
        // create replicas
        replicas = new ArrayList<>();
        for(int i=0; i<numReplicas; i++){
            double temperature = minTemperature + i*(maxTemperature - minTemperature)/(numReplicas - 1);
            MetropolisSearch<SolutionType> ms = metropolisFactory.create(problem, neighbourhood, temperature);
            replicas.add(ms);
        }
        // set default replica steps
        replicaSteps = 500;
        // create thread pool
        pool = Executors.newFixedThreadPool(numReplicas);
        // initialize (empty) futures queue
        futures = new LinkedList<>();
        // set initial swap base
        swapBase = 0;
        // listen to events fired by replicas
        ReplicaListener listener = new ReplicaListener();
        replicas.forEach(r -> r.addSearchListener(listener));
    }
    
    /**
     * Sets the number of steps performed by each replica in every iteration of the global parallel tempering
     * algorithm, before considering solution swaps. Defaults to 500. The specified number of steps should
     * be strictly positive.
     * 
     * @param steps number of steps performed by replicas in each iteration
     * @throws IllegalArgumentException if <code>steps</code> is not strictly positive
     */
    public void setReplicaSteps(long steps){
        // check number of steps
        if(steps <= 0){
            throw new IllegalArgumentException("Number of replica steps in parallel tempering "
                                                + "should be strictly positive.");
        }
        // set number
        this.replicaSteps = steps;
    }
    
    /**
     * Get the number of steps performed by each replica in every iteration of the global parallel
     * tempering algorithm, before considering solution swaps. Defaults to 500 and can be changed
     * using {@link #setReplicaSteps(long)}.
     * 
     * @return number of steps performed by replicas in each iteration
     */
    public long getReplicaSteps(){
        return replicaSteps;
    }
    
    /**
     * Set the same neighbourhood for each replica. Note that <code>neighbourhood</code> can not
     * be <code>null</code> and that this method may only be called when the search is idle.
     * 
     * @param neighbourhood neighbourhood to be set for each replica
     * @throws NullPointerException if <code>neighbourhood</code> is <code>null</code>
     * @throws SearchException if the search is not idle
     */
    @Override
    public void setNeighbourhood(Neighbourhood<? super SolutionType> neighbourhood){
        // synchronize with status updates
        synchronized(getStatusLock()){
            // call super
            super.setNeighbourhood(neighbourhood);
            // set neighbourhood in every replica
            replicas.forEach(r -> r.setNeighbourhood(neighbourhood));
        }
    }
    
    /**
     * Set a custom current solution, of which a copy is set as the current solution in each replica.
     * Note that <code>solution</code> can not be <code>null</code> and that this method may only be
     * called when the search is idle.
     * 
     * @param solution current solution to be set for each replica
     * @throws NullPointerException if <code>solution</code> is <code>null</code>
     * @throws SearchException if the search is not idle
     */
    @Override
    public void setCurrentSolution(SolutionType solution){
        // synchronize with status updates
        synchronized(getStatusLock()){
            // call super (also verifies status)
            super.setCurrentSolution(solution);
            // pass current solution to every replica (copy!)
            replicas.forEach(r -> r.setCurrentSolution(Solution.checkedCopy(solution)));
        }
    }
    
    /**
     * When initializing a parallel tempering search, the replicas are initialized as well (in parallel).
     */
    @Override
    public void init(){
        // init super
        super.init();
        // initialize replicas
        replicas.parallelStream().forEach(Search::init);
    }
    
    /**
     * In each search step, every replica performs several steps after which solutions of adjacent
     * replicas may be swapped.
     * 
     * @throws SearchException if an error occurs during concurrent execution of the Metropolis replicas
     * @throws JamesRuntimeException if depending on malfunctioning components (problem,
     *                               neighbourhood, replicas, ...)
     */
    @Override
    protected void searchStep() {
        // submit replicas for execution in thread pool
        // (future returns index of respective replica)
        for(int i=0; i < replicas.size(); i++){
            futures.add(pool.submit(replicas.get(i), i));
        }
        // logger.debug("{}: started {} Metropolis replicas", this, futures.size());
        // wait for completion of all replicas and remove corresponding future
        while(!futures.isEmpty()){
            // remove next future from queue and wait until it has completed
            try{
                int i = futures.poll().get();
                // logger.debug("{}: {}/{} replicas finished", this, replicas.size()-futures.size(), replicas.size());
                // update total number of accepted/rejected moves
                incNumAcceptedMoves(replicas.get(i).getNumAcceptedMoves());
                incNumRejectedMoves(replicas.get(i).getNumRejectedMoves());
            } catch (InterruptedException | ExecutionException ex){
                throw new SearchException("An error occured during concurrent execution of Metropolis replicas "
                                            + "in the parallel tempering algorithm.", ex);
            }
        }
        // consider swapping solutions of adjacent replicas
        for(int i=swapBase; i<replicas.size()-1; i+=2){
            MetropolisSearch<SolutionType> r1 = replicas.get(i);
            MetropolisSearch<SolutionType> r2 = replicas.get(i+1);
            // compute delta
            double delta = computeDelta(r2.getCurrentSolutionEvaluation(), r1.getCurrentSolutionEvaluation());
            // check if solutions should be swapped
            boolean swap = false;
            if(delta >= 0){
                // always swap
                swap = true;
            } else {
                // compute factor based on difference in temperature
                double b1 = 1.0 / (r1.getTemperature());
                double b2 = 1.0 / (r2.getTemperature());
                double diffb = b1 - b2;
                // randomized swap (with probability p)
                double p = Math.exp(diffb * delta);
                // generate random number
                double r = getRandom().nextDouble();
                // swap with probability p
                if(r < p){
                    swap = true;
                }
            }
            // swap solutions
            if(swap){
                SolutionType r1Sol = r1.getCurrentSolution();
                Evaluation r1Eval = r1.getCurrentSolutionEvaluation();
                Validation r1Val = r1.getCurrentSolutionValidation();
                r1.setCurrentSolution(
                        r2.getCurrentSolution(),
                        r2.getCurrentSolutionEvaluation(),
                        r2.getCurrentSolutionValidation()
                );
                r2.setCurrentSolution(r1Sol, r1Eval, r1Val);
            }
        }
        // flip swap base
        swapBase = 1 - swapBase;
    }
    
    /**
     * When disposing a parallel tempering search, it will dispose each contained Metropolis replica and will
     * shut down the thread pool used for concurrent execution of replicas.
     */
    @Override
    protected void searchDisposed(){
        // dispose replicas
        replicas.forEach(r -> r.dispose());
        // shut down thread pool
        pool.shutdown();
        // dispose super
        super.searchDisposed();
    }

    /**
     * Private listener attached to each replica, to keep track of the global best solution and aggregated number of
     * accepted and rejected moves, and to terminate a replica when it has performed the desired number of steps.
     */
    private class ReplicaListener implements SearchListener<SolutionType>{
    
        /*******************************/
        /* CALLBACKS FIRED BY REPLICAS */
        /*******************************/

        /**
         * Whenever a new best solution is reported inside a replica, it is verified whether this is also a global
         * improvement. If so, the main algorithm's current and best solution are both updated to refer to this new
         * global best solution. This method is synchronized to avoid conflicting updates by replicas running in
         * separate threads.
         * 
         * @param replica Metropolis replica that has found a (local) best solution
         * @param newBestSolution new best solution found in replica
         * @param newBestSolutionEvaluation evaluation of new best solution
         * @param newBestSolutionValidation validation of new best solution
         */
        @Override
        public synchronized void newBestSolution(Search<? extends SolutionType> replica,
                                                 SolutionType newBestSolution,
                                                 Evaluation newBestSolutionEvaluation,
                                                 Validation newBestSolutionValidation) {
            // update main algorithm's current and best solution
            updateCurrentAndBestSolution(newBestSolution, newBestSolutionEvaluation, newBestSolutionValidation);
        }

        /**
         * Whenever a replica has completed a step it is verified whether the desired number of steps have been
         * performed and, if so, the replica is stopped. This approach is favoured here over attaching a generic
         * maximum steps stop criterion (see {@link MaxSteps}) to each replica because it involves less overhead
         * (the stop criterion checker is never activated).
         * 
         * @param replica Metropolis replica that completed a search step
         * @param numSteps number of steps completed so far
         */
        @Override
        public void stepCompleted(Search<? extends SolutionType> replica, long numSteps) {
            if (numSteps >= replicaSteps){
                replica.stop();
            }
        }
        
    }

}
