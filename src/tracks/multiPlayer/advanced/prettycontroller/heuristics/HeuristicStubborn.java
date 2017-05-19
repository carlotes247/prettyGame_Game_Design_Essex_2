package tracks.multiPlayer.advanced.prettycontroller.heuristics;

import core.game.Event;
import core.game.StateObservationMulti;
import ontology.Types;

import java.util.TreeSet;

public class HeuristicStubborn extends StateHeuristicMulti
{
    double intrinsicScore;
    public int playerID;

    public HeuristicStubborn(int pid)
    {
        playerID = pid;
        reset();
    }

    public void reset()
    {
        intrinsicScore = 0;
    };

    public double evaluateState(StateObservationMulti stateObs)
    {
        return intrinsicScore;
    }

    public void update(StateObservationMulti stateObs, Types.ACTIONS[] acts, float depth)
    {
        int sameMove = (acts[playerID] == stateObs.getAvatarLastAction(playerID)) ? 1 : 0;
        int sameMoveBonus = 100;
        int diffMoveBonus = -100;
        int bonus = 0;

        sameMoveBonus *= sameMove;
        diffMoveBonus *= (1-sameMove);

        double depthMulti = (2*depth+1.0f);

        intrinsicScore += sameMoveBonus/depthMulti;
        intrinsicScore += diffMoveBonus*depthMulti;
    }
}


