package plu.red.reversi.core.game.player;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.util.Color;

/**
 * Created by daniel on 3/5/17.
 * Glory to the Red Team.
 *
 * Represents an abstract entity which is capable of acting as a player in the game. Note that by default a Player
 * object hashes to its <code>playerID</code>, for sorting purposes.
 */
public abstract class Player {

    // Reference to the Game this Player belongs to
    protected final Game game;

    // What Color is this Player?
    protected final Color color;

    // The name of this Player. Not final because it can change over time.
    //  (IE if a user decides to change their username in the middle of a game)
    protected String name;

    // Private because only the Abstract Player needs to interface with it
    private final ID playerID;

    /**
     * New Game Constructor. Creates a Player belonging to a newly created Game object. Player is automatically registered to
     * the <code>game</code>, and an ID is retrieved from the <code>game</code>.
     *
     * @param game Game object this Player belongs to
     * @param color Color to give this Player
     */
    protected Player(Game game, Color color) {
        this.game = game;
        this.color = color;
        this.playerID = new ID();
        game.registerPlayer(this, this.playerID);
        this.name = "Player";
    }

    /**
     * Load Game Constructor. Creates a Player belonging to a Game loaded from a saved state. Player is created with
     * an Integer ID already associated to it.
     *
     * @param game Game object this Player belongs to
     * @param playerID Integer ID associated with this Player
     * @param color Color to give this Player
     */
    protected Player(Game game, int playerID, Color color) {
        this.game = game;
        this.color = color;
        this.playerID = new ID();
        this.playerID.set(playerID);
        game.registerPlayer(this, this.playerID);
        this.name = "Player";
    }

    /**
     * Serial Constructor. Creates a Player belonging to an unserialized Game. Player is created with data unserialized
     * from a given JSONObject.
     *
     * @param game Game object this Player belongs to
     * @param json JSONObject storing this Player's data
     * @throws JSONException if there is a problem during unserialization
     */
    protected Player(Game game, JSONObject json) throws JSONException {
        this.game = game;
        this.color = new Color(json.getInt("color"));
        this.playerID = new ID();
        this.playerID.set(json.getInt("id"));
        this.name = json.getString("name");
        game.registerPlayer(this, this.playerID);
    }

    /**
     * Static Serial Creator. Unserializes a Player object from JSON based on the stored Player type.
     *
     * @param game Game object the Player belongs to
     * @param json JSONObject storing the Player's data
     * @return Newly created Player
     * @throws JSONException if there is a problem during unserialization
     */
    public static Player unserializePlayer(Game game, JSONObject json, boolean flip) throws JSONException {
        int type = json.getInt("type");
        switch(type) {
            case 0: return flip ? new NetworkPlayer(game, json) : new HumanPlayer(game, json);
            case 1: return new BotPlayer(game, json);
            case 2: return flip ? new HumanPlayer(game, json) : new NetworkPlayer(game, json);
            default: throw new IllegalArgumentException("Unknown Player Type '" + type + "' when trying to unserialize");
        }
    }

    public static Player unserializePlayer(Game game, JSONObject json) throws JSONException {
        return unserializePlayer(game, json, false);
    }

    /**
     * Serializes this Player into a JSONObject.
     *
     * @return New JSONObject from this Player
     * @throws JSONException if there is a problem during serialization
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("color", this.color.composite);
        json.put("id", this.playerID.get());
        json.put("name", this.name);
        return json;
    }

    /**
     * Called by the <code>game's</code> Board object when the current turn changes.
     *
     * @param yours whether or not the changed turn is now for this Player
     */
    public abstract void nextTurn(boolean yours);

    /**
     * Called when a click event is generated for a specific Board square, and returns whether or not the action is
     * accepted.
     *
     * @param position BoardIndex representing the square clicked
     * @return true if this action is valid, false otherwise
     */
    public boolean boardClicked(BoardIndex position) {
        // NOOP by default
        return false;
    }

    /**
     * Goes down the rabbit hole to retrieve this Player's score from the Game's Board object.
     *
     * @return this Player's score
     */
    public int getScore() {
        return game.getGameLogic().getScore(playerID.get());
    }

    /**
     * Game getter. Retrieves the Game this Player belongs to.
     *
     * @return The currently registered game
     */
    public Game getGame() { return game; }

    /**
     * Name getter. Retrieves the <code>name</code> this Player currently has.
     *
     * @return String <code>name</code>
     */
    public String getName() { return name; }

    /**
     * ID getter. Retrieves the ID belonging to this Player. Exact ordering is not guaranteed, but it is
     * guaranteed that there are no duplicates in a single Game object.
     *
     * @return Integer ID
     */
    public int getID() { return playerID.get(); }

    /**
     * Color getter. Retrieves the Color belonging to this Player.
     *
     * @return Color object
     */
    public Color getColor() { return color; }

    /**
     * Sets the display <code>name</code> of this player.
     *
     * @param name String <code>name</code> to set
     */
    public void setName(String name) { this.name = name; }

    /**
     * Overwritten hashCode() Method. Hashes to the Integer representation of this Player's <code>playerID</code>.
     *
     * @return Integer ID of this Player
     */
    @Override
    public int hashCode() {
        return playerID.get();
    }

    /**
     * ID Key class. The ID class can only be constructed by Player (and not any objects extending Player). The purpose
     * of the ID class is to create a key such that the public registerPlayer() method in Game can only be used from
     * inside this Player object (even though the two classes do not share a package). This ensures that it is not
     * possible to register a Player to a game with a conflicting ID number.
     */
    public static final class ID {
        private int id = -1;
        // Private Constructor so only Player can make it
        private ID() {}
        public void set(int id) { this.id = id; }
        public int get() { return id; }
    }

}
