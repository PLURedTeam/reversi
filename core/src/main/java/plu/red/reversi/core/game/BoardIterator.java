package plu.red.reversi.core.game;

/**
 * Created by JChase on 3/17/17.
 */
public class BoardIterator {
    public History hist;
    public Board board;
    private int pos;

    /**
     * Constructor
     * @param h history
     * @param b board
     */
    public BoardIterator(History h, Board b){
        hist = h;
        board = b;
        pos = 0;
    }

    /**
     * This method applies the move in history at a certain point
     * @param i index of the history
     */
    public void goTo(int i){
        pos = i;
        board = new Board(board.size);
        board.applyCommands(hist.getMoveCommandsUntil(i+1));
    }

    /**
     * This method returns the board at its previous state
     * @return the BoardIterator at the previous state
     */
    public BoardIterator previous(){
        board = new Board(board.size);
        board.applyCommands(hist.getMoveCommandsUntil(pos));
        pos--;
        return this;
    }

    /**
     * This method moves the BoardIterator to the next position
     * @return the BoardIterator at the next space
     */
    public BoardIterator next(){
        board.apply(hist.getBoardCommand(++pos));
        return this;
    }

    /**
     * Applies all the moves in the history to the board
     * @return the BoardIterator at the index with all commands applied
     */
    public BoardIterator end(){
        board = new Board(board.size);
        board.applyCommands(hist.getMoveCommandsUntil(hist.getNumBoardCommands()));
        return this;
    }

    public int getPos() {
        return pos;
    }
}
