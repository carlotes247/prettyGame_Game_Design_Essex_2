package tracks.singlePlayer.advanced.interactor;

import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;

import java.util.Random;

import static tracks.singlePlayer.advanced.interactor.Agent.*;

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
    public static int ROLLOUT_DEPTH = 10;
    public double K = Math.sqrt(2);
    public double REWARD_DISCOUNT = 1.00;
    public int NUM_ACTIONS;
    public Types.ACTIONS[] actions;

    public StateObservation rootState;

    public SingleTreeNode(Random rnd, int NUM_ACTIONS, Types.ACTIONS[] actions) {
        this(null, -1, rnd, NUM_ACTIONS, actions);
    }

    public SingleTreeNode(SingleTreeNode parent, int childIdx, Random rnd, int NUM_ACTIONS, Types.ACTIONS[] actions) {
        this.parent = parent;
        this.m_rnd = rnd;
        totValue = 0.0;
        this.childIdx = childIdx;
        if(parent != null)
            m_depth = parent.m_depth+1;
        else
            m_depth = 0;
        this.NUM_ACTIONS = NUM_ACTIONS;
        children = new SingleTreeNode[NUM_ACTIONS];
        this.actions = actions;

    }


    public void mctsSearch(ElapsedCpuTimer elapsedTimer) {

        double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis();
        int numIters = 0;

        if (heuristic == HEURISTIC_INTERACT)
            hInteract.update(rootState);
        else if (heuristic == HEURISTIC_EXPLORER)
            hExplorer.update(rootState);

        int remainingLimit = 5;
        while(remaining > 2*avgTimeTaken && remaining > remainingLimit){
            //while(numIters < Agent.MCTS_ITERATIONS){

            StateObservation state = rootState.copy();

            if (heuristic == HEURISTIC_INTERACT)
                hInteract.reset();
            else if (heuristic == HEURISTIC_EXPLORER)
                hExplorer.reset();
            else if (heuristic == HEURISTIC_STUBBORN)
                hStubborn.reset(state.getAvatarLastAction());

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

    public SingleTreeNode treePolicy(StateObservation state) {

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


    public SingleTreeNode expand(StateObservation state) {

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
        advance_state(state, actions[bestAction]);


        SingleTreeNode tn = new SingleTreeNode(this,bestAction,this.m_rnd, NUM_ACTIONS, actions);
        children[bestAction] = tn;
        return tn;
    }

    public SingleTreeNode uct(StateObservation state) {

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
        advance_state(state, actions[selected.childIdx]);;

        return selected;
    }

    void advance_state(StateObservation state, Types.ACTIONS act)
    {
        if (heuristic == HEURISTIC_STUBBORN)
            hStubborn.update(state, act, m_depth);
        state.advance(act);
    }

    public double rollOut(StateObservation state)
    {
        int thisDepth = this.m_depth;

        while (!finishRollout(state,thisDepth)) {

            //random move
            advance_state(state, actions[m_rnd.nextInt(NUM_ACTIONS)]);
            thisDepth++;
        }


        double delta = value(state);

//        System.out.println(delta);

        return delta;
    }

    public double value(StateObservation a_gameState) {

        double intrinsicValue = 0;
        double value = 0;

        if (heuristic == HEURISTIC_INTERACT)
            intrinsicValue = hInteract.evaluateState(a_gameState);
        else if(heuristic == HEURISTIC_EXPLORER)
            intrinsicValue = hExplorer.evaluateState(a_gameState);
        else if(heuristic == HEURISTIC_STUBBORN)
            intrinsicValue = hStubborn.evaluateState(a_gameState);

        boolean gameOver = a_gameState.isGameOver();
        Types.WINNER win = a_gameState.getGameWinner();
        double extrinsicValue = a_gameState.getGameScore();

        double normExtrinsic = Utils.normalise(extrinsicValue, bounds[0], bounds[1]);

        if(extrinsicValue < bounds[0])
            bounds[0] = extrinsicValue;
        if(extrinsicValue > bounds[1])
            bounds[1] = extrinsicValue;

        double weight = 0.8;
        value = weight * normExtrinsic + intrinsicValue * (1 - weight);

        if(gameOver && (win == Types.WINNER.PLAYER_LOSES))
            value += HUGE_NEGATIVE;

        if(gameOver && (win == Types.WINNER.PLAYER_WINS))
            value += HUGE_POSITIVE;

        return value;
    }

    public boolean finishRollout(StateObservation rollerState, int depth)
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
