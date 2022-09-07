// Name: Peiye Liu
// USC NetID: peiyeliu
// CSCI 455 PA3
// Fall 2020


import java.util.Arrays;

/**
 * VisibleField class
 * This is the data that's being displayed at any one point in the game (i.e., visible field, because it's what the
 * user can see about the minefield), Client can call getStatus(row, col) for any square.
 * It actually has data about the whole current state of the game, including
 * the underlying mine field (getMineField()).  Other accessors related to game status: numMinesLeft(), isGameOver().
 * It also has mutators related to actions the player could do (resetGameDisplay(), cycleGuess(), uncover()),
 * and changes the game state accordingly.
 * <p>
 * It, along with the MineField (accessible in mineField instance variable), forms
 * the Model for the game application, whereas GameBoardPanel is the View and Controller, in the MVC design pattern.
 * It contains the MineField that it's partially displaying.  That MineField can be accessed (or modified) from
 * outside this class via the getMineField accessor.
 */
public class VisibleField {
    // ----------------------------------------------------------
    // The following public constants (plus numbers mentioned in comments below) are the possible states of one
    // location (a "square") in the visible field (all are values that can be returned by public method
    // getStatus(row, col)).

    // Covered states (all negative values):
    public static final int COVERED = -1;   // initial value of all squares
    public static final int MINE_GUESS = -2;
    public static final int QUESTION = -3;

    // Uncovered states (all non-negative values):

    // values in the range [0,8] corresponds to number of mines adjacent to this square

    public static final int MINE = 9;      // this loc is a mine that hasn't been guessed already (end of losing game)
    public static final int INCORRECT_GUESS = 10;  // is displayed a specific way at the end of losing game
    public static final int EXPLODED_MINE = 11;   // the one you uncovered by mistake (that caused you to lose)
    // ----------------------------------------------------------

    // <put instance variables here>

    private MineField mineField;
    private int[][] visibleBoard;
    private int numGuessed;
    private boolean isGameOver;


    //Depth first search to open adjacent squares:
    //Each location has at most 8 adjacent positions.
    //For each adjacent position, the row, column changes can be represent by 1, 0 or -1.
    //deltaRow, deltaColumn stores the changes of row, column coordinates, respectively.
    private static final int NUM_ADJACENT_DIRECTIONS = 8;
    private static final int[] deltaRow = {-1, -1, -1, 1, 1, 1, 0, 0};
    private static final int[] deltaColumn = {-1, 1, 0, -1, 1, 0, 1, -1};


    /**
     * Create a visible field that has the given underlying mineField.
     * The initial state will have all the mines covered up, no mines guessed, and the game
     * not over.
     *
     * @param mineField the minefield to use for for this VisibleField
     */
    public VisibleField(MineField mineField) {
        this.mineField = mineField;
        visibleBoard = new int[mineField.numRows()][mineField.numCols()];
        for (int i = 0; i < visibleBoard.length; i++) {
            Arrays.fill(visibleBoard[i], COVERED);
        }
        numGuessed = 0;
        isGameOver = false;
    }


    /**
     * Reset the object to its initial state (see constructor comments), using the same underlying
     * MineField.
     */
    public void resetGameDisplay() {
        for (int i = 0; i < visibleBoard.length; i++) {
            Arrays.fill(visibleBoard[i], COVERED);
        }
        numGuessed = 0;
        isGameOver = false;
    }


    /**
     * Returns a reference to the mineField that this VisibleField "covers"
     *
     * @return the minefield
     */
    public MineField getMineField() {
        return this.mineField;
    }


    /**
     * Returns the visible status of the square indicated.
     *
     * @param row row of the square
     * @param col col of the square
     * @return the status of the square at location (row, col).  See the public constants at the beginning of the class
     * for the possible values that may be returned, and their meanings.
     * PRE: getMineField().inRange(row, col)
     */
    public int getStatus(int row, int col) {
        return visibleBoard[row][col];
    }


    /**
     * Returns the the number of mines left to guess.  This has nothing to do with whether the mines guessed are correct
     * or not.  Just gives the user an indication of how many more mines the user might want to guess.  This value can
     * be negative, if they have guessed more than the number of mines in the minefield.
     *
     * @return the number of mines left to guess.
     */
    public int numMinesLeft() {
        return mineField.numMines() - numGuessed;
    }


    /**
     * Cycles through covered states for a square, updating number of guesses as necessary.  Call on a COVERED square
     * changes its status to MINE_GUESS; call on a MINE_GUESS square changes it to QUESTION;  call on a QUESTION square
     * changes it to COVERED again; call on an uncovered square has no effect.
     *
     * @param row row of the square
     * @param col col of the square
     *            PRE: getMineField().inRange(row, col)
     */
    public void cycleGuess(int row, int col) {
        assert getMineField().inRange(row, col);
        if (visibleBoard[row][col] >= 0) {
            return;
        }
        if (visibleBoard[row][col] == COVERED) {
            visibleBoard[row][col] = MINE_GUESS;
            numGuessed++;
            return;
        }
        if (visibleBoard[row][col] == MINE_GUESS) {
            visibleBoard[row][col] = QUESTION;
            numGuessed--;
            return;
        }
        if (visibleBoard[row][col] == QUESTION) {
            visibleBoard[row][col] = COVERED;
        }
    }


    /**
     * Uncovers this square and returns false iff you uncover a mine here.
     * If the square wasn't a mine or adjacent to a mine it also uncovers all the squares in
     * the neighboring area that are also not next to any mines, possibly uncovering a large region.
     * Any mine-adjacent squares you reach will also be uncovered, and form
     * (possibly along with parts of the edge of the whole field) the boundary of this region.
     * Does not uncover, or keep searching through, squares that have the status MINE_GUESS.
     * Note: this action may cause the game to end: either in a win (opened all the non-mine squares)
     * or a loss (opened a mine).
     *
     * @param row of the square
     * @param col of the square
     * @return false   iff you uncover a mine at (row, col)
     * PRE: getMineField().inRange(row, col)
     */
    public boolean uncover(int row, int col) {
        assert getMineField().inRange(row, col);
        if (getMineField().hasMine(row, col)) {
            visibleBoard[row][col] = EXPLODED_MINE;
            displayLosingGame();
            isGameOver = true;
            return false;
        }
        openEmptyRegions(row, col);
        if (isWinning()) {
            displayWinningGame();
            isGameOver = true;
        }
        return true;
    }


    /**
     * Returns whether the game is over.
     * (Note: This is not a mutator.)
     *
     * @return whether game over
     */
    public boolean isGameOver() {
        //"Game over" means the game is terminated, either by winning or losing the game.
        return isGameOver;
    }


    /**
     * Returns whether this square has been uncovered.  (i.e., is in any one of the uncovered states,
     * vs. any one of the covered states).
     *
     * @param row of the square
     * @param col of the square
     * @return whether the square is uncovered
     * PRE: getMineField().inRange(row, col)
     */
    public boolean isUncovered(int row, int col) {
        assert getMineField().inRange(row, col);
        return visibleBoard[row][col] >= 0;
    }

    /**
     * This function will automatically open adjacent squares that has not mines until it reaches boundary or squares
     * adjacent to other mines.
     *
     * @param row of the starting square
     * @param col of the starting square
     */
    private void openEmptyRegions(int row, int col) {
        //depth first search will stop if it touches the boundary
        if (!mineField.inRange(row, col)) {
            return;
        }
        //when the square is already uncovered or the square has been marked as "guess", depth-first search should stop.
        if (isUncovered(row, col) || visibleBoard[row][col] == MINE_GUESS) {
            return;
        }

        visibleBoard[row][col] = mineField.numAdjacentMines(row, col);

        //when the square has mines nearby, the depth-first search will stop.
        if (visibleBoard[row][col] > 0) {
            return;
        }

        //exploration will be continued when applicable
        for (int i = 0; i < NUM_ADJACENT_DIRECTIONS; i++) {
            openEmptyRegions(row + deltaRow[i], col + deltaColumn[i]);
        }
    }


    /**
     * This function will display the board when the player loses the game.
     */
    private void displayLosingGame() {
        for (int row = 0; row < visibleBoard.length; row++) {
            for (int col = 0; col < visibleBoard[0].length; col++) {
                if (isUncovered(row, col)) {
                    continue;
                }
                if (mineField.hasMine(row, col) && visibleBoard[row][col] != MINE_GUESS) {
                    visibleBoard[row][col] = MINE;
                } else if (!mineField.hasMine(row, col) && visibleBoard[row][col] == MINE_GUESS) {
                    visibleBoard[row][col] = INCORRECT_GUESS;
                }
            }
        }
    }

    /**
     * This function will display the board when the player wins the game.
     */
    private void displayWinningGame() {
        for (int row = 0; row < visibleBoard.length; row++) {
            for (int col = 0; col < visibleBoard[0].length; col++) {
                if (isUncovered(row, col)) {
                    continue;
                }
                if (mineField.hasMine(row, col)) {
                    visibleBoard[row][col] = MINE_GUESS;
                }
            }
        }
    }

    /**
     * This function is to check whether the player wins the game.
     * If the player opened all non-mine squares, the player wins.
     *
     * @return true iff the player has opened all squares that have no mines.
     */
    private boolean isWinning() {
        for (int row = 0; row < visibleBoard.length; row++) {
            for (int col = 0; col < visibleBoard[0].length; col++) {
                if (!isUncovered(row, col) && !mineField.hasMine(row, col)) {
                    return false;
                }
            }
        }
        return true;
    }
}
