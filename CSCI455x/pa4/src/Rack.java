// Name: Peiye Liu
// USC NetID: peiyeliu
// CS 455 PA4
// Fall 2020

import java.util.ArrayList;

/**
 * A Rack of Scrabble tiles
 */

public class Rack {


    /**
     * Finds all subsets of the multiset starting at position k in unique and mult.
     * unique and mult describe a multiset such that mult[i] is the multiplicity of the char
     * unique.charAt(i).
     * PRE: mult.length must be at least as big as unique.length()
     * 0 <= k <= unique.length()
     *
     * @param unique a string of unique letters
     * @param mult   the multiplicity of each letter from unique.
     * @param k      the smallest index of unique and mult to consider.
     * @return all subsets of the indicated multiset.  Unlike the multiset in the parameters,
     * each subset is represented as a String that can have repeated characters in it.
     * @author Claire Bono
     */
    private static ArrayList<String> allSubsets(String unique, int[] mult, int k) {
        ArrayList<String> allCombos = new ArrayList<>();

        if (k == unique.length()) {  // multiset is empty
            allCombos.add("");
            return allCombos;
        }

        // get all subsets of the multiset without the first unique char
        ArrayList<String> restCombos = allSubsets(unique, mult, k + 1);

        // prepend all possible numbers of the first char (i.e., the one at position k)
        // to the front of each string in restCombos.  Suppose that char is 'a'...

        String firstPart = "";          // in outer loop firstPart takes on the values: "", "a", "aa", ...
        for (int n = 0; n <= mult[k]; n++) {
            for (int i = 0; i < restCombos.size(); i++) {  // for each of the subsets
                // we found in the recursive call
                // create and add a new string with n 'a's in front of that subset
                allCombos.add(firstPart + restCombos.get(i));
            }
            firstPart += unique.charAt(k);  // append another instance of 'a' to the first part
        }

        return allCombos;
    }

    /**
     * This is the wrapper method to give the subset
     *
     * @param word the word that need to be examined
     * @return all subsets of the indicated multiset.
     */
    public static ArrayList<String> allSubsetWapper(String word) {
        int[] freq = getFrequency(word);
        String unique = getStringUnique(freq);
        int[] mult = getMultiplicity(freq, word);
        return allSubsets(unique, mult, 0);
    }

    /**
     * For a given word, count the frequency of each character
     *
     * @param word the word input
     * @return the frequency of each character in that word
     */
    private static int[] getFrequency(String word) {

        //The range from 'z' to 'A' can cover all uppercase and lowercase letters.
        int[] freqResult = new int['z' - 'A' + 1];

        for (int i = 0; i < word.length(); i++) {
            char currChar = word.charAt(i);
            freqResult[currChar - 'A']++;
        }
        return freqResult;
    }

    /**
     * For a frequency count array, give the string with unique character in alphabetical order
     * For example "abadbb" will turn "abd"
     *
     * @param freq the word input
     * @return the string contains unique characters in alphabetical order
     */
    private static String getStringUnique(int[] freq) {

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < freq.length; i++) {
            if (freq[i] > 0) {
                result.append((char) (i + 'A'));
            }
        }
        return result.toString();
    }

    /**
     * From a canonical version of word, return the mutiplicity array
     * For example, "aabbbd" will have unique-formed string "abd" and multiplicity {2,1,3};
     *
     * @param freq   the frequency of each character in that word
     * @param unique the unique-form of the word
     * @return an integer array that stores multiplicity of each unique character
     */
    private static int[] getMultiplicity(int[] freq, String unique) {
        int[] multiplicity = new int[unique.length()];
        int index = 0;
        for (int i = 0; i < freq.length; i++) {
            if (freq[i] > 0) {
                multiplicity[index] = freq[i];
                index++;
            }
        }
        return multiplicity;
    }
}
