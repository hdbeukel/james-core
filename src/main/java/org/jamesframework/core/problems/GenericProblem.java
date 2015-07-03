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

package org.jamesframework.core.problems;

import org.jamesframework.core.problems.sol.Solution;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jamesframework.core.exceptions.IncompatibleDeltaEvaluationException;
import org.jamesframework.core.exceptions.IncompatibleDeltaValidationException;
import org.jamesframework.core.problems.constraints.Constraint;
import org.jamesframework.core.problems.constraints.PenalizingConstraint;
import org.jamesframework.core.problems.constraints.validations.PenalizingValidation;
import org.jamesframework.core.problems.constraints.validations.SimpleValidation;
import org.jamesframework.core.problems.constraints.validations.Validation;
import org.jamesframework.core.problems.constraints.validations.UnanimousValidation;
import org.jamesframework.core.problems.objectives.evaluations.Evaluation;
import org.jamesframework.core.problems.objectives.Objective;
import org.jamesframework.core.problems.objectives.evaluations.PenalizedEvaluation;
import org.jamesframework.core.problems.sol.RandomSolutionGenerator;
import org.jamesframework.core.search.neigh.Move;

/**
 * <p>
 * A generic problem is fully based on delegation of its responsabilities, by separating data from the objective,
 * constraints (if any) and random solution generation. The problem contains data of a specific type (parameter
 * <code>DataType</code>) and solutions are evaluated and validated based on a combination of an objective and
 * constraints, which use the underlying data. Two types of constraints can be specified:
 * </p>
 * <ul>
 *  <li>
 *  <p>
 *      <b>Mandatory constraints</b>: a solution is valid only if it satisfies all mandatory constraints.
 *      If not, it is discarded regardless of its evaluation. It is guaranteed that the best solution found by
 *      a search will always satisfy all mandatory constraints. In a neighbourhood search, only those neighbours
 *      of the current solution that satisfy all mandatory constraints are considered.
 *  </p>
 *  </li>
 *  <li>
 *  <p>
 *      <b>Penalizing constraints</b>: if a solution does not pass validation by a penalizing constraint, a penalty
 *      is assigned to its evaluation. The solution is not discarded. Penalties are usually chosen to reflect the
 *      severeness of the violation. Solutions closer to satisfaction are then favoured over solutions that violate
 *      the constraints more severely. In case of maximization, penalties are subtracted from the evaluation, while
 *      they are added to it in case of minimization. Depending on the interaction between the evaluation and penalties,
 *      the best found solution might not satisfy all penalizing constraints (which may or may not be desired).
 *  </p>
 *  </li>
 * </ul>
 * <p>
 * The problem uses an external, customizable random solution generator to create random instances of the solution type.
 * </p>
 * 
 * @param <SolutionType> type of solutions to the problem, required to extend {@link Solution}
 * @param <DataType> type of underlying data
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class GenericProblem<SolutionType extends Solution, DataType> implements Problem<SolutionType> {
    
    // objective function (can be more general than solution and data types of problem)
    private Objective<? super SolutionType, ? super DataType> objective;
    // underlying data
    private DataType data;
    // mandatory and penalizing constraints (+ unmodifiable views)
    // note: solution and data types can be more general than those of the problem
    private final List<Constraint<? super SolutionType, ? super DataType>>
            mandatoryConstraints, mandatoryConstraintsView;
    private final List<PenalizingConstraint<? super SolutionType, ? super DataType>>
            penalizingConstraints, penalizingConstraintsView;
    // random solution generator (allowed to generate subtypes of the problem's solution
    // type, requiring any supertype of the problem's data type)
    private RandomSolutionGenerator<? extends SolutionType, ? super DataType> randomSolutionGenerator;
    
    /**
     * <p>
     * Creates a new generic problem with given data, objective and random solution generator. Any objective
     * designed for the solution and data types of the problem, or supertypes thereof, is accepted. The random
     * solution generator may produce subtypes of the problem's solution type, requiring any supertype of the
     * problem's data type.
     * </p>
     * <p>
     * The objective and random solution generator can not be <code>null</code>.
     * </p>
     * 
     * @param data underlying data
     * @param objective objective function
     * @param randomSolutionGenerator random solution generator
     * @throws NullPointerException if <code>objective</code> or <code>randomSolutionGenerator</code> are <code>null</code>
     */
    public GenericProblem(DataType data, Objective<? super SolutionType, ? super DataType> objective,
                          RandomSolutionGenerator<? extends SolutionType, ? super DataType> randomSolutionGenerator) {
        // check that objective is not null
        if(objective == null){
            throw new NullPointerException("Error while creating generic problem: null not allowed for objective.");
        }
        // check that random solution generator is not null
        if(randomSolutionGenerator == null){
            throw new NullPointerException("Error while creating generic problem: null not allowed for random solution generator.");
        }
        // set fields
        this.data = data;
        this.objective = objective;
        this.randomSolutionGenerator = randomSolutionGenerator;
        // initialize constraint lists + views
        mandatoryConstraints = new ArrayList<>();
        penalizingConstraints = new ArrayList<>();
        mandatoryConstraintsView = Collections.unmodifiableList(mandatoryConstraints);
        penalizingConstraintsView = Collections.unmodifiableList(penalizingConstraints);
    }

    /**
     * Get the objective function.
     * 
     * @return objective function
     */
    public Objective<? super SolutionType, ? super DataType> getObjective() {
        return objective;
    }

    /**
     * Set the objective function. Any objective designed for the solution and data types of the problem,
     * or more general types, is accepted. The objective can not be <code>null</code>.
     * 
     * @param objective objective function
     * @throws NullPointerException if <code>objective</code> is <code>null</code>
     */
    public void setObjective(Objective<? super SolutionType, ? super DataType> objective) {
        // check not null
        if(objective == null){
            throw new NullPointerException("Error while setting objective: null is not allowed.");
        }
        this.objective = objective;
    }
    
    /**
     * Get the random solution generator.
     * 
     * @return random solution generator
     */
    public RandomSolutionGenerator<? extends SolutionType, ? super DataType> getRandomSolutionGenerator(){
        return randomSolutionGenerator;
    }

    /**
     * Set random solution generator. It is allowed for the generator to produce subtypes of the
     * problem's solution type, requiring any supertype of the problem's data type. The generator
     * can not be <code>null</code>.
     * 
     * @param randomSolutionGenerator random solution generator
     * @throws NullPointerException if <code>randomSolutionGenerator</code> is <code>null</code>
     */
    public void setRandomSolutionGenerator(RandomSolutionGenerator<? extends SolutionType, ? super DataType> randomSolutionGenerator){
        // check not null
        if(randomSolutionGenerator == null){
            throw new NullPointerException("Error while setting random solution generator: null is not allowed");
        }
        this.randomSolutionGenerator = randomSolutionGenerator;
    }
    
    /**
     * Get the underlying data.
     * 
     * @return underlying data
     */
    public DataType getData() {
        return data;
    }

    /**
     * Set new underlying data.
     * 
     * @param data new underlying data
     */
    public void setData(DataType data) {
        this.data = data;
    }
    
    /**
     * Add a mandatory constraint to the problem. Only those solutions that satisfy all mandatory
     * constraints will pass validation (see {@link #validate(Solution)}). Other solutions are discarded
     * regardless of their evaluation.
     * <p>
     * Only constraints designed for the solution and data type of the problem (or more general) are accepted.
     * Constraints should <b>not</b> be added while a search is solving this problem; doing so may lead to
     * exceptions and/or undefined search behaviour.
     * 
     * @param constraint mandatory constraint to add
     */
    public void addMandatoryConstraint(Constraint<? super SolutionType, ? super DataType> constraint){
        mandatoryConstraints.add(constraint);
    }
    
    /**
     * Remove a mandatory constraint. Returns <code>true</code> if the constraint has been successfully removed.
     * <p>
     * Constraints should <b>not</b> be removed while a search is solving this problem; doing so may lead to
     * exceptions and/or undefined search behaviour.
     * 
     * @param constraint mandatory constraint to be removed
     * @return <code>true</code> if the constraint is successfully removed
     */
    public boolean removeMandatoryConstraint(Constraint<? super SolutionType, ? super DataType> constraint){
        return mandatoryConstraints.remove(constraint);
    }
    
    /**
     * Get mandatory constraints (unmodifiable view).
     * 
     * @return list of mandatory constraints
     */
    public List<Constraint<? super SolutionType, ? super DataType>> getMandatoryConstraints(){
        return mandatoryConstraintsView;
    }
    
    /**
     * Add a penalizing constraint to the problem. For a solution that violates a penalizing constraint, a penalty
     * will be assigned to the objective score. Only penalizing constraints designed for the solution and data type
     * of the problem (or more general) are accepted.
     * <p>
     * Constraints should <b>not</b> be added while a search is solving this problem; doing so may lead to
     * exceptions and/or undefined search behaviour.
     * 
     * @param constraint penalizing constraint to add
     */
    public void addPenalizingConstraint(PenalizingConstraint<? super SolutionType, ? super DataType> constraint){
        penalizingConstraints.add(constraint);
    }
    
    /**
     * Remove a penalizing constraint. Returns <code>true</code> if the constraint has been successfully removed.
     * <p>
     * Constraints should <b>not</b> be removed while a search is solving this problem; doing so may lead to
     * exceptions and/or undefined search behaviour.
     * 
     * @param constraint penalizing constraint to be removed
     * @return <code>true</code> if the constraint is successfully removed
     */
    public boolean removePenalizingConstraint(PenalizingConstraint<? super SolutionType, ? super DataType> constraint){
        return penalizingConstraints.remove(constraint);
    }
    
    /**
     * Get penalizing constraints (unmodifiable view).
     * 
     * @return list of penalizing constraints
     */
    public List<PenalizingConstraint<? super SolutionType, ? super DataType>> getPenalizingConstraints(){
        return penalizingConstraintsView;
    }
    
    /**
     * <p>
     * Validate a solution by checking all mandatory constraints. The solution will only pass validation if
     * all mandatory constraints are satisfied.
     * </p>
     * <p>
     * In case there are no mandatory constraints, this method always returns {@link SimpleValidation#PASSED}.
     * If a single mandatory constraint has been specified, the corresponding validation is returned. In case
     * of two or more constraints, an aggregated validation is constructed that only passes if all constraints are
     * satisfied. Short-circuiting is applied: as soon as one violated constraint is found, the remaining constraints
     * are not checked.
     * </p>
     * 
     * @param solution solution to validate
     * @return aggregated validation
     */
    @Override
    public Validation validate(SolutionType solution){
        if(mandatoryConstraints.isEmpty()){
            // CASE 1: no mandatory constraints
            return SimpleValidation.PASSED;
        } else if (mandatoryConstraints.size() == 1){
            // CASE 2: single mandatory constraint
            return mandatoryConstraints.get(0).validate(solution, data);
        } else {
            // CASE 3 (default): aggregate multiple constraint validations
            UnanimousValidation val = new UnanimousValidation();
            mandatoryConstraints.stream()
                                .allMatch(c -> {
                                    // validate solution against constraint c
                                    Validation cval = c.validate(solution, data);
                                    // add to unanimous validation
                                    val.addValidation(c, cval);
                                    // continue until one constraint is not satisfied
                                    return cval.passed();
                                });
            return val;
        }
    }
    
    /**
     * <p>
     * Validate a move by checking all mandatory constraints (delta validation).
     * The move will only pass validation if all mandatory constraints are satisfied.
     * </p>
     * <p>
     * In case there are no mandatory constraints, this method always returns {@link SimpleValidation#PASSED}.
     * If a single mandatory constraint has been specified, the corresponding delta validation is returned. In case
     * of two or more constraints, an aggregated validation is constructed that only passes if all constraints are
     * satisfied. Short-circuiting is applied: as soon as one violated constraint is found, the remaining constraints
     * are not checked.
     * </p>
     * 
     * @param move move to validate
     * @param curSolution current solution of a local search
     * @param curValidation validation of current solution
     * @throws IncompatibleDeltaValidationException if the provided delta validation of any mandatory
     *                                              constraint is not compatible with the received move type
     * @return aggregated delta validation
     */
    @Override
    public Validation validate(Move<? super SolutionType> move,
                                        SolutionType curSolution,
                                        Validation curValidation){
        if(mandatoryConstraints.isEmpty()){
            // CASE 1: no mandatory constraints
            return SimpleValidation.PASSED;
        } else if (mandatoryConstraints.size() == 1){
            // CASE 2: single mandatory constraint
            return mandatoryConstraints.get(0).validate(move, curSolution, curValidation, data);
        } else {
            // CASE 3 (default): aggregate multiple constraint validations
            UnanimousValidation curUnanimousVal = (UnanimousValidation) curValidation;
            UnanimousValidation newUnanimousVal = new UnanimousValidation();
            mandatoryConstraints.stream()
                                .allMatch(c -> {
                                    // retrieve original validation produced by constraint c
                                    Validation curval = curUnanimousVal.getValidation(c);
                                    if(curval == null){
                                        // current validation unknown: perform full validation
                                        // (can happen due to short-circuiting behaviour)
                                        curval = c.validate(curSolution, data);
                                    }
                                    // validate move against constraint c
                                    Validation newval = c.validate(move, curSolution, curval, data);
                                    // add to unanimous validation
                                    newUnanimousVal.addValidation(c, newval);
                                    // continue until one constraint is not satisfied
                                    return newval.passed();
                                });
            return newUnanimousVal;
        }
    }
    
    /**
     * Returns a collection of all violated constraints (both mandatory and penalizing).
     * 
     * @param solution solution for which all violated constraints are determined
     * @return collection of all violated constraints (mandatory and penalizing); possibly empty
     */
    public Collection<Constraint<? super SolutionType, ? super DataType>> getViolatedConstraints(SolutionType solution){
        // return set with all violated constraints
        return Stream.concat(mandatoryConstraints.stream(), penalizingConstraints.stream())
                     .filter(c -> !c.validate(solution, data).passed())
                     .collect(Collectors.toSet());
    }

    /**
     * Evaluates a solution by taking into account both the evaluation calculated by the objective function and the
     * penalizing constraints (if any). Penalties are assigned for any violated penalizing constraint, which are
     * subtracted from the evaluation in case of maximization, and added to it in case of minimization.
     * <p>
     * If there are no penalizing constraints, this method returns the evaluation object obtained from applying
     * the objective function to the given solution. If one or more penalizing constraints have been specified,
     * a penalized evaluation is constructed taking into account both the main objective function evaluation
     * and assigned penalties.
     * 
     * @param solution solution to be evaluated
     * @return aggregated evaluation taking into account both the objective function and penalizing constraints
     */
    @Override
    public Evaluation evaluate(SolutionType solution) {
        if(penalizingConstraints.isEmpty()){
            // CASE 1: no penalizing constraints
            return objective.evaluate(solution, data);
        } else {
            // CASE 2 (default): aggregate evaluation and penalties
            Evaluation eval = objective.evaluate(solution, data);
            // initialize penalized evaluation object
            PenalizedEvaluation penEval = new PenalizedEvaluation(eval, isMinimizing());
            // add penalties
            penalizingConstraints.forEach(pc -> penEval.addPenalizingValidation(pc, pc.validate(solution, data)));
            // return aggregated evaluation
            return penEval;
        }
    }
    
    /**
     * Evaluate a move (delta evaluation) by taking into account both the evaluation of the modified solution and
     * the penalizing constraints (if any). Penalties are assigned for any violated penalizing constraint, which are
     * subtracted from the evaluation in case of maximization, and added to it in case of minimization.
     * <p>
     * If there are no penalizing constraints, this method returns the delta evaluation obtained from the objective
     * function. If one or more penalizing constraints have been specified, a penalized delta evaluation is constructed
     * taking into account both the main objective function evaluation and assigned penalties.
     * 
     * @param move move to evaluate
     * @param curSolution current solution
     * @param curEvaluation current evaluation
     * @throws IncompatibleDeltaEvaluationException if the provided delta evaluation of the objective
     *                                              is not compatible with the received move type
     * @throws IncompatibleDeltaValidationException if the provided delta validation of any penalizing
     *                                              constraint is not compatible with the received move type
     * @return aggregated evaluation of modified solution, taking into account both the objective
     *         function and penalizing constraints
     */
    @Override
    public Evaluation evaluate(Move<? super SolutionType> move, SolutionType curSolution, Evaluation curEvaluation){
        if(penalizingConstraints.isEmpty()){
            // CASE 1: no penalizing constraints -- directly apply delta
            return objective.evaluate(move, curSolution, curEvaluation, data);
        } else {
            // CASE 2 (default): penalizing constraint(s) -- extract components and apply deltas
            PenalizedEvaluation curPenalizedEval = (PenalizedEvaluation) curEvaluation;
            // retrieve current evaluation without penalties
            Evaluation curEval = curPenalizedEval.getEvaluation();
            // perform delta evaluation
            Evaluation newEval = objective.evaluate(move, curSolution, curEval, data);
            // initialize new penalized evaluation
            PenalizedEvaluation newPenalizedEval = new PenalizedEvaluation(newEval, isMinimizing());
            // perform delta validation for each penalizing constraint
            penalizingConstraints.forEach(pc -> {
                // retrieve current penalizing validation
                PenalizingValidation curVal = curPenalizedEval.getPenalizingValidation(pc);
                // delta validation
                PenalizingValidation newVal = pc.validate(move, curSolution, curVal, data);
                // add penalty
                newPenalizedEval.addPenalizingValidation(pc, newVal);
            });
            return newPenalizedEval;
        }
    }

    /**
     * Delegates to the contained random solution generator.
     * 
     * @param rnd source of randomness
     * @return random solution
     */
    @Override
    public SolutionType createRandomSolution(Random rnd) {
        return randomSolutionGenerator.create(rnd, data);
    }    

    /**
     * Indicates whether the underlying objective is minimizing.
     * 
     * @return true if the objective is minimizing
     */
    @Override
    public boolean isMinimizing(){
        return objective.isMinimizing();
    }

}
