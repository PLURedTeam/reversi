package plu.red.reversi.core.game.player;

import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.util.Color;

/**
 * Used for testing; does nothing.
 */
public class NullPlayer extends  Player {

    public NullPlayer(Game game, Color color) { super(game, color); }
    public NullPlayer(Game game, int playerID, Color color) { super(game, playerID, color); }

    /**
     * Called by the game board when the current turn changes.
     *
     * @param yours whether or not the changed turn is now for this player.
     */
    @Override
    public void nextTurn(boolean yours) {
    }
}
