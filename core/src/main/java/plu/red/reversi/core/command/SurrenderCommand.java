package plu.red.reversi.core.command;

import plu.red.reversi.core.Game;
import plu.red.reversi.core.PlayerColor;

/**
 * Glory to the Red Team.
 *
 * Command implementation class for a surrender.
 */
public class SurrenderCommand extends Command {

    public final PlayerColor player;

    /**
     * Constructs a new Surrender Command. Source defaults to PLAYER.
     *
     * @param player Player that is surrendering
     */
    public SurrenderCommand(PlayerColor player) {
        this(Source.PLAYER, player);
    }

    /**
     * Constructs a new Surrender Command.
     *
     * @param source Where the Command comes from
     * @param player Player that is surrendering
     */
    public SurrenderCommand(Source source, PlayerColor player) {
        super(source);
        this.player = player;
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
        return game.getCurrentPlayer().getRole() == player && game.getUsedPlayers().contains(player);
    }
}
