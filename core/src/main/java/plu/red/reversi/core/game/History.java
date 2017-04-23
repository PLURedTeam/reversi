package plu.red.reversi.core.game;

import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.util.DataMap;

import java.util.ArrayList;
import java.util.LinkedList;


/**
 * Stores all the past actions and moves of the players.
 */
public class History {

    /*
    // Register JSON Converter
    static {
        DataMap.Setting.registerConverter(History.class,
                (key, value, json) -> {
                    JSONObject jobj = new JSONObject();
                    int size = value.size;
                    jobj.put("size", size);
                    ArrayList<ArrayList<Integer>> b = new ArrayList<>(size);
                    for(int i = 0; i < size; i++) {
                        ArrayList<Integer> l = new ArrayList<>(size);
                        for(int j = 0; j < size; j++)
                            l.set(j, value.board[i][j]);
                        b.set(i, l);
                    }
                    jobj.put("data", b);
                    json.put(key, jobj);
                },
                (key, json) -> {
                    JSONObject jobj = json.getJSONObject(key);
                    int size = jobj.getInt("size");
                    Board b = new Board(size);
                    JSONArray rows = jobj.getJSONArray("data");
                    for(int i = 0; i < size; i++) {
                        JSONArray cols = rows.getJSONArray(i);
                        for(int j = 0; j < size; j++) {
                            b.board[i][j] = cols.getInt(j);
                        }
                    }
                    return b;
                });
    }
    */

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
