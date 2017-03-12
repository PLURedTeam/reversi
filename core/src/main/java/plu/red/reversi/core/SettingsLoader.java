package plu.red.reversi.core;


import org.codehaus.jettison.json.JSONObject;

public class SettingsLoader {
    public static final SettingsLoader INSTANCE = new SettingsLoader();

    public SettingsMap loadFromJSON(JSONObject json) {
        SettingsMap settings = new SettingsMap(json);


        // ****************************************
        //  Make sure values exist as default here
        // ****************************************
        // Using get[Type]() with a default will ensure that default exists
        
        settings.getBoolean("AllowTurnSkipping", false);


        return settings;
    }
}
