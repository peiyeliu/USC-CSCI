// Name: Peiye Liu
// USC NetID: peiyeliu
// CS 455 PA1
// Fall 2020

import javax.swing.*;
import java.util.Scanner;

/**
 * This class will display the bar chart of the coin-toss results
 * The program will run one time after a valid input (Positive integer) is typed
 * The program will stop when the input is not a integer
 */

public class CoinSimViewer {

    public static void main(String[] args) {
        JFrame frame = new JFrame();

        //the frame is initialized with default value: frame width = 800, frame height = 500
        frame.setSize(800, 500);
        frame.setTitle("CoinSim");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        Scanner in = new Scanner(System.in);
        int inputTrials = 0;
        //the while loop will continue unless a positive integer is typed.
        while (inputTrials <= 0) {
            System.out.println("Input the number of trials:");
            if (in.hasNextInt()) {
                int numOfTrial = in.nextInt();
                if (numOfTrial <= 0) {
                    //Print error message when the input is invalid.
                    System.out.println("ERROR: the input mush be a positive integer.");
                    continue;
                }
                //when the input is valid, pass the value to the sentinel
                inputTrials = numOfTrial;
            } else {
                System.out.println("ERROR: integer required.");
                //The program will stop if the input is not a integer.
                return;
            }
        }


        // the vertical buffer space and the bar width are fixed.
        int VERTICAL_BUFFER_SPACE = 50;
        int BAR_WIDTH = 75;
        CoinSimComponent coinSimComponent = new CoinSimComponent(inputTrials, VERTICAL_BUFFER_SPACE, BAR_WIDTH);

        frame.add(coinSimComponent);
        frame.setVisible(true);
    }
}
