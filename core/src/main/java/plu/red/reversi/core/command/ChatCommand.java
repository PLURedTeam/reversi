package plu.red.reversi.core.command;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.core.Coordinator;
import plu.red.reversi.core.util.ChatMessage;

/**
 * Glory to the Red Team.
 *
 * Command implementation for a ChatMessage. Simply carries the ChatMessage through the Command system.
 */
public class ChatCommand extends Command {

    static final int SERIAL_ID = 0;

    /**
     * ChatMessage <code>message</code> carried by this ChatCommand.
     */
    public final ChatMessage message;

    /**
     * Basic Constructor. Constructs a new ChatCommand with a given ChatMessage <code>message</code> and a
     * <code>source</code> of CLIENT.
     *
     * @param message ChatMessage <code>message</code> to carry
     */
    public ChatCommand(ChatMessage message) { this(Source.CLIENT, message); }

    /**
     * Full Constructor. Constructs a new ChatCommand with a given ChatMessage <code>message</code> and
     * <code>source</code>.
     *
     * @param source Source enum differentiating the origin of a Command between the client or the server
     * @param message ChatMessage <code>message</code> to carry
     */
    public ChatCommand(Source source, ChatMessage message) {
        super(source);
        this.message = message;
    }

    /**
     * Deserialize Constructor. Deserializes a ChatCommand from a JSONObject.
     *
     * @param json JSONObject to deserialize
     * @throws JSONException if there is a problem during serialization
     */
    ChatCommand(JSONObject json) throws JSONException {
        super(Source.values()[json.getInt("source")]);
        this.message = ChatMessage.fromJSON(json.getJSONObject("message"));
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
        json.put("message", message.toJSON());
        return json;
    }
}
