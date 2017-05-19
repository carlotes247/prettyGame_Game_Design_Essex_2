package tracks.multiPlayer.advanced.prettycontroller.heuristics;

import core.game.Event;
import core.game.StateObservationMulti;
import ontology.Types;

import java.util.TreeSet;

public class HeuristicStubborn extends StateHeuristicMulti
{
    double intrinsicScore;
    public int playerID;
    Types.ACTIONS lastAction;
    int multiplier;

    public HeuristicStubborn(int pid)
    {
        playerID = pid;
        reset(Types.ACTIONS.ACTION_NIL);
    }

    public void reset(Types.ACTIONS action)
    {
        intrinsicScore = 0;
        lastAction = action;
        multiplier = 1;
    };

    public double evaluateState(StateObservationMulti stateObs)
    {
//        System.out.println("Value: " + intrinsicScore);
        return intrinsicScore;
    }

    public void update(StateObservationMulti stateObs, Types.ACTIONS[] acts, float depth)
    {
//        System.out.print(acts[playerID] + ",");
        int sameMove = (acts[playerID] == lastAction) ? 1 : 0;
        int sameMoveBonus = 1;
        int diffMoveBonus = -1;

        sameMoveBonus *= sameMove;
        diffMoveBonus *= (1-sameMove);

        intrinsicScore += (sameMoveBonus + diffMoveBonus)*multiplier;

        if(sameMove == 0) multiplier = 0;
    }
}


