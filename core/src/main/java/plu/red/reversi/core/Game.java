package plu.red.reversi.core;

import plu.red.reversi.core.command.*;
import plu.red.reversi.core.listener.ICommandListener;
import plu.red.reversi.core.listener.IGameOverListener;
import plu.red.reversi.core.listener.IStatusListener;
import plu.red.reversi.core.player.Player;
import plu.red.reversi.core.util.DataMap;
import plu.red.reversi.core.db.DBUtilities;

import java.util.*;

/**
 * Glory to the Red Team.
 *
 * Game object to represent the Reversi game as a whole. Contains references to all other objects
 * required to play a game of Reversi, such as Players, the Board, and Settings.
 */
public class Game {

    // ***********
    //  Listeners
    // ***********

    // This listener exists only because of the separation of Core and Client packages, which allows for only one-way
    //  communication between classes in different modules
    protected HashSet<ICommandListener> listenerSetCommands = new HashSet<ICommandListener>();
    protected HashSet<IGameOverListener> listenerSetGameOver = new HashSet<IGameOverListener>();
    protected HashSet<IStatusListener> listenerSetStatus = new HashSet<IStatusListener>();

    /**
     * Registers an ICommandListener that will have signals sent to it when Commands are applied.
     *
     * @param listener ICommandListener to register
     */
    public void addCommandListener(ICommandListener listener) {
        listenerSetCommands.add(listener);
    }

    /**
     * Registers an IGameOverListener that will have signals sent to it when the Game ends.
     *
     * @param listener IGameOverListener to register
     */
    public void addGameOverListener(IGameOverListener listener) {
        listenerSetGameOver.add(listener);
    }

    /**
     * Registers an IStatusListener that will have signals sent to it when a status message occurs.
     *
     * @param listener IStatusListener to register
     */
    public void addStatusListener(IStatusListener listener) {
        listenerSetStatus.add(listener);
    }

    /**
     * Unregisters an existing ICommandListener that has previously been registered. Does nothing if the specified
     * ICommandListener has not previously been registered.
     *
     * @param listener ICommandListener to unregister
     */
    public void removeCommandListener(ICommandListener listener) {
        listenerSetCommands.remove(listener);
    }

    /**
     * Unregisters an existing IGameOverListener that has previously been registered. Does nothing if the specified
     * IGameOverListener has not previously been registered.
     *
     * @param listener IGameOverListener to unregister
     */
    public void removeGameOverListener(IGameOverListener listener) {
        listenerSetGameOver.remove(listener);
    }

    /**
     * Unregisters an existing IStatusListener that has previously been registered. Does nothing if the specified
     * IStatusListener has not previously been registered.
     *
     * @param listener IStatusListener to unregister
     */
    public void removeStatusListener(IStatusListener listener) {
        listenerSetStatus.remove(listener);
    }



    /**
     * Creates a new Game object and loads data from the Database into it. Does not initialized the Game object.
     *
     * @param gameID ID of Game in Database
     * @return New Game object
     */
    public static Game loadGameFromDatabase(int gameID) {
        Game game = new Game();
        game
                .setGameID(gameID)
                .setHistory(DBUtilities.INSTANCE.loadGame(gameID))
                .setSettings(new DataMap(DBUtilities.INSTANCE.loadGameSettings(gameID)));
        ArrayList<Player> players = DBUtilities.INSTANCE.loadGamePlayers(game);
        for(Player player : players) game.setPlayer(player);
        return game;
    }



    // ******************
    //  Member Variables
    // ******************

    protected DataMap settings = null;
    protected Board board = null;
    protected History history = null;

    // Store players as an array of possible roles. More extensible for possibly more than two players in the future.
    //  (I realize this is probably unnecessary, but it results in more extensible code, and is easier to manipulate
    //   as a whole, instead of manipulating individual Player references)
    protected final HashMap<PlayerColor, Player> players = new HashMap<PlayerColor, Player>();
    protected PlayerColor currentPlayerColor = null;
    protected final HashSet<PlayerColor> usedPlayers = new HashSet<PlayerColor>();
    protected final HashSet<PlayerColor> surrenderedPlayers = new HashSet<PlayerColor>();

    protected boolean gameInitialized = false;
    protected boolean gameRunning = true;


    private int gameID = -1;



    // ****************
    //  Member Methods
    // ****************

    /**
     * Constructor. Creates a new blank Game object. Said object then needs to have parts set to it (such as a
     * DataMap and Players) and then be initialized.
     */
    public Game() {}

    /**
     * Sets this Game's DataMap to the given DataMap. This operation must be performed before a Game can be
     * initialized, and cannot be performed afterwards.
     *
     * @param settings DataMap to use
     * @return Reference to this Game object for chain-construction
     * @throws IllegalStateException if the Game has already been initialized
     */
    public Game setSettings(DataMap settings) throws IllegalStateException {
        if(gameInitialized) throw new IllegalStateException("Game has already been initialized!");
        this.settings = settings;
        return this;
    }

    /**
     * Sets this Game's History to the given History. This operation can only be performed before a Game is initialized,
     * but unlike setSettings() does not need to be performed in order to initialize a game.
     *
     * @param history History to use
     * @return Reference to this Game object for chain-construction
     * @throws IllegalStateException if the Game has already been initialized
     */
    public Game setHistory(History history) throws IllegalStateException {
        if(gameInitialized) throw new IllegalStateException("Game has already been initialized!");
        this.history = history;
        return this;
    }

    /**
     * Sets the Game's gameID to the given gameID. This operation can only be performed before a Game is initialized,
     * but unlike setSettings() does not need to be performed in order to initialize a game.
     *
     * @param gameID GameID to use
     * @return Reference to this Game object for chain-construction
     * @throws IllegalStateException if the Game has already been initialized
     */
    public Game setGameID(int gameID) throws IllegalStateException {
        if(gameInitialized) throw new IllegalStateException("Game has already been initialized");
        this.gameID = gameID;
        return this;
    }

    /**
     * Initialization method. Initializes the Game after it has been loaded with all required data. Data loading methods
     * are as follows:
     *
     * [required] setSettings()
     * [required] setPlayer() - repeatedly
     * [optional] setHistory()
     * [optional] setGameID()
     *
     * @throws IllegalStateException if not all required data has been set (such as a DataMap)
     */
    public void initialize() throws IllegalStateException {

        if(settings == null) throw new IllegalStateException("A DataMap has not been set!");

        board = new Board(settings.get(SettingsLoader.GAME_BOARD_SIZE, Integer.class));

        // Ensure a GameID exists
        if(gameID < 0) {
            gameID = DBUtilities.INSTANCE.createGame();
            DBUtilities.INSTANCE.saveGame(gameID);
            DBUtilities.INSTANCE.saveGamePlayers(gameID, players.values());
        }

        // Ensure a History exists and setup the Board
        if(history == null) {

            history = new History();
            board.applyCommands(Board.getSetupCommands(this));

            // Save the initial setup
            List<BoardCommand> setup = Board.getSetupCommands(this);
            for(BoardCommand cmd : setup) DBUtilities.INSTANCE.saveMove(gameID, cmd);
        } else {
            LinkedList<BoardCommand> cmds = history.getMoveCommandsUntil(history.getNumBoardCommands());
            board.applyCommands(cmds);
            currentPlayerColor = cmds.getLast().player.getNext(usedPlayers);
        }

        gameInitialized = true;

        // Start the Game by signalling to players
        for(Player player : players.values()) player.nextTurn(player.getRole() == currentPlayerColor);
    }

    /**
     * Loads a predefined History object and optionally applies it to this Game, stepping this Game through History
     * until the most recent point.
     *
     * @param history History object to apply
     */
    /*
    public void initialize(History history, int newGameID) {
        this.history = history;
        this.gameID = newGameID;

        ArrayList<Player> players = DBUtilities.INSTANCE.loadGamePlayers(this);
        for(Player player : players) setPlayer(player);
        
        board.applyCommands(history.getMoveCommandsUntil(history.getNumBoardCommands()));
    }
    */

    /**
     * Retrieves the DataMap that this Game object is using.
     *
     * @return this Game's DataMap
     */
    public DataMap getSettings() { return settings; }

    /**
     * Retrieves the Board that this Game object is using.
     *
     * @return this Game's Board
     */
    public Board getBoard() { return board; }

    /**
     * Retrieves the History that this Game object is using.
     *
     * @return this Game's History
     */
    public History getHistory() { return history; }

    /**
     * Retrieves the number of Players playing in this game (Human or otherwise).
     *
     * @return this Game's Player count
     */
    public int getPlayerCount() { return usedPlayers.size(); }

    /**
     * Retrieves the Set of used PlayerRoles in this game (Human or otherwise).
     *
     * @return this Game's Set of PlayerRoles
     */
    public Set<PlayerColor> getUsedPlayers() { return usedPlayers; }

    /**
     * Retrieves a Collection of the Players in this game (Human or otherwise).
     *
     * @return this Game's Collection of Players
     */
    public Collection<Player> getAllPlayers() { return players.values(); }

    /**
     * Retrieves the Set of PlayerRoles that have surrendered.
     *
     * @return this Game's Set of surrendered PlayerRoles
     */
    public Set<PlayerColor> getSurrenderedPlayers() { return surrenderedPlayers; }

    /**
     * Sets the player for a game. Player role is dependant on the player's stored role. Will overwrite any preexisting
     * stored players, in order to make it easy to switch out players mid-game. Should not be used to add more players
     * in the middle of a game.
     *
     * @param player Player object to set
     * @throws IllegalArgumentException if the given Player object does not have this Game object assigned to it, or if
     * the given Player object does not have a valid role.
     */
    public void setPlayer(Player player) throws IllegalArgumentException {
        if(!player.getRole().isValid()) throw new IllegalArgumentException("Player given to game has an invalid role; IE None");
        if(player.getGame() != this) throw new IllegalArgumentException("Player given to game does not have game assigned to it");
        players.put(player.getRole(), player);
        usedPlayers.add(player.getRole());
        if(currentPlayerColor == null) currentPlayerColor = player.getRole();
    }

    /**
     * Retrieves the player stored for the given role.
     *
     * @param role PlayerColor to determine Player with
     * @return Player stored for the given role, or null if no Player is stored or the PlayerColor is invalid
     */
    public Player getPlayer(PlayerColor role) {
        if(role.isValid()) return players.get(role);
        else return null;
    }

    /**
     * Retrieves the player whose turn it currently is.
     *
     * @return Current Player
     */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerColor);
    }

    /**
     * Cycles turns and increments what player is current.
     *
     * @return Player whose turn is current after incrementing
     */
    public Player nextTurn() {
        if(!gameRunning) return players.get(currentPlayerColor);
        // Call internal helper recursive method
        return nextTurn(0);
    }
    
    protected final Player nextTurn(int skipCount) {

        // Check to see if we need to end immediately
        if(skipCount > usedPlayers.size()) {
            endGame();
            return players.get(currentPlayerColor);
        }

        // Iterate once through the turn loop
        currentPlayerColor = currentPlayerColor.getNext(usedPlayers);
        for(Player player : players.values()) player.nextTurn(player.getRole() == currentPlayerColor);

        // Check if the current player has already surrendered
        if(surrenderedPlayers.contains(currentPlayerColor)) nextTurn(skipCount + 1);

        // Check if its even possible for this Player to play
        else if (board.getPossibleMoves(currentPlayerColor).isEmpty()) {
            if (settings.get(SettingsLoader.GAME_ALLOW_TURN_SKIPPING, Boolean.class))
                nextTurn(skipCount + 1); // Skip to the next Player's turn
            else endGame();
        }

        // Return the Player we settled on
        return players.get(currentPlayerColor);
    }

    public synchronized boolean acceptCommand(Command cmd) {

        // Check to see if this Command is ok to apply and/or send to the server
        if(!cmd.isValid(this)) return false;

        // Propagate the Command to the server if it came from a player
        if(cmd.source == Command.Source.PLAYER) {
            // TODO: Send Command to Server
        }

        // Send Move Commands to the Board object
        if(cmd instanceof BoardCommand) {
            if(!gameRunning) return false;
            board.apply((BoardCommand)cmd);
            DBUtilities.INSTANCE.saveMove(gameID, (BoardCommand)cmd);
            nextTurn();
        }

        // Send Chat Commands somewhere
        if(cmd instanceof ChatCommand) {
            // TODO: Send Chat Command wherever it needs to go
        }

        // Listen to Surrender Commands
        if(cmd instanceof SurrenderCommand) {
            if(!gameRunning) return false;
            PlayerColor player = ((SurrenderCommand)cmd).player;
            surrenderedPlayers.add(player);
            if(player == currentPlayerColor) nextTurn();
        }

        // Register the Command in History
        history.addCommand(cmd);

        // Signal Listeners that a Command has been applied
        for(ICommandListener listener : listenerSetCommands)
            listener.commandApplied(cmd);

        return true;
    }

    /**
     * End this Game, and determine the winner. Afterwards, signal to all IGameOverListeners that the Game has ended.
     */
    public void endGame() {
        
        // Find the winner
        PlayerColor winner = PlayerColor.NONE;
        int score = -1;
        for(PlayerColor color : usedPlayers) {
            // Surrendered Players aren't in the running for winning :P
            if(surrenderedPlayers.contains(color)) continue;
            int ss = board.getScore(color);
            if(ss > score) {
                score = ss;
                winner = color;
            }
        }

        gameRunning = false;

        this.statusMessage("Game Over!");

        // Signal Listeners that the Game has ended
        for(IGameOverListener listener : listenerSetGameOver)
            listener.onGameOver(getPlayer(winner), score);
    }

    /**
     * Determines whether or not this game is still active and being played.
     *
     * @return true if this game is still running, false if it has ended
     */
    public boolean isGameOver() {
        return !gameRunning;
    }

    /**
     * Determines whether or not this game has been initialized yet.
     *
     * @return true if this game has been initialized
     */
    public boolean isInitialized() { return gameInitialized; }

    /**
     * Passes a status message to all IStatusListeners registered to this Game.
     *
     * @param message String message to pass
     */
    public void statusMessage(String message) {
        for(IStatusListener listener : listenerSetStatus)
            listener.onStatusMessage(message);
    }

    /**
     * Accessor for the gameID field
     * @return the gameID
     */
    public int getGameID() { return gameID; }



}
