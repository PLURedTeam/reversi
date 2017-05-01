package plu.red.reversi.core.game.player;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.SettingsLoader;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.listener.ISettingsListener;
import plu.red.reversi.core.util.Color;

/**
 * Created by daniel on 3/5/17.
 * Glory to the Red Team.
 * 
 * Extension of the Player class to represent a Human controllable Player entity.
 */
public class HumanPlayer extends Player implements ISettingsListener {

    protected int duplicateID = 1;

    /**
     * New Game Constructor. Creates a HumanPlayer belonging to a newly created Game object. HumanPlayer is
     * automatically registered to the <code>game</code>, and an ID is retrieved from the <code>game</code>.
     *
     * @param game Game object this HumanPlayer belongs to
     * @param color Color to give this HumanPlayer
     */
    public HumanPlayer(Game game, Color color) {
        super(game, color);
        SettingsLoader.INSTANCE.addSettingsListener(this);
    }

    /**
     * Load Game Constructor. Creates a HumanPlayer belonging to a Game loaded from a saved state. HumanPlayer is
     * created with an Integer ID already associated to it.
     *
     * @param game Game object this HumanPlayer belongs to
     * @param playerID Integer ID associated with this HumanPlayer
     * @param color Color to give this HumanPlayer
     */
    public HumanPlayer(Game game, int playerID, Color color) {
        super(game, playerID, color);
        SettingsLoader.INSTANCE.addSettingsListener(this);
    }

    /**
     * Serial Constructor. Creates a HumanPlayer belonging to an unserialized Game. HumanPlayer is created with data unserialized
     * from a given JSONObject.
     *
     * @param game Game object this Player belongs to
     * @param json JSONObject storing this Player's data
     * @throws JSONException if there is a problem during unserialization
     */
    public HumanPlayer(Game game, JSONObject json) throws JSONException {
        super(game, json);
        duplicateID = json.optInt("duplicate");
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
        json.put("duplicate", this.duplicateID);
        json.put("type", 0);
        return json;
    }

    /**
     * Called by the <code>game's</code> Board object when the current turn changes.
     *
     * @param yours whether or not the changed turn is now for this Player
     */
    @Override
    public void nextTurn(boolean yours) {
        // NOOP
    }

    /**
     * Called when a click event is generated for a specific Board square, and returns whether or not the action is
     * accepted.
     *
     * @param position BoardIndex representing the square clicked
     * @return true if this action is valid, false otherwise
     */
    @Override
    public boolean boardClicked(BoardIndex position) {
        // Don't bother checking validity, because its checked in Game.acceptCommand()
        MoveCommand cmd = new MoveCommand(Command.Source.CLIENT, getID(), position);
        return game.acceptCommand(cmd);
    }

    /**
     * Sets the duplicate ID number to display after this username of this HumanPlayer. Used when there is more than
     * one local player.
     *
     * @param ID Integer ID to set, lower bound of 1 (will automatically be 1 if given number is lower)
     */
    public void setDuplicateID(int ID) {
        if(ID < 1) duplicateID = 1;
        else duplicateID = ID;
    }

    /**
     * Called when the client's settings have been changed.
     */
    @Override
    public void onClientSettingsChanged() {
        setName(
                SettingsLoader.INSTANCE.getClientSettings()
                        .get(SettingsLoader.GLOBAL_USER_NAME, String.class) );
    }

    /**
     * Sets the display name of this player. Will also append a duplicate number on the end of this HumanPlayer's
     * duplicate number is higher than 1.
     *
     * @param name String name to set
     */
    @Override
    public void setName(String name) {
        if(duplicateID < 2) super.setName(name);
        else super.setName(name+duplicateID);
    }
}

