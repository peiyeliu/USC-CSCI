// Name: Peiye Liu
// USC NetID: peiyeliu
// CSCI455 PA2
// Fall 2020

import java.util.ArrayList;

/**
 * Class Bookshelf
 * Implements idea of arranging books into a bookshelf.
 * Books on a bookshelf can only be accessed in a specific way so books don’t fall down;
 * You can add or remove a book only when it’s on one of the ends of the shelf.
 * However, you can look at any book on a shelf by giving its location (starting at 0).
 * Books are identified only by their height; two books of the same height can be
 * thought of as two copies of the same book.
 */

public class Bookshelf {

    /**
     * Representation invariant:
     * <p>
     * PRE: shelf does not contain integers less or equal to 0.
     */
    private ArrayList<Integer> shelf;

    /**
     * Creates an empty Bookshelf object i.e. with no books
     */
    public Bookshelf() {
        shelf = new ArrayList<>();
        //Assertion statement is followed by an error message.
        //The error message will be printed out when AssertionError is caught.
        assert this.isValidBookshelf() : "ERROR: Height of a book must be positive.";
    }

    /**
     * Creates a Bookshelf with the arrangement specified in pileOfBooks. Example
     * values: [20, 1, 9].
     * <p>
     * PRE: pileOfBooks contains an array list of 0 or more positive numbers
     * representing the height of each book.
     */
    public Bookshelf(ArrayList<Integer> pileOfBooks) {
        shelf = new ArrayList<>(pileOfBooks);
        assert this.isValidBookshelf() : "ERROR: Height of a book must be positive.";
    }

    /**
     * Inserts book with specified height at the start of the Bookshelf, i.e., it
     * will end up at position 0.
     * <p>
     * PRE: height > 0 (height of book is always positive)
     */
    public void addFront(int height) {
        shelf.add(0, height);
        assert this.isValidBookshelf() : "ERROR: Height of a book must be positive.";
    }

    /**
     * Inserts book with specified height at the end of the Bookshelf.
     * <p>
     * PRE: height > 0 (height of book is always positive)
     */
    public void addLast(int height) {
        shelf.add(height);
        assert this.isValidBookshelf() : "ERROR: Height of a book must be positive.";
    }

    /**
     * Removes book at the start of the Bookshelf and returns the height of the
     * removed book.
     * Return -1 if the removeFront operation is invalid.
     * <p>
     * PRE: this.size() > 0 i.e. can be called only on non-empty BookShelf
     */
    public int removeFront() {
        assert this.size() > 0 : "ERROR: Entered pick operation is invalid on this shelf.";
        int removed = shelf.get(0);
        shelf.remove(0);

        return removed;
    }

    /**
     * Removes book at the end of the Bookshelf and returns the height of the
     * removed book.
     * Return -1 if the removeLast operation is invalid.
     * <p>
     * PRE: this.size() > 0 i.e. can be called only on non-empty BookShelf
     */
    public int removeLast() {
        assert this.size() > 0 : "ERROR: Entered pick operation is invalid on this shelf.";
        int removed = shelf.get(shelf.size() - 1);
        shelf.remove(shelf.size() - 1);
        return removed;
    }

    /**
     * Gets the height of the book at the given position.
     * Return -1 if the position is invalid.
     * PRE: 0 <= position < this.size()
     */
    public int getHeight(int position) {
        assert position >= 0 && position < this.size() : "ERROR: Entered pick operation is invalid on this shelf.";
        return shelf.get(position);
    }

    /**
     * Returns number of books on the this Bookshelf.
     */
    public int size() {
        return shelf.size();
    }

    /**
     * Returns string representation of this Bookshelf. Returns a string with the height of all
     * books on the bookshelf, in the order they are in on the bookshelf, using the format shown
     * by example here:  “[7, 33, 5, 4, 3]”
     * Return an empty string if the bookshelf is invalid.
     */
    public String toString() {
        String output = "[";
        for (int i = 0; i < shelf.size(); i++) {
            output += (shelf.get(i));
            //if the integer is not the last one, there should be a comma and a space after that
            if (i < shelf.size() - 1) {
                output += ", ";
            }
        }
        output += "]";
        assert this.isValidBookshelf() : "ERROR: Height of a book must be positive.";
        return output;
    }

    /**
     * Returns true iff the books on this Bookshelf are in non-decreasing order.
     * (Note: this is an accessor; it does not change the bookshelf.)
     */
    public boolean isSorted() {
        if (shelf.size() <= 1) {
            return true;
        }
        for (int i = 0; i <= shelf.size() - 2; i++) {
            if (shelf.get(i + 1) < shelf.get(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true iff the Bookshelf data is in a valid state.
     * (See representation invariant comment for more details.)
     */
    private boolean isValidBookshelf() {
        //check whether all integers are positive
        for (int x : shelf) {
            if (x <= 0) {
                return false;
            }
        }
        return true;
    }
}
