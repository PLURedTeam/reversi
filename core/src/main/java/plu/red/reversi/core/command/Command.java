package plu.red.reversi.core.command;

import plu.red.reversi.core.Game;

/**
 * Glory to the Red Team.
 *
 * Abstract representation of a command to be passed around.
 */
public abstract class Command {

    /**
     * Represents the source of the command, whether it came from a Player or the Server
     */
    public enum Source {
        PLAYER,
        SERVER
    }

    public final Source source;

    public Command(Source source) {
        this.source = source;
    }

    /**
     * Uses data from the Game object to determine whether or not this Command is valid. IE: Whether a move played by a
     * player is on a valid position of the board.
     *
     * @param game Game object to pull data from
     * @return true if this Command is valid, false otherwise
     */
    public abstract boolean isValid(Game game);
}
