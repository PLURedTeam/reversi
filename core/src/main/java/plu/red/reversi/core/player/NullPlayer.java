package plu.red.reversi.core.player;

import plu.red.reversi.core.Game;
import plu.red.reversi.core.PlayerColor;
import plu.red.reversi.core.command.MoveCommand;

/**
 * Used for testing, always returns the first valid move given by board.
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
        if(!yours) return;
        game.acceptCommand(
            new MoveCommand(
                role,
                game.getBoard()
                    .getPossibleMoves(role)
                    .iterator()
                    .next()
            )
        );
    }
}
