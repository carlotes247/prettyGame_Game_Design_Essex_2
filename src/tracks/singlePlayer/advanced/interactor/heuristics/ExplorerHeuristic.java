package tracks.singlePlayer.advanced.interactor.heuristics;

import core.game.StateObservation;
import tools.Utils;
import tools.Vector2d;
import tracks.multiPlayer.advanced.prettycontroller.heuristics.StateHeuristicMulti;

import java.util.ArrayList;

/**
 * Created by Carlotes(User) on 19/05/2017.
 * The explorer heuristic explores as much as of the level as possible,
 * or does the opposite and avoids movement
 */
public class ExplorerHeuristic extends StateHeuristic {

    // The actual positions that you played in the game
    private ArrayList<Vector2d> positions;
    // Positions of one particular iteration of MCTS
    private ArrayList<Vector2d> futurePositions;
    //Penalty you increase for futurePositions
    private float futurePenalty;
    // The coefficient of the penalty to add
    private float coeficientPenalty;

    // If the last strategy was explore
    boolean lastStrategyExplore;

    double[] bounds = new double[]{Double.MAX_VALUE, -Double.MAX_VALUE};

    /**
     * Constructor of the class, default values
     * @param stateObs
     * @param playerID
     */
    public ExplorerHeuristic(StateObservation stateObs, int playerID) {

        // Initialize all variables
        positions = new ArrayList<>();
        futurePositions = new ArrayList<>();
        futurePositions.addAll(positions);
        if (stateObs != null) {
            updatePositions(stateObs, positions);
        }
    }

    /**
     * Updates the state of the events that happen in the game
     * @param stateObs the actual state of the game (not simulated)
     */
    public void update(StateObservation stateObs) {
        updatePositions(stateObs, positions);
//        System.out.println("FUTUREPOS: " + futurePositions.size() + ", PENAL: " + futurePenalty + ", COEF: " + coeficientPenalty);
    }

    // Clear future positions
    public void reset() {
        futurePositions = new ArrayList<>();
        futurePenalty = 0f;
        coeficientPenalty = 50f;
        //futurePositions.addAll(positions);
    }

    /**
     * Evaluates if the state recorded is benefitial or not
     * @param stateObs
     * @return
     */
    @Override
    public double evaluateState(StateObservation stateObs) {
        // We update future positions to evaluate it
        //updatePositions(stateObs, futurePositions);

        float rewardPoints = 100;

        double rewardScore;

        //System.out.println(positions);
        // If the position we are going is already visited...
        if (  positions.contains(stateObs.getAvatarPosition()) ) {
            // We remove points
            rewardScore = -rewardPoints; //Lazy
            // Debug
            if (lastStrategyExplore) {
//                System.out.println("LAZY");
                lastStrategyExplore = false;
            }
        }
        // If the position is not visited...
        else {
            // We add points
            rewardScore = rewardPoints; //Explorer
            // Debug
            if (!lastStrategyExplore) {
//                System.out.println("EXPLORE");
                lastStrategyExplore = true;
            }
        }

        double normDelta = Utils.normalise(rewardScore, bounds[0], bounds[1]);

        if(rewardScore < bounds[0])
            bounds[0] = rewardScore;
        if(rewardScore > bounds[1])
            bounds[1] = rewardScore;

        return normDelta;
    }

    /**
     * Updates the list of positions with the position from the last tick
     * @param stateObs
     */
    private void updatePositions(StateObservation stateObs, ArrayList<Vector2d> positions) {
        if (stateObs != null) {
            if(positions.add(stateObs.getAvatarPosition())){}
//                System.out.println(stateObs.getAvatarPosition(playerID));
        }
    }

    /**
     * add a new position to the future pos array
     * @param avatarPosition
     */
    public void addFuturePosition(Vector2d avatarPosition) {
        futurePositions.add(avatarPosition);
    }

    /**
     * add a new position to the future pos array and accumulates a penalty
     * @param avatarPosition
     */
    public void addFuturePosition(Vector2d avatarPosition, float depthSearchTree) {
        futurePositions.add(avatarPosition);
        if (positions.contains(avatarPosition)) {
            // we add a penalty because is a position we have already visited
            futurePenalty += (coeficientPenalty);
            // add a decay to the penalty based on depth
            coeficientPenalty -= coeficientPenalty*0.5;
        }

    }

    /**
     * Compare if one vector2 is inside a hashSet
     * @param positions
     * @param posToCompare
     * @return
     */
    private boolean isPosInsideHashSet (ArrayList<Vector2d> positions, Vector2d posToCompare ) {

        for (int i = 0; i < positions.size(); i++) {
            if (positions.get(i) == posToCompare) {
                return true;
            } else {
                return false;
            }
        }

        return false;
    }
}
