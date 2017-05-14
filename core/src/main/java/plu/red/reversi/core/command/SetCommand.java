package plu.red.reversi.core.command;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.core.Coordinator;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.Game;

/**
 * Glory to the Red Team.
 *
 * Command implementation class for a board move that doesn't cause side-effects. Used in situations where only a single
 * piece is changed, such as when setting up the initial Board state.
 */
public class SetCommand extends BoardCommand {

    static final int SERIAL_ID = 4;

    /**
     * Basic Constructor. Constructs a new SetCommand with a given <code>playerID</code>, <code>position</code>, and
     * a <code>source</code> of CLIENT.
     *
     * @param playerID Integer ID representing which Player issued the Command
     * @param position BoardIndex <code>position</code> that this Command affects
     */
    public SetCommand(int playerID, BoardIndex position) { this(Source.CLIENT, playerID, position); }

    /**
     * Full Constructor. Constructs a new SetCommand with a given <code>source</code>, <code>playerID</code>, and
     * <code>position</code>.
     *
     * @param source Source enum differentiating the origin of a Command between the client or the server
     * @param playerID Integer ID representing which Player issued the Command
     * @param position BoardIndex <code>position</code> that this Command affects
     */
    public SetCommand(Source source, int playerID, BoardIndex position) { super(source, playerID, position); }

    /**
     * Deserialize Constructor. Deserializes a SetCommand from a JSONObject.
     *
     * @param json JSONObject to deserialize
     * @throws JSONException if there is a problem during serialization
     */
    SetCommand(JSONObject json) throws JSONException {
        super(
                Source.SERVER,
                json.getInt("playerID"),
                new BoardIndex(json.getInt("pos_row"), json.getInt("pos_column")));
    }

    /**
     * Constructs a new set command from a move command.
     *
     * @param cmd Move command to copy.
     */
    public SetCommand(MoveCommand cmd) { this(cmd.source, cmd.playerID, cmd.position); }

    /**
     * Uses data from a Coordinator object to determine whether or not this Command is valid. IE: Whether a move played
     * by a player is on a valid position of a board.
     *
     * @param controller Coordinator object to pull data from
     * @return true if this Command is valid, false otherwise
     */
    @Override
    public boolean isValid(Coordinator controller) {
        // Is the Coordinator a Game Coordinator
        return controller instanceof Game;
    }

    /**
     * Serializes this Command into a JSONObject.
     *
     * @return New JSONObject from this Command
     * @throws JSONException if there is a problem during serialization
     */
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", SERIAL_ID);
        json.put("source", source.ordinal());
        json.put("playerID", playerID);
        json.put("pos_column", position.column);
        json.put("pos_row", position.row);
        return json;
    }
}
