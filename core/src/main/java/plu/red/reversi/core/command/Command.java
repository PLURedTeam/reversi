package plu.red.reversi.core.command;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.core.Coordinator;

/**
 * Glory to the Red Team.
 *
 * Abstract representation of a command to be passed around.
 */
public abstract class Command {

    protected static int NEXT_ID = 0;

    /**
     * Source Enumeration. Used to represents where a Command originated from, either from the client or the server.
     */
    public enum Source {
        CLIENT,
        CLIENTSIDE_ONLY,
        SERVER
    }

    /**
     * Represents the Source of this Command.
     */
    public final Source source;

    /**
     * Abstract Constructor. Constructs a new Command with a given <code>source</code>.
     *
     * @param source Source enum differentiating the origin of a Command between the client or the server
     */
    protected Command(Source source) { this.source = source; }

    /**
     * Uses data from a Coordinator object to determine whether or not this Command is valid. IE: Whether a move played
     * by a player is on a valid position of a board.
     *
     * @param controller Coordinator object to pull data from
     * @return true if this Command is valid, false otherwise
     */
    public abstract boolean isValid(Coordinator controller);

    /**
     * Serializes this Command into a JSONObject.
     *
     * @return New JSONObject from this Command
     * @throws JSONException if there is a problem during serialization
     */
    public abstract JSONObject toJSON() throws JSONException;

    /**
     * De-Serializes a JSONObject into a Command. The exact type of Command is specified by the JSONObject.
     *
     * @param json JSONObject to de-serialize
     * @return New Command from the JSONObject
     * @throws JSONException if there is a problem during de-serialization
     */
    public static Command fromJSON(JSONObject json) throws JSONException {
        int type = json.getInt("type");
        switch(type) {
            case ChatCommand.SERIAL_ID:         return new ChatCommand(json);
            case StatusCommand.SERIAL_ID:       return new StatusCommand(json);
            case SurrenderCommand.SERIAL_ID:    return new SurrenderCommand(json);
            case MoveCommand.SERIAL_ID:         return new MoveCommand(json);
            case SetCommand.SERIAL_ID:          return new SetCommand(json);
            case JoinCommand.SERIAL_ID:         return new JoinCommand(json);
            default:    throw new IllegalArgumentException("Unknown Command Type when trying to deserialize");
        }
    }

}
