// Name: Peiye Liu
// USC NetID: peiyeliu
// CS 455 PA4
// Fall 2020

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;


/**
 * A dictionary of all anagram sets.
 * Note: the processing is case-sensitive; so if the dictionary has all lower
 * case words, you will likely want any string you test to have all lower case
 * letters too, and likewise if the dictionary words are all upper case.
 */
public class AnagramDictionary {

    /**
     * dictionary: a map object that store all words in the dictionary
     * the key is the canonical format of word
     * the value is a hash set that contains all words with the same canonical format
     * Representation invariant: for each value in the map, there is no duplicate words
     */
    private Map<String, HashSet<String>> dictionary;

    /**
     * Create an anagram dictionary from the list of words given in the file
     * indicated by fileName.
     *
     * @param fileName the name of the file to read from
     * @throws FileNotFoundException      if the file is not found
     * @throws IllegalDictionaryException if the dictionary has any duplicate words
     */
    public AnagramDictionary(String fileName) throws FileNotFoundException,
            IllegalDictionaryException {
        File file = new File(fileName);
        Scanner in = new Scanner(file);
        dictionary = new HashMap<>();

        while (in.hasNext()) {
            String currWord = in.next();
            String currKey = getCanonicalForm(currWord);
            if (!dictionary.containsKey(currKey)) {
                dictionary.put(currKey, new HashSet<>());
            }
            if (dictionary.get(currKey).contains(currWord)) {
                throw new IllegalDictionaryException("ERROR: Illegal dictionary: dictionary file has a duplicate word: " + currWord);
            }
            dictionary.get(currKey).add(currWord);
        }
    }


    /**
     * Get all anagrams of the given string. This method is case-sensitive.
     * E.g. "CARE" and "race" would not be recognized as anagrams.
     *
     * @param s string to process
     * @return a list of the anagrams of s
     */
    public ArrayList<String> getAnagramsOf(String s) {
        ArrayList<String> anagramsResult = new ArrayList<>();
        String canonical = getCanonicalForm(s);
        if (dictionary.containsKey(canonical)) {
            anagramsResult.addAll(dictionary.get(canonical));
        }
        return anagramsResult;
    }

    /**
     * For a given word, get its canonical form
     * For example, "calm" has canonical form "aclm";
     *
     * @param word the word input
     * @return the canonicalForm of that word
     */
    private String getCanonicalForm(String word) {
        char[] charArray = word.toCharArray();
        Arrays.sort(charArray);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < charArray.length; i++) {
            result.append(charArray[i]);
        }
        return result.toString();
    }

}
