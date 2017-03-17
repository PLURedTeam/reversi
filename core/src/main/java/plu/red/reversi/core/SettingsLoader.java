package plu.red.reversi.core;


import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.core.util.SettingsMap;

public class SettingsLoader {
    public static final SettingsLoader INSTANCE = new SettingsLoader();

    public static final String GAME_ALLOW_TURN_SKIPPING = "AllowTurnSkipping";
    public static final String GAME_BOARD_SIZE = "BoardSize";
    public static final String GAME_PLAYER_COUNT = "PlayerCount";

    public SettingsMap createGameSettings() {
        return loadGameSettingsFromJSON(new JSONObject());
    }

    public SettingsMap loadGameSettingsFromJSON(JSONObject json) {
        SettingsMap settings = new SettingsMap(json);


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


        return settings;
    }
}
