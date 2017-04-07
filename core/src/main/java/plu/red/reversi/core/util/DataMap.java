package plu.red.reversi.core.util;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * DataMap is a data structure designed to store values based on keys; similar to a map, with a few differences.
 * A DataMap can store any value type regardless of any other value types it currently has (IE can store Booleans
 * and Strings at the same time), and its keys are always Strings that represent a particular Setting's name.
 * In addition, a DataMap is designed to be able to be written to and read from a JSON representation (this
 * implementation uses the Jettison JSON library).
 *
 * Anyone who wishes can feel free to re-use this class in current and/or future projects, as long as proper credit is
 * given.
 *
 * @author James De Broeck (its ma baby)
 */
public class DataMap {

    // The atual data that is stored. This is the only data member in this class.
    protected HashMap<String, Setting> dataMap = new HashMap<>();

    /**
     * Default (Empty) Constructor. Creates a brand new DataMap, with nothing stored.
     */
    public DataMap() {
        // Default Constructor doesn't do anything, but allows for this class to also have a non-default constructor
    }

    /**
     * JSON Constructor. Creates a new DataMap and populates it with data from a given JSON representation.
     *
     * @param json JSONObject to populate with
     * @throws RuntimeException if there is an issue when parsing the JSON representation into a DataMap
     */
    public DataMap(JSONObject json) throws RuntimeException {
        // Iterate through the JSONObject's keys
        Iterator<String> it = json.keys();
        while(it.hasNext()) {
            String key = it.next();
            try {
                // Does this Setting's value extends Comparable?
                Class clazz = Class.forName(json.getJSONObject(key).getString("class"));
                if(Comparable.class.isAssignableFrom(clazz))
                    // If so, create a BoundedSetting
                    dataMap.put(key, BoundedSetting.fromJSONBounded(json.getJSONObject(key)));
                else
                    // Otherwise create a normal Setting
                    dataMap.put(key, Setting.fromJSON(json.getJSONObject(key)));
            } catch(JSONException | ClassNotFoundException ex) {
                // Report any problems in a RuntimeException
                throw new RuntimeException("Problem when turning JSON into settings: " + ex.getMessage());
            }
        }
    }

    /**
     * JSON Converter. Attempts to convert this SettingMap into a JSON representation based upon JSON converters
     * statically registered to the Setting class.
     *
     * @return JSONObject representation of this SettingMap
     * @throws RuntimeException if there is an issue writing a JSON representation from the DataMap
     */
    public JSONObject toJSON() throws RuntimeException {
        JSONObject result = new JSONObject();
        try {
            // Iterate through the DataMap and convert every entry to a JSONObject to insert into the main JSONObject
            for(Map.Entry<String, Setting> entry : dataMap.entrySet())
                result.put(entry.getKey(), entry.getValue().toJSON());
        } catch(JSONException ex) {
            // Report any problems in a RuntimeException
            throw new RuntimeException("Problem when turning settings into a JSON: " + ex.getMessage());
        }
        return result;
    }

    /**
     * Retrieves the set of entries in the internal map. Useful for ForEach iteration.
     *
     * @return The result of dataMap.entrySet()
     */
    public Set<Map.Entry<String, Setting>> entrySet() {
        return dataMap.entrySet();
    }

    /**
     * Does this DataMap contain a specific String key?
     *
     * @param key String key to check
     * @return true if the String key exists, false otherwise
     */
    public boolean containsKey(String key) {
        return dataMap.containsKey(key);
    }

    /**
     * Retrieves the Object value associated with a given key, if it exists. Otherwise returns null.
     *
     * @param key String key
     * @return Object associated with key, or null if the key doesn't exist
     */
    public Object get(String key) {
        if(dataMap.containsKey(key)) return dataMap.get(key).getValue();
        else return null;
    }

    /**
     * Retrieves the Setting associated with a given key, if it exists. Otherwise returns null.
     *
     * @param key String key
     * @return Setting associated with a key, or null if the key doesn't exist
     */
    public Setting getSetting(String key) {
        if(dataMap.containsKey(key)) return dataMap.get(key);
        else return null;
    }

    /**
     * Retrieves the value associated with a given key, if it exists. If the key doesn't exist, returns null.
     * Will attempt to cast existing value to the given Class type, if it does not already match.
     *
     * @param key String key
     * @param type Class type to filter by
     * @param <E> Generic type matching the given Class type
     * @return Value of type E, or null if the key doesn't exist
     * @throws IllegalArgumentException if key exists and it is not possible to cast existing value to requested Class type
     */
    @SuppressWarnings("unchecked")
    public <E> E get(String key, Class<E> type) throws IllegalArgumentException {
        if(dataMap.containsKey(key)) {
                try {
                    // Attempt to cast to requested type
                    return (E)dataMap.get(key).getValue();
                } catch(ClassCastException ex) {
                    // Casting failed
                    throw new IllegalArgumentException("Setting Type of '" + dataMap.get(key).getValue().getClass() + "' does not match requested Type of '" + type + "'");
                }
        } else return null; // Key doesn't exist
    }

    /**
     * Retrieves the Setting associated with a given key, if it exists. If the key doesn't exist, returns null.
     * Will throw an exception if the existing Setting generic type does not match the requested type.
     *
     * @param key String key
     * @param type Class type to filter by
     * @param <E> Generic type matching the given Class type
     * @return Setting with generic type of E, or null if the key doesn't exist
     * @throws IllegalArgumentException if key exists and existing Setting generic type does not match the requested type
     */
    @SuppressWarnings("unchecked")
    public <E> Setting<E> getSetting(String key, Class<E> type) throws IllegalArgumentException {
        if(dataMap.containsKey(key)) {
            Setting setting = dataMap.get(key);
            // Does the type match?
            if(type.isInstance(setting.getValue()))
                return (Setting<E>)setting;
            else
                throw new IllegalArgumentException("Setting Type of '" + setting.getValue().getClass() + "' does not match requested Type of '" + type + "'");
        } else return null; // Key doesn't exist
    }

    /**
     * Retrieves the BoundedSetting associated with a given key, if it exists. If the key doesn't exist, returns null.
     * Will throw an exception if the existing Setting generic type does not match the requested type, or if the existing
     * Setting is not an instance of a BoundedSetting.
     *
     * @param key String key
     * @param type Class type to filter by
     * @param <E> Generic type matching the given Class type
     * @return BoundedSetting with generic type of E, or null if the key doesn't exist
     * @throws IllegalArgumentException if existing Setting is not a BoundedSetting or its generic type does not match
     * the requested type
     */
    @SuppressWarnings("unchecked")
    public <E extends Comparable<E>> BoundedSetting<E> getBoundedSetting(String key, Class<E> type) throws IllegalArgumentException {
        if(dataMap.containsKey(key)) {
            Setting setting = dataMap.get(key);
            // Is it a BoundedSetting?
            if(!(setting instanceof BoundedSetting))
                throw new IllegalArgumentException("Setting requested for '" + type + "' is not a Bounded Setting");
            // Does the type match?
            if(type.isInstance(setting.getValue()))
                return (BoundedSetting<E>)setting;
            else
                throw new IllegalArgumentException("Setting Type of '" + setting.getValue().getClass() + "' does not match requsted Type of '" + type + "'");
        } else return null; // Key doesn't exist
    }

    /**
     * Checks to see if a Setting exists, and creates it if it doesn't.
     *
     * @param key String key
     * @param defaultValue Value to set setting to if it doesn't exist
     * @param <E> Generic type of default value
     */
    public <E> void checkDefault(String key, E defaultValue) {
        if(!dataMap.containsKey(key))
            dataMap.put(key, new Setting<>(defaultValue));
    }

    /**
     * Checks to see if a Setting exists, and creates it if it doesn't.
     *
     * @param key String key
     * @param defaultValue Value to set setting to if it doesn't exist
     * @param defaultDescription Description to attach to value (may be null)
     * @param <E> Generic type of default value
     */
    public <E> void checkDefault(String key, E defaultValue, String defaultDescription) {
        if(!dataMap.containsKey(key))
            dataMap.put(key, new Setting<>(defaultValue, defaultDescription));
    }

    /**
     * Checks to see if a Setting exists, and creates a BoundedSetting if it doesn't.
     *
     * @param key String key
     * @param defaultValue Value to set setting to if it doesn't exist
     * @param min Minimum constraint (may be null for no constraint)
     * @param max Maximum constraint (may be null for no constraint)
     * @param <E> Generic type of default value and constraints
     */
    public <E extends Comparable<E>> void checkDefault(String key, E defaultValue, E min, E max) {
        if(!dataMap.containsKey(key))
            dataMap.put(key, new BoundedSetting<>(defaultValue, min, max));
    }

    /**
     * Checks to see if a Setting exists, and creates a BoundedSetting if it doesn't.
     *
     * @param key String key
     * @param defaultValue Value to set setting to if it doesn't exist
     * @param defaultDescription Description to attach to value (may be null)
     * @param min Minimum constraint (may be null for no constraint)
     * @param max Maximum constraint (may be null for no constraint)
     * @param <E> Generic type of default value and constraints
     */
    public <E extends Comparable<E>> void checkDefault(String key, E defaultValue, String defaultDescription, E min, E max) {
        if(!dataMap.containsKey(key))
            dataMap.put(key, new BoundedSetting<E>(defaultValue, defaultDescription, min, max));
    }

    /**
     * Checks to see if a Setting exists, and creates it if it doesn't.
     *
     * @param key String key
     * @param defaultSetting Setting to set if it doesn't exist
     * @param <E> Generic type of default setting value
     */
    public <E> void checkDefaultSetting(String key, Setting<E> defaultSetting) {
        if(!dataMap.containsKey(key))
            dataMap.put(key, defaultSetting);
    }

    /**
     * Retrieves the value associated with a given key, if it exists. If it doesn't exist, create it with the given
     * default value.
     *
     * @param key String key
     * @param defaultValue Value to set setting to if it doesn't exist
     * @param <E> Generic type of default value and requested type
     * @return Existing value of type E, or newly created default value
     * @throws IllegalArgumentException if setting exists and casting to requested type fails
     */
    @SuppressWarnings("unchecked")
    public <E> E get(String key, E defaultValue) throws IllegalArgumentException {
        checkDefault(key, defaultValue);
        return get(key, (Class<E>)defaultValue.getClass());
    }

    /**
     * Retrieves the value associated with a given key, if it exists. If it doesn't exist, create it with the given
     * default value and description.
     *
     * @param key String key
     * @param defaultValue Value to set setting to if it doesn't exist
     * @param defaultDescription Description to attach to value (may be null)
     * @param <E> Generic type of default value and requested type
     * @return Existing value of type E, or newly created defaut value
     * @throws IllegalArgumentException if setting exists and casting to requested type fails
     */
    @SuppressWarnings("unchecked")
    public <E> E get(String key, E defaultValue, String defaultDescription) throws IllegalArgumentException {
        checkDefault(key, defaultValue, defaultDescription);
        return get(key, (Class<E>)defaultValue.getClass());
    }

    /**
     * Retrieves the value associated with a given key, if it exists. If it doesn't exist, create BoundedSetting with
     * the given default value and constraints.
     *
     * @param key String key
     * @param defaultValue Value to set setting to if it doesn't exist
     * @param min Minimum constraint (may be null for no constraint)
     * @param max Maximum constraint (may be null for no constraint)
     * @param <E> Generic type of default value and requested type
     * @return Existing value of type E, or newly created default value
     * @throws IllegalArgumentException if setting exists and casting to requested type fails
     */
    @SuppressWarnings("unchecked")
    public <E extends Comparable<E>> E get(String key, E defaultValue, E min, E max) throws IllegalArgumentException {
        checkDefault(key, defaultValue, min, max);
        return get(key, (Class<E>)defaultValue.getClass());
    }

    /**
     * Retrieves the value associated with a given key, if it exists. If it doesn't exist, create BoundedSetting with
     * the given default value, description and constraints.
     *
     * @param key String key
     * @param defaultValue Value to set setting to if it doesn't exist
     * @param defaultDescription Description to attach to value (may be null)
     * @param min Minimum constraint (may be null for no constraint)
     * @param max Maximum constraint (may be null for no constraint)
     * @param <E> Generic type of default value and requested type
     * @return Existing value of type E, or newly created default value
     * @throws IllegalArgumentException if setting exists and casting to requested type fails
     */
    @SuppressWarnings("unchecked")
    public<E extends Comparable<E>> E get(String key, E defaultValue, String defaultDescription, E min, E max) throws IllegalArgumentException {
        checkDefault(key, defaultValue, defaultDescription, min, max);
        return get(key, (Class<E>)defaultValue.getClass());
    }

    /**
     * Retrieves the Setting associated with a given key, if it exists. If it doesn't exist, create it with the given
     * default Setting.
     *
     * @param key String key
     * @param defaultSetting Setting to set to if it doesn't exist
     * @param <E> Generic type of default setting value and requested type
     * @return Existing setting of type E, or newly created default setting
     * @throws IllegalArgumentException if setting exists and does not match requested type
     */
    @SuppressWarnings("unchecked")
    public <E> Setting<E> getSetting(String key, Setting<E> defaultSetting) throws IllegalArgumentException {
        checkDefaultSetting(key, defaultSetting);
        return getSetting(key, (Class<E>)defaultSetting.getValue().getClass());
    }

    /**
     * Retrieves the BoundedSetting associated with a given key, if it exists. If it doesn't exist, create it with the
     * given default BoundedSetting.
     *
     * @param key String key
     * @param defaultSetting BoundedSetting to set to if it doesn't exist
     * @param <E> Generic type of default setting value and requested type
     * @return Existing BoundedSetting of type E, or newly created default BoundedSetting
     * @throws IllegalArgumentException if setting exists and does not match requested type or is not a BoundedSetting
     */
    @SuppressWarnings("unchecked")
    public <E extends Comparable<E>> BoundedSetting<E> getBoundedSetting(String key, BoundedSetting<E> defaultSetting) throws IllegalArgumentException {
        checkDefaultSetting(key, defaultSetting);
        return getBoundedSetting(key, (Class<E>)defaultSetting.getValue().getClass());
    }

    /**
     * Sets a value associated with a key, changing an existing setting, or creating a new one if no setting exists
     * or existing setting has incompatible type.
     *
     * @param key String key
     * @param value Value to set
     * @param <E> Generic type of value
     */
    @SuppressWarnings("unchecked")
    public <E> void set(String key, E value) {
        if(dataMap.containsKey(key)) {
            Setting setting = dataMap.get(key);
            try {
                // Try to cast
                setting.setValue(setting.getValue().getClass().cast(value));
            } catch(ClassCastException ex) {
                // Overwrite as new if casting fails
                dataMap.put(key, new Setting<>(value));
            }
        }
        // No setting exists, create one
        else dataMap.put(key, new Setting<>(value));
    }

    /**
     * Sets a value and description associated with a key, changing an existing setting, or creating a new one if no
     * setting exists or existing setting has incompatible type.
     *
     * @param key String key
     * @param value Value to set
     * @param description Description to set (may be null)
     * @param <E> Generic type of value
     */
    @SuppressWarnings("unchecked")
    public <E> void set(String key, E value, String description) {
        if(dataMap.containsKey(key)) {
            Setting setting = dataMap.get(key);
            try {
                // Try to cast
                setting.setValue(setting.getValue().getClass().cast(value));
                setting.setDescription(description);
            } catch(ClassCastException ex) {
                // Overwrite as new if casting fails
                dataMap.put(key, new Setting<>(value, description));
            }
        }
        // No setting exists, create one
        else dataMap.put(key, new Setting<>(value, description));
    }

    /**
     * Sets a value and constraints associated with a key, changing an existing BoundedSetting, or creating a new one if no
     * setting exists or existing setting has incompatible type. If setting exists and isn't a BoundedSetting, overwrites
     * it with new BoundedSetting.
     *
     * @param key String key
     * @param value Value to set
     * @param min Minimum constraint (may be null for no constraint)
     * @param max Maximum constraint (may be null for no constraint)
     * @param <E> Generic type of value
     */
    @SuppressWarnings("unchecked")
    public <E extends Comparable<E>> void set(String key, E value, E min, E max) {
        if(dataMap.containsKey(key) && dataMap.get(key) instanceof BoundedSetting) {
            Setting setting = dataMap.get(key);
            try {
                // Try to cast
                setting.setValue(setting.getValue().getClass().cast(value));
                ((BoundedSetting)setting).setConstraints(min, max);
            } catch(ClassCastException ex) {
                // Overwrite as new if casting fails
                dataMap.put(key, new BoundedSetting<>(value, min, max));
            }
        }
        // No setting exists, create one
        else dataMap.put(key, new BoundedSetting<>(value, min, max));
    }

    /**
     * Sets a value, description and constraints associated with a key, changing an existing BoundedSetting, or creating
     * a new one if no setting exists or existing setting has incompatible type. If setting exists and isn't a
     * BoundedSetting, overwrites it with new BoundedSetting.
     *
     * @param key String key
     * @param value Value to set
     * @param description Description to set (may be null)
     * @param min Minimum constraint (may be null for no constraint)
     * @param max Maximum constraint (may be null for no constraint)
     * @param <E> Generic type of value
     */
    @SuppressWarnings("unchecked")
    public <E extends Comparable<E>> void set(String key, E value, String description, E min, E max) {
        if(dataMap.containsKey(key) && dataMap.get(key) instanceof BoundedSetting) {
            Setting setting = dataMap.get(key);
            try {
                // Try to cast
                setting.setValue(setting.getValue().getClass().cast(value));
                setting.setDescription(description);
                ((BoundedSetting)setting).setConstraints(min, max);
            } catch(ClassCastException ex) {
                // Overwrite as new if casting fails
                dataMap.put(key, new BoundedSetting<>(value, description, min, max));
            }
        }
        // No setting exists, create one
        else dataMap.put(key, new BoundedSetting<>(value, description, min, max));
    }

    /**
     * Sets a setting associated with a key, overwriting any existing setting.
     *
     * @param key String key
     * @param setting Setting to set
     * @param <E> Generic type of setting's value
     */
    public <E> void setSetting(String key, Setting<E> setting) {
        dataMap.put(key, setting);
    }

    /**
     * Sets a BoundedSetting associated with a key, overwriting any existing setting.
     *
     * @param key String key
     * @param setting BoundedSetting to set
     * @param <E> Generic type of BoundedSetting's value
     */
    public <E extends Comparable<E>> void setBoundedSetting(String key, BoundedSetting<E> setting) {
        dataMap.put(key, setting);
    }

    /**
     * Clears all setings from this DataMap.
     */
    public void clear() {
        dataMap.clear();
    }

    /**
     * Determines if this DataMap is empty.
     *
     * @return true if there are no settings stored, false otherwise
     */
    public boolean isEmpty() {
        return dataMap.isEmpty();
    }

    /**
     * Determines how many settings are stored in this DataMap.
     *
     * @return number of stored settings
     */
    public int size() {
        return dataMap.size();
    }


    /**
     * Setting class. Represents a single setting stored in a SettingMap. A setting may contains a value, and
     * (optionally) a description of what the setting is for. The Setting class also has method for converting
     * a single Setting to and from a JSONObject (each setting in a JSON representation of a SettingMap is its
     * own JSONObject).
     *
     * @param <T> A Setting object is Generic typed so that it may store any Object. By default, only the simple
     *           objects String, Boolean, and various subtypes of Number are supported for JSON conversion, but
     *           any Object may stored regardless of existing JSON converters. To make another Object type
     *           compatible with JSON conversion, register an Inserter and Extractor with the static method
     *           registerConverter().
     */
    public static class Setting<T> {

        /**
         * Interface for JSON Insertions. Used for lambda functionality.
         *
         * @param <E> Type of Object to insert.
         */
        public interface JSONInserter<E> {
            /**
             * Method to insert Object of type E into a JSONObject.
             *
             * @param key String key to insert into JSONObject with
             * @param value Object to insert
             * @param json JSONObject to insert into
             * @throws JSONException if there is a problem inserting into the JSONObject
             */
            void insert(String key, E value, JSONObject json) throws JSONException;
        }

        /**
         * Interface for JSON Extractions. Use for lambda functionality.
         *
         * @param <E> Type of Object to extract.
         */
        public interface JSONExtractor<E> {
            /**
             * Method to extract Object of type E from a JSONObject.
             *
             * @param key String key to find Object in JSONObject with
             * @param json JSONObject to extract from
             * @return extracted Object
             * @throws JSONException if there is a problem extracting from the JSONObject
             */
            E extract(String key, JSONObject json) throws JSONException;
        }

        /**
         * Pair wrapper for a pair of JSONInserter and JSONExtractor.
         *
         * @param <E> Generic type of JSONInserter and JSONExtractor
         */
        protected static final class JSONConverter<E> {
            public final JSONInserter<E> inserter; public final JSONExtractor<E> extractor;
            public JSONConverter(JSONInserter<E> inserter, JSONExtractor<E> extractor) {
                this.inserter = inserter; this.extractor = extractor;
            }
        }

        // Internal registry of JSONConverters
        protected static final HashMap<Class, JSONConverter> convertMap = new HashMap<>();

        /**
         * Register a new JSON conversion. Will be used when converting Object of the type given to and from a JSON
         * representation.
         *
         * @param clazz Class type that will be converted
         * @param inserter JSONInserter lambda
         * @param extractor JSONExtractor lambda
         * @param <E> Generic type matching the given Class type
         */
        public static <E> void registerConverter(Class<E> clazz, JSONInserter<E> inserter, JSONExtractor<E> extractor) {
            convertMap.put(clazz, new JSONConverter<>(inserter, extractor));
        }

        static {

            // Register some basic Conversions
            registerConverter(Boolean.class, (key, value, json) -> json.put(key, value), (key, json) -> json.getBoolean(key));
            registerConverter(Number.class,  (key, value, json) -> json.put(key, value), (key, json) -> json.getDouble(key));
            registerConverter(String.class,  (key, value, json) -> json.put(key, value), (key, json) -> json.getString(key));

            // Register more basic Conversions for specific Number types
            registerConverter(Integer.class, (key, value, json) -> json.put(key, value), (key, json) -> json.getInt(key));
            registerConverter(Float.class,   (key, value, json) -> json.put(key, value), (key, json) -> (float)json.getDouble(key) );
            registerConverter(Double.class,  (key, value, json) -> json.put(key, value), (key, json) -> json.getDouble(key));
            registerConverter(Short.class,   (key, value, json) -> json.put(key, value), (key, json) -> (short)json.getInt(key));
            registerConverter(Long.class,    (key, value, json) -> json.put(key, value), (key, json) -> (long)json.getInt(key));

            // Register Recursive Conversion
            registerConverter(DataMap.class, (key, value, json) -> json.put(key, value.toJSON()), (key, json) -> new DataMap(json.getJSONObject(key)));
        }

        protected String description;
        protected T value;

        /**
         * Create a new Setting with a given value and no description.
         *
         * @param value Object of type T
         */
        public Setting(T value) {
            this.description = null;
            this.value = value;
        }

        /**
         * Create a new Setting with a given value and description.
         *
         * @param value Object of type T
         * @param description String description of what this Setting is used for (may be null for no description)
         */
        public Setting(T value, String description) {
            this.description = description;
            this.value = value;
        }

        /**
         * Retrieves this Setting's description.
         *
         * @return String description, or null if there is no description
         */
        public String getDescription() { return description; }

        /**
         * Sets this Setting's description.
         *
         * @param description String description (may be null for no description)
         */
        public void setDescription(String description) { this.description = description; }

        /**
         * Retrieves this Setting's value.
         *
         * @return Object of type T
         */
        public T getValue() { return value; }

        /**
         * Sets this Setting's value.
         *
         * @param value Object of type T
         */
        public void setValue(T value) { this.value = value; }

        /**
         * Converts this Setting to a JSON representation.
         *
         * @return JSONObject representing this Setting
         * @throws JSONException if there is a problem during conversion
         * @throws IllegalArgumentException if no JSON conversion is registered for type T
         */
        @SuppressWarnings("unchecked")
        public JSONObject toJSON() throws JSONException, IllegalArgumentException {
            JSONObject jobj = new JSONObject();

            // Attempt to retrieve and use JSON conversion
            if(convertMap.containsKey(value.getClass()))
                ((JSONConverter<T>)convertMap.get(value.getClass())).inserter.insert("value", value, jobj);
            else
                throw new IllegalArgumentException("No JSON Converter Exists for Type " + value.getClass());

            // JSONObject will simply clear these fields if the Object given is null
            jobj.put("description", this.description);
            jobj.put("class", value.getClass().getName());

            return jobj;
        }

        /**
         * Converts a JSON representation of a Setting into an actual Setting object.
         *
         * @param jobj JSONObject representing a Setting
         * @param clazz Class type that the Setting stores
         * @param <E> Generic type matching given Class type
         * @return Setting object created from JSON representation
         * @throws JSONException if there is a problem during conversion
         * @throws IllegalArgumentException if no JSON conversion is registered for given Class type
         */
        @SuppressWarnings("unchecked")
        public static <E> Setting<E> fromJSON(JSONObject jobj, Class<E> clazz) throws JSONException, IllegalArgumentException {
            E value;
            String description = null;

            // Attempt to retrieve and use JSON conversion
            if(convertMap.containsKey(clazz))
                // Grab value
                value = ((JSONConverter<E>)convertMap.get(clazz)).extractor.extract("value", jobj);
            else
                throw new IllegalArgumentException("No JSON Converter Exists for Type " + clazz);

            // Attempt to grab description, or leave as null if no description is stored
            try { description = jobj.getString("description"); }
            catch(JSONException ex) {} // Doesn't exist

            return new Setting<>(value, description);
        }

        /**
         * Converts a JSON representation of a Setting into an actual Setting object. Attempts to determine Class type
         * of Setting based on stored String representation of Class type stored in JSON.
         *
         * @param jobj JSONObject representing a Setting
         * @param <E> Generic type matching stored Class type
         * @return Setting object created from JSON representation
         * @throws JSONException if there is a problem during conversion
         * @throws ClassNotFoundException if String representation of Class does not match an actual Class
         * @throws IllegalArgumentException if no JSON conversion is registered for stored Class type
         */
        @SuppressWarnings("unchecked")
        public static<E> Setting<E> fromJSON(JSONObject jobj) throws JSONException, ClassNotFoundException, IllegalArgumentException {
            return fromJSON(jobj, (Class<E>)Class.forName(jobj.getString("class")));
        }
    }

    /**
     * BoundedSetting class. Extension of the Setting class. Represents a Setting that has Minimum and/or Maximum
     * constraints on the possible value of the Setting.
     *
     * @param <T> Generic type that implements Comparable. Must implement Comparable for the BoundedSetting class so
     *           that less than / greater than checks can be made when setting this Setting's value.
     */
    public static class BoundedSetting<T extends Comparable<T>> extends Setting<T> {

        protected T min;
        protected T max;

        /**
         * Checks the values of min and max, and throws an Exception if min is not less than or equal max.
         */
        protected final void checkMinMaxOrder() {
            if(min != null && max != null && min.compareTo(max) > 0)
                throw new IllegalArgumentException("Minimum Constraint must be less than Maximum Constraint");
        }

        /**
         * Creates a new BoundedSetting with the given value, no description, and no constraints.
         *
         * @param value Object of type T
         */
        public BoundedSetting(T value) {
            super(value);
            this.min = null;
            this.max = null;
        }

        /**
         * Creates a new BoundedSetting with the given value and description, but no constraints.
         *
         * @param value Object of type T
         * @param description String description of what this setting is used for (may be null for no description)
         */
        public BoundedSetting(T value, String description) {
            super(value, description);
            this.min = null;
            this.max = null;
        }

        /**
         * Creates a new BoundedSetting with the given value and constraints, but no description.
         *
         * @param value Object of type T
         * @param min Minimum constraint (may be null for no constraint)
         * @param max Maximum constraint (may be null for no constraint)
         * @throws IllegalArgumentException if neither constraint is null and min is greater than max
         */
        public BoundedSetting(T value, T min, T max) throws IllegalArgumentException {
            super(value);
            this.min = min;
            this.max = max;
            checkMinMaxOrder();
        }

        /**
         * Creates a new BoundedSetting with the given value, description, and constraints.
         *
         * @param value Object of type T
         * @param description String description of what this seting is used for (may be null for no description)
         * @param min Minimum constraint (may be null for no constraint)
         * @param max Maximum constraint (may be null for no constraint)
         * @throws IllegalArgumentException if neither constraint is null and min is greater than max
         */
        public BoundedSetting(T value, String description, T min, T max) throws IllegalArgumentException {
            super(value, description);
            this.min = min;
            this.max = max;
            checkMinMaxOrder();
        }

        /**
         * Retrieves this Setting's Minimum constraint.
         *
         * @return Minimum constraint of type T, or null if there is no constraint
         */
        public T getMin() { return min; }

        /**
         * Retrieves this Setting's Maximum constraint.
         *
         * @return Maximum constraint of type T, or null if there is no constraint
         */
        public T getMax() { return max; }

        /**
         * Sets this Setting's constraints.
         *
         * @param min Minimum constraint (may be null for no constraint)
         * @param max Maximum constraint (may be null for no constraint)
         * @throws IllegalArgumentException if neither constraint is null, and min is greater than max
         */
        public void setConstraints(T min, T max) throws IllegalArgumentException {
            this.min = min;
            this.max = max;
            checkMinMaxOrder();
        }

        /**
         * Sets this Setting's value. Ensures that the value stays between the Minimum and Maximum constraints.
         * If there is a Minimum constraint and the given value is less than said constraint, instead sets to Minimum constraint.
         * If there is a Maximum constraint and the given value is greater than said constraint, instead sets to Maximum constraint.
         *
         * @param value Object of type T
         */
        @Override
        public void setValue(T value) {
            if(value == null) this.value = null;
            else {
                if(min != null && value.compareTo(min) < 0)
                    this.value = min;
                else if(max != null && value.compareTo(max) > 0)
                    this.value = max;
                else
                    this.value = value;
            }
        }

        /**
         * Converts this BoundedSetting to a JSON representation.
         *
         * @return JSONObject representing this BoundedSetting
         * @throws JSONException if there is a problem during conversion
         * @throws IllegalArgumentException if no JSON conversion is registered for type T
         */
        @Override
        @SuppressWarnings("unchecked")
        public JSONObject toJSON() throws JSONException {

            // Get a JSON representation of basic Setting from the super method
            JSONObject jobj = super.toJSON();

            // Attempt to retrieve and use JSON conversion
            if(convertMap.containsKey(value.getClass())) {
                JSONConverter<T> converter = (JSONConverter<T>) convertMap.get(value.getClass());

                // Store constraints
                converter.inserter.insert("min", min, jobj);
                converter.inserter.insert("max", max, jobj);
            } else
                throw new IllegalArgumentException("No JSON Converter Exists for Type " + value.getClass());

            return jobj;
        }

        /**
         * Converts a JSON representation of a BoundedSetting into an actual BoundedSetting object.
         *
         * @param jobj JSONObject representing a BoundedSetting
         * @param clazz Class type implementing Comparable that the BoundedSetting stores
         * @param <E> Generic type matching given Class type
         * @return BoundedSetting object created from JSON representation
         * @throws JSONException if there is a problem during conversion
         * @throws IllegalArgumentException if no JSON conversion is registered for given Class type
         */
        @SuppressWarnings("unchecked")
        public static <E extends Comparable<E>> BoundedSetting<E> fromJSONBounded(JSONObject jobj, Class<E> clazz) throws JSONException {
            E value, min = null, max = null;
            String description = null;

            // Attempt to retrieve and use JSON conversion
            if(convertMap.containsKey(clazz)) {
                JSONConverter<E> converter = (JSONConverter<E>) convertMap.get(clazz);

                // Grab value
                value = converter.extractor.extract("value", jobj);

                // Attempt to grab minimum constraint, or leave null if it doesn't exist
                try { min = converter.extractor.extract("min", jobj); }
                catch(JSONException ex) {} // Doesn't exist

                // Attempt to grab maximum constraint, or leave null if it doesn't exist
                try { max = converter.extractor.extract("max", jobj); }
                catch(JSONException ex) {} // Doesn't exist

            } else throw new IllegalArgumentException("No JSON Converter Exists for Type " + clazz);

            // Attempt to grab description, or leave null if it doesn't exist
            try { description = jobj.getString("description"); }
            catch(JSONException ex) {} // Doesn't exist

            return new BoundedSetting<>(value, description, min, max);
        }

        /**
         * Converts a JSON representation of a BoundedSetting into an actual BoundedSetting object. Attempts to
         * determine Class type of BoundedSetting based on String representation of Class type stored in JSON.
         *
         * @param jobj JSONObject representing a Setting
         * @param <E> Generic type matching stored Class type
         * @return BoundedSetting object created from JSON representation
         * @throws JSONException if there is a problem during conversion
         * @throws ClassNotFoundException if String representation of Class does not match an actual Class
         * @throws IllegalArgumentException if no JSON conversion is registered for stored Class type
         */
        @SuppressWarnings("unchecked")
        public static<E extends Comparable<E>> BoundedSetting<E> fromJSONBounded(JSONObject jobj) throws JSONException, ClassNotFoundException {
            return fromJSONBounded(jobj, (Class<E>)Class.forName(jobj.getString("class")));
        }

    }
}
