package plu.red.reversi.core.listener;

import plu.red.reversi.core.command.Command;

/**
 * Glory to the Red Team.
 *
 * Interface for when a valid command is being applied.
 */
public interface ICommandListener extends IListener {

    /**
     * Called when a Command is being passed through Game and has been validated.
     *
     * @param cmd Command object that is being applied
     */
    void commandApplied(Command cmd);

}
