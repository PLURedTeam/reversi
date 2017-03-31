package plu.red.reversi.core;

import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.command.ChatCommand;
import plu.red.reversi.core.command.Command;

import java.util.ArrayList;
import java.util.LinkedList;


/**
 * Stores all the past actions and moves of the players.
 */
public class History {
    private ArrayList<BoardCommand> moves;
    private ArrayList<ChatCommand>  messages;

    /**
     * Basic constructor, initializes lists to be empty.
     */
    public History() {
        moves = new ArrayList<>();
        messages = new ArrayList<>();
    }

    /**
     * Copy constructor, makes a copy of another history object.
     * @param other History object to be copied.
     */
    public History(History other) {
        moves = new ArrayList<>(other.moves);
        messages = new ArrayList<>(other.messages);
    }

    /**
     * Used to find out how many moves have been stored in history.
     * @return Total number of moves stored in history.
     */
    public int getNumBoardCommands() {
        return moves.size();
    }

    /**
     * Used to find out how many messages have been stored in histroy.
     * @return  Total number of messages stored in history.
     */
    public int getNumChatCommands() {
        return messages.size();
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
     * Used to retrieve a specifc message.
     * @param i Index of the desired command.
     * @return Message stored at that index.
     * @throws IndexOutOfBoundsException If the requesed index is invalid.
     */
    public ChatCommand getChatCommand(int i) throws IndexOutOfBoundsException {
        return messages.get(i);
    }

    /**
     * Adds a new message to the history.
     * @param c ChatCommand to be added.
     */
    public void addCommand(ChatCommand c) {
        messages.add(c);
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
        else if(c instanceof ChatCommand)
            addCommand((ChatCommand)c);
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
}
