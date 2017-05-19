package tracks.multiPlayer.rokasv.simple;


import core.game.StateObservation;
import tools.Vector2d;
import tracks.multiPlayer.tools.heuristics.SimpleStateHeuristic;
import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;

import java.util.ArrayList;
import java.util.Random;

public class Agent extends AbstractMultiPlayer
{
    int oppID; //player ID of the opponent
    int id; //ID of this player
    int no_players; //number of players in the game
    public static double epsilon = 1e-6;
    public static Random m_rnd;

    Types.ACTIONS lastAction;
    int repeatsDone;
    final int maxRepeats;

    public Agent(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer, int playerID)
    {
        lastAction = Types.ACTIONS.ACTION_NIL;
        repeatsDone = 0;
        maxRepeats = 10;

        m_rnd = new Random();
        no_players = stateObs.getNoPlayers();
        id = playerID;
        oppID = (playerID + 1) % stateObs.getNoPlayers();
    }

    public Types.ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer)
    {
        Types.ACTIONS action = lastAction;
        Types.ACTIONS oppAction = stateObs.getAvatarLastAction(oppID);

        long remaining = elapsedTimer.remainingTimeMillis();
        boolean panic = true;
        int repeatsLeft = maxRepeats - repeatsDone;

        int remainingLimit = 5;
        while(remaining > remainingLimit)
        {
            StateObservationMulti stateCopy = stateObs.copy();
            Types.ACTIONS[] acts = new Types.ACTIONS[no_players];

            acts[id] = action;
            acts[oppID] = oppAction;

            double stateImprovement = 0;

            for(int i=0;i<3;i++)
            {
                stateCopy.advance(acts);
                if(!stateObs.getAvailableActions(id).contains(action)) break;

                stateImprovement = compare_states(stateObs, stateCopy);
                if(stateImprovement < 0)
                {
                    break;
                }
            }

            if(stateImprovement >= 0)
            {
                panic = false;
                ArrayList<Types.ACTIONS> avActions = stateObs.getAvailableActions(id);

                for(int j=0;j<avActions.size();j++)
                {
                    stateCopy = stateObs.copy();
                    for(int k=0;k<10;k++)
                    {
                        acts[id] = avActions.get(j);
                        if(!stateObs.getAvailableActions(id).contains(avActions.get(j))) break;
                        stateCopy.advance(acts);
                        if(compare_states(stateObs, stateCopy) < 0) break;
                    }
                    if(compare_states(stateObs, stateCopy) > stateImprovement)
                    {
                        action = avActions.get(j);
                        lastAction = action;
                    }
                }
                break;
            }
            else
            {
                action = change_action(stateObs);
                repeatsLeft = maxRepeats - repeatsDone;
            }

            remaining = elapsedTimer.remainingTimeMillis();
        }

        System.out.println(remaining);

        repeatsDone++;
        if(repeatsDone == maxRepeats)
        {
            lastAction = change_action(stateObs);
        }

        if(panic)
        {
            System.out.println("AAAAAaaaAA");
            action = change_action(stateObs);
            lastAction = action;
        }

        return action;
    }

    Types.ACTIONS change_action(StateObservationMulti stateObs)
    {
        repeatsDone = 0;
        ArrayList<Types.ACTIONS> a = stateObs.getAvailableActions(id);
        return Types.ACTIONS.values()[new Random().nextInt(a.size())];
    }

    double compare_states(StateObservationMulti aState, StateObservationMulti bState)
    {
        double aValue = state_value(aState);
        double bValue = state_value(bState);
        double diff = bValue - aValue;

        Vector2d posdiff = bState.getAvatarPosition(id).subtract( aState.getAvatarPosition(id) );
        posdiff.normalise();
        diff += posdiff.mag();

        return diff;
    }

    double state_value(StateObservationMulti state)
    {
        boolean gameOver = state.isGameOver();

        Types.WINNER win = state.getMultiGameWinner()[id];
        double rawScore = state.getGameScore(id);

        if(gameOver && win == Types.WINNER.PLAYER_LOSES)
            rawScore -= 100000;

        if(gameOver && win == Types.WINNER.PLAYER_WINS)
            rawScore += 100000;

        return rawScore;
    }
}
