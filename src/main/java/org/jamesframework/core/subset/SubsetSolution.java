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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.jamesframework.core.exceptions.SolutionModificationException;
import org.jamesframework.core.problems.sol.Solution;

/**
 * High-level subset solution modeled in terms of IDs of selected items. The subset is sampled from a
 * data set of items which are each required to be identified with a unique integer ID. By default, the
 * IDs are unordered. If desired, they can be ordered according to the natural ordering of integers
 * (ascending) or a custom comparator, by using an appropriate constructor. In the latter case, it is
 * safe to cast the views returned by {@link #getSelectedIDs()}, {@link #getUnselectedIDs()} and
 * {@link #getAllIDs()} to the {@link NavigableSet} subtype.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class SubsetSolution extends Solution {
    
    // set of selected IDs (+ unmodifiable view)
    private final Set<Integer> selected, selectedView;
    // set of unselected IDs (+ unmodifiable view)
    private final Set<Integer> unselected, unselectedView;
    // set of all IDs (+ unmodifiable view)
    private final Set<Integer> all, allView;
    // comparator according to which IDs are sorted;
    // null in case no order has been imposed
    private Comparator<Integer> orderOfIDs;
    
    /**
     * Creates a new subset solution given the set of all IDs, each corresponding to an underlying entity,
     * from which a subset is to be selected. Initially, no IDs are selected. Note: IDs are copied to the
     * internal data structures of the subset solution; no reference is stored to the set given at construction.
     * IDs are stored in unordered sets.
     * 
     * @param allIDs set of all IDs from which a subset is to be selected
     * @throws NullPointerException if <code>allIDs</code> is <code>null</code>
     *                              or contains any <code>null</code> elements
     * @throws IllegalArgumentException if <code>allIDs</code> is empty
     */
    public SubsetSolution(Set<Integer> allIDs){
        this(allIDs, false);
    }
    
    /**
     * Creates a new subset solution given the set of all IDs, and the set of currently selected IDs. Note: IDs
     * are copied to the internal data structures of the subset solution; no reference is stored to the sets given
     * at construction. IDs are stored in unordered sets.
     * 
     * @param allIDs set of all IDs from which a subset is to be selected
     * @param selectedIDs set of currently selected IDs (subset of all IDs)
     * @throws NullPointerException if <code>allIDs</code> or <code>selectedIDs</code> are <code>null</code>
     *                              or contain any <code>null</code> elements
     * @throws IllegalArgumentException if <code>allIDs</code> is empty or <code>selectedIDs</code>
     *                                  is not a subset of <code>allIDs</code>
     */
    public SubsetSolution(Set<Integer> allIDs, Set<Integer> selectedIDs){
        this(allIDs, selectedIDs, false);
    }
    
    /**
     * Creates a new subset solution given the set of all IDs, each corresponding to an underlying entity,
     * from which a subset is to be selected. Initially, no IDs are selected. Note: IDs are copied to the
     * internal data structures of the subset solution; no reference is stored to the set given at construction.
     * If <code>naturalOrder</code> is <code>true</code>, the sets of selected, unselected and all IDs are represented
     * as navigable sorted sets ordered according to the natural ordering of integers (ascending); else, no ordering
     * is imposed.
     * 
     * @param allIDs set of all IDs from which a subset is to be selected
     * @param naturalOrder if <code>naturalOrder</code> is <code>true</code>, IDs will be ordered according to
     *                     their natural ordering; else, no ordering is imposed on the IDs
     * @throws NullPointerException if <code>allIDs</code> is <code>null</code>
     *                              or contains any <code>null</code> elements
     * @throws IllegalArgumentException if <code>allIDs</code> is empty
     */
    public SubsetSolution(Set<Integer> allIDs, boolean naturalOrder){
        this(allIDs, naturalOrder ? Comparator.naturalOrder() : null);
    }
    
    /**
     * Creates a new subset solution given the set of all IDs, and the set of currently selected IDs. Note: IDs
     * are copied to the internal data structures of the subset solution; no reference is stored to the sets given
     * at construction. If <code>naturalOrder</code> is <code>true</code>, the sets of selected, unselected and all
     * IDs are represented as navigable sorted sets ordered according to the natural ordering of integers (ascending);
     * else, no ordering is imposed.
     * 
     * @param allIDs set of all IDs from which a subset is to be selected
     * @param selectedIDs set of currently selected IDs (subset of all IDs)
     * @param naturalOrder if <code>naturalOrder</code> is <code>true</code>, IDs will be ordered according to
     *                     their natural ordering; else, no ordering is imposed on the IDs
     * @throws NullPointerException if <code>allIDs</code> or <code>selectedIDs</code> are <code>null</code>
     *                              or contain any <code>null</code> elements
     * @throws IllegalArgumentException if <code>allIDs</code> is empty or <code>selectedIDs</code>
     *                                  is not a subset of <code>allIDs</code>
     */
    public SubsetSolution(Set<Integer> allIDs, Set<Integer> selectedIDs, boolean naturalOrder){
        this(allIDs, selectedIDs, naturalOrder ? Comparator.naturalOrder() : null);
    }
    
    /**
     * Creates a new subset solution given the set of all IDs, each corresponding to an underlying entity,
     * from which a subset is to be selected. Initially, no IDs are selected. Note: IDs are copied to the
     * internal data structures of the subset solution; no reference is stored to the set given at construction.
     * If <code>orderOfIDs</code> is not <code>null</code>, the sets of selected, unselected and all IDs are
     * represented as navigable sorted sets ordered according to this comparator; else, no ordering is imposed.
     * 
     * @param allIDs set of all IDs from which a subset is to be selected
     * @param orderOfIDs comparator according to which IDs are ordered, allowed to be
     *                   <code>null</code> in which case no order is imposed
     * @throws NullPointerException if <code>allIDs</code> is <code>null</code>
     *                              or contains any <code>null</code> elements
     * @throws IllegalArgumentException if <code>allIDs</code> is empty
     */
    public SubsetSolution(Set<Integer> allIDs, Comparator<Integer> orderOfIDs){
        this(allIDs, Collections.emptySet(), orderOfIDs);
    }
    
    /**
     * Creates a new subset solution given the set of all IDs, and the set of currently selected IDs. Note: IDs
     * are copied to the internal data structures of the subset solution; no reference is stored to the sets given
     * at construction. If <code>orderOfIDs</code> is not <code>null</code>, the sets of selected, unselected and
     * all IDs are represented as navigable sorted sets ordered according to this comparator; else, no ordering is
     * imposed.
     * 
     * @param allIDs set of all IDs from which a subset is to be selected
     * @param selectedIDs set of currently selected IDs (subset of all IDs)
     * @param orderOfIDs comparator according to which IDs are ordered, allowed to be
     *                   <code>null</code> in which case no order is imposed
     * @throws NullPointerException if <code>allIDs</code> or <code>selectedIDs</code> are <code>null</code>
     *                              or contain any <code>null</code> elements
     * @throws IllegalArgumentException if <code>allIDs</code> is empty or <code>selectedIDs</code>
     *                                  is not a subset of <code>allIDs</code>
     */
    public SubsetSolution(Set<Integer> allIDs, Set<Integer> selectedIDs, Comparator<Integer> orderOfIDs){
        this(allIDs, selectedIDs, orderOfIDs, true);
    }
    
    /**
     * Private constructor used to skip input checks when copying a solution (see {@link #copy()}).
     * 
     * @param allIDs set of all IDs from which a subset is to be selected
     * @param selectedIDs set of currently selected IDs (subset of all IDs)
     * @param orderOfIDs comparator according to which IDs are ordered, allowed to be
     *                   <code>null</code> in which case no order is imposed
     * @param checkInput indicates whether input should be checked (see thrown exceptions)
     * @throws NullPointerException if <code>checkInput</code> is <code>true</code> and
     *                              <code>allIDs</code> or <code>selectedIDs</code> are
     *                              <code>null</code> or contain any <code>null</code> elements
     * @throws IllegalArgumentException if <code>checkInput</code> is <code>true</code>, and
     *                                  <code>allIDs</code> is empty or <code>selectedIDs</code>
     *                                  is not a subset of <code>allIDs</code>
     */
    private SubsetSolution(Set<Integer> allIDs,
                           Set<Integer> selectedIDs,
                           Comparator<Integer> orderOfIDs,
                           boolean checkInput){

        // check input if requested
        if(checkInput){
            if(allIDs == null){
                throw new NullPointerException("Error when creating subset solution: set of all IDs can not be null.");
            }
            if(allIDs.stream().anyMatch(Objects::isNull)){
                throw new NullPointerException("Error when creating subset solution: set of all IDs can not contain any null elements.");
            }
            if(allIDs.isEmpty()){
                throw new IllegalArgumentException("Error when creating subset solution: set of all IDs can not be empty.");
            }
            if(selectedIDs == null){
                throw new NullPointerException("Error when creating subset solution: set of selected IDs can not be null.");
            }
            if(selectedIDs.stream().anyMatch(Objects::isNull)){
                throw new NullPointerException("Error when creating subset solution: set of selected IDs can not contain any null elements.");
            }
        }
        
        // store comparator according to which IDs are ordered
        this.orderOfIDs = orderOfIDs;
        
        // create sets of selection, unselected and all IDs (+ unmodifiable views)
        if(orderOfIDs == null){
            // CASE 1: no order
            all = new LinkedHashSet<>(allIDs);                      // set with all IDs (copy)
            selected = new LinkedHashSet<>();                       // set with selected IDs (empty)
            unselected = new LinkedHashSet<>(allIDs);               // set with unselected IDs (all)
            // create views
            allView = Collections.unmodifiableSet(all);
            selectedView = Collections.unmodifiableSet(selected);
            unselectedView = Collections.unmodifiableSet(unselected);
        } else {
            // CASE 2: order IDs according to given comparator
            all = new TreeSet<>(orderOfIDs);           // sorted set with all IDs (copy)
            all.addAll(allIDs);
            selected = new TreeSet<>(orderOfIDs);      // sorted set with selected IDs (empty)
            unselected = new TreeSet<>(orderOfIDs);    // sorted set with unselected IDs (all)
            unselected.addAll(allIDs);
            // create views (navigable!)
            allView = Collections.unmodifiableNavigableSet((TreeSet<Integer>) all);
            selectedView = Collections.unmodifiableNavigableSet((TreeSet<Integer>) selected);
            unselectedView = Collections.unmodifiableNavigableSet((TreeSet<Integer>) unselected);
        }
        
        // select specified IDs
        for(int ID : selectedIDs){
            // check validtiy if requested
            if(checkInput && !allIDs.contains(ID)){
                throw new IllegalArgumentException("Error while creating subset solution: "
                                + "set of selected IDs should be a subset of set of all IDs.");
            }
            selected.add(ID);
            unselected.remove(ID);
        }
        
    }
        
    /**
     * Copy constructor. Creates a new subset solution which is identical to the given solution, but does not have
     * any reference to any data structures contained within the given solution (deep copy). The obtained subset
     * solution will have exactly the same selected/unselected IDs as the given solution, and will impose the
     * same ordering on the IDs (if any).
     * 
     * @param sol solution to copy (deep copy)
     */
    public SubsetSolution(SubsetSolution sol){
        // skip input checks, we know it's a valid subset solution
        this(sol.getAllIDs(), sol.getSelectedIDs(), sol.getOrderOfIDs(), false);
    }
    
    /**
     * Create a deep copy of this subset solution, obtained through the copy constructor.
     * 
     * @return deep copy of this subset solution
     */
    @Override
    public SubsetSolution copy() {
        return new SubsetSolution(this);
    }
    
    /**
     * Get the comparator according to which the IDs are ordered, as specified at construction.
     * May return <code>null</code> which means that no order has been imposed.
     * 
     * @return comparator according to which IDs are ordered, may be <code>null</code>
     */
    public Comparator<Integer> getOrderOfIDs(){
        return orderOfIDs;
    }
    
    /**
     * Select the given ID. If there is no entity with the given ID, a {@link SolutionModificationException} is thrown.
     * If the ID is currently already selected, the subset solution is not modified and false is returned. Finally,
     * true is returned if the ID has been successfully selected.
     * 
     * @param ID ID to be selected
     * @throws SolutionModificationException if there is no entity with this ID
     * @return true if the ID has been successfully selected, false if it was already selected
     */
    public boolean select(int ID) {
        // verify that the ID occurs
        if(!all.contains(ID)){
            throw new SolutionModificationException("Error while modifying subset solution: "
                                + "unable to select ID " +  ID + " (no entity with this ID).", this);
        }
        // verify that ID is currently not selected
        if(selected.contains(ID)){
            // already selected: return false
            return false;
        }
        // currently unselected, existing ID: select it
        selected.add(ID);
        unselected.remove(ID);
        return true;
    }
    
    /**
     * Deselect the given ID. If there is no entity with the given ID, a {@link SolutionModificationException} is thrown.
     * If the ID is currently not selected, the subset solution is not modified and false is returned. Finally,
     * true is returned if the ID has been successfully deselected.
     * 
     * @param ID ID to be deselected
     * @throws SolutionModificationException if there is no entity with this ID
     * @return true if the ID has been successfully deselected, false if it is currently not selected
     */
    public boolean deselect(int ID) {
        // verify that the ID occurs
        if(!all.contains(ID)){
            throw new SolutionModificationException("Error while modifying subset solution: "
                                + "unable to deselect ID " +  ID + " (no entity with this ID).", this);
        }
        // verify that ID is currently selected
        if(!selected.contains(ID)){
            // not selected: return false
            return false;
        }
        // currently selected, existing ID: deselect it
        selected.remove(ID);
        unselected.add(ID);
        return true;
    }
    
    /**
     * Select all IDs contained in the given collection. Returns true if the subset solution was modified by this
     * operation, i.e. if at least one previously unselected ID has been selected.
     * 
     * @param IDs collection of IDs to be selected
     * @throws SolutionModificationException if the given collection contains at least one ID which does not correspond to an entity
     * @throws NullPointerException if <code>null</code> is passed or the given collection contains at least one <code>null</code> element
     * @return true if the subset solution was modified
     */
    public boolean selectAll(Collection<Integer> IDs) {
        boolean modified = false;
        for(int ID : IDs){
            if(select(ID)){
                modified = true;
            }
        }
        return modified;
    }
    
    /**
     * Deselect all IDs contained in the given collection. Returns true if the subset solution was modified by this
     * operation, i.e. if at least one previously selected ID has been deselected.
     * 
     * @param IDs collection of IDs to be deselected
     * @throws SolutionModificationException if the given collection contains at least one ID which does not correspond to an entity
     * @throws NullPointerException if <code>null</code> is passed or the given collection contains at least one <code>null</code> element
     * @return true if the subset solution was modified
     */
    public boolean deselectAll(Collection<Integer> IDs) {
        boolean modified = false;
        for(int ID : IDs){
            if(deselect(ID)){
                modified = true;
            }
        }
        return modified;
    }
    
    /**
     * Select all IDs.
     */
    public void selectAll(){
        selectAll(getAllIDs());
    }
    
    /**
     * Deselect all IDs.
     */
    public void deselectAll(){
        deselectAll(getAllIDs());
    }
    
    /**
     * Returns an unmodifiable view of the set of currently selected IDs.  Any attempt to
     * modify the returned set will result in an {@link UnsupportedOperationException}. If
     * the IDs are ordered (see {@link #getOrderOfIDs()}), it is safe to cast the returned
     * view to the {@link NavigableSet} subtype.
     * 
     * @return unmodifiable view of currently selected IDs
     */
    public Set<Integer> getSelectedIDs(){
        return selectedView;
    }
    
    /**
     * Returns an unmodifiable view of the set of currently non selected IDs. Any attempt to
     * modify the returned set will result in an {@link UnsupportedOperationException}. If
     * the IDs are ordered (see {@link #getOrderOfIDs()}), it is safe to cast the returned
     * view to the {@link NavigableSet} subtype.
     * 
     * @return unmodifiable view of currently non selected IDs
     */
    public Set<Integer> getUnselectedIDs(){
        return unselectedView;
    }
    
    /**
     * Returns an unmodifiable view of the set of all IDs. Any attempt to modify the returned
     * set will result in an {@link UnsupportedOperationException}. If the IDs are ordered (see
     * {@link #getOrderOfIDs()}), it is safe to cast the returned view to the {@link NavigableSet}
     * subtype. This set will always be equal to the union of {@link #getSelectedIDs()} and
     * {@link #getUnselectedIDs()}.
     * 
     * @return unmodifiable view of all IDs
     */
    public Set<Integer> getAllIDs(){
        return allView;
    }
    
    /**
     * Get the number of IDs which are currently selected. Corresponds to the size of the selected subset.
     * 
     * @return number of selected IDs
     */
    public int getNumSelectedIDs(){
        return getSelectedIDs().size();
    }
    
    /**
     * Get the number of IDs which are currently unselected.
     * 
     * @return number of unselected IDs
     */
    public int getNumUnselectedIDs(){
        return getUnselectedIDs().size();
    }
    
    /**
     * Get the total number of IDs. The returned number will always be equal to the sum of
     * {@link #getNumSelectedIDs()} and {@link #getNumUnselectedIDs()}.
     * 
     * @return total number of IDs
     */
    public int getTotalNumIDs(){
        return getAllIDs().size();
    }

    /**
     * Checks whether the given other object represents the same subset solution.
     * Subset solutions are considered equal if and only if they contain exactly
     * the same selected and unselected IDs.
     * 
     * @param other other object to check for equality
     * @return <code>true</code> if the other object is also a subset solution and contains exactly the same
     *         selected and unselected IDs as this solution
     */
    @Override
    public boolean equals(Object other) {
        // check null
        if (other == null) {
            return false;
        }
        // check type
        if (getClass() != other.getClass()) {
            return false;
        }
        // cast to subset solution
        final SubsetSolution otherSubsetSolution = (SubsetSolution) other;
        // check selected and unselected IDs
        return Objects.equals(getSelectedIDs(), otherSubsetSolution.getSelectedIDs())
                && Objects.equals(getUnselectedIDs(), otherSubsetSolution.getUnselectedIDs());
    }

    /**
     * Computes a hash code that is consistent with the implementation of {@link #equals(Object)} meaning that
     * the same hash code is returned for equal subset solutions. The computed hash code is a linear combination
     * of the hash codes of both the set of selected and unselected IDs, added to a constant.
     * 
     * @return hash code of this subset solution
     */
    @Override
    public int hashCode() {
        int hash = 7;
        // account for selected IDs
        hash = 23 * hash + Objects.hashCode(getSelectedIDs());
        // account for unselected IDs
        hash = 23 * hash + Objects.hashCode(getUnselectedIDs());
        return hash;    
    }
    
    /**
     * Creates a formatted string containing the selected IDs.
     * 
     * @return formatted string
     */
    @Override
    public String toString(){
        return getSelectedIDs().stream()
                               .map(Object::toString)
                               .collect(Collectors.joining(", ", "Subset solution: {", "}"));
    }

}
