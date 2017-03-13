package plu.red.reversi.core;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import plu.red.reversi.core.util.SettingsMap;

import static org.junit.Assert.*;

/**
 * Glory to the Red Team.
 */
public class SettingsMapTest {

    @Test
    public void testNumbers() {
        SettingsMap settings = new SettingsMap();
        assertEquals(false, settings.containsNumber("TestNumber"));
        assertEquals(null, settings.getNumber("TestNumber"));
        settings.setNumber("TestNumber", 1);
        assertEquals(1, settings.getNumber("TestNumber"));
        assertEquals(-123.456, settings.getNumber("ComplexNumber", -123.456));
        assertEquals(-123.456, settings.getNumber("ComplexNumber", 42));
        settings.setNumber("ComplexNumber", 99999);
        assertEquals(99999, settings.getNumber("ComplexNumber"));
    }

    @Test
    public void testStrings() {
        SettingsMap settings = new SettingsMap();
        assertEquals(false, settings.containsString("TestString"));
        assertEquals(null, settings.getString("TestString"));
        settings.setString("TestString", "test");
        assertEquals("test", settings.getString("TestString"));
        assertEquals("This is a long string", settings.getString("ComplexString", "This is a long string"));
        assertEquals("This is a long string", settings.getString("ComplexString", "This should not be saved"));
        settings.setString("ComplexString", "This should be saved");
        assertEquals("This should be saved", settings.getString("ComplexString"));
    }

    @Test
    public void testBooleans() {
        SettingsMap settings = new SettingsMap();
        assertEquals(false, settings.containsBoolean("TestBoolean"));
        assertEquals(null, settings.getBoolean("TestBoolean"));
        settings.setBoolean("TestBoolean", true);
        assertEquals(true, settings.getBoolean("TestBoolean"));
        assertEquals(true, settings.getBoolean("ComplexBoolean", true));
        assertEquals(true, settings.getBoolean("ComplexBoolean", false));
        settings.setBoolean("ComplexBoolean", false);
        assertEquals(false, settings.getBoolean("ComplexBoolean"));
    }

    @Test
    public void testSizes() {
        SettingsMap settings = new SettingsMap();
        assertEquals(true, settings.isEmpty());
        settings.getNumber("Fill");
        settings.setString("Fill", "Spam");
        settings.getBoolean("Fill");
        settings.setNumber("Fill2", 8);
        settings.setNumber("Fill2", -13);
        settings.setBoolean("Fill", true);
        settings.setBoolean("Fill2", false);
        assertEquals(false, settings.isEmpty());
        assertEquals(4, settings.size());
        settings.clear();
        assertEquals(true, settings.isEmpty());
        assertEquals(0, settings.size());
    }

    @Test
    public void testToJSON() {
        SettingsMap settings = new SettingsMap();
        settings.setBoolean("Val1", true);
        settings.setBoolean("Val2", false);
        settings.setString("Val1", "A String");
        settings.setString("Val2", "Another String");
        settings.setString("Val3", "Yet Another String");
        settings.setNumber("Val1", 42);
        settings.setNumber("Val2", -12345);
        settings.setNumber("Val3", 592.593);
        JSONObject json = settings.toJSON();
        try {
            assertEquals(true,  json.getJSONObject("booleans").getBoolean("Val1"));
            assertEquals(false, json.getJSONObject("booleans").getBoolean("Val2"));
            assertEquals("A String",            json.getJSONObject("strings").getString("Val1"));
            assertEquals("Another String",      json.getJSONObject("strings").getString("Val2"));
            assertEquals("Yet Another String",  json.getJSONObject("strings").getString("Val3"));
            assertEquals(42,    json.getJSONObject("numbers").getInt("Val1"));
            assertEquals(-12345,    json.getJSONObject("numbers").getInt("Val2"));
            assertEquals(592.593,   json.getJSONObject("numbers").getDouble("Val3"), 0.00001);
        } catch(JSONException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testFromJSON() {
        JSONObject json = new JSONObject();
        try {
            JSONObject booleanMap = new JSONObject();
            booleanMap.put("Val1", true);
            booleanMap.put("Val2", false);
            json.put("booleans", booleanMap);
            JSONObject stringMap = new JSONObject();
            stringMap.put("Val1", "A String");
            stringMap.put("Val2", "Another String");
            stringMap.put("Val3", "Yet Another String");
            json.put("strings", stringMap);
            JSONObject numberMap = new JSONObject();
            numberMap.put("Val1", 42);
            numberMap.put("Val2", -12345);
            numberMap.put("Val3", 592.593);
            json.put("numbers", numberMap);
        } catch(JSONException ex) {
            fail(ex.getMessage());
        }
        SettingsMap settings = new SettingsMap(json);
        assertEquals(true, settings.getBoolean("Val1"));
        assertEquals(false, settings.getBoolean("Val2"));
        assertEquals("A String", settings.getString("Val1"));
        assertEquals("Another String", settings.getString("Val2"));
        assertEquals("Yet Another String", settings.getString("Val3"));
        assertEquals(42, settings.getNumber("Val1").intValue());
        assertEquals(-12345, settings.getNumber("Val2").intValue());
        assertEquals(592.593, settings.getNumber("Val3").doubleValue(), 0.00001);
    }


}