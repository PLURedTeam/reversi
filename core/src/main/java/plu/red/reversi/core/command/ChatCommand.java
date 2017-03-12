package plu.red.reversi.core.command;

import plu.red.reversi.core.Game;

import java.time.ZonedDateTime;

/**
 * Glory to the Red Team.
 *
 * Command implementation class for chatting.
 */
public class ChatCommand extends Command {

    public final String message;
    public final ZonedDateTime timestamp;

    public ChatCommand(Source source, String message) {
        super(source);
        this.message = message;
        timestamp = ZonedDateTime.now();
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
        return true;
    }
}
