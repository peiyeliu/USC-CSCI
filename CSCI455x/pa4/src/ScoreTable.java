// Name: Peiye Liu
// USC NetID: peiyeliu
// CS 455 PA4
// Fall 2020

/**
 * This class will calculate the score for a given word
 */
public class ScoreTable {

    /**
     * An integer array with size 26 that stores scores for every letters.
     * Uppercase letters and their lowercase formats will have the same score.
     */
    private static final char[] scoreList = {1, 3, 3, 2, 1,
            4, 2, 4, 1, 8, 5, 1, 3, 1, 1, 3, 10,
            1, 1, 1, 1, 4, 4, 8, 4, 10};


    /**
     * For a given word, calculate the score for it.
     * Uppercase letters and their lowercase formats will have the same score.
     *
     * @param word the word need for score
     * @return the score of that word
     */
    public static int getScore(String word) {
        int score = 0;
        //The score is not case sensitive, we can turn the word in lowercase format.
        String lowerCaseWord = word.toLowerCase();
        for (int i = 0; i < lowerCaseWord.length(); i++) {
            char currChar = lowerCaseWord.charAt(i);
            score += scoreList[currChar - 'a'];
        }
        return score;
    }

}
