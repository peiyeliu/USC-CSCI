// Name: Peiye Liu
// USC NetID: peiyeliu
// CSCI455 PA2
// Fall 2020

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Class BookshelfKeeperProg
 * This class is a terminal-based program that allow users to perform a series of pick and put operations on a
 * BookshelfKeeper object. This class contains the main method.
 * The BookshelfKeeper need to be initialized first, the input should be a series of positive integers separated by
 * space (more spaces or tab is accepted).
 * The program supports "pick", "put" and "end" operations(case sensitive). Each line will only perform one operation.
 * "pick" and "put" operation should has a following number (separated by space) indicating the position to pick and
 * the height of the book, respectively.
 * The program will be terminated when typing "end" in the terminal.
 */

public class BookshelfKeeperProg {

    public static void main(String[] args) {
        System.out.println("Please enter initial arrangement of books followed by newline:");
        Scanner in = new Scanner(System.in);
        try {
            Bookshelf bookshelf = readInput(in);
            BookshelfKeeper bookshelfKeeper = new BookshelfKeeper(bookshelf);
            System.out.println(bookshelfKeeper.toString());
            System.out.println("Type pick <index> or put <height> followed by newline. Type end to exit.");
            while (in.hasNextLine()) {
                String input = in.nextLine();
                //trim is used to remove the blank space on the left and right
                input = input.trim();
                //split the string into a String array, "\\s+" will identify one or more spaces.
                String[] inputArray = input.split("\\s+");

                //If the opeartion is end, the program will be terminated.
                if (inputArray[0].equals("end")) {
                    System.out.println("Exiting Program.");
                    return;
                }
                performOpeartions(bookshelfKeeper, inputArray);
            }
        } catch (AssertionError error) {
            //When an assertion error detected, we print out the error message.
            //The program will be terminated.
            System.out.println(error.getMessage());
            System.out.println("Exiting Program.");
            return;
        }
    }


    /**
     * Read the input of height values and initialize a Bookshelf object
     *
     * @param in : the Scanner object use to receive input.
     */
    private static Bookshelf readInput(Scanner in) {
        ArrayList<Integer> shelf = new ArrayList<>();
        String input = in.nextLine();
        input = input.trim();
        if (input.equals("")) {
            return new Bookshelf();
        }
        String[] inputArray = input.split("\\s+");
        for (String numString : inputArray) {
            shelf.add(Integer.parseInt(numString));
        }
        return new Bookshelf(shelf);
    }

    /**
     * Perform valid pick or put operations on the bookshelfKeeper object
     *
     * @param bookshelfKeeper: the BookshelfKeeper object
     * @param inputArray:      a String array that stores operation identifier and digits (In String format) for that
     *                         operations
     */
    private static void performOpeartions(BookshelfKeeper bookshelfKeeper, String[] inputArray) {
        if (inputArray[0].equals("put")) {
            int height = Integer.parseInt(inputArray[1]);
            int result = bookshelfKeeper.putHeight(height);
            if (result != -1) {
                //-1 indicates the operation is invalid, the bookshelfKeeper will not be printed out.
                System.out.println(bookshelfKeeper);
            }
        } else if (inputArray[0].equals("pick")) {
            int position = Integer.parseInt(inputArray[1]);
            int result = bookshelfKeeper.pickPos(position);
            if (result != -1) {
                System.out.println(bookshelfKeeper);
            }
        } else {
            System.out.println("ERROR: Operation should be either pick or put.");
        }
    }
}
