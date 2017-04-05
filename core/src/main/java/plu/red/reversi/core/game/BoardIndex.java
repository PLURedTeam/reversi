package plu.red.reversi.core.game;

/**
 * Represents a single tile on the board
 */
public class BoardIndex implements Comparable<BoardIndex> {
    public int row, column;

    public BoardIndex() {
        row = 0;
        column = 0;
    }

    public BoardIndex(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public BoardIndex(BoardIndex i){
        this(i.row, i.column);
    }

    // Added so we can use BoardIndex as a key in something like a HashMap. Otherwise every BoardIndex object has a
    // unique hash, even if they have the same contents.
    @Override
    public int hashCode() {
        // Not guaranteed to be unique if a Board size is more than 10000, but really, are we gonna have a Board bigger
        // than 10000
        return row + column*10000;
    }


    @Override
    public boolean equals(Object o) {
        return o instanceof BoardIndex && ((BoardIndex)o).row == row && ((BoardIndex)o).column == column;
    }

    @Override
    public int compareTo(BoardIndex boardIndex) {
        if(row < boardIndex.row) {
            return -1;
        } else if(row > boardIndex.row) {
            return 1;
        } else {
            if(column < boardIndex.column) {
                return -1;
            } else if(column > boardIndex.column) {
                return 1;
            } else return 0;
        }
    }

    /**
     * Prints the current board index in chess form (see image: http://www.chess-poster.com/english/learn_chess/notation/images/coordinates_2.gif)
     * @return the string representing this board coordinate
     */
    public String getCoordinateString() {
        char c = 'a';
        c += column;
        return "" + c + (row + 1);
    }

    @Override
    public String toString() {
        return getCoordinateString();
    }
}
