package plu.red.reversi.core.player;

import plu.red.reversi.core.Game;
import plu.red.reversi.core.PlayerColor;

/**
 * Used for testing; does nothing.
 */
public class NullPlayer extends  Player {

    public NullPlayer(Game game, PlayerColor role) {
        super(game, role);
    }

    /**
     * Called by the game board when the current turn changes.
     *
     * @param yours whether or not the changed turn is now for this player.
     */
    @Override
    public void nextTurn(boolean yours) {
    }
}
