package plu.red.reversi.core.game;

import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.command.SetCommand;
import plu.red.reversi.core.game.logic.GameLogic;
import plu.red.reversi.core.game.logic.GameLogicCache;

//TODO: stop using logic.initBoard when possible and replace with forward stepping
public class BoardIterator {
    public History history;
    public GameLogicCache cache;
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
        history = h;
        logic = l;
        board = new Board(boardsize);
        cache = l.createCache();
        pos = -1;
    }

    public BoardIterator(BoardIterator other) {
        history = other.history;
        logic = other.logic;
        board = new Board(other.board);
        cache = logic.createCache();

        goTo(other.pos);
    }

    /**
     * This method applies the move in history at a certain point
     * @param i index of the history
     */
    public void goTo(int i){
        pos = i;
        board = new Board(board.size);
        cache.invalidate();
        logic.initBoard(cache, board, history.getMoveCommandsUntil(i + 1), false, false);
    }

    /**
     * This method returns the board at its previous state
     * @return the BoardIterator at the previous state
     */
    public BoardIterator previous(){
        board = new Board(board.size);
        cache.invalidate();
        logic.initBoard(cache, board, history.getMoveCommandsUntil(pos), false, false);
        pos--;
        return this;
    }

    /**
     * This method moves the BoardIterator to the next position
     * @return the BoardIterator at the next space
     */
    public BoardIterator next() {
        BoardCommand c = history.getBoardCommand(++pos);

        if(c instanceof SetCommand)
            logic.apply(cache, board, (SetCommand)c, false, false);
        else
            logic.play(cache, board, (MoveCommand)c, false, false);
        return this;
    }

    /**
     * Applies all the moves in the history to the board
     * @return the BoardIterator at the index with all commands applied
     */
    public BoardIterator end() {
        board = new Board(board.size);
        cache.invalidate();
        logic.initBoard(cache, board, history.getMoveCommandsUntil(history.getNumBoardCommands()), false, false);
        return this;
    }

    public int getPos() {
        return pos;
    }
}
