package org.jamesframework.core.search.algo.tabu;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

import org.jamesframework.core.problems.Problem;
import org.jamesframework.core.problems.constraints.validations.Validation;
import org.jamesframework.core.problems.objectives.evaluations.Evaluation;
import org.jamesframework.core.problems.sol.Solution;
import org.jamesframework.core.search.SingleNeighbourhoodSearch;
import org.jamesframework.core.search.neigh.Move;
import org.jamesframework.core.search.neigh.Neighbourhood;
/**
 * Tabu search algorithm using first-best-admissible move strategy. 
 * In every search step, shuffle the list of moves and then iterate over all moves. If a valid neighbour that is better 
 * than the current solution is found, it is adopted as the new solution. Otherwise, the best valid and non-tabu neighbour 
 * is adopted, even if it is no improvement over the current solution, which is the same as the ordinary tabu search.
 * To avoid repeatedly revisiting the same solutions, moves might be declared tabu based on a tabu memory.
 * This memory dynamically tracks (a limited number of) recently visited solutions, features of these solutions and/or
 * recently applied moves (i.e. recently modified features). If a move is tabu, it is not considered, unless it yields
 * a solution which is better than the best solution found so far (aspiration criterion).
 * <p>
 * If all valid neighbours of the current solution are tabu and no valid neighbours are better than the current solution, 
 * the search stops. Note that this may never happen so that a stop criterion should preferably be set to ensure termination.
 * 
 * @param <SolutionType> solution type of the problems that may be solved using this search, required to extend {@link Solution}
 * @author <a href="mailto:chenhuanfa@gmail.com">Huanfa Chen</a>
 */
public class FirstBestAdmissibleTabuSearch<SolutionType extends Solution> extends TabuSearch<SolutionType>{

	public FirstBestAdmissibleTabuSearch(Problem<SolutionType> problem, Neighbourhood<? super SolutionType> neighbourhood,
            TabuMemory<SolutionType> tabuMemory) {
		super(problem, neighbourhood, tabuMemory);
		// TODO Auto-generated constructor stub
	}
	
    @Override
    protected void searchStep() {
        // get best valid, non tabu move
        Move<? super SolutionType> move = getBestMove(
                                            // inspect all moves
                                            getNeighbourhood().getAllMoves(getCurrentSolution()),
                                            // not necessarily an improvement
                                            false,
                                            // return first improvement move
                                            true,
                                            // filter tabu moves (with aspiration criterion)
                                            m -> !getTabuMemory().isTabu(m, getCurrentSolution())
                                                    || computeDelta(evaluate(m), getBestSolutionEvaluation()) > 0
        );                                               
        if(move != null){
            // accept move (also updates tabu memory by overriding move acceptance)
            accept(move);
        } else {
            // no valid, non tabu neighbour found: stop search
            stop();
        }
    }
}
