package plu.red.reversi.core;

import java.util.ArrayList;

/**
 * Stores all the past actions and commands of the player.
 */
public class History {
    private ArrayList<Command> commands;

    /**
     * Used to find out how many commands have been stored in history.
     * @return The total number of commands stored in history
     */
    public int getNumCommands() {
        return commands.size();
    }

    /**
     * Used to retrieve a specific command.
     * @param i Index of the desired command.
     * @return Command stored at the index.
     * @throws ArrayIndexOutOfBoundsException If the requested index is invalid.
     */
    public final Command getCommand(int i) throws ArrayIndexOutOfBoundsException {
        return commands.get(i);
    }

    /**
     * Adds a new command to the history.
     * @param c Command to be added.
     */
    public void addCommand(Command c) {
        commands.add(c);
    }
}
