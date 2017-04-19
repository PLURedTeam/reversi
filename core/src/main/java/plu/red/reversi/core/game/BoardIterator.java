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
     * @param boardsize Size of the board
     */
    public BoardIterator(History h, GameLogic l, int boardsize){
        hist = h;
        logic = l;
        board = new Board(boardsize);
        pos = 0;
    }

    public BoardIterator(BoardIterator other) {
        hist = other.hist;
        logic = other.logic;
        board = new Board(other.board);
        pos = other.pos;
    }

    /**
     * This method applies the move in history at a certain point
     * @param i index of the history
     */
    public void goTo(int i){
        pos = i;
        board = new Board(board.size);
        logic.initBoard(hist.getMoveCommandsUntil(i+1), board, false, false);
    }

    /**
     * This method returns the board at its previous state
     * @return the BoardIterator at the previous state
     */
    public BoardIterator previous(){
        board = new Board(board.size);
        logic.initBoard(hist.getMoveCommandsUntil(pos), board, false, false);
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
            logic.apply((SetCommand)c, board, false, false);
        else
            logic.play((MoveCommand)c, board, false, false);
        return this;
    }

    /**
     * Applies all the moves in the history to the board
     * @return the BoardIterator at the index with all commands applied
     */
    public BoardIterator end() {
        board = new Board(board.size);
        logic.initBoard(hist.getMoveCommandsUntil(hist.getNumBoardCommands()), board, false, false);
        return this;
    }

    public int getPos() {
        return pos;
    }
}
