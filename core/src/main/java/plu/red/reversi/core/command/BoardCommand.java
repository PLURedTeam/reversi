package plu.red.reversi.core.command;

import plu.red.reversi.core.game.BoardIndex;

/**
 * Glory to the Red Team.
 *
 * Command implementation class for a board action.
 */
public abstract class BoardCommand extends Command {

    /**
     * Integer ID specifying what Player issued this BoardCommand.
     */
    public final int playerID;

    /**
     * BoardIndex specifying what position on a Board is affected by this BoardCommand.
     */
    public final BoardIndex position;

    /**
     * Abstract Constructor. Constructs a new BoardCommand with a given <code>source</code>, <code>playerID</code>, and
     * <code>position</code>.
     *
     * @param source Source enum differentiating the origin of a Command between the client or the server
     * @param playerID Integer ID representing which Player issued the Command
     * @param position BoardIndex <code>position</code> that this Command affects
     */
    protected BoardCommand(Source source, int playerID, BoardIndex position) {
        super(source);
        this.playerID = playerID;
        this.position = position;
    }
}
