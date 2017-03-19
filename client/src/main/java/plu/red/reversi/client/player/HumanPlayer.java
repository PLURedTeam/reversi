package plu.red.reversi.client.player;

import plu.red.reversi.core.BoardIndex;
import plu.red.reversi.core.Game;
import plu.red.reversi.core.PlayerColor;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.player.Player;

/**
 * Created by daniel on 3/5/17.
 * Glory to the Red Team.
 */
public class HumanPlayer extends Player {

    public HumanPlayer(Game game, PlayerColor role) {
        super(game, role);
    }

    /**
     * Called by the game board when the current turn changes.
     *
     * @param yours whether or not the changed turn is now for this player.
     */
    @Override
    public void nextTurn(boolean yours) {
        // NOOP
    }

    /**
     * Called when a click event is generated for a specific Board square, and returns whether or not the action is
     * accepted.
     *
     * @param position BoardIndex representing the square clicked
     * @return true if this action is valid, false otherwise
     */
    @Override
    public boolean boardClicked(BoardIndex position) {
        // Don't bother checking validity, because its checked in Game.acceptCommand()
        MoveCommand cmd = new MoveCommand(Command.Source.PLAYER, role, position);
        return game.acceptCommand(cmd);
    }
}

