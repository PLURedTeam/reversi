package plu.red.reversi.core;

import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.command.ChatCommand;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.listener.ICommandListener;
import plu.red.reversi.core.player.Player;
import plu.red.reversi.core.util.SettingsMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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

    /**
     * Registers an ICommandListener that will have signals sent to it when Commands are applied.
     *
     * @param listener ICommandListener to register
     */
    public void addCommandListener(ICommandListener listener) {
        listenerSetCommands.add(listener);
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



    // ******************
    //  Member Variables
    // ******************

    protected SettingsMap settings;
    protected Board board;
    protected History history;

    // Store players as an array of possible roles. More extensible for possibly more than two players in the future.
    //  (I realize this is probably unnecessary, but it results in more extensible code, and is easier to manipulate
    //   as a whole, instead of manipulating individual Player references)
    protected final HashMap<PlayerColor, Player> players = new HashMap<PlayerColor, Player>();
    protected PlayerColor currentPlayerColor = null;
    protected final HashSet<PlayerColor> usedPlayers = new HashSet<PlayerColor>();



    // ****************
    //  Member Methods
    // ****************

    /**
     * Constructor. Creates a new Game object with given settings and default board size of 8.
     *
     * @param settings SettingsMap to start Game with
     */
    public Game(SettingsMap settings) {
        this(settings, 8);
    }

    /**
     * Constructor. Creates a new Game object with given settings and board size.
     *
     * @param settings SettingsMap to start Game with
     * @param boardSize Size of the board to create for this Game
     */
    public Game(SettingsMap settings, int boardSize) {
        this.settings = settings;
        this.board = new Board(boardSize);
        this.history = new History();
    }

    /**
     * Initialization method. To be used after the board has been loaded with all required objects, such as players
     * and settings being used.
     */
    public void initialize() {
        board.setupBoard(this);
    }

    /**
     * Loads a predefined History object and optionally applies it to this Game, stepping this Game through History
     * until the most recent point.
     *
     * @param history History object to apply
     */
    public void loadHistory(History history, boolean apply) {
        this.history = history;
        if(apply) {
            // TODO: Apply History to this game
        }
    }

    /**
     * Retrieves the SettingsMap that this Game object is using.
     *
     * @return this Game's SettingsMap
     */
    public SettingsMap getSettings() { return settings; }

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
     * Sets the player for a game. Player role is dependant on the player's stored role. Will overwrite any preexisting
     * stored players, in order to make it easy to switch out players mid-game.
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
        currentPlayerColor = currentPlayerColor.getNext(usedPlayers);
        for(Player player : players.values()) player.nextTurn(player.getRole() == currentPlayerColor);
        return players.get(currentPlayerColor);
    }

    public boolean acceptCommand(Command cmd) {

        // Check to see if this Command is ok to apply and/or send to the server
        if(!cmd.isValid(this)) return false;

        // Propagate the Command to the servere if it came from a player
        if(cmd.source == Command.Source.PLAYER) {
            // TODO: Send Command to Server
        }

        // Send Move Commands to the Board object
        if(cmd instanceof MoveCommand) {
            board.apply((MoveCommand)cmd);
            nextTurn();
        }

        // Send Chat Commands somewhere
        if(cmd instanceof ChatCommand) {
            // TODO: Send Chat Command wherever it needs to go
        }

        // Register the Command in History
        history.addCommand(cmd);

        // Signal Listeners that a Command has been applied
        for(ICommandListener listener : listenerSetCommands)
            listener.commandApplied(cmd);

        return true;
    }
}
