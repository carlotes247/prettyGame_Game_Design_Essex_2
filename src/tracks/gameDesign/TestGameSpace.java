package tracks.gameDesign;

import tracks.DesignMachine;

import java.util.Random;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 04/10/13 Time: 16:29 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class TestGameSpace {

    public static void main(String[] args) {
        // Available tracks:
        String sampleRandomController = "tracks.singlePlayer.simple.sampleRandom.Agent";
        String doNothingController = "tracks.singlePlayer.simple.doNothing.Agent";
        String sampleOneStepController = "tracks.singlePlayer.simple.sampleonesteplookahead.Agent";
        String sampleMCTSController = "tracks.singlePlayer.deprecated.sampleMCTS.Agent";
        String sampleFlatMCTSController = "tracks.singlePlayer.simple.greedyTreeSearch.Agent";
        String sampleOLMCTSController = "tracks.singlePlayer.advanced.sampleMCTS.Agent";
        String sampleGAController = "tracks.singlePlayer.deprecated.sampleGA.Agent";
        String sampleOLETSController = "tracks.singlePlayer.advanced.olets.Agent";
        String repeatOLETS = "tracks.singlePlayer.tools.repeatOLETS.Agent";

        String interactorController = "tracks.singlePlayer.advanced.interactor.Agent";


        // Available games:
        String gamesPath = "examples/gameDesign/";
        String games[] = new String[] {};
        String gameRules[] = new String[] {};


        // All public games
        games = new String[] { "prettygame"}; 				// 0

        // Other settings
        boolean visuals = true;

        // Game and level to play
        int gameIdx = 0;
        int levelIdx = 0; // level names from 0 to 4 (game_lvlN.txt).
        int seed = new Random().nextInt();

        String game = gamesPath + games[gameIdx] + ".txt";
        String level1 = gamesPath + games[gameIdx] + "_lvl" + levelIdx + ".txt";

        String recordActionsFile = null;// "actions_" + games[gameIdx] + "_lvl"
                        // + levelIdx + "_" + seed + ".txt";
                        // where to record the actions
                        // executed. null if not to save.



        /** Game Spaces stuff starts here **/

        //Reads VGDL and loads game with parameters.
        DesignMachine dm = new DesignMachine(game);

        //0: Assigns values to parameters to play the game. Two ways: random and explicit.
        //0.a: Creating an individual at random:
        int[] individual = new int[dm.getNumDimensions()];
        for (int i = 0; i < individual.length; ++i)
            individual[i] = new Random().nextInt(dm.getDimSize(i));

        //0.b: Creating a new individual with an int[]:
        //    Each parameter will take a value = "lower_bound + i*increment" in the order defined in VGDL
//       individual = new int[]{0,1,0,1,0,1};

        //We can print a report with the parameters and values:
//        dm.printValues(individual);


        //1. Play as a human.
//        dm.playGame(individual, game, level1, seed);

        //2. Play with a controller.
        tracks.singlePlayer.advanced.interactor.Agent.heuristic = 1;
        dm.runOneGame(individual, game, level1, visuals, interactorController, recordActionsFile, seed, 0);


        //3. Random Search test.
//        int NUM_TRIALS = 2000;
//        individual = new int[dm.getNumDimensions()];
//        int[] best = new int[dm.getNumDimensions()];
//        double bestFit = -Integer.MAX_VALUE;
//        visuals = false;
//        for(int count = 0; count < NUM_TRIALS; ++count)
//        {
//            for(int i = 0; i < individual.length; ++i)
//                individual[i] = new Random().nextInt(dm.getDimSize(i));
//
////            dm.printValues(individual);
//
//            double fit = evaluate(individual,dm,game,level1,interactorController);
//
//            if(fit > bestFit)
//            {
//                bestFit = fit;
//                System.arraycopy(individual, 0, best, 0, dm.getNumDimensions());
//            }
//
//            System.out.print(count + " -- " + bestFit + ": ");
//            for (int i = 0; i < best.length; i++) {
//                System.out.print(best[i] + " ");
//            }
//            System.out.println();
//
//        }
//
//        visuals = true;
//        System.out.println("##########################");
//        System.out.println("Best individual with fitness " + bestFit);
//        System.out.println("##########################");
//        dm.runOneGame(individual, game, level1, visuals, sampleMCTSController, recordActionsFile, seed, 0);
//        dm.printDimensions();
    }


    private static double evaluate(int[] individual, DesignMachine dm, String game, String level1, String controller) {
        double value = 0;

        int seed = new Random().nextInt();

        int no_heuristics = tracks.singlePlayer.advanced.interactor.Agent.no_heuristics;
        double[][] scores = new double[no_heuristics][];

        int bonus = 1000;

        for (int i = 0; i < no_heuristics; i++) {
            tracks.singlePlayer.advanced.interactor.Agent.heuristic = i;
            double totWin = 0;
            double totSc = 0;
            for (int j = 0; j < 3; j++) {
                scores[i] = dm.runOneGame(individual, game, level1, false, controller, null, seed, 0);
                totWin += scores[i][0];
                totSc += scores[i][1];
            }

            totWin /= 2;
            totSc /= 3;

            if ( i == 0 )
                value = -totWin*bonus*no_heuristics - totSc*no_heuristics;
            else
                value += totWin*bonus + totSc;
        }

        return value;
    }
}
