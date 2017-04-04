package plu.red.reversi.core.command;

import plu.red.reversi.core.Controller;

/**
 * Glory to the Red Team.
 *
 * Abstract representation of a command to be passed around.
 */
public abstract class Command {

    /**
     * Source Enumeration. Used to represents where a Command originated from, either from the client or the server.
     */
    public enum Source {
        CLIENT,
        CLIENTSIDE_ONLY,
        SERVER
    }

    /**
     * Represents the Source of this Command.
     */
    public final Source source;

    /**
     * Abstract Constructor. Constructs a new Command with a given <code>source</code>.
     *
     * @param source Source enum differentiating the origin of a Command between the client or the server
     */
    protected Command(Source source) { this.source = source; }

    /**
     * Uses data from a Controller object to determine whether or not this Command is valid. IE: Whether a move played
     * by a player is on a valid position of a board.
     *
     * @param controller Controller object to pull data from
     * @return true if this Command is valid, false otherwise
     */
    public abstract boolean isValid(Controller controller);

}
