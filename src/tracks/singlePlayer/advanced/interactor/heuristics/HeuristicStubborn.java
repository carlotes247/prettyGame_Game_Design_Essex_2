package tracks.singlePlayer.advanced.interactor.heuristics;

import core.game.StateObservation;
import ontology.Types;
import tracks.multiPlayer.advanced.prettycontroller.heuristics.StateHeuristicMulti;

public class HeuristicStubborn extends StateHeuristic
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

    public double evaluateState(StateObservation stateObs)
    {
//        System.out.println("Value: " + intrinsicScore);
        return intrinsicScore;
    }

    public void update(StateObservation stateObs, Types.ACTIONS action, float depth)
    {
//        System.out.print(acts[playerID] + ",");
        int sameMove = (action == lastAction) ? 1 : 0;
        int sameMoveBonus = 1;
        int diffMoveBonus = -1;

        sameMoveBonus *= sameMove;
        diffMoveBonus *= (1-sameMove);

        intrinsicScore += (sameMoveBonus + diffMoveBonus)*multiplier;

        if(sameMove == 0) multiplier = 0;
    }
}


