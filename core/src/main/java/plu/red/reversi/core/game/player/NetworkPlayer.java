package plu.red.reversi.core.game.player;


import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.util.Color;

public class NetworkPlayer extends Player {

    public NetworkPlayer(Game game, Color color) { super(game, color); }
    public NetworkPlayer(Game game, int playerID, Color color) { super(game, playerID, color); }

    /**
     * Serial Constructor. Creates a NetworkPlayer belonging to an unserialized Game. NetworkPlayer is created with data unserialized
     * from a given JSONObject.
     *
     * @param game Game object this Player belongs to
     * @param json JSONObject storing this Player's data
     * @throws JSONException if there is a problem during unserialization
     */
    public NetworkPlayer(Game game, JSONObject json) throws JSONException {
        super(game, json);
    }

    /**
     * Called by the game board when the current turn changes.
     *
     * @param yours whether or not the changed turn is now for this player.
     */
    @Override
    public void nextTurn(boolean yours) {
    }

    /**
     * Serializes this Player into a JSONObject.
     *
     * @return New JSONObject from this Player
     * @throws JSONException if there is a problem during serialization
     */
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = super.toJSON();
        json.put("type", 2);
        return json;
    }
}
