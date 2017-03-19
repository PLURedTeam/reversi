package plu.red.reversi.core.player;

import plu.red.reversi.core.BoardIndex;
import plu.red.reversi.core.Game;
import plu.red.reversi.core.PlayerColor;

/**
 * Created by daniel on 3/5/17.
 * Glory to the Red Team.
 *
 * Represents an entity which is capable of acting as a player in the game.
 */
public abstract class Player {

    /// Reference to the current game this player is a part of.
    protected Game game;
    protected String name;
    protected PlayerColor role;

    public Player(Game game, PlayerColor role) {
        this.game = game;
        this.role = role;
        this.name = role.toString() + " Player";
    }

    /**
     * Called by the game board when the current turn changes.
     *
     * @param yours whether or not the changed turn is now for this player.
     */
    public abstract void nextTurn(boolean yours);

    /**
     * Called when a click event is generated for a specific Board square, and returns whether or not the action is
     * accepted.
     *
     * @param position BoardIndex representing the square clicked
     * @return true if this action is valid, false otherwise
     */
    public boolean boardClicked(BoardIndex position) {
        // NOOP by default
        return false;
    }

    /**
     * Goes down the rabbit hole to retrieve this Player's score from the Game's Board object.
     *
     * @return this Player's score
     */
    public int getScore() {
        return game.getBoard().getScore(role);
    }

    /**
     * Gets the registered game for this player
     * @return The currently registered game
     */
    public Game getGame() { return game; }
    public String getName() { return name; }
    public PlayerColor getRole() { return role; }

}
