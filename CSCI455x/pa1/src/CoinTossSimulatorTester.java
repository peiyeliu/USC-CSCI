// Name: Peiye Liu
// USC NetID: peiyeliu
// CS 455 PA1
// Fall 2020

/**
 * This class is used to test the CoinTossSimulator class without showing bar chart
 * Number of trials, number of two-head tosses, two-tail tosses, and one-head one-tail tosses will be printed
 */

public class CoinTossSimulatorTester {

    public static void main(String[] args) {
        CoinTossSimulator testSimulator = new CoinTossSimulator();

        //totalTrials and currTrial will store expected results
        int totalTrials = 0;
        int currTrial = 0;

        //Print messages to test the constructor, no simulation performed
        System.out.println("After constructor:");
        System.out.println("Number of trials [exp : " + totalTrials + " ]: " + testSimulator.getNumTrials());
        System.out.println("Two-head tosses: " + testSimulator.getTwoHeads());
        System.out.println("Two-tail tosses: " + testSimulator.getTwoTails());
        System.out.println("One-head one-tail tosses: " + testSimulator.getHeadTails());
        System.out.println("Tosses add up correctly? " + (totalTrials == testSimulator.getNumTrials()));

        currTrial = 1;
        totalTrials += currTrial;
        testSimulator.run(currTrial);
        System.out.println("After run(" + currTrial + "):");
        System.out.println("Number of trials [exp : " + totalTrials + " ]: " + testSimulator.getNumTrials());
        System.out.println("Two-head tosses: " + testSimulator.getTwoHeads());
        System.out.println("Two-tail tosses: " + testSimulator.getTwoTails());
        System.out.println("One-head one-tail tosses: " + testSimulator.getHeadTails());
        System.out.println("Tosses add up correctly? " + (totalTrials == testSimulator.getNumTrials()));

        currTrial = 100;
        totalTrials += currTrial;
        testSimulator.run(currTrial);
        System.out.println("After run(" + currTrial + "):");
        System.out.println("Number of trials [exp : " + totalTrials + " ]: " + testSimulator.getNumTrials());
        System.out.println("Two-head tosses: " + testSimulator.getTwoHeads());
        System.out.println("Two-tail tosses: " + testSimulator.getTwoTails());
        System.out.println("One-head one-tail tosses: " + testSimulator.getHeadTails());
        System.out.println("Tosses add up correctly? " + (totalTrials == testSimulator.getNumTrials()));

        currTrial = 1500;
        totalTrials += currTrial;
        testSimulator.run(currTrial);
        System.out.println("After run(" + currTrial + "):");
        System.out.println("Number of trials [exp : " + totalTrials + " ]: " + testSimulator.getNumTrials());
        System.out.println("Two-head tosses: " + testSimulator.getTwoHeads());
        System.out.println("Two-tail tosses: " + testSimulator.getTwoTails());
        System.out.println("One-head one-tail tosses: " + testSimulator.getHeadTails());
        System.out.println("Tosses add up correctly? " + (totalTrials == testSimulator.getNumTrials()));

        //after reset, totalTrials should be zero
        totalTrials = 0;
        testSimulator.reset();
        System.out.println("After reset:");
        System.out.println("Number of trials [exp : " + totalTrials + " ]: " + testSimulator.getNumTrials());
        System.out.println("Two-head tosses: " + testSimulator.getTwoHeads());
        System.out.println("Two-tail tosses: " + testSimulator.getTwoTails());
        System.out.println("One-head one-tail tosses: " + testSimulator.getHeadTails());
        System.out.println("Tosses add up correctly? " + (totalTrials == testSimulator.getNumTrials()));

        currTrial = 500;
        totalTrials += currTrial;
        testSimulator.run(currTrial);
        System.out.println("After run(" + currTrial + "):");
        System.out.println("Number of trials [exp : " + totalTrials + " ]: " + testSimulator.getNumTrials());
        System.out.println("Two-head tosses: " + testSimulator.getTwoHeads());
        System.out.println("Two-tail tosses: " + testSimulator.getTwoTails());
        System.out.println("One-head one-tail tosses: " + testSimulator.getHeadTails());
        System.out.println("Tosses add up correctly? " + (totalTrials == testSimulator.getNumTrials()));


    }
}
