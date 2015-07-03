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
import org.jamesframework.core.problems.constraints.Constraint;
import org.jamesframework.core.problems.constraints.PenalizingConstraint;
import org.jamesframework.core.problems.constraints.validations.SimpleValidation;
import org.jamesframework.core.problems.constraints.validations.Validation;
import org.jamesframework.core.problems.sol.RandomSolutionGenerator;
import org.jamesframework.core.search.neigh.Move;
import org.jamesframework.test.stubs.AlwaysSatisfiedConstraintStub;
import org.jamesframework.test.stubs.AlwaysSatisfiedPenalizingConstraintStub;
import org.jamesframework.test.stubs.EmptySolutionStub;
import org.jamesframework.test.stubs.FixedEvaluationObjectiveStub;
import org.jamesframework.test.fakes.IntegerSolution;
import org.jamesframework.test.stubs.NeverSatisfiedConstraintStub;
import org.jamesframework.test.stubs.NeverSatisfiedPenalizingConstraintStub;
import org.jamesframework.test.util.TestConstants;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 * Test general functionalities of GenericProblem that do not actually rely on the specific data,
 * using objectives and constraints that ignore the data.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class GenericProblemTest {

    // problem stub to work with
    private GenericProblem<Solution, Object> problem;
    
    /**
     * Print message when starting tests.
     */
    @BeforeClass
    public static void setUpClass() {
        System.out.println("# Testing GenericProblem ...");
    }

    /**
     * Print message when tests are complete.
     */
    @AfterClass
    public static void tearDownClass() {
        System.out.println("# Done testing GenericProblem!");
    }
    
    /**
     * Create problem stub to work with in each test method.
     */
    @Before
    public void setUp(){
        FixedEvaluationObjectiveStub o = new FixedEvaluationObjectiveStub(10.0);
        problem = new GenericProblem<>(null, o, (r,d) -> null);
    }

    /**
     * Test constructor, of class GenericProblem.
     */
    @Test
    public void testConstructor() {
        
        System.out.println(" - test constructor");
        
        // try to create problem without objective, should result in error
        boolean thrown = false;
        try {
            GenericProblem p = new GenericProblem<>(null, null, null);
        } catch (NullPointerException ex) {
            thrown = true;
        }
        assertTrue(thrown);
        
        // try to create problem without random solution generator, should result in error
        thrown = false;
        try {
            GenericProblem p = new GenericProblem<>(new FixedEvaluationObjectiveStub(10.0), null, null);
        } catch (NullPointerException ex) {
            thrown = true;
        }
        assertTrue(thrown);
        
    }
    
    /**
     * Test of getObjective method, of class GenericProblem.
     */
    @Test
    public void testGetObjective() {
        
        System.out.println(" - test getObjective");
        
        FixedEvaluationObjectiveStub o = new FixedEvaluationObjectiveStub(128736.0);
        problem.setObjective(o);
        assertEquals(o, problem.getObjective());
        
    }

    /**
     * Test of setObjective method, of class GenericProblem.
     */
    @Test
    public void testSetObjective() {
        
        System.out.println(" - test setObjective");
        
        // try to set objective to null, should throw error
        boolean thrown = false;
        try {
            problem.setObjective(null);
        } catch (NullPointerException ex) {
            thrown = true;
        }
        assertTrue(thrown);
        
    }

    /**
     * Test of getData method, of class GenericProblem.
     */
    @Test
    public void testGetData() {
        
        System.out.println(" - test getData");
        
        // without data
        assertNull(problem.getData());
        
        // set data and get it again
        Object dummyData = "Dummy Data Object";
        problem.setData(dummyData);
        assertEquals(dummyData, problem.getData());
        
    }
    
    /**
     * Test of getRandomSolutionGenerator method, of class GenericProblem.
     */
    @Test
    public void testGetRandomSolutionGenerator() {
        
        System.out.println(" - test getRandomSolutionGenerator");
        
        // set, get and verify
        RandomSolutionGenerator<IntegerSolution, Object> rsl = (r,d) -> new IntegerSolution(r.nextInt());
        problem.setRandomSolutionGenerator(rsl);
        assertEquals(rsl, problem.getRandomSolutionGenerator());
        
    }
    
    /**
     * Test of setRandomSolutionGenerator method, of class GenericProblem.
     */
    @Test
    public void testSetRandomSolutionGenerator() {
        
        System.out.println(" - test setRandomSolutionGenerator");
        
        // try to set to null, should produce error
        boolean thrown = false;
        try {
            problem.setRandomSolutionGenerator(null);
        } catch (NullPointerException ex){
            thrown = true;
        }
        assertTrue(thrown);
        
    }
    
    /**
     * Test of addMandatoryConstraint method, of class GenericProblem.
     */
    @Test
    public void testAddMandatoryConstraint() {
       
        System.out.println(" - test addMandatoryConstraint");
        
        // check that both general constraints as well as penalizing
        // constraints can be added as a mandatory constraint
        problem.addMandatoryConstraint(new AlwaysSatisfiedConstraintStub());
        problem.addMandatoryConstraint(new NeverSatisfiedConstraintStub());
        problem.addMandatoryConstraint(new AlwaysSatisfiedPenalizingConstraintStub());
        problem.addMandatoryConstraint(new NeverSatisfiedPenalizingConstraintStub(123.0));
        
    }

    /**
     * Test of removeMandatoryConstraint method, of class GenericProblem.
     */
    @Test
    public void testRemoveMandatoryConstraint() {
        
        System.out.println(" - test removeMandatoryConstraint");
        
        // create some constraints
        Constraint<Solution,Object> c0 = new AlwaysSatisfiedConstraintStub();
        Constraint<Solution,Object>c1 = new NeverSatisfiedConstraintStub();
        Constraint<Solution,Object> c2 = new AlwaysSatisfiedPenalizingConstraintStub();
        
        
        // add constraint c0 and c1 as mandatory constraint
        problem.addMandatoryConstraint(c0);
        problem.addMandatoryConstraint(c1);
        // try to remove c2 which was never added
        assertFalse(problem.removeMandatoryConstraint(c2));
        // remove constraints that were added
        assertTrue(problem.removeMandatoryConstraint(c0));
        assertTrue(problem.removeMandatoryConstraint(c1));
        // try to remove again
        assertFalse(problem.removeMandatoryConstraint(c0));
        assertFalse(problem.removeMandatoryConstraint(c1));
        
    }

    /**
     * Test of addPenalizingConstraint method, of class GenericProblem.
     */
    @Test
    public void testAddPenalizingConstraint() {
        
        System.out.println(" - test addPenalizingConstraint");
        
        // add some penalizing constraints
        problem.addPenalizingConstraint(new AlwaysSatisfiedPenalizingConstraintStub());
        problem.addPenalizingConstraint(new NeverSatisfiedPenalizingConstraintStub(123.0));
        
    }

    /**
     * Test of removePenalizingConstraint method, of class GenericProblem.
     */
    @Test
    public void testRemovePenalizingConstraint() {
        
        System.out.println(" - test removePenalizingConstraint");
        
        // create some constraints
        PenalizingConstraint<Solution,Object> c0 = new AlwaysSatisfiedPenalizingConstraintStub();
        PenalizingConstraint<Solution,Object> c1 = new NeverSatisfiedPenalizingConstraintStub(123.0);
        
        
        // add constraint c0 as penalizing constraint
        problem.addPenalizingConstraint(c0);
        // try to remove c1 which was never added
        assertFalse(problem.removePenalizingConstraint(c1));
        // try to remove c0 as MANDATORY constraint -- NEVER added with this role
        assertFalse(problem.removeMandatoryConstraint(c0));
        // remove c0 as PENALIZING constraint
        assertTrue(problem.removePenalizingConstraint(c0));
        // try again
        assertFalse(problem.removePenalizingConstraint(c0));
        
    }

    /**
     * Test of validate method, of class GenericProblem.
     */
    @Test
    public void testValidate1() {
    
        System.out.println(" - test validate (1)");
        
        Solution sol = new EmptySolutionStub();
        
        // test without constraints
        assertTrue(problem.validate(sol).passed());
        
        // add constraints which are always satisfied
        problem.addMandatoryConstraint(new AlwaysSatisfiedConstraintStub());
        problem.addPenalizingConstraint(new AlwaysSatisfiedPenalizingConstraintStub());
        // verify
        assertTrue(problem.validate(sol).passed());
        
        // add unsatisfiable mandatory constraint
        Constraint<Solution,Object> unsatisfiable = new NeverSatisfiedConstraintStub();
        problem.addMandatoryConstraint(unsatisfiable);
        assertFalse(problem.validate(sol).passed());
        // remove the constraint
        problem.removeMandatoryConstraint(unsatisfiable);
        
        // same thing with unsatisfiable penalizing constraint
        PenalizingConstraint<Solution,Object> unsatisfiable2 = new NeverSatisfiedPenalizingConstraintStub(123.0);
        problem.addPenalizingConstraint(unsatisfiable2);
        // penalizing constraint does not cause solution to be invalid!
        assertTrue(problem.validate(sol).passed());
        // remove the constraint
        problem.removePenalizingConstraint(unsatisfiable2);
    
    }
    
    @Test
    public void testValidate2() {
    
        System.out.println(" - test validate (2)");
        
        // create an integer solution with value 0
        IntegerSolution sol = new IntegerSolution(0);
        
        // create a dummy move that increases the value of an integer solution by one
        Move<IntegerSolution> m = new Move<IntegerSolution>() {
            public void apply(IntegerSolution sol) { sol.setI(sol.getI()+1); }
            public void undo(IntegerSolution sol)  { sol.setI(sol.getI()-1); }
        };
        
        // create a dummy constraint that only accepts integer solutions with strictily positive value
        Constraint<IntegerSolution, Object> c = (s, data) -> new SimpleValidation(s.getI() > 0);
        
        // create problem stub with solution type IntegerSolution and fixed evaluation of 0.0
        GenericProblem<IntegerSolution, Object> intprob = new GenericProblem<>(null, new FixedEvaluationObjectiveStub(0.0), (r,d) -> null);
        // add constraint to problem
        intprob.addMandatoryConstraint(c);
        
        // validate solution
        Validation curval = intprob.validate(sol);
        assertFalse(curval.passed());
        
        // validate move
        assertTrue(intprob.validate(m, sol, curval).passed());
        
        // add a second constraint which is always satisfied
        intprob.addMandatoryConstraint(new AlwaysSatisfiedConstraintStub());
        
        // re-validate solution
        curval = intprob.validate(sol);
        assertFalse(curval.passed());
        // NOTE: due to short-circuiting, the second constraint will NOT be checked
        
        // re-validate move (for the second constraint, a full validation of
        // the current solution will first be performed since it is missing!)
        assertTrue(intprob.validate(m, sol, curval).passed());
    
    }
    
    /**
     * Test of getViolatedConstraints method, of class GenericProblem.
     */
    @Test
    public void testGetViolatedConstraints() {
        
        System.out.println(" - test getViolatedConstraints");
        
        Solution sol = new EmptySolutionStub();
        
        // test without constraints
        assertTrue(problem.getViolatedConstraints(sol).isEmpty());
        
        // add constraints which are always satisfied
        problem.addMandatoryConstraint(new AlwaysSatisfiedConstraintStub());
        problem.addPenalizingConstraint(new AlwaysSatisfiedPenalizingConstraintStub());
        // verify
        assertTrue(problem.getViolatedConstraints(sol).isEmpty());
        
        // add unsatisfiable mandatory constraint
        Constraint<Solution,Object> unsatisfiable = new NeverSatisfiedConstraintStub();
        problem.addMandatoryConstraint(unsatisfiable);
        assertEquals(1, problem.getViolatedConstraints(sol).size());
        assertTrue(problem.getViolatedConstraints(sol).contains(unsatisfiable));
        // remove the constraint
        problem.removeMandatoryConstraint(unsatisfiable);
        
        // same thing with unsatisfiable penalizing constraint
        PenalizingConstraint<Solution,Object> unsatisfiable2 = new NeverSatisfiedPenalizingConstraintStub(123.0);
        problem.addPenalizingConstraint(unsatisfiable2);
        assertEquals(1, problem.getViolatedConstraints(sol).size());
        assertTrue(problem.getViolatedConstraints(sol).contains(unsatisfiable2));
        // remove the constraint
        problem.removePenalizingConstraint(unsatisfiable2);
        
    }

    /**
     * Test of evaluate method, of class GenericProblem.
     */
    @Test
    public void testEvaluate() {
        
        System.out.println(" - test evaluate");
        
        double fixedEval = 123.0;
        Solution sol = new EmptySolutionStub();
        
        // test with a fixed objective only, no constraints
        FixedEvaluationObjectiveStub o = new FixedEvaluationObjectiveStub(fixedEval);
        problem.setObjective(o);
        assertEquals(fixedEval, problem.evaluate(sol).getValue(), TestConstants.DOUBLE_COMPARISON_PRECISION);
        
        // throw in a penalizing constraint that is always satisfied
        PenalizingConstraint<Solution,Object> c1 = new AlwaysSatisfiedPenalizingConstraintStub();
        problem.addPenalizingConstraint(c1);
        assertEquals(fixedEval, problem.evaluate(sol).getValue(), TestConstants.DOUBLE_COMPARISON_PRECISION);
        
        // add penalizing constraint which is never satisfied
        double c3penalty = 1234.0;
        PenalizingConstraint<Solution,Object> c3 = new NeverSatisfiedPenalizingConstraintStub(c3penalty);
        problem.addPenalizingConstraint(c3);
        assertEquals(fixedEval-c3penalty, problem.evaluate(sol).getValue(), TestConstants.DOUBLE_COMPARISON_PRECISION);
        
        // switch to minimizing and repeat
        o.setMinimizing();
        assertEquals(fixedEval+c3penalty, problem.evaluate(sol).getValue(), TestConstants.DOUBLE_COMPARISON_PRECISION);
        
        // switch back to maximizing
        o.setMaximizing();
        
        // add another unsatisfiable penalizing constraint
        double c4penalty = 12345.0;
        PenalizingConstraint<Solution,Object> c4 = new NeverSatisfiedPenalizingConstraintStub(c4penalty);
        problem.addPenalizingConstraint(c4);
        assertEquals(fixedEval-c3penalty-c4penalty, problem.evaluate(sol).getValue(), TestConstants.DOUBLE_COMPARISON_PRECISION);
        
        // switch to minimizing and repeat
        o.setMinimizing();
        assertEquals(fixedEval+c3penalty+c4penalty, problem.evaluate(sol).getValue(), TestConstants.DOUBLE_COMPARISON_PRECISION);
        
    }

    /**
     * Test of isMinimizing method, of class GenericProblem.
     */
    @Test
    public void testIsMinimizing() {
        
        System.out.println(" - test isMinimizing");
        
        // by default, objectives are maximizing
        assertFalse(problem.isMinimizing());
        
        // now switch to minimizing
        FixedEvaluationObjectiveStub o = new FixedEvaluationObjectiveStub(123.0);
        o.setMinimizing();
        problem.setObjective(o);
        assertTrue(problem.isMinimizing());
        
    }

}