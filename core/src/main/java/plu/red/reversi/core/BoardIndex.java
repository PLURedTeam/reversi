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

    //TODO: Convert to and from character

}
