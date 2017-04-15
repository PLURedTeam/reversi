package plu.red.reversi.core.game;

import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.command.SetCommand;
import plu.red.reversi.core.game.logic.GameLogic;

import java.util.Set;

public class BoardIterator {
    public History hist;
    public Board board;
    private GameLogic logic;
    private int pos;

    /**
     * Constructor
     * @param h history
     * @param l logic
     * @param b board
     */
    public BoardIterator(History h, GameLogic l, Board b){
        hist = h;
        logic = l;
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
        logic.initBoard(hist.getMoveCommandsUntil(i+1), board, true, false);
    }

    /**
     * This method returns the board at its previous state
     * @return the BoardIterator at the previous state
     */
    public BoardIterator previous(){
        board = new Board(board.size);
        logic.initBoard(hist.getMoveCommandsUntil(pos), board, true, false);
        pos--;
        return this;
    }

    /**
     * This method moves the BoardIterator to the next position
     * @return the BoardIterator at the next space
     */
    public BoardIterator next() {
        BoardCommand c = hist.getBoardCommand(++pos);
        if(c instanceof SetCommand)
            logic.apply((SetCommand)c, board, true, false);
        else
            logic.play((MoveCommand)c, board, true, false);
        return this;
    }

    /**
     * Applies all the moves in the history to the board
     * @return the BoardIterator at the index with all commands applied
     */
    public BoardIterator end(){
        board = new Board(board.size);
        logic.initBoard(hist.getMoveCommandsUntil(hist.getNumBoardCommands()), board, true, false);
        return this;
    }

    public int getPos() {
        return pos;
    }
}
