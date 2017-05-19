package PrettyTeam.heuristics;

import core.game.StateObservationMulti;
import tools.Vector2d;

import java.util.ArrayList;

/**
 * Created by Carlotes(User) on 19/05/2017.
 * The explorer heuristic explores as much as of the level as possible,
 * or does the opposite and avoids movement
 */
public class ExplorerHeuristic extends StateHeuristicMulti {

    // Penalty and reward
    private static final double HUGE_NEGATIVE = -1000.0;
    private static final double HUGE_POSITIVE =  1000.0;

    // The actual positions that you played in the game
    private ArrayList<Vector2d> positions;
    // Positions of one particular iteration of MCTS
    private ArrayList<Vector2d> futurePositions;
    //Penalty you increase for futurePositions
    private float futurePenalty;
    // The coefficient of the penalty to add
    private float coeficientPenalty;
    // Players 0 or 1
    private int playerID;
    private int playerType;

    // Last tick for which you have all the previous events recorded
    private int lastGameTick;

    // If the last strategy was explore
    boolean lastStrategyExplore;

    /**
     * Constructor of the class, default values
     * @param stateObs
     * @param playerID
     */
    public ExplorerHeuristic(StateObservationMulti stateObs, int playerID) {

        // Initialize all variables
        positions = new ArrayList<>();
        futurePositions = new ArrayList<>();
        futurePositions.addAll(positions);
        this.playerID = playerID;
        lastGameTick = 0;
        if (stateObs != null) {
            updatePositions(stateObs, positions);
            playerType = stateObs.getAvatarType(playerID);
        }
    }

    /**
     * Updates the state of the events that happen in the game
     * @param stateObs the actual state of the game (not simulated)
     */
    public void update(StateObservationMulti stateObs) {
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

    // Setter for last game tick
    public void setLastGameTick(int tick) {
        lastGameTick = tick;
    }

    /**
     * Evaluates if the state recorded is benefitial or not
     * @param stateObs
     * @return
     */
    @Override
    public double evaluateState(StateObservationMulti stateObs) {
        // We update future positions to evaluate it
        //updatePositions(stateObs, futurePositions);

        float rewardPoints = 100;

        double rewardScore;

        //System.out.println(positions);
        // If the position we are going is already visited...
        if (  positions.contains(stateObs.getAvatarPosition(playerID)) ) {
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

        return rewardScore;
    }

    /**
     * Updates the list of positions with the position from the last tick
     * @param stateObs
     */
    private void updatePositions(StateObservationMulti stateObs, ArrayList<Vector2d> positions) {
        if (stateObs != null) {
            if(positions.add(stateObs.getAvatarPosition(playerID))){}
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
