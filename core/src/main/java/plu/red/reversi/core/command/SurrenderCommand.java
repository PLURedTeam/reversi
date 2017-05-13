package plu.red.reversi.core.command;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.core.Coordinator;
import plu.red.reversi.core.game.Game;

/**
 * Glory to the Red Team.
 *
 * Command implementation class for a surrender action.
 */
public class SurrenderCommand extends Command {

    static final int SERIAL_ID = 2;

    /**
     * Integer ID specifying what Player issued this SurrenderCommand.
     */
    public final int playerID;

    /**
     * Basic Constructor. Constructs a new SurrenderCommand with a given <code>playerID</code> and a <code>source</code>
     * of CLIENT.
     *
     * @param playerID Integer ID of the Player that is surrendering
     */
    public SurrenderCommand(int playerID) { this(Source.CLIENT, playerID); }

    /**
     * Full Constructor. Constructs a new SurrenderCommand with a given <code>playerID</code> and <code>source</code>.
     *
     * @param source Source enum differentiating the origin of a Command between the client or the server
     * @param playerID Integer ID of the Player that is surrendering
     */
    public SurrenderCommand(Source source, int playerID) {
        super(source);
        this.playerID = playerID;
    }

    /**
     * Deserialize Constructor. Deserializes a SurrenderCommand from a JSONObject.
     *
     * @param json JSONObject to deserialize
     * @throws JSONException if there is a problem during serialization
     */
    SurrenderCommand(JSONObject json) throws JSONException {
        super(Source.SERVER);
        this.playerID = json.getInt("playerID");
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
        return controller instanceof Game &&
                ((Game)controller).getCurrentPlayer().getID() == playerID;
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
        return json;
    }
}
