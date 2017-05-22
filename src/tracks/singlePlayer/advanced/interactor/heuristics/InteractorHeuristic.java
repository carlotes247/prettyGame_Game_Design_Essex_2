package tracks.singlePlayer.advanced.interactor.heuristics;

import core.game.Event;
import core.game.Observation;
import core.game.StateObservation;
import tools.Utils;
import tools.Vector2d;
import tools.pathfinder.PathFinder;

import java.util.*;

import static tracks.singlePlayer.advanced.interactor.SingleTreeNode.ROLLOUT_DEPTH;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 11/02/14
 * Time: 15:44
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class InteractorHeuristic extends StateHeuristic {

    private ArrayList<Integer> objects;
    private ArrayList<Integer> newObjects;

    public InteractorHeuristic(StateObservation stateObs) {
        objects = new ArrayList<>();
        newObjects = new ArrayList<>();
        newObjects.addAll(objects);
        if (stateObs != null) {
            updateEvents(stateObs, objects);
        }
    }

    public void update(StateObservation stateObs) {
        updateEvents(stateObs, objects);
    }

    public void reset() {
        newObjects = new ArrayList<>();
        newObjects.addAll(objects);
    }

    public double evaluateState(StateObservation stateObs) {
        updateEvents(stateObs, newObjects);

        double rawScore = newObjects.size(); //Interactor
        //rawScore -= newObjects.size(); //Avoider

        // If no new sprites have been interacted with, subtract distance to closest new sprite
        // to nudge agent to move towards sprites it has not interacted with yet
        if (newObjects.size() == objects.size()) {
            rawScore += findNewEvent(stateObs);
        }

//        System.out.println(newObjects);

        return rawScore;
    }

    private void updateEvents(StateObservation stateObs, ArrayList<Integer> objects) {
        if (stateObs != null) {
            for (Event e : stateObs.getEventsHistory()) {
                if (!objects.contains(e.activeSpriteId)) {
                    objects.add(e.activeSpriteId);
                }
                if (!objects.contains(e.passiveSpriteId)) {
                    objects.add(e.passiveSpriteId);
                }
            }
        }
    }

    private double findNewEvent(StateObservation stateObs) {
        Vector2d position = stateObs.getAvatarPosition();

        ArrayList<Observation>[] immovablePositions = stateObs.getImmovablePositions(position);
        ArrayList<Observation>[] movablePositions = stateObs.getMovablePositions(position);
        ArrayList<Observation>[] npcPositions = stateObs.getNPCPositions(position);
        ArrayList<Observation>[] portalPositions = stateObs.getPortalsPositions(position);
        ArrayList<Observation>[] resourcesPositions = stateObs.getResourcesPositions(position);

        ArrayList<Observation> allObjects = new ArrayList<>();

        if (movablePositions != null)
            for (ArrayList<Observation> ar : movablePositions)
                allObjects.addAll(ar);

        if (portalPositions != null)
            for (ArrayList<Observation> ar : portalPositions)
                allObjects.addAll(ar);

        if (npcPositions != null)
            for (ArrayList<Observation> ar : npcPositions)
                allObjects.addAll(ar);

        if (resourcesPositions != null)
            for (ArrayList<Observation> ar : resourcesPositions)
                allObjects.addAll(ar);

        if (immovablePositions != null)
            for (ArrayList<Observation> ar : immovablePositions)
                allObjects.addAll(ar);

        Collections.sort(allObjects);

        for (Observation o : allObjects) {
            if (!newObjects.contains(o.itype)) {
                double normalDist = Utils.normalise(o.sqDist / stateObs.getBlockSize(), 0, 5000);
                return 1 - normalDist;
            }
        }

        return 0;
    }
}