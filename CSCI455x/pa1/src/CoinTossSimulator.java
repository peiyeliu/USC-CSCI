// Name: Peiye Liu
// USC NetID: peiyeliu
// CS 455 PA1
// Fall 2020

import java.util.Random;

/**
 * class CoinTossSimulator
 * <p>
 * Simulates trials of repeatedly tossing two coins and allows the user to access the
 * cumulative results.
 * <p>
 * NOTE: we have provided the public interface for this class.  Do not change
 * the public interface.  You can add private instance variables, constants,
 * and private methods to the class.  You will also be completing the
 * implementation of the methods given.
 * <p>
 * Invariant: getNumTrials() = getTwoHeads() + getTwoTails() + getHeadTails()
 */
public class CoinTossSimulator {

    /**
     * simulationResult will store the result for every toss
     * numGenerator is a Random object that will be used to generate random number
     */
    private int[] simulationResult;
    private Random numGenerator;


    /**
     * Creates a coin toss simulator with no trials done yet.
     */
    public CoinTossSimulator() {
        /**
         * create an integer array with length = 3 to store the simulation results
         * in every trial, we toss two coins
         * the number of tails could only be 0, 1 or 2
         * the index of the array represents the possible outcome for every trial:
         * 0 ----   no tail ----- two heads
         * 1 ----  one tail ----- one head
         * 2 ---- two tails ----- no head
         */
        simulationResult = new int[3];
        numGenerator = new Random();
    }


    /**
     * Runs the simulation for numTrials more trials. Multiple calls to this method
     * without a reset() between them *add* these trials to the current simulation.
     *
     * @param numTrials number of trials to for simulation; must be >= 1
     */
    public void run(int numTrials) {

        //when the input numTrials is less or equal to zero, print error message
        if (numTrials < 1) {
            System.out.println("Invalid input: the number of trials must be greater than zero!");
            return;
        }
        // if the parameter is valid, update the number of total trials

        for (int i = 1; i <= numTrials; i++) {
            // in every beginning of the for loop, reset the counter as zero
            // numOfTail will count how many tail in these two trails
            int numOfTail = 0;

            //simulate the tossing of two coins by creating two random double value ranging from 0 to 1.
            double trial1 = numGenerator.nextDouble();
            double trial2 = numGenerator.nextDouble();

            // the coin is tail when the random double number larger or equal to 0.5
            // numOfTail will count how many tails in these two trails
            if (trial1 >= 0.5) {
                numOfTail += 1;
            }
            if (trial2 >= 0.5) {
                numOfTail += 1;
            }

            //numOfTail could only be 0, 1, or 2
            //update the result of this trial accordingly
            simulationResult[numOfTail]++;
        }
    }


    /**
     * Get number of trials performed since last reset.
     */
    public int getNumTrials() {
        return simulationResult[0] + simulationResult[1] + simulationResult[2];
    }


    /**
     * Get number of trials that came up two heads since last reset.
     */
    public int getTwoHeads() {
        return simulationResult[0];
    }


    /**
     * Get number of trials that came up two tails since last reset.
     */
    public int getTwoTails() {
        return simulationResult[2];
    }


    /**
     * Get number of trials that came up one head and one tail since last reset.
     */
    public int getHeadTails() {
        return simulationResult[1];
    }


    /**
     * Resets the simulation, so that subsequent runs start from 0 trials done.
     */
    public void reset() {
        simulationResult[0] = 0;
        simulationResult[1] = 0;
        simulationResult[2] = 0;
    }
}
