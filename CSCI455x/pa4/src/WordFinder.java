// Name: Peiye Liu
// USC NetID: peiyeliu
// CS 455 PA4
// Fall 2020

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

/**
 * This class contains the main function to run the program.
 * The default dictionary is "sowpod.txt".
 * The dictionary file can be loaded by typing commend in this way:
 * java WordFinder (followed by the path of the dictionary file)
 * For example "java WordFinder testFiles/tinyDictionary.txt"
 * When the program print out "Rack?", the user can type a string.
 * In the input string, only lowercase or uppercase characters will be taken into account.
 * The program will print out all words matching the requirements (case sensitive) and their scores.
 * Typing a dot "." will terminate the program.
 */

public class WordFinder {

    /**
     * The main function that will be executed
     *
     * @param args the file name of the dictionary. If not fill name is given, the default dictionary will be used.
     */
    public static void main(String[] args) {
        //The default dictionary is "sowpods.txt"
        String filePath = "sowpods.txt";
        if (args.length > 0) {
            filePath = args[0];
        }
        try {
            AnagramDictionary anagramDictionary = new AnagramDictionary(filePath);
            System.out.println("Type . to quit.");
            System.out.print("Rack? ");
            Scanner in = new Scanner(System.in);
            while (in.hasNext()) {
                String nextInput = in.next();
                if (nextInput.equals(".")) {
                    return;
                }
                String rackWord = preProcessWord(nextInput);
                processScrabble(anagramDictionary, rackWord, nextInput);
                System.out.print("Rack? ");
            }
        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("ERROR: Dictionary file \"" + filePath + "\" does not exist.");
        } catch (IllegalDictionaryException illegalDictionaryException) {
            System.out.println(illegalDictionaryException.getMessage());
        }
    }

    /**
     * This function will pre-process each string input from scanner by removing all non-letter characters
     *
     * @param input the input word from scanner
     * @return the string after the processing
     */
    public static String preProcessWord(String input) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int index = 0; index < input.length(); index++) {
            char currChar = input.charAt(index);
            if (Character.isLetter(currChar)) {
                stringBuilder.append(currChar);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * This function will search the word input in the anagram dictionary, collect and sort the results, and print out them.
     *
     * @param dictionary           the anagram dictionary created from the file
     * @param inputRack            the rack word after calling preProcessWord function
     * @param wordBeforeProcessing the input word before calling preProcessWord function
     */
    public static void processScrabble(AnagramDictionary dictionary, String inputRack, String wordBeforeProcessing) {

        //create an arraylist of WordAndScore objects for sorting
        ArrayList<WordAndScore> wordAndScores = new ArrayList<>();

        //subset will store all combination possibilities in canonical form
        ArrayList<String> subset = Rack.allSubsetWapper(inputRack);

        for (String canonicalWord : subset) {
            //For words share the same canonical form, there scores are equal
            int score = ScoreTable.getScore(canonicalWord);
            ArrayList<String> anagram = dictionary.getAnagramsOf(canonicalWord);
            for (String finalWord : anagram) {
                wordAndScores.add(new WordAndScore(finalWord, score));
            }
        }

        Collections.sort(wordAndScores);
        //As Piazza @373 instructed, the word before processing needs to be printed here
        System.out.println("We can make " + wordAndScores.size() + " words from " + "\"" + wordBeforeProcessing + "\"");
        if (wordAndScores.size() == 0) {
            return;
        }
        System.out.println("All of the words with their scores (sorted by score):");
        for (WordAndScore entry : wordAndScores) {
            System.out.println(entry.toString());
        }
    }
}
