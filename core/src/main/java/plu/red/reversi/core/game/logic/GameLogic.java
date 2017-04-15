package plu.red.reversi.core.game.logic;

import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.command.SetCommand;
import plu.red.reversi.core.game.Board;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.listener.IBoardUpdateListener;

import java.security.InvalidParameterException;
import java.util.*;

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
    protected final HashSet<IBoardUpdateListener> boardUpdateListeners = new HashSet<>();
    protected final Game game;


    /**
     * Constructs a new GameLogic class with a reference to the game.
     * The board should not be modified outside of this class once this class has been
     * constructed. (Initial state can be before).
     * @param game Game this logic is associated with.
     */
    public GameLogic(Game game) {
        this.game = game;
    }


    /**
     * This constructor should only be used for testing.
     */
    public GameLogic() {
        game = null;
    }


    /**
     * Registers an iFlipListener that will have signal sent to it when Flips are applied.
     * @param listener IFlipListener to register.
     * @return This object for chaining.
     */
    public final GameLogic addBoardUpdateListener(IBoardUpdateListener listener) {
        boardUpdateListeners.add(listener);
        return this;
    }


    /**
     * Unregister an existing IFlipListener that has been previously registered.
     * No action is performed if the it has not been previously registered.
     * @param listener IFlipListener to unregister.
     * @return This object for chaining.
     */
    public final GameLogic removeBoardUpdateListener(IBoardUpdateListener listener) {
        boardUpdateListeners.remove(listener);
        return this;
    }


    /**
     * Calls update on all the board listeners...
     * @see IBoardUpdateListener
     * @param origin The index of the board where a piece has been set to a new value
     * @param playerId The new value of the piece at BoardIndex (could be -1 to indicate the piece was removed)
     * @param update A collection of tiles which have been updated to match origin as a result of the change at origin
     * @return This object for chaining.
     */
    protected final GameLogic updateBoardListeners(BoardIndex origin, int playerId, Collection<BoardIndex> update) {
        for (IBoardUpdateListener i : boardUpdateListeners)
            i.onBoardUpdate(origin, playerId, update);

        return this;
    }


    /**
     * Apply multiple commands at once. Used when you have a saved game state you wish to restore.
     * @param commands List of commands to be applied in order.
     * @return This object for chaining.
     */
    public final GameLogic initBoard(Collection<BoardCommand> commands) {
        return initBoard(commands, game.getBoard(), true, false);
    }


    /**
     * Apply multiple commands at once. Used when you have a saved game state you wish to restore.
     * @param commands List of commands to be applied in order.
     * @param board Board to apply the commands to.
     * @param notify True if this should notify subscribed listeners.
     * @param record True if this should update the game history.
     * @return This object for chaining.
     */
    public final GameLogic initBoard(Collection<BoardCommand> commands, Board board, boolean notify, boolean record) {
        for(BoardCommand c: commands) {
            if(c instanceof MoveCommand)
                play((MoveCommand)c, board, notify, record);
            if(c instanceof SetCommand)
                apply((SetCommand)c, board, notify, record);
        }
        return this;
    }


    /**
     * Initialize the board with the appropriate initial game state. Used for new games.
     * @return This object for chaining.
     */
    public final GameLogic initBoard() {
        return initBoard(Arrays.stream(game.getUsedPlayers()).mapToInt(Integer::intValue).toArray(), game.getBoard(), true, true);
    }


    /**
     * Initialize the board with the appropriate initial game state. Used for new games.
     * @param players Array of the player ids used in current game in order.
     * @param board Board to apply commands to.
     * @param notify True if this should notify subscribed listeners.
     * @param record True if this should update the game history.
     * @return This object for chaining.
     */
    public final GameLogic initBoard(int[] players, Board board, boolean notify, boolean record) {
        Collection<SetCommand> commands = getSetupCommands(players, board.size);
        for(SetCommand c : commands)
            apply(c, board, notify, record);

        return this;
    }


    /**
     * Sets a tile on the board without considering legality or playing out the move.
     * @param command Command specifying a location and its new player value.
     * @return This object for chaining.
     */
    public final GameLogic apply(SetCommand command) {
        return apply(command, game.getBoard(), true, true);
    }


    /**
     * Sets a tile on the board without considering legality or playing out the move.
     * @param command Command specifying a location and its new player value.
     * @param board Board to apply commands to.
     * @param notify True if this should notify subscribed listeners.
     * @param record True if this should update the game history.
     * @return This object for chaining.
     */
    public final GameLogic apply(SetCommand command, Board board, boolean notify, boolean record) {
        board.apply(command);
        if(notify) updateBoardListeners(command.position, command.playerID, new LinkedList<>());
        if(record) game.getHistory().addCommand(command);

        return this;
    }


    /**
     * Make a move on the board.
     * @param command Represents the move which is to be played.
     * @return This object for chaining.
     * @throws InvalidParameterException If it is an invalid move, no move will be made.
     */
    public final GameLogic play(MoveCommand command) throws InvalidParameterException {
        return play(command, game.getBoard(), true, true);
    };


    /**
     * Make a move on the board.
     * @param command Represents the move which is to be played.
     * @param board Board to apply commands to.
     * @param notify True if this should notify subscribed listeners.
     * @param record True if this should update the game history.
     * @return This object for chaining.
     * @throws InvalidParameterException If it is an invalid move, no move will be made.
     */
    public abstract GameLogic play(MoveCommand command, Board board, boolean notify, boolean record) throws InvalidParameterException;


    /**
     * Checks the board to see if the move attempted is valid.
     * Prefer calling play and handling an exception than checking
     * if it is valid first.
     *
     * @param command Includes player and board index.
     */
    public final boolean isValidMove(MoveCommand command) {
        return isValidMove(command, game.getBoard());
    }


    /**
     * Checks the board to see if the move attempted is valid.
     * Prefer calling play and handling an exception than checking
     * if it is valid first.
     *
     * @param command Includes player and board index.
     * @param board Board to apply commands to.
     */
    public abstract boolean isValidMove(MoveCommand command, Board board);


    /**
     * Find the different moves that could be made and return them.
     *
     * @param player Integer Player ID to check for
     * @return ArrayList moves
     */
    public final Set<BoardIndex> getValidMoves(int player) {
        return getValidMoves(player, game.getBoard());
    }


    /**
     * Find the different moves that could be made and return them.
     *
     * @param player Integer Player ID to check for
     * @param board Board to apply commands to.
     * @return ArrayList moves
     */
    public abstract Set<BoardIndex> getValidMoves(int player, Board board);


    /**
     * Checks if the given player has at least one available move. Prefer using this to getValidMoves as it can be
     * much more efficient when only checking true/false.
     * @param player The player to check.
     * @return True if the player is able to play, else false.
     */
    public final boolean canPlay(int player) {
        return canPlay(player, game.getBoard());
    }


    /**
     * Checks if the given player has at least one available move. Prefer using this to getValidMoves as it can be
     * much more efficient when only checking true/false.
     * @param player The player to check.
     * @param board Board to apply commands to.
     * @return True if the player is able to play, else false.
     */
    public abstract boolean canPlay(int player, Board board);


    /**
     * Returns the score of the Player ID passed in
     * @param player Integer Player ID
     * @return Score for the given player.
     */
    public final int getScore(int player) {
        return getScore(player, game.getBoard());
    }


    /**
     * Returns the score of the Player ID passed in
     * @param player Integer Player ID.
     * @param board Board to apply commands to.
     * @return Score for the given player.
     */
    public abstract int getScore(int player, Board board);


    /**
     * Retrieve the initial setup commands based on the specific game gameLogic.
     * @return List of the moves to be made to create the initial state.
     * @throws IllegalArgumentException If player count is invalid.
     */
    public final Collection<SetCommand> getSetupCommands() throws IllegalArgumentException {
        return getSetupCommands(Arrays.stream(game.getUsedPlayers()).mapToInt(Integer::intValue).toArray(), game.getBoard().size);
    }


    /**
     * Retrieve the initial setup commands based on the specific game gameLogic.
     * @param players Array of the player ids used in current game in order.
     * @size Size of board for which to generate the setup commands for.
     * @return List of the moves to be made to create the initial state.
     * @throws IllegalArgumentException If player count is invalid.
     */
    public abstract Collection<SetCommand> getSetupCommands(int[] players, int size) throws IllegalArgumentException;
}
