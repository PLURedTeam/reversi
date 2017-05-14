package plu.red.reversi.core.command;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.core.Coordinator;

/**
 * Glory to the Red Team.
 *
 * Command implementation class for a status message.
 */
public class StatusCommand extends Command {

    static final int SERIAL_ID = 1;

    /**
     * String status <code>message</code> carried by this StatusCommand.
     */
    public final String message;

    /**
     * Basic Constructor. Constructs a new StatusCommand with a given status <code>message</code> and a
     * <code>source</code> of CLIENTSIDE_ONLY.
     *
     * @param message String status <code>message</code> to display
     */
    public StatusCommand(String message) { this(Source.CLIENTSIDE_ONLY, message); }

    /**
     * Full Constructor. Constructs a new StatusCommand with a given status <code>message</code> and
     * <code>source</code>.
     *
     * @param source Source enum differentiating the origin of a Command between the client or the server
     * @param message String status <code>message</code> to display
     */
    public StatusCommand(Source source, String message) {
        super(source);
        this.message = message;
    }

    /**
     * Deserialize Constructor. Deserializes a StatusCommand from a JSONObject.
     *
     * @param json JSONObject to deserialize
     * @throws JSONException if there is a problem during serialization
     */
    StatusCommand(JSONObject json) throws JSONException {
        super(Source.SERVER);
        this.message = json.getString("message");
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
        // Always valid
        return true;
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
        json.put("message", message);
        return json;
    }
}
