package RHEA;

import RHEA.bandits.BanditArray;
import RHEA.bandits.BanditGene;
import RHEA.sampleOLMCTS.SingleTreeNode;
import tracks.DesignMachine;
import tracks.singlePlayer.tools.Heuristics.WinScoreHeuristic;
import tracks.singlePlayer.tools.Heuristics.StateHeuristic;
import RHEA.utils.ParameterSet;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import static RHEA.utils.Constants.*;
import static tracks.singlePlayer.advanced.interactor.Agent.HEURISTIC_DEFAULT;
import static tracks.singlePlayer.advanced.interactor.Agent.heuristic;
import static tracks.singlePlayer.advanced.interactor.Agent.no_heuristics;

import java.awt.*;
import java.util.*;

public class Agent {

    protected ParameterSet params;

    DesignMachine dm;
    String gamesPath = "examples/gameDesign/";
    String[] games = new String[] { "prettygame" };
    String game = gamesPath + games[0] + ".txt";
    String level1 = gamesPath + games[0] + "_lvl" + 0 + ".txt";
    String controller = "tracks.singlePlayer.advanced.interactor.Agent";
    String simpleController = "tracks.singlePlayer.advanced.simple.Agent";
    int[] dims;
    Individual worst;

    public int MCTS_BUDGET;
    public int ONESTEP_BUDGET;

    private Individual[] population, nextPop;
    private Random randomGenerator;

    // number of evaluations
    private int numEvals = 0;
    private int numCalls = 0;
    private int numPop = 0;
    private int numIters = 0;
    int lastAct = -1;

    //Bandits
    protected BanditArray bandits; // bandits for each gene


    /**
     * Public constructor with state observation and time due.
     *
     */
    public Agent() {
        randomGenerator = new Random();
        params = new ParameterSet();
    }

    private void run() {
        MCTS_BUDGET = params.MAX_FM_CALLS / 2;

        numCalls = 0;
        numEvals = 0;
        numPop = 0;
        numIters = 0;
        do {
            numPop++;

            // mutate one individual randomly
            if (params.POPULATION_SIZE < 2) {
                Individual newind = population[0].mutate(params.MUTATION, params.BANDIT_MUTATION, bandits, false); //only 1 individual in population, mutate it
                evaluate(newind);

                if (population[0].value < newind.value) {
                    if (population[0].value < worst.value)
                        worst = population[0].copy();
                    nextPop[0] = newind.copy();
                } else {
                    if (newind.value < worst.value)
                        worst = newind.copy();
                }
            } else {
                for (int i = params.ELITISM; i < params.POPULATION_SIZE; i++) {
                    Individual newind = population[i].mutate(params.MUTATION, params.BANDIT_MUTATION, bandits, false);

                    // evaluate new individual, insert into population
                    add_individual(newind,nextPop,i);
                }
                Arrays.sort(nextPop);
            }

            population = nextPop.clone();
            numIters++;

            System.out.println(numIters + " -- " + population[0]);
            System.out.println(numIters + " -- " + worst);

        } while (numIters < params.MAX_ITERS);
    }

    private double evaluate(Individual individual) {
        double value = 0;

        int no_agents = no_heuristics + 1;
        double[][] scores = new double[no_agents][];
        int[] seed = new int[params.RESAMPLE];

        int bonus = 1000;

        for (int i = 0; i < no_agents; i++) {
            String playController;
            if (i == 0)
                playController = simpleController;
            else {
                playController = controller;
                heuristic = i;
            }
            double totWin = 0;
            double totSc = 0;
            for (int j = 0; j < params.RESAMPLE; j++) {
                if (i == 0) //set seed, keep for all algorithms
                    seed[j] = new Random().nextInt();
                scores[i] = dm.runOneGame(individual.actions, game, level1, false, playController, null, seed[j], 0);
                totWin += scores[i][0];
                totSc += scores[i][1];
            }

            totWin /= params.RESAMPLE;
            totSc /= params.RESAMPLE;

            if ( i <= 1 )
                value = -totWin*bonus*no_agents/2 - totSc*no_agents/2;
            else
                value += totWin*bonus + totSc;
        }

        individual.value = value;

        return value;
    }

    private void add_individual(Individual newind, Individual[] pop, int idx) {
        evaluate(newind);
        pop[idx] = newind.copy();
    }


    public void print_pop(Individual[] pop) {
        System.out.println("------------");

        for (int i = 0; i < pop.length; i++)
            System.out.println(pop[i]);

        System.out.println("------------");

    }

    private void init_pop(int[] dims) {

        this.dims = dims;
        params.SIMULATION_DEPTH = dims.length;

        population = new Individual[params.POPULATION_SIZE];
            nextPop = new Individual[params.POPULATION_SIZE];

            for (int i = 0; i < params.POPULATION_SIZE; i++) {

                if (i == 0 || (numCalls + params.SIMULATION_DEPTH) < params.MAX_FM_CALLS) {
                    population[i] = new Individual(params.SIMULATION_DEPTH, dims, randomGenerator);
                }
            }

        bandits = new BanditArray(population, dims);

        for (int i = 0; i < params.POPULATION_SIZE; i++) {
            evaluate(population[i]);
        }
        if (params.POPULATION_SIZE > 1)
            Arrays.sort(population);
        for (int i = 0; i < params.POPULATION_SIZE; i++) {
            nextPop[i] = population[i].copy();
        }

        worst = population[0];

    }

    public static void main(String[] args) {
        Agent rhea = new Agent();
        rhea.dm = new DesignMachine(rhea.game);
        int[] dims = new int[rhea.dm.getNumDimensions()];
        for (int i = 0; i < dims.length; i++) {
            dims[i] = rhea.dm.getDimSize(i);
        }
        rhea.dm.printDimensions();

        rhea.init_pop(dims);
        rhea.run();
    }
}
