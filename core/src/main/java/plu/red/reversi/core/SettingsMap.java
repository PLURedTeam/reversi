package plu.red.reversi.core;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Glory to the Red Team.
 *
 * Settings map to store game settings in an abstract manner. This is one possible method to store settings.
 */
public class SettingsMap {

    protected HashMap<String, Number>  dataNumbers  = new HashMap<String, Number>();
    protected HashMap<String, String>  dataStrings  = new HashMap<String, String>();
    protected HashMap<String, Boolean> dataBooleans = new HashMap<String, Boolean>();

    public SettingsMap() {
        // Default Constructor doesn't do much, but allows for a non-default constructor to construct
        //  from a Database, as an example
    }

    public SettingsMap(JSONObject json) {
        
        // Get Strings
        JSONObject stringMap = json.optJSONObject("strings");
        if(stringMap != null) {
            Iterator it = stringMap.keys();
            while(it.hasNext()) {
                String key = it.next().toString();
                dataStrings.put(key, stringMap.optString(key));
            }
        }

        // Get Numbers
        JSONObject numberMap = json.optJSONObject("numbers");
        if(numberMap != null) {
            Iterator it = numberMap.keys();
            while(it.hasNext()) {
                String key = it.next().toString();
                dataNumbers.put(key, numberMap.optDouble(key));
            }
        }

        // Get Booleans
        JSONObject booleanMap = json.optJSONObject("booleans");
        if(booleanMap != null) {
            Iterator it = booleanMap.keys();
            while(it.hasNext()) {
                String key = it.next().toString();
                dataBooleans.put(key, booleanMap.optBoolean(key));
            }
        }
    }

    public JSONObject toJSON() throws RuntimeException {
        JSONObject result = new JSONObject();
        try {
            result.put("strings",  new JSONObject(dataStrings));
            result.put("numbers",  new JSONObject(dataNumbers));
            result.put("booleans", new JSONObject(dataBooleans));
        } catch(JSONException ex) {
            throw new RuntimeException("Problem when turning settings into a JSON: " + ex.getMessage());
        }
        return result;
    }


    /**
     * Retrieve a Number setting if it exists, otherwise store setting with a default value and return newly
     * stored default value.
     * 
     * @param key String key to retrieve a setting with
     * @param defaultValue Default Number value to store if setting does not already exist
     * @return Number value stored under requested key
     */
    public Number getNumber(String key, Number defaultValue) {
        if(dataNumbers.containsKey(key)) return dataNumbers.get(key);
        else {
            dataNumbers.put(key, defaultValue);
            return dataNumbers.get(key);
        }
    }

    /**
     * Retrieve a Number setting if it exists, otherwise store setting with a default value of '0' and return newly
     * stored default value.
     * 
     * @param key String key to retrieve a setting with
     * @return Number value stored under requested key
     */
    public Number getNumber(String key) { return getNumber(key, 0); }

    /**
     * Stores a Number setting.
     * 
     * @param key String key to store a setting with
     * @param value Number value to store
     */
    public void setNumber(String key, Number value) {
        dataNumbers.put(key, value);
    }

    /**
     * Determines if a specified Number setting exists.
     * 
     * @param key String key that represents wanted Number setting
     * @return true if setting exists, false otherwise
     */
    public boolean containsNumber(String key) {
        return dataNumbers.containsKey(key);
    }

    /**
     * Retrieve a String setting if it exists, otherwise store setting with a default value and return newly
     * stored default value.
     * 
     * @param key String key to retrieve a setting with
     * @param defaultValue Default String value to store if setting does not already exist
     * @return String value stored under requested key
     */
    public String getString(String key, String defaultValue) {
        if(dataStrings.containsKey(key)) return dataStrings.get(key);
        else {
            dataStrings.put(key, defaultValue);
            return dataStrings.get(key);
        }
    }

    /**
     * Retrieve a String setting if it exists, otherwise store setting with a default value of "" and return newly
     * stored default value.
     * 
     * @param key String key to retrieve a setting with
     * @return String value stored under requested key
     */
    public String getString(String key) { return getString(key, ""); }

    /**
     * Stores a String setting.
     *
     * @param key String key to store a setting with
     * @param value String value to store
     */
    public void setString(String key, String value) {
        dataStrings.put(key, value);
    }

    /**
     * Determines if a specified String setting exists.
     *
     * @param key String key that represents wanted String setting
     * @return true if setting exists, false otherwise
     */
    public boolean containsString(String key) {
        return dataStrings.containsKey(key);
    }

    /**
     * Retrieve a Boolean setting if it exists, otherwise store setting with a default value and return newly
     * stored default value.
     * 
     * @param key String key to retrieve a setting with
     * @param defaultValue Default Boolean value to store if setting does not already exist
     * @return Boolean value stored under requested key
     */
    public Boolean getBoolean(String key, Boolean defaultValue) {
        if(dataBooleans.containsKey(key)) return dataBooleans.get(key);
        else {
            dataBooleans.put(key, defaultValue);
            return dataBooleans.get(key);
        }
    }

    /**
     * Retrieve a Boolean setting if it exists, otherwise store setting with a default value of 'false' and return newly
     * stored default value.
     * 
     * @param key String key to retrieve a setting with
     * @return Boolean value stored under requested key
     */
    public Boolean getBoolean(String key) { return getBoolean(key, false); }

    /**
     * Stores a Boolean setting.
     *
     * @param key String key to store a setting with
     * @param value Boolean value to store
     */
    public void setBoolean(String key, Boolean value) {
        dataBooleans.put(key, value);
    }

    /**
     * Determines if a specified Boolean setting exists.
     *
     * @param key String key that represents wanted Boolean setting
     * @return true if setting exists, false otherwise
     */
    public boolean containsBoolean(String key) {
        return dataBooleans.containsKey(key);
    }

    /**
     * Clears all setings from this SettingsMap.
     */
    public void clear() {
        dataNumbers.clear();
        dataStrings.clear();
        dataBooleans.clear();
    }

    /**
     * Determines if this SettingsMap is empty.
     *
     * @return true if there are no settings stored, false otherwise
     */
    public boolean isEmpty() {
        return dataNumbers.isEmpty()
                && dataStrings.isEmpty()
                && dataBooleans.isEmpty();
    }

    /**
     * Determines how many settings are stored in this SettingsMap.
     *
     * @return number of stored settings
     */
    public int size() {
        return dataNumbers.size()
                + dataStrings.size()
                + dataBooleans.size();
    }
}
