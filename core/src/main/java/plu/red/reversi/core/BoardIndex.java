package plu.red.reversi.core;

/**
 * Represents a single tile on the board
 */
public class BoardIndex {
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

    //TODO: Convert to and from character
}
