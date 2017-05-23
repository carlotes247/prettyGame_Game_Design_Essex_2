package tracks.singlePlayer.advanced.interactor.heuristics;

import core.game.StateObservation;
import ontology.Types;
import tools.Utils;
import tracks.multiPlayer.advanced.prettycontroller.heuristics.StateHeuristicMulti;

public class HeuristicStubborn extends StateHeuristic
{
    double intrinsicScore;
    public int playerID;
    Types.ACTIONS lastAction;
    int multiplier;
    double[] bounds = new double[]{Double.MAX_VALUE, -Double.MAX_VALUE};


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

        double normDelta = Utils.normalise(intrinsicScore, bounds[0], bounds[1]);

        if(intrinsicScore < bounds[0])
            bounds[0] = intrinsicScore;
        if(intrinsicScore > bounds[1])
            bounds[1] = intrinsicScore;

        return normDelta;
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


