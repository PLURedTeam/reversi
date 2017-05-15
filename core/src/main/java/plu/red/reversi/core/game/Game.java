package plu.red.reversi.core.game;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.core.Controller;
import plu.red.reversi.core.Coordinator;
import plu.red.reversi.core.IMainGUI;
import plu.red.reversi.core.SettingsLoader;
import plu.red.reversi.core.command.*;
import plu.red.reversi.core.db.DBUtilities;
import plu.red.reversi.core.game.logic.GameLogic;
import plu.red.reversi.core.game.logic.GameLogicCache;
import plu.red.reversi.core.game.logic.GoLogic;
import plu.red.reversi.core.game.logic.ReversiLogic;
import plu.red.reversi.core.game.player.HumanPlayer;
import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.listener.IGameOverListener;
import plu.red.reversi.core.listener.IListener;
import plu.red.reversi.core.util.ChatMessage;
import plu.red.reversi.core.util.DataMap;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Glory to the Red Team.
 *
 * Game object to represent the Reversi game as a whole. Contains references to all other objects
 * required to play a game of Reversi, such as Players, the Board, and Settings. As as a Coordinator
 * object for a running game.
 */
public class Game extends Coordinator {

    /**
     * Notifies that the Game has ended. Iterates through and tells every IGameOverListener that has been
     * registered to this Game that the Game has ended and a winner has been chosen.
     *
     * @param player Player that won the game; can be null if no Player won
     * @param score The winning score of the game; can be 0 if no Player won
     */
    protected final void notifyGameOverListeners(Player player, int score) {
        synchronized(listenerSet) {
            for (IListener listener : listenerSet)
                if (listener instanceof IGameOverListener) ((IGameOverListener) listener).onGameOver(player, score);
        }
        synchronized(listenerSetStatic) {
            for (IListener listener : listenerSetStatic)
                if (listener instanceof IGameOverListener) ((IGameOverListener) listener).onGameOver(player, score);
        }
    }

    /**
     * Creates a new Game object and loads data from the Database into it. Does not initialized the Game object.
     *
     * @param gui IMainGUI object that displays for the program
     * @param gameID ID of Game in Database
     * @return New Game object
     */
    public static Game loadGameFromDatabase(IMainGUI gui, int gameID) {
        Game game = new Game(Controller.getInstance(), gui);
        game
                .setGameID(gameID)
                .setHistory(DBUtilities.INSTANCE.loadGame(gameID))
                .setSettings(new DataMap(DBUtilities.INSTANCE.loadGameSettings(gameID)));
        DBUtilities.INSTANCE.loadGamePlayers(game);

        GameLogic.Type type = GameLogic.Type.values()[DBUtilities.INSTANCE.loadGameType(gameID)];
        switch(type) {
            case REVERSI:
                game.setLogic(new ReversiLogic(game));
                break;
            case GO:
                game.setLogic(new GoLogic(game));
                break;
            default:

        }

        return game;
    }



    // ******************
    //  Member Variables
    // ******************

    // Model Objects
    protected DataMap settings = null;
    protected Board board = null;
    protected GameLogic gameLogic = null;
    protected GameLogicCache gameCache = null;
    protected History history = null;

    // Player Data
    protected int currentPlayer = -1;
    protected final HashMap<Integer, Player> players = new HashMap<>();
    protected final HashSet<Integer> surrenderedPlayers = new HashSet<>();

    // State Flags
    protected boolean gameInitialized = false;
    protected boolean gameRunning = true;
    private boolean gameSaved = false;
    protected final boolean host;

    // Game Identification
    private int gameID = -1;
    protected final boolean networked;
    protected final String name;



    // *******************
    //  Assertion Methods
    // *******************
    // These are used internally to assert certain conditions during runtime.

    protected final void assertInitialized() {
        if(!gameInitialized) throw new IllegalStateException("Game must be initialized before using this method.");
    }

    protected final void assertNotInitialized() {
        if(gameInitialized) throw new IllegalStateException("Method can only be used with an uninitialized Game.");
    }


    // ****************
    //  Member Methods
    // ****************

    /**
     * Helper method used to get the next highest ID from a set of IDs.
     *
     * @param ID Integer ID to start search from
     * @param usedIDs Set of Integer IDs to search in
     * @return Next highest ID, or lowest ID if the given ID was already the highest
     */
    public static int getNextPlayerID(int ID, Set<Integer> usedIDs) {

        // Get the next highest ID
        int nextID = Integer.MAX_VALUE;
        boolean changed = false;
        for(Integer i : usedIDs) {
            if (i > ID && i < nextID) {
                nextID = i;
                changed = true;
            }
        }

        if(changed) return nextID; // Got the ID, return it
        else if(ID < 0) return -1; // Already restarted once, just return invalid
        else return getNextPlayerID(-1, usedIDs); // Restart the count from 0
    }

    /**
     * Helper method used to get the next highest ID from this Game's used Player IDs.
     *
     * @param ID Integer ID to start search from
     * @return Next highest ID, or lowest ID if the given ID was already the highest
     */
    public int getNextPlayerID(int ID) {
        return getNextPlayerID(ID, players.keySet());
    }

    /**
     * Constructor. Creates a new blank Game object. Said object then needs to have parts set to it (such as a
     * DataMap and Players) and then be initialized.
     *
     * @param master Controller object that is the master Controller for this program
     * @param gui IMainGUI object that displays for the program
     */
    public Game(Controller master, IMainGUI gui) {
        this(master, gui, false, "Local Game");
    }

    /**
     * Full Constructor. Creates a new blank Game object. Said object then needs to have parts set to it (such as a
     * DataMap and Players) and then be initialized.
     *
     * @param master Controller object that is the master Controller for this program
     * @param gui IMainGUI object that displays for the program
     * @param networked Whether the Game is singleplayer or multiplayer
     * @param name String <code>name</code> of the Game
     */
    public Game(Controller master, IMainGUI gui, boolean networked, String name) {
        super(master, gui);
        this.networked = networked;
        this.name = name;
        this.host = true;
    }

    /**
     * Set this Game's GameLoic to the given object. Used to specifiy what type of game is being played. e.g. reversi.
     * @param logic Logic class to be used for the game.
     * @return Reference to this Game object for chain-construction.
     */
    public Game setLogic(GameLogic logic) {
        assertNotInitialized();
        gameLogic = logic;
        return this;
    }

    /**
     * Sets this Game's <code>settings</code> to the given DataMap object. This operation must be performed before a
     * Game can be initialized, and cannot be performed afterwards.
     *
     * @param settings DataMap to use
     * @return Reference to this Game object for chain-construction
     * @throws IllegalStateException if the Game has already been initialized
     */
    public Game setSettings(DataMap settings) throws IllegalStateException {
        assertNotInitialized();
        this.settings = settings;
        return this;
    }

    /**
     * Sets this Game's History to the given History object. This operation can only be performed before a Game is
     * initialized, but unlike <code>setSettings()</code> does not need to be performed in order to initialize a Game.
     *
     * @param history History to use
     * @return Reference to this Game object for chain-construction
     * @throws IllegalStateException if the Game has already been initialized
     */
    public Game setHistory(History history) throws IllegalStateException {
        assertNotInitialized();
        this.history = history;
        return this;
    }

    /**
     * Sets the Game's <code>gameID</code> to the given ID. This operation can only be performed before a Game is
     * initialized, but unlike <code>setSettings()</code> does not need to be performed in order to initialize a game.
     *
     * @param gameID Integer ID to use
     * @return Reference to this Game object for chain-construction
     * @throws IllegalStateException if the Game has already been initialized
     */
    public Game setGameID(int gameID) throws IllegalStateException {
        assertNotInitialized();
        this.gameID = gameID;
        return this;
    }

    /**
     * Initialization method. Initializes the Game after it has been loaded with all required data. Data loading methods
     * are as follows:
     *
     * [required] <code>setSettings()</code>
     * [optional] <code>setHistory()</code>
     * [optional] <code>setGameID()</code>
     *
     * @throws IllegalStateException if not all required data has been set (such as a DataMap for <code>settings</code>)
     */
    public void initialize() throws IllegalStateException {

        if(settings == null) throw new IllegalStateException("A DataMap has not been set!");
        if(gameLogic == null) throw new IllegalStateException("GameLogic has not been determined!");
        if(players.isEmpty()) throw new IllegalStateException("No Players have been registered!");

        board = new Board(settings.get(SettingsLoader.GAME_BOARD_SIZE, Integer.class));
        gameCache = gameLogic.createCache();

        // Ensure a History exists and setup the Board
        //History after these if conditions will be updated by GameLogic
        if(history == null) {
            history = new History();
            gameLogic.initBoard();

            // Get first player
            if(history.getNumBoardCommands() < 1)
                currentPlayer = getNextPlayerID(-1, players.keySet());
            else {
                LinkedList<BoardCommand> cmds = history.getMoveCommandsUntil(history.getNumBoardCommands());
                currentPlayer = getNextPlayerID(cmds.getLast().playerID);
            }
        } else if(history.getNumBoardCommands() < 1)
            currentPlayer = getNextPlayerID(-1, players.keySet());
        else {
            LinkedList<BoardCommand> cmds = history.getMoveCommandsUntil(history.getNumBoardCommands());
            gameLogic.initBoard(cmds);
            currentPlayer = getNextPlayerID(cmds.getLast().playerID);
        }

        // Create Game Chat
        if(networked && master != null && master.getChat() != null)
            master.getChat().create(ChatMessage.Channel.game(name));

        gameInitialized = true;

        // Start the Game by signalling to players
        for(Player player : players.values()) player.nextTurn(player.getID() == currentPlayer);
    }

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
     * Retrieves the GameLogic by which the current game is operating by.
     *
     * @return this Game's GameLogic
     */
    public GameLogic getGameLogic() { return gameLogic; }

    /**
     * Retrieves the GameLogicCache which holds information about the current game state.
     *
     * @return this Game's GameLogicCache
     */
    public GameLogicCache getGameCache() { return gameCache; }

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
    public int getPlayerCount() { return players.size(); }

    /**
     * Retrieves an array of used Player IDs in this Game (Human or otherwise).
     *
     * @return this Game's Player IDs
     */
    public Integer[] getUsedPlayers() { return players.keySet().toArray(new Integer[]{}); }

    /**
     * Retrieves an array of the Players in this Game (Human or otherwise).
     *
     * @return this Game's Players
     */
    public Player[] getAllPlayers() { return players.values().toArray(new Player[]{}); }

    /**
     * Retrieves an array of Player IDs that have surrendered.
     *
     * @return this Game's surrendered Player IDs
     */
    public Integer[] getSurrenderedPlayers() { return surrenderedPlayers.toArray(new Integer[]{}); }

    /**
     * Registers a Player with this Game object. This method can ONLY be used from within the Player class (and not
     * even in subclasses of Player). This ensures that Players are not registered willy-nilly, and that it is not
     * possible to register Players with conflicting IDs. This method can also only be invoked before a Game is
     * initialized, so if a Player is constructed with this Game afterwards, an IllegalStateException will be thrown.
     *
     * @param player Player object to register
     * @param id Internal Player.ID object that only Player can construct; used as a key to ensure only Player can
     *           call this method
     * @throws IllegalStateException if the Game has already been initialized
     */
    public void registerPlayer(Player player, Player.ID id) throws IllegalStateException {
        assertNotInitialized();

        // Make sure that this player has not already been attempted to be registered. Should not be possible, but just in case.
        for(Player p : players.values())
            if(p == player) return; // Looking for an exact reference copy, not an equivalency

        // Set the ID object's proper ID if it hasn't already been set
        // IE sets it if this is a new game, or leaves it alone if this is a loaded game
        if(id.get() < 0) id.set(players.size());
        players.put(player.getID(), player);
    }

    /**
     * Retrieves the Player stored for the given Integer ID.
     *
     * @param playerID Integer ID to determine player with
     * @return Player stored for the given <code>playerID</code>, or <code>null</code> if no Player is stored or the
     *         <code>playerID</code> is invalid
     */
    public Player getPlayer(int playerID) {
        return players.get(playerID);
    }

    /**
     * Retrieves the Player whose turn it currently is.
     *
     * @return Current Player
     */
    public Player getCurrentPlayer() {
        return players.get(currentPlayer);
    }

    /**
     * Cycles turns and increments what player is current.
     *
     * @return Player whose turn is current after incrementing
     */
    public Player nextTurn() {
        if(!gameRunning) return players.get(currentPlayer);
        // Call internal helper recursive method
        return nextTurn(0);
    }

    /**
     * Internal helper method for recursively incrementing the turn state.
     *
     * @param skipCount Recursive Integer argument representing how many turns we've already skipped in this recursive loop
     * @return Player eventually found to have the next turn
     */
    private Player nextTurn(int skipCount) {

        // Check to see if we need to end immediately
        if(skipCount > players.size()) {
            endGame();
            return players.get(currentPlayer);
        }

        // Iterate once through the turn loop
        currentPlayer = getNextPlayerID(currentPlayer);
        //for(Player player : players.values()) player.nextTurn(player.getRole() == currentPlayerColor);

        // Check if the current player has already surrendered
        if(surrenderedPlayers.contains(currentPlayer)) nextTurn(skipCount + 1);

        // Check if its even possible for this Player to play
        else if(!gameLogic.canPlay(currentPlayer)) {
            if (settings.get(SettingsLoader.GAME_ALLOW_TURN_SKIPPING, Boolean.class))
                nextTurn(skipCount + 1); // Skip to the next Player's turn
            else endGame();
        }

        // Notify everyone we found a Player and return that Player
        for(Player player : players.values()) player.nextTurn(player.getID() == currentPlayer);
        return players.get(currentPlayer);
    }

    /**
     * Parse Command for action. Check the type of Command given and perform an action dependant upon that type.
     *
     * @param cmd Command object to parse
     * @return True if the Command's actions were successful
     */
    @Override
    protected boolean parseCommand(Command cmd) {

        // Send Move Commands to the Board object
        if(cmd instanceof MoveCommand) {
            if(!gameRunning) return false;
            try {
                gameLogic.play((MoveCommand)cmd);
                nextTurn();
            } catch(InvalidParameterException e) {
                //System.err.println(e.getMessage());
            }
        }

        // Listen to Surrender Commands
        if(cmd instanceof SurrenderCommand) {
            if(!gameRunning) return false;
            int player = ((SurrenderCommand)cmd).playerID;
            surrenderedPlayers.add(player);
            if(players.size() - surrenderedPlayers.size() < 2) endGame(); // End the Game if theres only one player left
            else if(player == currentPlayer) nextTurn();
            history.addCommand(cmd);
        }

        return true;
    }

    /**
     * End this Game, and determine the winner. Afterwards, signal to all IGameOverListeners that the Game has ended.
     */
    public void endGame() {
        
        // Find the winner
        int winner = -1;
        int score = -1;
        for(int player : players.keySet()) {
            // Surrendered Players aren't in the running for winning :P
            if(surrenderedPlayers.contains(player)) continue;
            int ss = gameLogic.getScore(player);
            if(ss > score) {
                score = ss;
                winner = player;
            }
        }

        gameRunning = false;
        this.acceptCommand(new StatusCommand("Game Over!"));

        // Signal Listeners that the Game has ended
        notifyGameOverListeners(getPlayer(winner), score);
    }

    /**
     * Determines whether or not this game is still active and being played.
     *
     * @return true if this game is still running, false if it has ended
     */
    public boolean isGameOver() { return !gameRunning; }

    /**
     * Determines whether or not this game has been initialized yet.
     *
     * @return true if this game has been initialized
     */
    public boolean isInitialized() { return gameInitialized; }

    /**
     * Determines whether or not this game is the host game for a multiplayer session. Always <code>true</code> for
     * singleplayer.
     *
     * @return <code>true</code> if this game is the host game, <code>false</code> otherwise
     */
    public boolean isHost() { return host; }

    /**
     * GameID getter. Retrieves this Game's <code>gameID</code>.
     *
     * @return Integer <code>gameID</code> of this Game
     */
    public int getGameID() { return gameID; }

    /**
     * Determines whether or not this game has been saved yet.
     *
     * @return True if game saved, false otherwise
     */
    public boolean getGameSaved() { return gameSaved; }

    /**
     * GameSaved setter. Sets whether or not this game has been saved yet.
     */
    public void setGameSaved(boolean gs) { gameSaved = gs; }

    /**
     * Networked Getter. Determines whether or not this Game is a singleplayer or multiplayer Game.
     *
     * @return <code>true</code> if this Game is networked/multiplayer, <code>false</code> otherwise
     */
    public boolean isNetworked() { return networked; }

    /**
     * Name Getter. Retrieves the <code>name</code> of this Game.
     *
     * @return String <code>name</code>
     */
    public String getName() { return name; }

    /**
     * Perform any cleanup operations that are needed, such as removing listeners that are not automatically cleaned up.
     */
    public void cleanup() {

        // Unregister HumanPlayer ISettingsListeners to avoid reference leaks
        for(Player player : getAllPlayers()) {
            if(player instanceof HumanPlayer)
                SettingsLoader.INSTANCE.removeSettingsListener((HumanPlayer)player);
        }

        // Clear Game Chat
        master.getChat().clear(ChatMessage.Channel.game(name));

        // TODO: Manually stop any running BotPlayer Minimax threads
    }

    /**
     * Serialize this Game. Takes the data of this Game object and serializes it into a DataMap.
     *
     * @return Serialized DataMap
     */
    public DataMap serialize() {
        DataMap data = new DataMap();

        // Model Objects
        data.set("settings",    settings);
        data.set("board",       board);
        data.set("history",     history);
        data.set("type",        gameLogic.getType().ordinal());

        // Player Data
        data.set("playerCount", players.size());
        int i = 0;
        for(Player player : players.values())
            try { data.set("player"+(i++), player.toJSON()); }
            catch(JSONException ex) { throw new RuntimeException(ex.getMessage()); }

        // State Flags
        data.set("initialized", gameInitialized);
        data.set("running",     gameRunning);
        data.set("saved",       gameSaved);
        data.set("networked",   networked);
        data.set("name",        name);

        // ID
        data.set("id", gameID);

        return data;
    }

    public Game(Controller master, IMainGUI gui, DataMap data) {
        super(master, gui);

        // Model Objects
        settings    = data.get("settings",  DataMap.class);
        board       = data.get("board",     Board.class);
        history     = data.get("history",   History.class);
        int ord     = data.get("type",      Integer.class);
        GameLogic.Type type = GameLogic.Type.values()[ord];
        switch(type) {
            case REVERSI:   gameLogic = new ReversiLogic(this); break;
            case GO:        gameLogic = new GoLogic(this); break;
            default:    throw new IllegalArgumentException("Unknown GameLogic Type '"+type+"'");
        }

        // State Flags
        gameRunning     = data.get("running",       Boolean.class);
        gameSaved       = data.get("saved",         Boolean.class);
        networked       = data.get("networked",     Boolean.class);
        name            = data.get("name",          String.class);

        // Player Data
        int pc = data.get("playerCount", Integer.class);
        for(int i = 0; i < pc; i++)
            try { Player.unserializePlayer(this, data.get("player"+i, JSONObject.class),networked); }
            catch(JSONException ex) { throw new RuntimeException(ex.getMessage()); }

        gameInitialized = data.get("initialized",   Boolean.class);

        // ID
        gameID = data.get("id", Integer.class);

        this.host = false;

        // Start the Game
        if(gameInitialized) this.initialize();
    }

}
