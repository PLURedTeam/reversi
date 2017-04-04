package plu.red.reversi.core.command;

import plu.red.reversi.core.Controller;

/**
 * Glory to the Red Team.
 *
 * Command implementation class for a status message.
 */
public class StatusCommand extends Command {

    /**
     * String status <code>message</code> carried by this StatusCommand.
     */
    public final String message;

    /**
     * Basic Constructor. Constructs a new StatusCommand with a given status <code>message</code> and a
     * <code>source</code> of CLIENTSIDE_ONLY.
     *
     * @param message String status <code>message</code> to display
     */
    public StatusCommand(String message) { this(Source.CLIENTSIDE_ONLY, message); }

    /**
     * Full Constructor. Constructs a new StatusCommand with a given status <code>message</code> and
     * <code>source</code>.
     *
     * @param source Source enum differentiating the origin of a Command between the client or the server
     * @param message String status <code>message</code> to display
     */
    public StatusCommand(Source source, String message) {
        super(source);
        this.message = message;
    }

    /**
     * Uses data from a Controller object to determine whether or not this Command is valid. IE: Whether a move played
     * by a player is on a valid position of a board.
     *
     * @param controller Controller object to pull data from
     * @return true if this Command is valid, false otherwise
     */
    @Override
    public boolean isValid(Controller controller) {
        // Always valid
        return true;
    }
}
