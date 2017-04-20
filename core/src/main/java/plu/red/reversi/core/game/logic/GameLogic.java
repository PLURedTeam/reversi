package plu.red.reversi.core.game.logic;

import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.command.SetCommand;
import plu.red.reversi.core.game.Board;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.listener.IBoardUpdateListener;
import plu.red.reversi.core.listener.IBoardUpdateListener.BoardUpdate;

import java.security.InvalidParameterException;
import java.util.*;

/**
 * GameLogic is responsible for handling a set of games rules. Each specific game will be
 * a subclass implementing the abstract functions.
 *
 * This is designed as a semi-singleton class, it has to have instances to support inheritance,
 * but you should not need to construct GameLogic class except at the beginning of a new game.
 * This class will update history with new changes, and update listeners as needed. Pay
 * attention to the defaults, and how it will automatically reference Game.
 *
 * This will hold a reference to board. All modifications to board (after initialization)
 * should go through this class to validate the actions with the game rules.
 *
 * For any change made to the board, the registered IBoardUpdateListeners will be updated
 * by default, and you can manually specify otherwise in each case.
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
     * Constructs a new cache objcet of the appropriate subtype.
     * @return A new GameLogic cache of the appropriate subtype.
     */
    public abstract GameLogicCache createCache();


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
     * @see BoardUpdate
     * @return This object for chaining.
     */
    protected final GameLogic updateBoardListeners(BoardUpdate update) {
        for (IBoardUpdateListener i : boardUpdateListeners)
            i.onBoardUpdate(update);

        return this;
    }


    /**
     * Apply multiple commands at once. Used when you have a saved game state you wish to restore.
     * NOTE: This will note update history.
     * @param commands List of commands to be applied in order.
     * @return This object for chaining.
     */
    public final GameLogic initBoard(Collection<BoardCommand> commands) {
        //since the collection will be from a history object, don't update histroy
        return initBoard(game.getGameCache(), game.getBoard(), commands, true, false);
    }


    /**
     * Apply multiple commands at once. Used when you have a saved game state you wish to restore.
     * @param commands List of commands to be applied in order.
     * @param board Board to apply the commands to.
     * @param notify True if this should notify subscribed listeners.
     * @param record True if this should update the game history.
     * @return This object for chaining.
     */
    public final GameLogic initBoard(GameLogicCache cache, Board board, Collection<BoardCommand> commands, boolean notify, boolean record) {
        for(BoardCommand c: commands) {
            if(c instanceof MoveCommand)
                play(cache, board, (MoveCommand)c, notify, record);
            if(c instanceof SetCommand)
                apply(cache, board, (SetCommand)c, notify, record);
        }
        return this;
    }


    /**
     * Initialize the board with the appropriate initial game state. Used for new games.
     * @return This object for chaining.
     */
    public final GameLogic initBoard() {
        return initBoard(game.getGameCache(), game.getBoard(), Arrays.stream(game.getUsedPlayers()).mapToInt(Integer::intValue).toArray(), true, true);
    }


    /**
     * Initialize the board with the appropriate initial game state. Used for new games.
     * @param players Array of the player ids used in current game in order.
     * @param board Board to apply commands to.
     * @param notify True if this should notify subscribed listeners.
     * @param record True if this should update the game history.
     * @return This object for chaining.
     */
    public final GameLogic initBoard(GameLogicCache cache, Board board, int[] players, boolean notify, boolean record) {
        Collection<SetCommand> commands = getSetupCommands(players, board.size);
        for(SetCommand c : commands)
            apply(cache, board, c, notify, record);

        return this;
    }


    /**
     * Sets a tile on the board without considering legality or playing out the move.
     * @param command Command specifying a location and its new player value.
     * @return This object for chaining.
     */
    public final GameLogic apply(SetCommand command) {
        return apply(game.getGameCache(), game.getBoard(), command, true, true);
    }


    /**
     * Sets a tile on the board without considering legality or playing out the move.
     * @param command Command specifying a location and its new player value.
     * @param board Board to apply commands to.
     * @param notify True if this should notify subscribed listeners.
     * @param record True if this should update the game history.
     * @return This object for chaining.
     */
    public final GameLogic apply(GameLogicCache cache, Board board, SetCommand command, boolean notify, boolean record) {
        ReversiLogicCache rcache = (ReversiLogicCache)cache;
        if(rcache == null) throw new InvalidParameterException("Invalid cache passed to apply in ReversiLogic.");

        rcache.addToScore(board.at(command.position), -1); //decrement score of old player
        board.apply(command);
        rcache.addToScore(command.playerID, 1); //inc new player score

        if(notify) {
            BoardUpdate update = new BoardUpdate();
            update.player = command.playerID;
            update.added.add(command.position);
            updateBoardListeners(update);
        }
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
        return play(game.getGameCache(), game.getBoard(), command, true, true);
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
    public abstract GameLogic play(GameLogicCache cache, Board board, MoveCommand command, boolean notify, boolean record) throws InvalidParameterException;


    /**
     * Checks the board to see if the move attempted is valid.
     * Prefer calling play and handling an exception than checking
     * if it is valid first.
     *
     * @param command Includes player and board index.
     */
    public final boolean isValidMove(MoveCommand command) {
        return isValidMove(game.getGameCache(), game.getBoard(), command);
    }


    /**
     * Checks the board to see if the move attempted is valid.
     * Prefer calling play and handling an exception than checking
     * if it is valid first.
     *
     * @param command Includes player and board index.
     * @param board Board to apply commands to.
     */
    public abstract boolean isValidMove(GameLogicCache cache, Board board, MoveCommand command);


    /**
     * Find the different moves that could be made and return them.
     *
     * @param player Integer Player ID to check for
     * @return ArrayList moves
     */
    public final Set<BoardIndex> getValidMoves(int player) {
        return getValidMoves(game.getGameCache(), game.getBoard(), player);
    }


    /**
     * Find the different moves that could be made and return them.
     *
     * @param player Integer Player ID to check for
     * @param board Board to apply commands to.
     * @return ArrayList moves
     */
    public abstract Set<BoardIndex> getValidMoves(GameLogicCache cache, Board board, int player);


    /**
     * Checks if the given player has at least one available move. Prefer using this to getValidMoves as it can be
     * much more efficient when only checking true/false.
     * @param player The player to check.
     * @return True if the player is able to play, else false.
     */
    public final boolean canPlay(int player) {
        return canPlay(game.getGameCache(), game.getBoard(), player);
    }


    /**
     * Checks if the given player has at least one available move. Prefer this over getValidMoves as this can be
     * much more efficient when only checking true/false.
     * @param player The player to check.
     * @param board Board to apply commands to.
     * @return True if the player is able to play, else false.
     */
    public boolean canPlay(GameLogicCache cache, Board board, int player) {
        BoardIndex index = new BoardIndex();
        for(index.row = 0; index.row < board.size; index.row++)
            for(index.column = 0; index.column < board.size; index.column++)
                if(isValidMove(cache, board, new MoveCommand(player, index)))
                    return true;
        return false;
    }


    /**
     * Returns the score of the Player ID passed in
     * @param player Integer Player ID
     * @return Score for the given player.
     */
    public final int getScore(int player) {
        return getScore(game.getGameCache(), game.getBoard(), player);
    }


    /**
     * Returns the score of the Player ID passed in
     * @param player Integer Player ID.
     * @param board Board to apply commands to.
     * @return Score for the given player.
     */
    public abstract int getScore(GameLogicCache cache, Board board, int player);


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
     * @param size of board for which to generate the setup commands for.
     * @return List of the moves to be made to create the initial state.
     * @throws IllegalArgumentException If player count is invalid.
     */
    public abstract Collection<SetCommand> getSetupCommands(int[] players, int size) throws IllegalArgumentException;
}
