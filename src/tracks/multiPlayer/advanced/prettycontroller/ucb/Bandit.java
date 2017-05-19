package tracks.multiPlayer.advanced.prettycontroller.ucb;
import tools.StatSummary;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by simonmarklucas on 27/05/2016.
 *
 *
 *  Idea is to keep track of which changes lead to an improvement
 *
 */
public class Bandit {

    private static Random random = new Random();

    private static double eps = 0.01;
    private int nArms;

    // double[] rewards = new double[nArms];
    private double[] deltaRewards;
    private ArrayList<Double>[] allRewards;
    private int[] armPulls;
    private int nPulls;

    public int x;

    // start all at one to avoid div zero
    private int nMutations = 1;
    private static double k = Math.sqrt(2);

    private int xPrevious;

    public static final boolean STATIONARY = true;
    public static final int MAX_SIZE = 20;

    public Bandit(int arms) {
        nArms = arms;
        armPulls = new int[nArms];
        deltaRewards = new double[nArms];
        allRewards = new ArrayList[nArms];
        for (int i=0; i<nArms; i++) {
            allRewards[i] = new ArrayList<>();
        }
        x = 0;
    }

    public void pullArm() {
        Picker<Integer> picker = new Picker<>(Picker.MAX_FIRST);

        for (int i = 0; i < nArms; i++) {
            // never choose the current value of x
            // that would not be a mutation!!!
            if (i != x) {
                double exploit = exploit(i);
                double explore = explore(nPulls, armPulls[i]);
                // small random numbers: break ties in unexpanded nodes
                double noise = random.nextDouble() * eps;
//                 System.out.format("%d\t %.2f\t %.2f\n", i, exploit, explore);
                picker.add(exploit + explore + noise, i);
            }
        }
        xPrevious = x;
        x = picker.getBest();
        armPulls[x]++;
        nPulls++;
        nMutations++;
    }

    // standard UCB Explore term
    // consider modifying a value that's not been changed much yet
    private double explore(int n, int nA) {
        return k * Math.sqrt(Math.log(n) / (nA));
    }
    private double exploit(int i) {
        double value = deltaRewards[i];
        if (STATIONARY) {
            value = 0;
            for (double d : allRewards[i]) {
                value += d;
            }
            value /= allRewards[i].size();
        }
        return value;
    }


    public void applyReward(double delta) {
        if (x != xPrevious) {
            if (STATIONARY) {
                if (allRewards[x].size() >= MAX_SIZE) {
                    allRewards[x].remove(0);
                }
                allRewards[x].add(delta);
            }
            deltaRewards[x] += delta;
        }
    }

    public double getReward() {
        return deltaRewards[x];
    }

    // returns true if reverting to old value
    public boolean revertOrKeep(double delta) {
        if (delta < 0) {
            x = xPrevious;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Gene:" + nArms + ":" + nPulls + ":" + nMutations + ":" + x + ":" + armPulls[x] + ":" + deltaRewards[x];
    }
}
