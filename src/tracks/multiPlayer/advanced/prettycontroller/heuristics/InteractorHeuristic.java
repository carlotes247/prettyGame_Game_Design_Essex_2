package tracks.multiPlayer.advanced.prettycontroller.heuristics;

import core.game.Event;
import core.game.StateObservationMulti;
import ontology.Types;

import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 11/02/14
 * Time: 15:44
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class InteractorHeuristic extends StateHeuristicMulti {

    private static final double HUGE_NEGATIVE = -1000.0;
    private static final double HUGE_POSITIVE =  1000.0;

    private TreeSet<Integer> objects;
    private TreeSet<Integer> newObjects;
    private int playerID;
    private int playerType;

    private int lastGameTick;

    public InteractorHeuristic(StateObservationMulti stateObs, int playerID) {
        objects = new TreeSet<>();
        newObjects = new TreeSet<>();
        newObjects.addAll(objects);
        this.playerID = playerID;
        lastGameTick = 0;
        if (stateObs != null) {
            updateEvents(stateObs, objects);
            playerType = stateObs.getAvatarType(playerID);
        }
    }

    public void update(StateObservationMulti stateObs) {
        updateEvents(stateObs, objects);
    }

    public void reset() {
        newObjects = new TreeSet<>();
        newObjects.addAll(objects);
    }

    public void setLastGameTick(int tick) {
        lastGameTick = tick;
    }

    public double evaluateState(StateObservationMulti stateObs) {
        updateEvents(stateObs, newObjects);

        boolean gameOver = stateObs.isGameOver();
        Types.WINNER win = stateObs.getMultiGameWinner()[playerID];
        Types.WINNER oppWin = stateObs.getMultiGameWinner()[(playerID + 1) % stateObs.getNoPlayers()];
        double rawScore = stateObs.getGameScore(playerID);

        if(gameOver && (win == Types.WINNER.PLAYER_LOSES || oppWin == Types.WINNER.PLAYER_WINS))
            return HUGE_NEGATIVE;

        if(gameOver && (win == Types.WINNER.PLAYER_WINS || oppWin == Types.WINNER.PLAYER_LOSES))
            return HUGE_POSITIVE;

        rawScore += newObjects.size(); //Interactor
        //rawScore -= newObjects.size(); //Avoider

        return rawScore;
    }

    private void updateEvents(StateObservationMulti stateObs, TreeSet<Integer> objects) {
        if (stateObs != null) {
            for (Event e : stateObs.getEventsHistory()) {
                if (e.gameStep >= lastGameTick) {
                    objects.add(e.activeSpriteId);
                    objects.add(e.passiveSpriteId);
                }
            }
        }
    }
}


