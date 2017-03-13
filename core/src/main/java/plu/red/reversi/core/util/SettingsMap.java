package plu.red.reversi.core.util;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Glory to the Red Team.
 *
 * Settings map to store game settings in an abstract manner. This is one possible method to store settings.
 */
public class SettingsMap {

    /**
     * Internal Wrapper class for Number which optionally has constraints as well.
     */
    public static final class NumWrapper {
        private Number number;
        private Number min;
        private Number max;

        public NumWrapper(Number number) {
            this.number = number;
            this.min = null;
            this.max = null;
        }

        public NumWrapper(Number number, Number min, Number max) {
            this.min = min;
            this.max = max;
            set(number);
        }

        public Number get() { return number; }

        public void set(Number num) {
            if(num == null) num = 0;
            if(min != null && num.doubleValue() < min.doubleValue())       this.number = this.min;
            else if(max != null && num.doubleValue() > max.doubleValue())  this.number = this.max;
            else                                                           this.number = num;
        }

        public Number getMin() { return min; }
        public Number getMax() { return max; }

        public void setMin(Number num) { this.min = num; }
        public void setMax(Number num) { this.max = num; }

    }

    protected HashMap<String, NumWrapper>  dataNumbers  = new HashMap<String, NumWrapper>();
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
                Object obj = numberMap.opt(key);
                if(obj instanceof Number) dataNumbers.put(key, new NumWrapper((Number)obj));
                else if(obj instanceof JSONObject) {
                    JSONObject jobj = (JSONObject)obj;
                    Object val = jobj.opt("value");
                    Object min = jobj.opt("min");
                    Object max = jobj.opt("max");
                    if(val instanceof Number) {
                        dataNumbers.put(key, new NumWrapper(
                                (Number)val,
                                min instanceof Number ? (Number)min : null,
                                max instanceof Number ? (Number)max : null));
                    }
                }
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
            //result.put("numbers",  new JSONObject(dataNumbers));
            result.put("booleans", new JSONObject(dataBooleans));
            JSONObject numbers = new JSONObject();
            for(Map.Entry<String, NumWrapper> entry : dataNumbers.entrySet()) {
                JSONObject jobj = new JSONObject();
                NumWrapper val = entry.getValue();
                jobj.put("value", val.get());
                if(val.getMin() != null) jobj.put("min", val.getMin());
                if(val.getMax() != null) jobj.put("max", val.getMax());
                numbers.put(entry.getKey(), jobj);
            }
            result.put("numbers", numbers);
        } catch(JSONException ex) {
            throw new RuntimeException("Problem when turning settings into a JSON: " + ex.getMessage());
        }
        return result;
    }


    public Map<String, Boolean> getBooleanData() { return dataBooleans; }
    public Map<String, String> getStringData() { return dataStrings; }
    public Map<String, NumWrapper> getNumberData() { return dataNumbers; }


    /**
     * Retrieve a Number setting if it exists, otherwise store setting with a default value and return newly
     * stored default value.
     * 
     * @param key String key to retrieve a setting with
     * @param defaultValue Default Number value to store if setting does not already exist
     * @return Number value stored under requested key
     */
    public Number getNumber(String key, Number defaultValue) {
        return getNumber(key, defaultValue, null, null);
    }

    /**
     * Retrieve a Number setting if it exists, otherwise store setting with a default value and return newly
     * stored default value. Also stores optional minimum and maximum constraints to keep a value in between.
     *
     * @param key String key to retrieve a setting with
     * @param defaultValue Default Number value to store if setting does not already exist
     * @param min Minimum value constraint; leave null for no minimum
     * @param max Maximum value constraint; leave null for no maximum
     * @return Number value stored under requested key
     */
    public Number getNumber(String key, Number defaultValue, Number min, Number max) {
        if(dataNumbers.containsKey(key)) return dataNumbers.get(key).get();
        else {
            dataNumbers.put(key, new NumWrapper(defaultValue, min, max));
            return dataNumbers.get(key).get();
        }
    }

    /**
     * Retrieve a Number setting if it exists.
     * 
     * @param key String key to retrieve a setting with
     * @return Number value stored under requested key, or null if setting doesn't exist
     */
    public Number getNumber(String key) {
        if(dataNumbers.containsKey(key)) return dataNumbers.get(key).get();
        else return null;
    }

    /**
     * Stores a Number setting.
     * 
     * @param key String key to store a setting with
     * @param value Number value to store
     */
    public void setNumber(String key, Number value) {
        if(dataNumbers.containsKey(key)) dataNumbers.get(key).set(value);
        else setNumber(key, value, null, null);
    }

    /**
     * Stores a Number setting. Also stores optional minimum and maximum constraints to keep a value in between.
     *
     * @param key String key to store a setting with
     * @param value Number value to store
     * @param min Minimum value constraint; leave null for no minimum
     * @param max Maximum value constraint; leave null for no maximum
     */
    public void setNumber(String key, Number value, Number min, Number max) {
        dataNumbers.put(key, new NumWrapper(value, min, max));
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
     * Retrieve a String setting if it exists.
     *
     * @param key String key to retrieve a setting with
     * @return String value stored under requested key, or null if setting doesn't exist
     */
    public String getString(String key) {
        if(dataStrings.containsKey(key)) return dataStrings.get(key);
        else return null;
    }

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
     * Retrieve a Boolean setting if it exists.
     *
     * @param key String key to retrieve a setting with
     * @return Boolean value stored under requested key, or null if setting doesn't exist
     */
    public Boolean getBoolean(String key) {
        if(dataBooleans.containsKey(key)) return dataBooleans.get(key);
        else return null;
    }

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
