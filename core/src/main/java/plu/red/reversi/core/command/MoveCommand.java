package plu.red.reversi.core.command;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.Coordinator;
import plu.red.reversi.core.game.Game;

/**
 * Glory to the Red Team.
 *
 * Command implementation class for a board move that causes flip side-effects.
 */
public class MoveCommand extends BoardCommand {

    static final int SERIAL_ID = 3;

    /**
     * Basic Constructor. Constructs a new MoveCommand with a given <code>playerID</code>, <code>position</code>, and
     * a <code>source</code> of CLIENT.
     *
     * @param playerID Integer ID representing which Player issued the Command
     * @param position BoardIndex <code>position</code> that this Command affects
     */
    public MoveCommand(int playerID, BoardIndex position) { this(Source.CLIENT, playerID, position); }

    /**
     * Full Constructor. Constructs a new MoveCommand with a given <code>source</code>, <code>playerID</code>, and
     * <code>position</code>.
     *
     * @param source Source enum differentiating the origin of a Command between the client or the server
     * @param playerID Integer ID representing which Player issued the Command
     * @param position BoardIndex <code>position</code> that this Command affects
     */
    public MoveCommand(Source source, int playerID, BoardIndex position) {
        super(source, playerID, position);
    }

    /**
     * Deserialize Constructor. Deserializes a MoveCommand from a JSONObject.
     *
     * @param json JSONObject to deserialize
     * @throws JSONException if there is a problem during serialization
     */
    MoveCommand(JSONObject json) throws JSONException {
        super(
                Source.SERVER,
                json.getInt("playerID"),
                new BoardIndex(json.getInt("pos_row"), json.getInt("pos_column")));
    }

    /**
     * Uses data from a Coordinator object to determine whether or not this Command is valid. IE: Whether a move played
     * by a player is on a valid position of a board.
     *
     * @param controller Coordinator object to pull data from
     * @return true if this Command is valid, false otherwise
     */
    @Override
    public boolean isValid(Coordinator controller) {
        // Is it this Player's turn and is the position valid on the board
        return controller instanceof Game &&
                ((Game)controller).getCurrentPlayer().getID() == playerID &&
                ((Game)controller).getGameLogic().isValidMove(new MoveCommand(playerID, position));
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
