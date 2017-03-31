package plu.red.reversi.core.command;

import plu.red.reversi.core.BoardIndex;
import plu.red.reversi.core.PlayerColor;

/**
 * Glory to the Red Team.
 *
 * Command implementation class for a board action.
 */
public abstract class BoardCommand extends Command {

    public final PlayerColor player;
    public final BoardIndex position;

    public BoardCommand(Source source, PlayerColor player, BoardIndex position) {
        super(source);
        this.player = player;
        this.position = position;
    }
}
