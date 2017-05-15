package plu.red.reversi.core;


import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.core.listener.ISettingsListener;
import plu.red.reversi.core.util.DataMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;

public class SettingsLoader {
    public static final SettingsLoader INSTANCE = new SettingsLoader();

    public static final String GAME_ALLOW_TURN_SKIPPING = "AllowTurnSkipping";
    public static final String GAME_BOARD_SIZE = "BoardSize";
    public static final String GAME_PLAYER_COUNT = "PlayerCount";
    public static final String GAME_TIME_LIMIT = "TimeLimit";
    public static final String GAME_TIME_BONUS = "TimeBonus";

    public static final String GLOBAL_USER_NAME = "Username";
    public static final String GLOBAL_USER_PASS = "Password";
    public static final String GLOBAL_USER_COLOR = "UserColor";
    public static final String GLOBAL_USE_3D_VIEW = "Use3DView";

    // ***********
    //  Listeners
    // ***********

    protected final HashSet<ISettingsListener> listenerSetSettings = new HashSet<>();

    /**
     * Registers an ISettingsListener that will have signals sent to it when Settngs are changed.
     *
     * @param listener ISettingsListener to register
     */
    public void addSettingsListener(ISettingsListener listener) {
        synchronized(listenerSetSettings) {
            listenerSetSettings.add(listener);
        }
    }

    /**
     * Unregisters an existing ISettingsListener that has previously been registered. Does nothing if the specified
     * ISettingsListener has not previously been registered.
     *
     * @param listener ISettingsListener to unregister
     */
    public void removeSettingsListener(ISettingsListener listener) {
        synchronized(listenerSetSettings) {
            listenerSetSettings.remove(listener);
        }
    }



    // ****************
    //  Member Methods
    // ****************

    public DataMap createGameSettings() {
        return loadGameSettingsFromJSON(new JSONObject());
    }

    public DataMap loadGameSettingsFromJSON(JSONObject json) {
        DataMap settings = new DataMap(json);


        // ****************************************
        //  Make sure values exist as default here
        // ****************************************
        // Using checkDefault(key, defaultVal, [description], [min], [max]) will ensure that default exists
        
        settings.checkDefault(GAME_ALLOW_TURN_SKIPPING, false,
                "Allow Players to skip their turn if they can't play. Otherwise the game ends as soon as one Player can't play. Defaults to false.");
        settings.checkDefault(GAME_BOARD_SIZE, 8,
                "The size of the Board. Defaults to 8.");
        settings.checkDefault(GAME_PLAYER_COUNT, 2,
                "How many Players are playing. Defaults to 2.");
        settings.checkDefault(GAME_TIME_LIMIT, 0,
                "Give the game a time limit (in seconds). Each player will have a time that counts down on their turn. A value of 0 means no limit.");
        settings.checkDefault(GAME_TIME_BONUS, 0,
                "Give each player bonus time when they complete a turn (in seconds).");


        return settings;
    }

    private DataMap clientSettings;



    private SettingsLoader() {
        loadClientSettings();
    }

    public static DataMap checkClientDefaults(DataMap settings) {

        // ****************************************
        //  Make sure values exist as default here
        // ****************************************
        // Using checkDefault(key, defaultVal, [description], [min], [max]) will ensure that default exists

        /*
        settings.checkDefault(GLOBAL_USER_NAME, "Player",
                "Name to associate user with.");
        settings.checkDefault(GLOBAL_USER_COLOR, Color.BLACK.composite,
                "Default color to use for user, in an integer representation. Bits are stored as follows: A[31:24]-R[23:16]-G[15:8]-B[7:0]");
                */
        settings.checkDefault(GLOBAL_USE_3D_VIEW, false,
                "Whether or not to use the 3D Board View when playing a game");

        return settings;
    }

    public DataMap getClientSettings() { return clientSettings; }

    public void setClientSettings(DataMap settings) {
        this.clientSettings = settings;
        synchronized(listenerSetSettings) {
            for (ISettingsListener listener : listenerSetSettings) listener.onClientSettingsChanged();
        }
    }

    public void loadClientSettings() {

        try {
            BufferedReader reader = new BufferedReader(new FileReader("settings.json"));
            String contents = "";
            String line;
            while ((line = reader.readLine()) != null)
                contents += line;
            clientSettings = new DataMap(new JSONObject(contents));
        } catch (Exception ex) {
            //System.err.println("Warning: Problem when trying to read Client Settings: " + ex.getMessage());
            clientSettings = new DataMap();
        }

        // Ensure defaults are set
        checkClientDefaults(clientSettings);

        // Save any changes via defaults or even a new file itself
        saveClientSettings();
    }

    public void saveClientSettings() {
        try {
            FileWriter writer = new FileWriter("settings.json");
            writer.write(clientSettings.toJSON().toString(1));
            writer.flush();
            writer.close();
        } catch(Exception ex) {
            //System.err.println("Error when trying to write Client Settings: " + ex.getMessage());
        }
    }
}
