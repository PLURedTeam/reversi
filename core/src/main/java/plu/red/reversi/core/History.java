package plu.red.reversi.core;

import java.util.ArrayList;

public class History {
    private ArrayList<Command> commands;

    public int getNumCommands() {
        return commands.size();
    }

    public final Command getCommand(int i) throws ArrayIndexOutOfBoundsException {
        return commands.get(i);
    }

    public void addCommand(Command c) {
        commands.add(c);
    }
}
