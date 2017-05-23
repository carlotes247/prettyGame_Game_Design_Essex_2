package RHEA.utils;

import static RHEA.utils.Constants.*;

/**
 * Created by rdgain on 3/20/2017.
 */
public class ParameterSet {
    // variable
    public int POPULATION_SIZE = 1; //try 1,2,5
    public int SIMULATION_DEPTH = 6; //try 6,8,10
    public int INIT_TYPE = INIT_RANDOM;
    public int BUDGET_TYPE = HALF_BUDGET;
    public int MAX_FM_CALLS = 900;
    public int MAX_ITERS = 2000;
    public int HEURISTIC_TYPE = HEURISTIC_WINSCORE;
    public int MACRO_ACTION_LENGTH = 1; //LENGTH OF EACH MACRO-ACTION

    public boolean BANDIT_MUTATION = false; //if false - random; if true - bandit
    public int CROSSOVER_TYPE = UNIFORM_CROSS; // 0 - 1point; 1 - uniform

    public boolean TREE = false;
    public boolean CHOOSE_TREE = false;
    public boolean SHIFT_BUFFER = false;

    public boolean ROLLOUTS = true;
    public int ROLLOUT_LENGTH = 0;
    public int REPEAT_ROLLOUT = 1; //try 1,2,5

    // set
    public boolean REEVALUATE = false;
    public int MUTATION = 1;
    public int TOURNAMENT_SIZE = 2;
    public int RESAMPLE = 3; //try 1,2,3
    public int ELITISM = 1;
    public double DISCOUNT = 1; //0.99;
    public double SHIFT_DISCOUNT = 0.99;


    @Override
    public String toString() {
        String s = "";

        String init = "none";
        if (INIT_TYPE == INIT_RANDOM) init = "random";
        else if (INIT_TYPE == INIT_ONESTEP) init = "OneStep";
        else if (INIT_TYPE == INIT_MCTS) init = "MCTS";

        String bud = "none";
        if (BUDGET_TYPE == FULL_BUDGET) bud = "full budget";
        else if (BUDGET_TYPE == HALF_BUDGET) bud = "half budget";

        String heur = "none";
        if (HEURISTIC_TYPE == HEURISTIC_WINSCORE) heur = "WinScore";
        else if (HEURISTIC_TYPE == HEURISTIC_SIMPLESTATE) heur = "SimpleState";

        String cross = "none";
        if (CROSSOVER_TYPE == UNIFORM_CROSS) cross = "uniform";
        else if (CROSSOVER_TYPE == POINT1_CROSS) cross = "1-Point";

        s += "---------- PARAMETER SET ----------\n";
        s += String.format("%1$-20s", "Population size") + ": " + POPULATION_SIZE + "\n";
        s += String.format("%1$-20s", "Individual length") + ": " + SIMULATION_DEPTH + "\n";
        s += "\n";
        s += String.format("%1$-20s", "Initialization type") + ": " + init + "\n";
        s += String.format("%1$-20s", "Budget type") + ": " + bud + "\n";
        s += String.format("%1$-20s", "Budget") + ": " + MAX_FM_CALLS + "\n";
        s += String.format("%1$-20s", "Resampling") + ": " + RESAMPLE + "\n";
        s += String.format("%1$-20s", "Heuristic") + ": " + heur + "\n";
        s += String.format("%1$-20s", "Value discount") + ": " + DISCOUNT + "\n";
        s += String.format("%1$-20s", "Elitism") + ": " + ELITISM + "\n";
        s += String.format("%1$-20s", "Reevaluate?") + ": " + REEVALUATE + "\n";
        s += "\n";
        s += String.format("%1$-20s", "Macro Action Length") + ": " + MACRO_ACTION_LENGTH + "\n";
        s += "\n";
        s += String.format("%1$-20s", "Bandit mutation?") + ": " + BANDIT_MUTATION + "\n";
        s += String.format("%1$-20s", "Genes mutated") + ": " + MUTATION + "\n";
        s += "\n";
        s += String.format("%1$-20s", "Tournament size") + ": " + TOURNAMENT_SIZE + "\n";
        s += String.format("%1$-20s", "Crossover type") + ": " + cross + "\n";
        s += "\n";
        s += String.format("%1$-20s", "Stats tree?") + ": " + TREE + "\n";
        s += String.format("%1$-20s", "Choose tree?") + ": " + CHOOSE_TREE + "\n";
        s += "\n";
        s += String.format("%1$-20s", "Shift buffer?") + ": " + SHIFT_BUFFER + "\n";
        s += String.format("%1$-20s", "Shift discount?") + ": " + SHIFT_DISCOUNT + "\n";
        s += "\n";
        s += String.format("%1$-20s", "Rollouts?") + ": " + ROLLOUTS + "\n";
        s += String.format("%1$-20s", "Rollout length") + ": " + ROLLOUT_LENGTH + "\n";
        s += String.format("%1$-20s", "Repeat rollouts") + ": " + REPEAT_ROLLOUT + "\n";
        s += "---------- ------------- ----------\n";

        return s;
    }
}
