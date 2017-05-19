package tracks.multiPlayer.advanced.prettycontroller;

import java.util.Random;

import core.game.StateObservationMulti;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;

import static tracks.multiPlayer.advanced.prettycontroller.Agent.*;

public class SingleTreeNode
{
    private final double HUGE_NEGATIVE = -10000000.0;
    private final double HUGE_POSITIVE =  10000000.0;
    public double epsilon = 1e-6;
    public double egreedyEpsilon = 0.05;
    public SingleTreeNode parent;
    public SingleTreeNode[] children;
    public double totValue;
    public int nVisits;
    public Random m_rnd;
    public int m_depth;
    protected static double[] bounds = new double[]{Double.MAX_VALUE, -Double.MAX_VALUE};
    public int childIdx;

    public int MCTS_ITERATIONS = 100;
    public int ROLLOUT_DEPTH = 10;
    public double K = Math.sqrt(2);
    public double REWARD_DISCOUNT = 1.00;
    public int[] NUM_ACTIONS;
    public Types.ACTIONS[][] actions;
    public int id, oppID, no_players;

    public StateObservationMulti rootState;

    public SingleTreeNode(Random rnd, int[] NUM_ACTIONS, Types.ACTIONS[][] actions, int id, int oppID, int no_players) {
        this(null, -1, rnd, id, oppID, no_players, NUM_ACTIONS, actions);
    }

    public SingleTreeNode(SingleTreeNode parent, int childIdx, Random rnd, int id, int oppID, int no_players, int[] NUM_ACTIONS, Types.ACTIONS[][] actions) {
        this.id = id;
        this.oppID = oppID;
        this.no_players = no_players;
        this.parent = parent;
        this.m_rnd = rnd;
        totValue = 0.0;
        this.childIdx = childIdx;
        if(parent != null)
            m_depth = parent.m_depth+1;
        else
            m_depth = 0;
        this.NUM_ACTIONS = NUM_ACTIONS;
        children = new SingleTreeNode[NUM_ACTIONS[id]];
        this.actions = actions;

    }


    public void mctsSearch(ElapsedCpuTimer elapsedTimer) {

        double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis();
        int numIters = 0;

        hInteract.setLastGameTick(rootState.getGameTick() - 1);
        hInteract.update(rootState);
        hExplore.setLastGameTick(rootState.getGameTick() - 1);
        hExplore.update(rootState);

        int remainingLimit = 5;
        while(remaining > 2*avgTimeTaken && remaining > remainingLimit){
            //while(numIters < Agent.MCTS_ITERATIONS){

            StateObservationMulti state = rootState.copy();

            switch (heuristic)
            {
                case HEURISTIC_INTERACT:
                {
                    hInteract.reset();
                    break;
                }
                case HEURISTIC_STUBBORN:
                {
                    heuristicStubborn.reset();
                    break;
                }
                case HEURISTIC_EXPLORE:
                {
                    hExplore.reset();
                    break;
                }
                default: break;
            }

            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
            SingleTreeNode selected = treePolicy(state);
            double delta = selected.rollOut(state);
            backUp(selected, delta);

            numIters++;
            acumTimeTaken += (elapsedTimerIteration.elapsedMillis()) ;
            //System.out.println(elapsedTimerIteration.elapsedMillis() + " --> " + acumTimeTaken + " (" + remaining + ")");
            avgTimeTaken  = acumTimeTaken/numIters;
            remaining = elapsedTimer.remainingTimeMillis();
        }


        //System.out.println("-- " + numIters + " -- ( " + avgTimeTaken + ")");
    }

    public SingleTreeNode treePolicy(StateObservationMulti state) {

        SingleTreeNode cur = this;

        while (!state.isGameOver() && cur.m_depth < ROLLOUT_DEPTH)
        {
            if (cur.notFullyExpanded()) {
                return cur.expand(state);

            } else {
                SingleTreeNode next = cur.uct(state);
                cur = next;
            }
        }

        return cur;
    }


    public SingleTreeNode expand(StateObservationMulti state) {

        int bestAction = 0;
        double bestValue = -1;

        for (int i = 0; i < children.length; i++) {
            double x = m_rnd.nextDouble();
            if (x > bestValue && children[i] == null) {
                bestAction = i;
                bestValue = x;
            }
        }

        //Roll the state

        //need to provide actions for all players to advance the forward model
        Types.ACTIONS[] acts = new Types.ACTIONS[no_players];

        //set this agent's action
        acts[id] = actions[id][bestAction];

        //get actions available to the opponent and assume they will do a random action
        Types.ACTIONS[] oppActions = actions[oppID];
        acts[oppID] = oppActions[new Random().nextInt(oppActions.length)];

        advance_state(state, acts);

        // to do add future positions
        // Add the position in our new array after this node has been explored (exploration of the future)
        hExplore.addFuturePosition(state.getAvatarPosition(id), m_depth);

        SingleTreeNode tn = new SingleTreeNode(this,bestAction,this.m_rnd, id, oppID, no_players, NUM_ACTIONS, actions);
        children[bestAction] = tn;
        return tn;
    }

    public SingleTreeNode uct(StateObservationMulti state) {

        SingleTreeNode selected = null;
        double bestValue = -Double.MAX_VALUE;
        for (SingleTreeNode child : this.children)
        {
            double hvVal = child.totValue;
            double childValue =  hvVal / (child.nVisits + this.epsilon);

            childValue = Utils.normalise(childValue, bounds[0], bounds[1]);
            //System.out.println("norm child value: " + childValue);

            double uctValue = childValue +
                    K * Math.sqrt(Math.log(this.nVisits + 1) / (child.nVisits + this.epsilon));

            uctValue = Utils.noise(uctValue, this.epsilon, this.m_rnd.nextDouble());     //break ties randomly

            // small sampleRandom numbers: break ties in unexpanded nodes
            if (uctValue > bestValue) {
                selected = child;
                bestValue = uctValue;
            }
        }
        if (selected == null)
        {
            throw new RuntimeException("Warning! returning null: " + bestValue + " : " + this.children.length + " " +
                    + bounds[0] + " " + bounds[1]);
        }

        //Roll the state:

        //need to provide actions for all players to advance the forward model
        Types.ACTIONS[] acts = new Types.ACTIONS[no_players];

        //set this agent's action
        acts[id] = actions[id][selected.childIdx];

        //get actions available to the opponent and assume they will do a random action
        Types.ACTIONS[] oppActions = actions[oppID];
        acts[oppID] = oppActions[new Random().nextInt(oppActions.length)];

        advance_state(state, acts);

        // Add the position in our new array after this node has been explored (exploration of the future)
        hExplore.addFuturePosition(state.getAvatarPosition(id), m_depth);

        return selected;
    }

    void advance_state(StateObservationMulti state, Types.ACTIONS[] acts)
    {
        heuristicStubborn.update(state, acts, m_depth);
        state.advance(acts);
    }

    public double rollOut(StateObservationMulti state)
    {
        int thisDepth = this.m_depth;

        while (!finishRollout(state,thisDepth)) {

            //random move for all players
            Types.ACTIONS[] acts = new Types.ACTIONS[no_players];
            for (int i = 0; i < no_players; i++) {
                acts[i] = actions[i][m_rnd.nextInt(NUM_ACTIONS[i])];
            }

            advance_state(state, acts);
            thisDepth++;

            // Add the position in our new array after this node has been explored (exploration of the future)
            hExplore.addFuturePosition(state.getAvatarPosition(id), m_depth);
        }


        double delta = value(state);

//        System.out.println(delta);

        return delta;
    }

    public double value(StateObservationMulti a_gameState) {

        double value = 0;

        switch (heuristic)
        {
            case HEURISTIC_INTERACT:
            {
                value = hInteract.evaluateState(a_gameState);
                break;
            }
            case HEURISTIC_STUBBORN:
                value += heuristicStubborn.evaluateState(a_gameState);
                break;
                //fall-through
            case HEURISTIC_EXPLORE:
            {
                value = hExplore.evaluateState(a_gameState);
                break;
            }
            default: break;
        }


        boolean gameOver = a_gameState.isGameOver();
        Types.WINNER win = a_gameState.getMultiGameWinner()[id];
        Types.WINNER oppWin = a_gameState.getMultiGameWinner()[(id + 1) % a_gameState.getNoPlayers()];;
        value += a_gameState.getGameScore(id);

        double normDelta = Utils.normalise(value, bounds[0], bounds[1]);

        if(value < bounds[0])
            bounds[0] = value;
        if(value > bounds[1])
            bounds[1] = value;

        if(gameOver && (win == Types.WINNER.PLAYER_LOSES || oppWin == Types.WINNER.PLAYER_WINS))
            normDelta += HUGE_NEGATIVE;

        if(gameOver && (win == Types.WINNER.PLAYER_WINS || oppWin == Types.WINNER.PLAYER_LOSES))
            normDelta += HUGE_POSITIVE;

        return normDelta;
    }

    public boolean finishRollout(StateObservationMulti rollerState, int depth)
    {
        if(depth >= ROLLOUT_DEPTH)      //rollout end condition.
            return true;

        if(rollerState.isGameOver())               //end of game
            return true;

        return false;
    }

    public void backUp(SingleTreeNode node, double result)
    {
        SingleTreeNode n = node;
        while(n != null)
        {
            n.nVisits++;
            n.totValue += result;
            if (result < bounds[0]) {
                bounds[0] = result;
            }
            if (result > bounds[1]) {
                bounds[1] = result;
            }
            n = n.parent;
        }
    }

    public double bestReward;

    public int mostVisitedAction() {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;
        boolean allEqual = true;
        double first = -1;

        for (int i=0; i<children.length; i++) {

            if(children[i] != null)
            {
                if(first == -1)
                    first = children[i].nVisits;
                else if(first != children[i].nVisits)
                {
                    allEqual = false;
                }

                double childValue = children[i].nVisits;
                childValue = Utils.noise(childValue, this.epsilon, this.m_rnd.nextDouble());     //break ties randomly
                if (childValue > bestValue) {
                    bestValue = childValue;
                    selected = i;
                }
            }
        }

        if (selected == -1)
        {
            System.out.println("Unexpected selection!");
            selected = 0;
        }else if(allEqual)
        {
            //If all are equal, we opt to choose for the one with the best Q.
            selected = bestAction();
        }

        bestReward = children[selected].totValue / children[selected].nVisits;
        bestReward = Utils.normalise(bestReward, bounds[0], bounds[1]);

        return selected;
    }

    public int bestAction()
    {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;

        for (int i=0; i<children.length; i++) {

            if(children[i] != null) {
                //double tieBreaker = m_rnd.nextDouble() * epsilon;
                double childValue = children[i].totValue / (children[i].nVisits + this.epsilon);
                childValue = Utils.noise(childValue, this.epsilon, this.m_rnd.nextDouble());     //break ties randomly
                if (childValue > bestValue) {
                    bestValue = childValue;
                    selected = i;
                }
            }
        }

        if (selected == -1)
        {
            System.out.println("Unexpected selection!");
            selected = 0;
        }

        return selected;
    }


    public boolean notFullyExpanded() {
        for (SingleTreeNode tn : children) {
            if (tn == null) {
                return true;
            }
        }

        return false;
    }
}
