package plu.red.reversi.core;

import java.util.HashSet;

/**
 * Glory to the Red Team.
 *
 * Game object to represent the Reversi game as a whole. Contains references to all other objects
 * required to play a game of Reversi, such as Players, the Board, and Settings.
 */
public class Game {



    // ******************
    //  Member Variables
    // ******************

    protected SettingsMap settings;
    protected Board board;

    // Store players as an array of possible roles. More extensible for possibly more than two players in the future.
    //  (I realize this is probably unnecessary, but it results in more extensible code, and is easier to manipulate
    //   as a whole, instead of manipulating individual Player references)
    protected Player[] players = new Player[PlayerRole.validPlayers().length];
    protected PlayerRole currentPlayerRole = PlayerRole.validPlayers()[0];
    protected HashSet<PlayerRole> usedPlayers = new HashSet<PlayerRole>();



    /**
     * Constructor. Creates a new Game object.
     *
     * @param settings SettingsMap to create Game with
     * @param playerCount Number of players to play this Game with
     * @throws IllegalArgumentException if playerCount is less than 2 or more than the maximum valid PlayerRole count
     */
    public Game(SettingsMap settings, int playerCount) throws IllegalArgumentException {
        if(playerCount < 2 || playerCount >= PlayerRole.validPlayers().length)
            throw new IllegalArgumentException("Amount of Players for a game must be between 2 and " + PlayerRole.validPlayers().length);
        this.settings = settings;
        this.board = new Board(8);
        for(int i = 0; i < playerCount; i++) usedPlayers.add(PlayerRole.validPlayers()[i]);
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
        players[player.getRole().validOrdinal()] = player;
    }

    /**
     * Retrieves the player stored for the given role.
     *
     * @param role PlayerRole to determine Player with
     * @return Player stored for the given role, or null if no Player is stored or the PlayerRole is invalid
     */
    public Player getPlayer(PlayerRole role) {
        if(role.isValid()) return players[role.validOrdinal()];
        else return null;
    }

    /**
     * Retrieves the player whose turn it currently is.
     *
     * @return Current Player
     */
    public Player getCurrentPlayer() {
        return players[currentPlayerRole.validOrdinal()];
    }

    /**
     * Cycles turns and increments what player is current.
     *
     * @return Player whose turn is current after incrementing
     */
    public Player nextTurn() {
        currentPlayerRole = currentPlayerRole.getNext(usedPlayers);
        for(Player player : players) player.nextTurn(player.getRole() == currentPlayerRole);
        return players[currentPlayerRole.validOrdinal()];
    }

    public boolean acceptCommand(Command cmd) {

        // Check to see if this Command is ok to apply and/or send to the server
        if(!cmd.isValid(this)) return false;

        // Propogate the Command to the servere if it came from a player
        if(cmd.source == Command.Source.PLAYER) {
            // TODO: Send Command to Server
        }

        // Send Move Commands to the Board object
        if(cmd instanceof CommandMove) board.apply((CommandMove)cmd);

        // Send Chat Commands somewhere
        if(cmd instanceof CommandChat) {
            // TODO: Send Chat Command wherever it needs to go
        }

        return true;
    }
}
