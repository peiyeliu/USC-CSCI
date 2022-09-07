// Name: Peiye Liu
// USC NetID: peiyeliu
// CS 455 PA4
// Fall 2020


import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is a tester class I used to test functions I wrote in Rack class and ScoreTable class.
 */
public class MyTester {

    public static void main(String[] args) {
        scoreTableTester();
        rackTester();
    }

    public static void rackTester() {
        ArrayList<String> subset = Rack.allSubsetWapper("sfwe");
        System.out.println(subset.toString());
    }

    /**
     * This function test the implementation of ScoreTable class.
     * This function will print out points and corresponding letters.
     */
    public static void scoreTableTester() {
        String alpha = "abcdefghijklmnopqrstuvwxyz";
        HashMap<Integer, StringBuilder> letterMap = new HashMap<>();

        for (int i = 0; i < alpha.length(); i++) {
            String letter = alpha.substring(i, i + 1);
            int score = ScoreTable.getScore(letter);
            if (!letterMap.containsKey(score)) {
                letterMap.put(score, new StringBuilder());
            }
            letterMap.get(score).append(letter.toUpperCase());
        }
        for (int point : letterMap.keySet()) {
            System.out.println(point + ": " + letterMap.get(point));
        }
    }
}
