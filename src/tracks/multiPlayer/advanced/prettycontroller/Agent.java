package tracks.multiPlayer.advanced.prettycontroller;

import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tracks.multiPlayer.advanced.prettycontroller.heuristics.ExplorerHeuristic;
import tracks.multiPlayer.advanced.prettycontroller.heuristics.HeuristicStubborn;
import tracks.multiPlayer.advanced.prettycontroller.heuristics.InteractorHeuristic;
import tracks.multiPlayer.advanced.prettycontroller.ucb.Bandit;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is an implementation of MCTS UCT
 */
public class Agent extends AbstractMultiPlayer {

    public int[] NUM_ACTIONS;
    public Types.ACTIONS[][] actions;
    public int id, oppID, no_players;

    protected SingleMCTSPlayer mctsPlayer;

    static InteractorHeuristic hInteract;
    static ExplorerHeuristic hExplore;
    static HeuristicStubborn heuristicStubborn;
    public static int heuristic;
    public int no_heuristics = 5;
    static final int HEURISTIC_DEFAULT = 0;
    static final int HEURISTIC_INTERACT = 1;
    static final int HEURISTIC_STUBBORN = 2;
    static final int HEURISTIC_EXPLORE = 3;
    static Bandit ucb;

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservationMulti so, ElapsedCpuTimer elapsedTimer, int playerID)
    {
        //get game information

        no_players = so.getNoPlayers();
        id = playerID;
        oppID = (id + 1) % so.getNoPlayers();

        //Get the actions for all players in a static array.

        NUM_ACTIONS = new int[no_players];
        actions = new Types.ACTIONS[no_players][];
        for (int i = 0; i < no_players; i++) {

            ArrayList<Types.ACTIONS> act = so.getAvailableActions(i);

            actions[i] = new Types.ACTIONS[act.size()];
            for (int j = 0; j < act.size(); ++j) {
                actions[i][j] = act.get(j);
            }
            NUM_ACTIONS[i] = actions[i].length;
        }

        //Create the player.

        mctsPlayer = getPlayer(so, elapsedTimer, NUM_ACTIONS, actions, id, oppID, no_players);

        hInteract = new InteractorHeuristic(so, playerID);

        hExplore = new ExplorerHeuristic(so, playerID);

        heuristicStubborn = new HeuristicStubborn(playerID);
        ucb = new Bandit(no_heuristics);

        heuristic = HEURISTIC_STUBBORN;
    }

    public SingleMCTSPlayer getPlayer(StateObservationMulti so, ElapsedCpuTimer elapsedTimer, int[] NUM_ACTIONS, Types.ACTIONS[][] actions, int id, int oppID, int no_players) {
        return new SingleMCTSPlayer(new Random(), NUM_ACTIONS, actions, id, oppID, no_players);
    }


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer) {

        //Set the state observation object as the new root of the tree.
        mctsPlayer.init(stateObs);

//        ucb.pullArm();
//        heuristic = ucb.x;

        //Determine the action using MCTS...
//        System.out.println("Last Action: " + stateObs.getAvatarLastAction(id));
        int action = mctsPlayer.run(elapsedTimer);

//        System.out.println("Action: " + actions[id][action]);
        //... and return it.
        return actions[id][action];
    }

}
