package plu.red.reversi.core;


import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.core.util.SettingsMap;

public class SettingsLoader {
    public static final SettingsLoader INSTANCE = new SettingsLoader();

    public SettingsMap loadFromJSON(JSONObject json) {
        SettingsMap settings = new SettingsMap(json);


        // ****************************************
        //  Make sure values exist as default here
        // ****************************************
        // Using get[Type]() with a default will ensure that default exists
        
        settings.getBoolean("AllowTurnSkipping", false);

        settings.getNumber("BoardSize", 8, 4, null);


        return settings;
    }
}
