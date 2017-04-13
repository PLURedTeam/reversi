package plu.red.reversi.core.game.logic;

import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.command.SetCommand;
import plu.red.reversi.core.game.Board;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.listener.IBoardUpdateListener;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * GameLogic is responsible for handling a set of games rules. Each specific game will be
 * a subclass implementing the abstract functions.
 *
 * This will hold a reference to board. All modifications to board (after initialization)
 * should go through this class to validate the actions with the game rules.
 *
 * For any change made to the board, the registered IBoardUpdateListeners will be updated.
 */
public abstract class GameLogic {
    //TODO: move History here?
    protected final HashSet<IBoardUpdateListener> boardUpdateListeners = new HashSet<>();
    protected final Board board;
    protected boolean validCache = false;

    /**
     * Constructs a new GameLogic class with a reference to the game board.
     * The board should not be modified outside of this class once this class has been
     * constructed. (Initial state can be before).
     * @param board Board used for game play (logic).
     */
    public GameLogic(Board board) {
        this.board = board;
    }

    /**
     * Registers an iFlipListener that will have signal sent to it when Flips are applied.
     * @param listener IFlipListener to register.
     */
    public void addBoardUpdateListener(IBoardUpdateListener listener) {
        boardUpdateListeners.add(listener);
    }

    /**
     * Unregister an existing IFlipListener that has been previously registered.
     * No action is performed if the it has not been previously registered.
     * @param listener IFlipListener to unregister.
     */
    public void removeBoardUpdateListener(IBoardUpdateListener listener) {
        boardUpdateListeners.remove(listener);
    }

    /**
     * Calls update on all the board listeners...
     * @see IBoardUpdateListener
     * @param origin The index of the board where a piece has been set to a new value
     * @param playerId The new value of the piece at BoardIndex (could be -1 to indicate the piece was removed)
     * @param update A collection of tiles which have been updated to match origin as a result of the change at origin
     */
    protected void updateBoardListeners(BoardIndex origin, int playerId, Collection<BoardIndex> update) {
        for (IBoardUpdateListener i : boardUpdateListeners) {
            i.onBoardUpdate(origin, playerId, update);
        }
    }

    /**
     * Apply multiple commands at once. Used when you have a saved game state you wish to restore.
     * @param commands List of commands to be applied in order.
     */
    public void initBoard(Collection<BoardCommand> commands) {
        for(BoardCommand c: commands) {
            if(c instanceof MoveCommand)
                play((MoveCommand)c);
            if(c instanceof SetCommand)
                apply((SetCommand)c);
        }
    }

    /**
     * Initialize the board with the appropriate initial game state. Used for new games.
     * @param players Array of the player ids used in current game in order.
     */
    public void initBoard(int[] players) {
        Collection<SetCommand> commands = this.getSetupCommands(players);
        for(SetCommand c : commands)
            apply(c);
    }

    /**
     * Sets a tile on the board without considering legality or playing out the move.
     * @param c Command specifying a location and its new player value.
     */
    public void apply(SetCommand c) {
        if(validCache) this.invalidateCache();
        board.apply(c);
    }

    /**
     * @return Reference to board.
     */
    public Board getBoard() { return board; }

    /**
     * Make a move on the board.
     * @param command Represents the move which is to be played.
     * @throws InvalidParameterException If it is an invalid move, no move will be made.
     */
    public abstract void play(MoveCommand command) throws InvalidParameterException;

    /**
     * Checks the board to see if the move attempted is valid.
     * Prefer calling play and handling an exception than checking
     * if it is valid first.
     *
     * @param command Includes player and board index.
     */
    public abstract boolean isValidMove(MoveCommand command);

    /**
     * Find the different moves that could be made and return them.
     *
     * @param player Integer Player ID to check for
     * @return ArrayList moves
     */
    public abstract Set<BoardIndex> getValidMoves(int player);

    /**
     * Returns the score of the Player ID passed in
     * @param player Integer Player ID
     * @return Score for the given player.
     */
    public abstract int getScore(int player);

    /**
     * Retrieve the initial setup commands based on the specific game logic.
     * @param players Array of the player ids used in current game in order.
     * @return List of the moves to be made to create the initial state.
     * @throws IllegalArgumentException If player count is invalid.
     */
    public abstract Collection<SetCommand> getSetupCommands(int[] players) throws IllegalArgumentException;

    /**
     * In the event board is changed outside of this class, this function will invalidate
     * the cache and force it to be regenerated given the current board state. Do not rely
     * on regular calls to this function.
     */
    public void invalidateCache() {
        validCache = false;
    }
}
