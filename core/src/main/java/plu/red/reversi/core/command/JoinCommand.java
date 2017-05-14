package plu.red.reversi.core.command;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.core.Coordinator;
import plu.red.reversi.core.lobby.Lobby;
import plu.red.reversi.core.util.User;

/**
 * Glory to the Red Team.
 *
 * Command implementation for joining a Lobby.
 *
 * CURRENTLY UNUSED AND UNIMPLEMENTED.
 */
public class JoinCommand extends Command {

    static final int SERIAL_ID = 5;

    public final User user;

    public final boolean claim;

    public JoinCommand(User user) { this(Source.CLIENT, user, false); }

    public JoinCommand(User user, boolean claim) { this(Source.CLIENT, user, claim); }

    public JoinCommand(Source source, User user, boolean claim) {
        super(source);
        this.user = user;
        this.claim = claim;
    }

    /**
     * Deserialize Constructor. Deserializes a JoinCommand from a JSONObject.
     *
     * @param json JSONObject to deserialize
     * @throws JSONException if there is a problem during serialization
     */
    JoinCommand(JSONObject json) throws JSONException {
        super(Source.SERVER);
        this.user = new User(json.getJSONObject("user"));
        this.claim = json.getBoolean("claim");
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
        if(controller instanceof Lobby) {
            Lobby lobby = (Lobby)controller;
            if(claim) {
                // TODO: Look for open Network Slots in order to validate
                return true;
            } else return true;
        } else return false;
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
        json.put("user", user.toJSON());
        json.put("claim", claim);
        return json;
    }
}
