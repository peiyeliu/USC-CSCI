// Name: Peiye Liu
// USC NetID: peiyeliu
// CS 455 PA4
// Fall 2020


/**
 * The class will create a WordAndScore object that store the word and its score.
 * This class implements Comparable interface that allows customized sorting.
 */
public class WordAndScore implements Comparable<WordAndScore> {

    /**
     * Representation invariants:
     * word: the String word only contains uppercase or lowercase letter. The word is not empty.
     * score: the score is a positive integer.
     */
    private String word;
    private int score;

    public WordAndScore(String word, int score) {
        this.word = word;
        this.score = score;
    }

    /**
     * This function will allow the sorting by scores in descending order
     * When two words have the same score, they are ordered in alphabetic order
     *
     * @param o the other WordAndScore object in the comparing process
     * @return positive integer if this object should be in front of the other one
     */
    @Override
    public int compareTo(WordAndScore o) {
        if (this.score == o.score) {
            //When the scores are equal, they are ordered in alphabetic order
            //Here String's comparaTo method will be called
            return this.word.compareTo(o.word);
        }
        return o.score - this.score;
    }

    /**
     * This method allows the object to be printed as the way defined in the assignment requirements
     *
     * @return a string start with the score followed by word, separated by a comma and a space
     */
    @Override
    public String toString() {
        return score + ": " + word;
    }
}
