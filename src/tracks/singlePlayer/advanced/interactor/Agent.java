package tracks.singlePlayer.advanced.interactor;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tracks.singlePlayer.advanced.interactor.heuristics.ExplorerHeuristic;
import tracks.singlePlayer.advanced.interactor.heuristics.HeuristicStubborn;
import tracks.singlePlayer.advanced.interactor.heuristics.InteractorHeuristic;
import tracks.singlePlayer.advanced.interactor.ucb.Bandit;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is an implementation of MCTS UCT
 */
public class Agent extends AbstractPlayer {

    public int NUM_ACTIONS;
    public Types.ACTIONS[] actions;

    protected SingleMCTSPlayer mctsPlayer;

    static InteractorHeuristic hInteract;
    static ExplorerHeuristic hExplorer;
    static HeuristicStubborn hStubborn;
    public static int heuristic;
    public int no_heuristics = 4;
    static final int HEURISTIC_DEFAULT = 0;
    static final int HEURISTIC_INTERACT = 1;
    static final int HEURISTIC_EXPLORER = 2;
    static final int HEURISTIC_STUBBORN = 3;
    static Bandit ucb;

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
        //Get the actions for all players in a static array.

        ArrayList<Types.ACTIONS> act = so.getAvailableActions();
        NUM_ACTIONS = act.size();
        actions = new Types.ACTIONS[NUM_ACTIONS];
        for (int j = 0; j < act.size(); ++j) {
            actions[j] = act.get(j);
        }

        //Create the player.

        mctsPlayer = getPlayer(so, elapsedTimer, NUM_ACTIONS, actions);

        hInteract = new InteractorHeuristic(so);
        hExplorer = new ExplorerHeuristic(so, getPlayerID());
        hStubborn = new HeuristicStubborn(getPlayerID());
//        ucb = new Bandit(no_heuristics);

        heuristic = HEURISTIC_INTERACT;
        heuristic = HEURISTIC_EXPLORER;
        heuristic = HEURISTIC_STUBBORN;
    }

    public SingleMCTSPlayer getPlayer(StateObservation so, ElapsedCpuTimer elapsedTimer, int NUM_ACTIONS, Types.ACTIONS[] actions) {
        return new SingleMCTSPlayer(new Random(), NUM_ACTIONS, actions);
    }


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        //Set the state observation object as the new root of the tree.
        mctsPlayer.init(stateObs);

//        ucb.pullArm();
//        heuristic = ucb.x;

        //Determine the action using MCTS...
//        System.out.println("Last Action: " + stateObs.getAvatarLastAction(id));
        int action = mctsPlayer.run(elapsedTimer);

//        System.out.println("Action: " + actions[id][action]);
        //... and return it.
        return actions[action];
    }

}
