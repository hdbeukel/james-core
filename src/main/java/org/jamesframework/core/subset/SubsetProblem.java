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

package org.jamesframework.core.subset;

import java.util.Comparator;
import java.util.Set;
import org.jamesframework.core.exceptions.IncompatibleDeltaValidationException;
import org.jamesframework.core.problems.GenericProblem;
import org.jamesframework.core.problems.constraints.validations.SimpleValidation;
import org.jamesframework.core.problems.constraints.validations.Validation;
import org.jamesframework.core.subset.validations.SubsetValidation;
import org.jamesframework.core.problems.datatypes.IntegerIdentifiedData;
import org.jamesframework.core.problems.objectives.Objective;
import org.jamesframework.core.search.neigh.Move;
import org.jamesframework.core.subset.neigh.moves.SubsetMove;
import org.jamesframework.core.util.SetUtilities;

/**
 * High-level subset problem consisting of data, an objective and possibly some constraints (see {@link GenericProblem}).
 * All items in the data set are identified using a unique integer ID so that any subset selection problem comes down to
 * selection of a subset of these IDs. The solution type is fixed to {@link SubsetSolution} and the data type can be set
 * to any implementation of the {@link IntegerIdentifiedData} interface. When creating the problem, the minimum and maximum
 * allowed subset size are specified. A default random solution generator is used, which generates random subsets within
 * the imposed size limits, based on the IDs of the items as retrieved from the underlying data. An additional method is
 * provided to generate subset solutions with an empty selection.
 * 
 * @param <DataType> underlying data type, should implement the interface {@link IntegerIdentifiedData}
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class SubsetProblem<DataType extends IntegerIdentifiedData> extends GenericProblem<SubsetSolution, DataType> {
    
    // minimum and maximum subset size
    private int minSubsetSize, maxSubsetSize;
    
    // indicates the order imposed on the IDs
    private final Comparator<Integer> orderOfIDs;
    
    /**
     * <p>
     * Creates a new subset problem with given data, objective and minimum/maximum subset size. Both <code>objective</code>
     * and <code>data</code> are not allowed to be <code>null</code>, an exception will be thrown if they are. Any objective
     * designed to evaluate subset solutions (or more general solutions) using the specified data type (or more general data)
     * is accepted. The minimum and maximum subset size should be contained in <code>[0,n]</code> where <code>n</code>
     * is the number of items in the given data from which a subset is to be selected. Also, the minimum size
     * should be smaller than or equal to the maximum size.
     * </p>
     * <p>
     * The given comparator <code>orderOfIDs</code> is used to impose an ordering on the IDs in generated subset solutions.
     * This argument may be <code>null</code> in which case no ordering is imposed.
     * </p>
     * 
     * @param data underlying data, can not be <code>null</code>
     * @param objective objective function, can not be <code>null</code>
     * @param minSubsetSize minimum subset size (should be &ge; 0 and &le; maximum subset size)
     * @param maxSubsetSize maximum subset size (should be &ge; minimum subset size and &le; number of items in underlying data)
     * @param orderOfIDs the order that is imposed on the IDs in any generated subset solution, may be <code>null</code>
     *                   in case no order is to be imposed
     * 
     * @throws NullPointerException if <code>objective</code> or <code>data</code> is <code>null</code>
     * @throws IllegalArgumentException if an invalid minimum or maximum subset size is specified
     */
    public SubsetProblem(DataType data, Objective<? super SubsetSolution, ? super DataType> objective,
                         int minSubsetSize, int maxSubsetSize, Comparator<Integer> orderOfIDs) {
        // call constructor of generic problem (already checks that objective is not null)
        // set default random subset solution generator to create random subsets within the imposed size limits
        super(data, objective, (r, d) -> {
            // pick random number of selected IDs within bounds
            int size = minSubsetSize + r.nextInt(maxSubsetSize-minSubsetSize+1);
            // randomly generate selection
            Set<Integer> selection = SetUtilities.getRandomSubset(d.getIDs(), size, r);
            // create subset solution with this selection
            SubsetSolution sol = new SubsetSolution(d.getIDs(), selection, orderOfIDs);
            // return random solution
            return sol;
        });
        // check that data is not null
        if(data == null){
            throw new NullPointerException("Error while creating subset problem: data is required, can not be null.");
        }
        // check constraints on minimum/maximum size
        if(minSubsetSize < 0){
            throw new IllegalArgumentException("Error while creating subset problem: minimum subset size should be >= 0.");
        }
        if(maxSubsetSize > data.getIDs().size()){
            throw new IllegalArgumentException("Error while creating subset problem: maximum subset size can not be larger "
                                                + "than number of items in underlying data.");
        }
        if(minSubsetSize > maxSubsetSize){
            throw new IllegalArgumentException("Error while creating subset problem: minimum subset size should be <= maximum subset size.");
        }
        // store min/max size
        this.minSubsetSize = minSubsetSize;
        this.maxSubsetSize = maxSubsetSize;
        // store ID order
        this.orderOfIDs = orderOfIDs;
    }
    
    /**
     * <p>
     * Creates a new subset problem with given data, objective and minimum/maximum subset size. Both <code>objective</code>
     * and <code>data</code> are not allowed to be <code>null</code>, an exception will be thrown if they are. Any objective
     * designed to evaluate subset solutions (or more general solutions) using the specified data type (or more general data)
     * is accepted. The minimum and maximum subset size should be contained in <code>[0,n]</code> where <code>n</code>
     * is the number of items in the given data from which a subset is to be selected. Also, the minimum size
     * should be smaller than or equal to the maximum size.
     * </p>
     * <p>
     * If <code>orderIDs</code> is <code>true</code>, subset solutions generated by this problem will order IDs
     * in ascending order (according to the natural integer ordering).
     * </p>
     * 
     * @param data underlying data, can not be <code>null</code>
     * @param objective objective function, can not be <code>null</code>
     * @param minSubsetSize minimum subset size (should be &ge; 0 and &le; maximum subset size)
     * @param maxSubsetSize maximum subset size (should be &ge; minimum subset size and &le; number of items in underlying data)
     * @param orderIDs indicates whether IDs are ordered (ascending) in generated subset solutions
     * 
     * @throws NullPointerException if <code>objective</code> or <code>data</code> is <code>null</code>
     * @throws IllegalArgumentException if an invalid minimum or maximum subset size is specified
     */
    public SubsetProblem(DataType data, Objective<? super SubsetSolution, ? super DataType> objective,
                         int minSubsetSize, int maxSubsetSize, boolean orderIDs) {
        this(data, objective, minSubsetSize, maxSubsetSize, orderIDs ? Comparator.naturalOrder() : null);
    }
    
    /**
     * Creates a new subset problem with given data, objective and minimum/maximum subset size. Both <code>objective</code>
     * and <code>data</code> are not allowed to be <code>null</code>, an exception will be thrown if they are. Any objective
     * designed to evaluate subset solutions (or more general solutions) using the specified data type (or more general data)
     * is accepted. The minimum and maximum subset size should be contained in <code>[0,n]</code> where <code>n</code>
     * is the number of items in the given data from which a subset is to be selected. Also, the minimum size
     * should be smaller than or equal to the maximum size. Generated subset solutions do not impose any
     * order on the IDs.
     * 
     * @param data underlying data, can not be <code>null</code>
     * @param objective objective function, can not be <code>null</code>
     * @param minSubsetSize minimum subset size (should be &ge; 0 and &le; maximum subset size)
     * @param maxSubsetSize maximum subset size (should be &ge; minimum subset size and &le; number of items in underlying data)
     * @throws NullPointerException if <code>objective</code> or <code>data</code> is <code>null</code>
     * @throws IllegalArgumentException if an invalid minimum or maximum subset size is specified
     */
    public SubsetProblem(DataType data, Objective<? super SubsetSolution, ? super DataType> objective,
                         int minSubsetSize, int maxSubsetSize) {
        this(data, objective, minSubsetSize, maxSubsetSize, false);
    }
    
    /**
     * Creates a subset problem with fixed subset size. Equivalent to calling<pre>
     * SubsetProblem p = new SubsetProblem(data, objective, fixedSubsetSize, fixedSubsetSize);</pre>
     * The fixed subset size should be contained in <code>[0,n]</code> where <code>n</code>
     * is the number of items in the given data from which a subset is to be selected.
     * Generated subset solutions do not impose any order on the IDs.
     * 
     * @param data underlying data, can not be <code>null</code>
     * @param objective objective function, can not be <code>null</code>
     * @param fixedSubsetSize fixed subset size
     * @throws NullPointerException if <code>objective</code> or <code>data</code> is <code>null</code>
     * @throws IllegalArgumentException if an invalid fixed subset size is specified 
     */
    public SubsetProblem(DataType data, Objective<? super SubsetSolution, ? super DataType> objective, int fixedSubsetSize) {
        this(data, objective, fixedSubsetSize, fixedSubsetSize);
    }
    
    /**
     * Creates a subset problem without subset size limits. Equivalent to calling<pre>
     * SubsetProblem p = new SubsetProblem(data, objective, 0, data.getIDs().size());</pre>
     * Generated subset solutions do not impose any order on the IDs.
     * 
     * @param data underlying data, can not be <code>null</code>
     * @param objective objective function, can not be <code>null</code>
     * @throws NullPointerException if <code>objective</code> or <code>data</code> is <code>null</code>
     * @throws IllegalArgumentException if an invalid fixed subset size is specified 
     */
    public SubsetProblem(DataType data, Objective<? super SubsetSolution, ? super DataType> objective) {
        this(data, objective, 0, data.getIDs().size());
    }
    
    /**
     * Set new data.
     * 
     * @param data new data (can not be <code>null</code>)
     * @throws NullPointerException if <code>data</code> is <code>null</code>
     */
    @Override
    public void setData(DataType data) {
        // check not null
        if(data == null){
            throw new NullPointerException("Error while setting data in subset problem: data can not be null.");
        }
        // not null: call super
        super.setData(data);
    }
    
    /**
     * Creates an empty subset solution in which no IDs are selected.
     * The set of all IDs is obtained from the underlying data and passed
     * to the created empty solution.
     *
     * @return empty subset solution with no selected IDs
     */
    public SubsetSolution createEmptySubsetSolution(){
        return new SubsetSolution(getData().getIDs(), orderOfIDs);
    }
    
    /**********************************************************************/
    /* CONSTANT VALIDATION OBJECTS USED FOR UNCONSTRAINED SUBSET PROBLEMS */
    /**********************************************************************/

    private static final SubsetValidation UNCONSTRAINED_VALID_SIZE = new SubsetValidation(true, SimpleValidation.PASSED);
    private static final SubsetValidation UNCONSTRAINED_INVALID_SIZE = new SubsetValidation(false, SimpleValidation.PASSED);
    
    /**********************************************************************/
    
    /**
     * Validate a subset solution. The returned validation object separately indicates whether
     * the solution passed general mandatory constraint validation and whether it has a valid size.
     * 
     * @param solution solution to validate
     * @return subset validation
     */
    @Override
    public SubsetValidation validate(SubsetSolution solution){
        // check size
        boolean validSize = solution.getNumSelectedIDs() >= getMinSubsetSize()
                                && solution.getNumSelectedIDs() <= getMaxSubsetSize();
        // combine with mandatory constraint validation
        if(getMandatoryConstraints().isEmpty()){
            // CASE 1: no mandatory constraints -- return constant validation object
            return validSize ? UNCONSTRAINED_VALID_SIZE : UNCONSTRAINED_INVALID_SIZE;
        } else {
            // CASE 2: mandatory constraint(s) -- wrap constraints validation in subset validation
            Validation constraintVal = super.validate(solution);
            return new SubsetValidation(validSize, constraintVal);
        }
    }
    
    /**
     * Validate a move to be applied to the current subset solution of a local search (delta validation).
     * A subset problem can only perform delta validation for moves of type {@link SubsetMove}. If the
     * received move has a different type an {@link IncompatibleDeltaValidationException} is thrown.
     * 
     * @param move subset move to be validated
     * @param curSolution current solution
     * @param curValidation current validation
     * @return subset validation of modified subset solution
     * @throws IncompatibleDeltaValidationException if the received move is not of type {@link SubsetMove}
     *                                              or if the delta validation of any mandatory constraint
     *                                              is not compatible with the received move type
     */
    @Override
    public SubsetValidation validate(Move<? super SubsetSolution> move,
                                     SubsetSolution curSolution,
                                     Validation curValidation){
        // check type and cast
        if(move instanceof SubsetMove){

            SubsetMove subsetMove = (SubsetMove) move;
            
            // update and check size
            int newSize = curSolution.getNumSelectedIDs() + subsetMove.getNumAdded() - subsetMove.getNumDeleted();
            boolean validSize = newSize >= getMinSubsetSize() && newSize <= getMaxSubsetSize();
            
            // combine with mandatory constraint delta validation
            if(getMandatoryConstraints().isEmpty()){
                // CASE 1: no mandatory constraints -- return constant validation object
                return validSize ? UNCONSTRAINED_VALID_SIZE : UNCONSTRAINED_INVALID_SIZE;
            } else {
                // CASE 2: mandatory constraint(s) -- extract and update mandatory constraints validation
                SubsetValidation subsetVal = (SubsetValidation) curValidation;
                // delta validation of mandatory constraints
                Validation deltaVal = super.validate(subsetMove, curSolution, subsetVal.getConstraintValidation());
                // create and return new subset validation
                return new SubsetValidation(validSize, deltaVal);
            }
            
        } else {
            throw new IncompatibleDeltaValidationException("Delta validation in subset problem expects moves "
                                                            + "of type SubsetMove. Received: "
                                                            + move.getClass().getSimpleName());
        }
    }
    
    /**
     * Get the minimum subset size.
     * 
     * @return minimum subset size
     */
    public int getMinSubsetSize() {
        return minSubsetSize;
    }

    /**
     * Set the minimum subset size. Specified size should be &ge; 1 and &le; the current maximum subset size.
     * 
     * @param minSubsetSize new minimum subset size
     * @throws IllegalArgumentException if an invalid minimum size is given
     */
    public void setMinSubsetSize(int minSubsetSize) {
        // check size
        if(minSubsetSize <= 0){
            throw new IllegalArgumentException("Error while setting minimum subset size: should be > 0.");
        }
        if(minSubsetSize > maxSubsetSize){
            throw new IllegalArgumentException("Error while setting minimum subset size: should be <= maximum subset size.");
        }
        this.minSubsetSize = minSubsetSize;
    }

    /**
     * Get the maximum subset size.
     * 
     * @return maximum subset size
     */
    public int getMaxSubsetSize() {
        return maxSubsetSize;
    }

    /**
     * Set the maximum subset size. Specified size should be &ge; the current minimum subset size
     * and &le; the number of items in the underlying data.
     * 
     * @param maxSubsetSize new maximum subset size
     * @throws IllegalArgumentException if an invalid maximum size is given
     */
    public void setMaxSubsetSize(int maxSubsetSize) {
        // check size
        if(maxSubsetSize < minSubsetSize){
            throw new IllegalArgumentException("Error while setting maximum subset size: should be >= minimum subset size.");
        }
        if(maxSubsetSize > getData().getIDs().size()){
            throw new IllegalArgumentException("Error while setting maximum subset size: can not be larger "
                                                + "than number of items in underlying data.");
        }
        this.maxSubsetSize = maxSubsetSize;
    }

}
