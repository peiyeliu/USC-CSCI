// Name: Peiye Liu
// USC NetID: peiyeliu
// CSCI455 PA2
// Fall 2020


/**
 * Class BookshelfKeeper
 * <p>
 * Enables users to perform efficient putPos or pickHeight operation on a bookshelf of books kept in
 * non-decreasing order by height, with the restriction that single books can only be added
 * or removed from one of the two *ends* of the bookshelf to complete a higher level pick or put
 * operation.  Pick or put operations are performed with minimum number of such adds or removes.
 */
public class BookshelfKeeper {

    /**
     * bookshelf: a Bookshelf object that will be maintained
     * totalNumOperations: count the cumulative number of bookshelf mutator calls
     * lastNumOperations: count the number of bookshelf mutator calls for the last pick or put operation
     * Representation invariant:
     * <p>
     * PRE: shelf.isSorted() is true
     * PRE: totalNumOperations >= 0
     * PRE: lastNumOperations >= 0
     */
    private Bookshelf bookshelf;
    private int totalNumOperations;
    private int lastNumOperations;

    /**
     * Creates a BookShelfKeeper object with an empty bookshelf
     */
    public BookshelfKeeper() {
        bookshelf = new Bookshelf();
        totalNumOperations = 0;
        lastNumOperations = 0;
    }

    /**
     * Creates a BookshelfKeeper object initialized with the given sorted bookshelf.
     * Note: method does not make a defensive copy of the bookshelf.
     * <p>
     * PRE: sortedBookshelf.isSorted() is true.
     */
    public BookshelfKeeper(Bookshelf sortedBookshelf) {
        bookshelf = sortedBookshelf;
        totalNumOperations = 0;
        lastNumOperations = 0;
        assert sortedBookshelf.isSorted() : "ERROR: Heights must be specified in non-decreasing order.";
    }

    /**
     * Removes a book from the specified position in the bookshelf and keeps bookshelf sorted
     * after picking up the book.
     * <p>
     * Returns the number of calls to mutators on the contained bookshelf used to complete this
     * operation. This must be the minimum number to complete the operation.
     * Return -1 if the pick operation is invalid here.
     * <p>
     * PRE: position must be in the range [0, getNumBooks()).
     */
    public int pickPos(int position) {
        assert position >= 0 && position < getNumBooks() : "ERROR: Entered pick operation is invalid on this shelf.";

        // calculate the number of books need to be removed from left/right
        int left = position;
        int right = this.getNumBooks() - position - 1;

        // if left <= right, do the move from left, otherwise do the move from right
        boolean isFromLeft = left <= right;
        int numBookMoved = Math.min(left, right);

        //call doPick function to perform the move
        doPick(numBookMoved, isFromLeft);
        assert this.isValidBookshelfKeeper() : "ERROR: Heights must be specified in non-decreasing order.";

        //Step 1: move books into a buffer shelf (+numBookMoved)
        //Step 2: pick up the target book (+1)
        //Step 3: put the books from the butter shelf back to the main shelf (+numBookMoved)
        lastNumOperations = numBookMoved + 1 + numBookMoved;
        totalNumOperations += lastNumOperations;
        return lastNumOperations;
    }

    /**
     * Inserts book with specified height into the shelf.  Keeps the contained bookshelf sorted
     * after the insertion.
     * <p>
     * Returns the number of calls to mutators on the contained bookshelf used to complete this
     * operation. This must be the minimum number to complete the operation.
     * Return -1 if the put operation is invalid here.
     * <p>
     * PRE: height > 0
     */
    public int putHeight(int height) {
        assert height > 0 : "ERROR: Height of a book must be positive.";
        // calculate the number of books need to be removed from left/right
        // when there are duplicates height numbers in the Bookshelf
        // we need to check both size to determine the minimum move (For extra point)
        int left = 0;
        while (left < bookshelf.size() && bookshelf.getHeight(left) < height) {
            left++;
        }
        int right = bookshelf.size() - 1;
        while (right >= 0 && bookshelf.getHeight(right) > height) {
            right--;
        }

        // if left <= right, do the move from left, otherwise do the move from right
        boolean isFromLeft = left <= bookshelf.size() - 1 - right;
        int numBookMoved = Math.min(left, bookshelf.size() - 1 - right);

        //call doPut function to perform the move
        doPut(numBookMoved, height, isFromLeft);
        assert this.isValidBookshelfKeeper() : "ERROR: Heights must be specified in non-decreasing order.";

        lastNumOperations = numBookMoved + 1 + numBookMoved;
        totalNumOperations += lastNumOperations;
        return lastNumOperations;
    }

    /**
     * Returns the total number of calls made to mutators on the contained bookshelf
     * so far, i.e., all the ones done to perform all of the pick and put operations
     * that have been requested up to now.
     */
    public int getTotalOperations() {
        return totalNumOperations;
    }

    /**
     * Returns the number of books on the contained bookshelf.
     */
    public int getNumBooks() {
        return bookshelf.size();
    }

    /**
     * Returns string representation of this BookshelfKeeper. Returns a String containing height
     * of all books present in the bookshelf in the order they are on the bookshelf, followed
     * by the number of bookshelf mutator calls made to perform the last pick or put operation,
     * followed by the total number of such calls made since we created this BookshelfKeeper.
     * <p>
     * Example return string showing required format: “[1, 3, 5, 7, 33] 4 10”
     */
    public String toString() {
        assert this.isValidBookshelfKeeper() : "ERROR: Heights must be specified in non-decreasing order.";
        return bookshelf.toString() + " " + this.lastNumOperations + " " + this.totalNumOperations;
    }

    /**
     * Returns true iff the BookshelfKeeper data is in a valid state.
     * (See representation invariant comment for details.)
     */
    private boolean isValidBookshelfKeeper() {
        return bookshelf.isSorted();
    }

    // add any other private methods here

    /**
     * Perform the pick operation on the Bookshelf
     *
     * @param numMoved:   the number of books that need to be removed before picking
     *                    the target book.
     * @param isFromLeft: true if the books on the left side are removed
     */
    private void doPick(int numMoved, boolean isFromLeft) {
        // Create a buffer Bookshelf to store the books that are temporarily removed
        Bookshelf bufferShelf = new Bookshelf();
        if (isFromLeft) {
            for (int i = 1; i <= numMoved; i++) {
                bufferShelf.addLast(bookshelf.removeFront());
            }
            bookshelf.removeFront();
            while (bufferShelf.size() > 0) {
                bookshelf.addFront((bufferShelf.removeLast()));
            }
        } else {
            for (int i = 1; i <= numMoved; i++) {
                bufferShelf.addFront(bookshelf.removeLast());
            }
            bookshelf.removeLast();
            while (bufferShelf.size() > 0) {
                bookshelf.addLast((bufferShelf.removeFront()));
            }
        }
    }

    /**
     * Perform the put operation on the Bookshelf
     *
     * @param numMoved:   the number of books that need to be removed before putting
     *                    the target book.
     * @param height:     the height of the book that we want to put
     * @param isFromLeft: true if the books on the left side are removed
     */
    private void doPut(int numMoved, int height, boolean isFromLeft) {
        // Create a buffer Bookshelf to store the books that are temporarily removed
        Bookshelf bufferShelf = new Bookshelf();
        if (isFromLeft) {
            for (int i = 1; i <= numMoved; i++) {
                bufferShelf.addLast(bookshelf.removeFront());
            }
            bookshelf.addFront(height);
            while (bufferShelf.size() > 0) {
                bookshelf.addFront((bufferShelf.removeLast()));
            }
        } else {
            for (int i = 1; i <= numMoved; i++) {
                bufferShelf.addFront(bookshelf.removeLast());
            }
            bookshelf.addLast(height);
            while (bufferShelf.size() > 0) {
                bookshelf.addLast((bufferShelf.removeFront()));
            }
        }
    }
}
