// Name: Peiye Liu
// USC NetID: peiyeliu
// CSCI 455 PA3
// Fall 2020


import java.util.Arrays;
import java.util.Random;

/**
 * MineField
 * class with locations of mines for a game.
 * This class is mutable, because we sometimes need to change it once it's created.
 * mutators: populateMineField, resetEmpty
 * includes convenience method to tell the number of mines adjacent to a location.
 */
public class MineField {

    // <put instance variables here>
    private boolean[][] mineData;
    private int numMines;

    //Each location has at most 8 adjacent positions.
    //For each adjacent position, the row, column changes can be represent by 1, 0 or -1.
    //deltaRow, deltaColumn stores the changes of row, column coordinates, respectively.
    private static final int NUM_ADJACENT_DIRECTIONS = 8;
    private static final int[] deltaRow = {-1, -1, -1, 1, 1, 1, 0, 0};
    private static final int[] deltaColumn = {-1, 1, 0, -1, 1, 0, 1, -1};

    /**
     * Create a minefield with same dimensions as the given array, and populate it with the mines in the array
     * such that if mineData[row][col] is true, then hasMine(row,col) will be true and vice versa.  numMines() for
     * this minefield will corresponds to the number of 'true' values in mineData.
     *
     * @param mineData the data for the mines; must have at least one row and one col,
     *                 and must be rectangular (i.e., every row is the same length)
     */
    public MineField(boolean[][] mineData) {
        //Make defensive copy of mineData matrix.
        this.mineData = new boolean[mineData.length][mineData[0].length];
        for (int row = 0; row < this.mineData.length; row++) {
            System.arraycopy(mineData[row], 0, this.mineData[row], 0, this.mineData[0].length);
        }

        //Count the number of mines in this matrix.
        numMines = 0;
        for (boolean[] mineDatum : mineData) {
            for (int col = 0; col < mineData[0].length; col++) {
                if (mineDatum[col]) {
                    numMines++;
                }
            }
        }
    }


    /**
     * Create an empty minefield (i.e. no mines anywhere), that may later have numMines mines (once
     * populateMineField is called on this object).  Until populateMineField is called on such a MineField,
     * numMines() will not correspond to the number of mines currently in the MineField.
     *
     * @param numRows  number of rows this minefield will have, must be positive
     * @param numCols  number of columns this minefield will have, must be positive
     * @param numMines number of mines this minefield will have,  once we populate it.
     *                 PRE: numRows > 0 and numCols > 0 and 0 <= numMines < (1/3 of total number of field locations).
     */
    public MineField(int numRows, int numCols, int numMines) {
        assert numRows > 0 && numCols > 0 && numMines * 3 < numRows * numCols;
        mineData = new boolean[numRows][numCols];
        this.numMines = numMines;
    }


    /**
     * Removes any current mines on the minefield, and puts numMines() mines in random locations on the minefield,
     * ensuring that no mine is placed at (row, col).
     *
     * @param row the row of the location to avoid placing a mine
     * @param col the column of the location to avoid placing a mine
     *            PRE: inRange(row, col)
     */
    public void populateMineField(int row, int col) {
        assert inRange(row, col);
        resetEmpty();
        int numMinesFixed = 0;
        Random randNumGenerator = new Random();
        while (numMinesFixed < numMines) {
            int mineRow = randNumGenerator.nextInt(numRows());
            int mineColumn = randNumGenerator.nextInt(numCols());
            if (!mineData[mineRow][mineColumn]) {
                if (mineRow != row && mineColumn != col) {
                    mineData[mineRow][mineColumn] = true;
                    numMinesFixed++;
                }
            }
        }
    }


    /**
     * Reset the minefield to all empty squares.  This does not affect numMines(), numRows() or numCols()
     * Thus, after this call, the actual number of mines in the minefield does not match numMines().
     * Note: This is the state a minefield created with the three-arg constructor is in
     * at the beginning of a game.
     */
    public void resetEmpty() {
        for (int i = 0; i < mineData.length; i++) {
            Arrays.fill(mineData[i], false);
        }
    }


    /**
     * Returns the number of mines adjacent to the specified mine location (not counting a possible
     * mine at (row, col) itself).
     * Diagonals are also considered adjacent, so the return value will be in the range [0,8]
     *
     * @param row row of the location to check
     * @param col column of the location to check
     * @return the number of mines adjacent to the square at (row, col)
     * PRE: inRange(row, col)
     */
    public int numAdjacentMines(int row, int col) {
        assert inRange(row, col);
        int adjacentMines = 0;
        for (int i = 0; i < NUM_ADJACENT_DIRECTIONS; i++) {
            int adjacentRow = row + deltaRow[i];
            int adjacentColumn = col + deltaColumn[i];
            if (inRange(adjacentRow, adjacentColumn)) {
                if (hasMine(adjacentRow, adjacentColumn)) {
                    adjacentMines++;
                }
            }
        }
        return adjacentMines;
    }


    /**
     * Returns true iff (row,col) is a valid field location.  Row numbers and column numbers
     * start from 0.
     *
     * @param row row of the location to consider
     * @param col column of the location to consider
     * @return whether (row, col) is a valid field location
     */
    public boolean inRange(int row, int col) {
        return row >= 0 && col >= 0 && row < numRows() && col < numCols();
    }


    /**
     * Returns the number of rows in the field.
     *
     * @return number of rows in the field
     */
    public int numRows() {
        return mineData.length;
    }


    /**
     * Returns the number of columns in the field.
     *
     * @return number of columns in the field
     */
    public int numCols() {
        return mineData[0].length;
    }


    /**
     * Returns whether there is a mine in this square
     *
     * @param row row of the location to check
     * @param col column of the location to check
     * @return whether there is a mine in this square
     * PRE: inRange(row, col)
     */
    public boolean hasMine(int row, int col) {
        assert inRange(row, col);
        return mineData[row][col];
    }


    /**
     * Returns the number of mines you can have in this minefield.  For mines created with the 3-arg constructor,
     * some of the time this value does not match the actual number of mines currently on the field.  See doc for that
     * constructor, resetEmpty, and populateMineField for more details.
     *
     * @return the total number of mines in this board.
     */
    public int numMines() {
        return this.numMines;
    }
}

