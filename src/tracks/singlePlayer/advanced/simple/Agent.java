package tracks.singlePlayer.advanced.simple;


import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.Random;

public class Agent extends AbstractPlayer
{
    int no_players; //number of players in the game
    public static double epsilon = 1e-6;
    public static Random m_rnd;

    Types.ACTIONS lastAction;
    int repeatsDone;
    final int maxRepeats;

    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
    {
        lastAction = Types.ACTIONS.ACTION_NIL;
        repeatsDone = 0;
        maxRepeats = 10;

        m_rnd = new Random();
        no_players = stateObs.getNoPlayers();
    }

    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
    {
        Types.ACTIONS action = lastAction;
        Types.ACTIONS oppAction = stateObs.getAvatarLastAction();

        long remaining = elapsedTimer.remainingTimeMillis();
        boolean panic = true;
        int repeatsLeft = maxRepeats - repeatsDone;

        int remainingLimit = 5;
        while(remaining > remainingLimit)
        {
            StateObservation stateCopy = stateObs.copy();
            Types.ACTIONS[] acts = new Types.ACTIONS[no_players];

            double stateImprovement = 0;

            for(int i=0;i<3;i++)
            {
                stateCopy.advance(action);
                if(!stateObs.getAvailableActions().contains(action)) break;

                stateImprovement = compare_states(stateObs, stateCopy);
                if(stateImprovement < 0)
                {
                    break;
                }
            }

            if(stateImprovement >= 0)
            {
                panic = false;
                ArrayList<Types.ACTIONS> avActions = stateObs.getAvailableActions();

                for(int j=0;j<avActions.size();j++)
                {
                    Types.ACTIONS tmpaction = avActions.get(j);
                    stateCopy = stateObs.copy();
                    for(int k=0;k<10;k++)
                    {
                        if(!stateObs.getAvailableActions().contains(avActions.get(j))) break;
                        stateCopy.advance(tmpaction);
                        if(compare_states(stateObs, stateCopy) < 0) break;
                    }
                    if(compare_states(stateObs, stateCopy) > stateImprovement)
                    {
                        action = tmpaction;
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

    Types.ACTIONS change_action(StateObservation stateObs)
    {
        repeatsDone = 0;
        ArrayList<Types.ACTIONS> a = stateObs.getAvailableActions();
        return Types.ACTIONS.values()[new Random().nextInt(a.size())];
    }

    double compare_states(StateObservation aState, StateObservation bState)
    {
        double aValue = state_value(aState);
        double bValue = state_value(bState);
        double diff = bValue - aValue;

        Vector2d posdiff = bState.getAvatarPosition().subtract( aState.getAvatarPosition() );
        posdiff.normalise();
        diff += posdiff.mag();

        return diff;
    }

    double state_value(StateObservation state)
    {
        boolean gameOver = state.isGameOver();

        Types.WINNER win = state.getGameWinner();
        double rawScore = state.getGameScore();

        if(gameOver && win == Types.WINNER.PLAYER_LOSES)
            rawScore -= 100000;

        if(gameOver && win == Types.WINNER.PLAYER_WINS)
            rawScore += 100000;

        return rawScore;
    }
}
