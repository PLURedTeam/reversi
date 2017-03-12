package plu.red.reversi.core;

/**
 * Created by daniel on 3/5/17.
 * Glory to the Red Team.
 */

/**
 * Represents an entity which is capable of acting as a player in the game.
 */
public abstract class Player {

    /// Reference to the current game this player is a part of.
    private Game game;

    protected String name;

    private PlayerColor role;

    public Player(Game game, PlayerColor role) {
        this.game = game;
        this.role = role;
    }

    /**
     * Called by the game board when the current turn changes.
     * @param yours whether or not the changed turn is now for this player.
     */
    public abstract void nextTurn(boolean yours);

    /**
     * Gets the registered game for this player
     * @return The currently registered game
     */
    protected Game getGame() { return game; }
    public String getName() { return name; }
    public PlayerColor getRole() { return role; }

}
