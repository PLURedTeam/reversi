package plu.red.reversi.core.command;

import plu.red.reversi.core.BoardIndex;
import plu.red.reversi.core.Game;
import plu.red.reversi.core.PlayerColor;

/**
 * Glory to the Red Team.
 *
 * Command implementation class for a board move that doesn't cause side-effects.
 */
public class SetCommand extends BoardCommand {

    public final PlayerColor player;
    public final BoardIndex position;

    /**
     * Constructs a new move command.
     * Defaults to PLAYER Source.
     * @param player Player to make the move
     * @param position Location on the board.
     */
    public SetCommand(PlayerColor player, BoardIndex position) {
        this(Source.PLAYER, player, position);
    }

    /**
     * Constructs a new move command.
     * @param source Where the command comes from.
     * @param player Player to make the move.
     * @param position Location on the board.
     */
    public SetCommand(Source source, PlayerColor player, BoardIndex position) {
        super(source);
        this.player = player;
        this.position = position;
    }

    /**
     * Uses data from the Game object to determine whether or not this Command is valid.
     *
     * @param game Game object to pull data from
     * @return true if this Command is valid, false otherwise
     */
    @Override
    public boolean isValid(Game game) {
        // Always valid
        return true;
    }
}
