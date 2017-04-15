package plu.red.reversi.core.game;

import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.command.Command;

import java.util.ArrayList;
import java.util.LinkedList;


/**
 * Stores all the past actions and moves of the players.
 */
public class History {
    private ArrayList<BoardCommand> moves;

    /**
     * Basic constructor, initializes lists to be empty.
     */
    public History() {
        moves = new ArrayList<>();
    }

    /**
     * Copy constructor, makes a copy of another history object.
     * @param other History object to be copied.
     */
    public History(History other) {
        moves = new ArrayList<>(other.moves);
    }

    /**
     * Used to find out how many moves have been stored in history.
     * @return Total number of moves stored in history.
     */
    public int getNumBoardCommands() {
        return moves.size();
    }

    /**
     * Used to retrieve a specific move.
     * @param i Index of the desired move.
     * @return Move stored at the index.
     * @throws IndexOutOfBoundsException If the requested index is invalid.
     */
    public BoardCommand getBoardCommand(int i) throws IndexOutOfBoundsException {
        return moves.get(i);
    }

    /**
     * Adds a new board command to the history.
     * @param c BoardCommand to be added.
     */
    public void addCommand(BoardCommand c) {
        moves.add(c);
    }

    /**
     * Adds a command to the history. Auto sorts based on sub-type.
     * @param c Command to be added.
     */
    public void addCommand(Command c) {
        if(c instanceof BoardCommand)
            addCommand((BoardCommand)c);
    }

    /**
     * Used to retrieve all commands made until a certain point in the game.
     * @param i Index of the furthest command (exclusive).
     * @return List containing indicies [0, i).
     * @throws IndexOutOfBoundsException If the requested index is invalid.
     */
    public LinkedList<BoardCommand> getMoveCommandsUntil(int i) throws IndexOutOfBoundsException {
        LinkedList<BoardCommand> list = new LinkedList<>();
        list.addAll(moves.subList(0, i));
        return list;
    }

    /**
     * Used to retrieve all commands made after a certain point in the game.
     * @param i Index of the first command (exclusive).
     * @return List containing indicies [i, size()).
     * @throws IndexOutOfBoundsException If the requested index is invalid.
     */
    public LinkedList<BoardCommand> getMoveCommandsAfter(int i) throws IndexOutOfBoundsException {
        LinkedList<BoardCommand> list = new LinkedList<>();
        list.addAll(moves.subList(i, moves.size()));
        return list;
    }
}
