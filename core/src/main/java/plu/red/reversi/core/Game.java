package plu.red.reversi.core;

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



    /**
     * Constructor. Creates a new Game object.
     *
     * @param settings SettingsMap to create Game with
     * @param board Board to create Game with
     */
    public Game(SettingsMap settings, Board board) {
        this.settings = settings;
        this.board = board;
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
     * Retrievs the player stored for the given role.
     *
     * @param role PlayerRole to determine Player with
     * @return Player stored for the given role, or null if no Player is stored or the PlayerRole is invalid
     */
    public Player getPlayer(PlayerRole role) {
        if(role.isValid()) return players[role.validOrdinal()];
        else return null;
    }
}
