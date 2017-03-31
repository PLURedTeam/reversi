package plu.red.reversi.core.command;

import plu.red.reversi.core.BoardIndex;
import plu.red.reversi.core.Game;
import plu.red.reversi.core.PlayerColor;

/**
 * Glory to the Red Team.
 *
 * Command implementation class for a board move that causes flip side-effects.
 */
public class MoveCommand extends BoardCommand {

    /**
     * Constructs a new move command.
     * Defaults to PLAYER Source.
     * @param player Player to make the move
     * @param position Location on the board.
     */
    public MoveCommand(PlayerColor player, BoardIndex position) {
        this(Source.PLAYER, player, position);
    }

    /**
     * Constructs a new move command.
     * @param source Where the command comes from.
     * @param player Player to make the move.
     * @param position Location on the board.
     */
    public MoveCommand(Source source, PlayerColor player, BoardIndex position) {
        super(source, player, position);
    }

    /**
     * Uses data from the Game object to determine whether or not this Command is valid. IE: Whether a move played by a
     * player is on a valid position of the board.
     *
     * @param game Game object to pull data from
     * @return true if this Command is valid, false otherwise
     */
    @Override
    public boolean isValid(Game game) {
        // Is it this Player's turn and is the position valid on the board
        return game.getCurrentPlayer().getRole() == player && game.getBoard().isValidMove(player, position);
    }
}
