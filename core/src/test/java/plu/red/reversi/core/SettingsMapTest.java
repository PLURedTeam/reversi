package plu.red.reversi.core;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import plu.red.reversi.core.util.SettingsMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Glory to the Red Team.
 */
public class SettingsMapTest {

    private SettingsMap settings;

    @Before
    public void setValues() {
        settings = new SettingsMap();
        settings.set("Val1", "A String", "This is a test String");
        settings.set("Val2", false);
        settings.set("Val3", true, "This Boolean should be true");
        settings.set("Val4", "Second String");
        settings.set("Val5", 0, -99, 42);
        settings.set("Val6", -42, "The meaning of Life, the Universe, and Everything");
        settings.set("Val7", 1234.5678, "Double Test");
    }

    @Test
    public void testDescriptions() {
        assertEquals("This is a test String", settings.getSetting("Val1").getDescription());
        assertEquals(null, settings.getSetting("Val2").getDescription());
        assertEquals("This Boolean should be true", settings.getSetting("Val3").getDescription());
        assertEquals(null, settings.getSetting("Val4").getDescription());
        assertEquals(null, settings.getSetting("Val5").getDescription());
        assertEquals("The meaning of Life, the Universe, and Everything", settings.getSetting("Val6").getDescription());
        assertEquals("Double Test", settings.getSetting("Val7").getDescription());
    }

    @Test
    public void testContains() {
        assertEquals(true, settings.containsKey("Val1"));
        assertEquals(true, settings.containsKey("Val2"));
        assertEquals(true, settings.containsKey("Val3"));
        assertEquals(true, settings.containsKey("Val4"));
        assertEquals(true, settings.containsKey("Val5"));
        assertEquals(true, settings.containsKey("Val6"));
        assertEquals(true, settings.containsKey("Val7"));
        assertEquals(false, settings.containsKey("Val8"));
        assertEquals(false, settings.containsKey("Val9"));
        assertEquals(false, settings.containsKey("Val10"));
        assertEquals(false, settings.containsKey("Val11"));
        assertEquals(false, settings.containsKey("Val12"));
    }

    @Test
    public void testGetterSetterBasic() {
        assertEquals("A String", settings.get("Val1"));
        assertEquals("A String", settings.get("Val1", String.class));
        assertEquals(false, settings.get("Val2"));
        assertEquals(false, settings.get("Val2", Boolean.class));
        assertEquals(true, settings.get("Val3"));
        assertEquals(true, settings.get("Val3", Boolean.class));
        assertEquals("Second String", settings.get("Val4"));
        assertEquals("Second String", settings.get("Val4", String.class));
        assertEquals(0, settings.get("Val5"));
        assertEquals(0, settings.get("Val5", Integer.class).intValue());
        assertEquals(0, settings.get("Val5", Number.class).intValue());
        assertEquals(-42, settings.get("Val6"));
        assertEquals(-42, settings.get("Val6", Integer.class).intValue());
        assertEquals(-42, settings.get("Val6", Number.class).intValue());
        assertEquals(1234.5678, settings.get("Val7"));
        assertEquals(1234.5678, settings.get("Val7", Double.class), 0.000001);
        assertEquals(1234.5678, settings.get("Val7", Number.class).doubleValue(), 0.000001);
    }

    @Test
    public void testDefaults() {
        settings.checkDefault("Val1", "rand1");
        settings.checkDefault("Val2", "rand2");
        settings.checkDefault("Val3", "rand3");
        settings.checkDefault("Val4", "rand4");
        settings.checkDefault("Val5", "rand5");
        settings.checkDefault("Val6", "rand6");
        settings.checkDefault("Val7", "rand7");
        settings.checkDefault("Val8", "rand8");
        settings.checkDefault("Val9", "rand9");
        settings.checkDefault("Val10", -10);
        settings.checkDefault("Val11", -101.33f);
        settings.checkDefault("Val12", true);
        assertEquals("A String", settings.get("Val1", String.class));
        assertEquals(false, settings.get("Val2", Boolean.class));
        assertEquals(true, settings.get("Val3", Boolean.class));
        assertEquals("Second String", settings.get("Val4", String.class));
        assertEquals(0, settings.get("Val5", Number.class).intValue());
        assertEquals(-42, settings.get("Val6", Number.class).intValue());
        assertEquals(1234.5678, settings.get("Val7", Number.class).doubleValue(), 0.000001);
        assertEquals("rand8", settings.get("Val8", String.class));
        assertEquals("rand9", settings.get("Val9", String.class));
        assertEquals(-10, settings.get("Val10", Number.class).intValue());
        assertEquals(-101.33f, settings.get("Val11", Number.class).floatValue(), 0.000001f);
        assertEquals(true, settings.get("Val12", Boolean.class));
    }

    @Test
    public void testGetterDefaults() {
        settings.get("Val1", "rand1");
        settings.get("Val2", "rand2");
        settings.get("Val3", "rand3");
        settings.get("Val4", "rand4");
        settings.get("Val5", "rand5");
        settings.get("Val6", "rand6");
        settings.get("Val7", "rand7");
        settings.get("Val8", "rand8");
        settings.get("Val9", "rand9");
        settings.get("Val10", -10);
        settings.get("Val11", -101.33f);
        settings.get("Val12", true);
        assertEquals("A String", settings.get("Val1", String.class));
        assertEquals(false, settings.get("Val2", Boolean.class));
        assertEquals(true, settings.get("Val3", Boolean.class));
        assertEquals("Second String", settings.get("Val4", String.class));
        assertEquals(0, settings.get("Val5", Number.class).intValue());
        assertEquals(-42, settings.get("Val6", Number.class).intValue());
        assertEquals(1234.5678, settings.get("Val7", Number.class).doubleValue(), 0.000001);
        assertEquals("rand8", settings.get("Val8", String.class));
        assertEquals("rand9", settings.get("Val9", String.class));
        assertEquals(-10, settings.get("Val10", Number.class).intValue());
        assertEquals(-101.33f, settings.get("Val11", Number.class).floatValue(), 0.000001f);
        assertEquals(true, settings.get("Val12", Boolean.class));
    }

    @Test
    public void testConstraints() {
        settings.set("CVal1", 1, 0, 5);
        assertEquals(1, settings.get("CVal1", Integer.class).intValue());
        settings.set("CVal1", -10);
        assertEquals(0, settings.get("CVal1", Integer.class).intValue());
        settings.set("CVal1", 10);
        assertEquals(5, settings.get("CVal1", Integer.class).intValue());
        assertEquals(-8, settings.get("CVal2", -8, -42, 42).intValue());
        settings.checkDefault("CVal3", 123.456, -999.999, 999.999);
        assertEquals(123.456, settings.get("CVal3", Double.class), 0.000001);
        settings.set("CVal4", 100, 50, null);
        assertEquals(100, settings.get("CVal4", Integer.class).intValue());
        settings.set("CVal4", 0);
        assertEquals(50, settings.get("CVal4", Integer.class).intValue());
        settings.set("CVal4", 99999);
        assertEquals(99999, settings.get("CVal4", Integer.class).intValue());
    }

    @Test
    public void testMismatches() {
        settings.set("Mismatch1", "A String");
        try {
            Boolean test = settings.get("Mismatch1", Boolean.class);
            fail("Expected IllegalArgumentException from Mismatch1");
        } catch(Exception ex) {}
        settings.set("Mismatch2", true);
        try {
            Float test = settings.get("Mismatch2", Float.class);
            fail("Expected IllegalArgumentException from Mismatch2");
        } catch(Exception ex) {}
        settings.set("Mismatch3", 0.365f);
        try {
            String test = settings.get("Mismatch3", String.class);
            fail("Expected IllegalArgumentException from Mismatch3");
        } catch(Exception ex) {}
    }


    @Test
    public void testSizes() {
        assertEquals(false, settings.isEmpty());
        assertEquals(7, settings.size());
        settings.clear();
        assertEquals(true, settings.isEmpty());
        assertEquals(0, settings.size());
    }


    @Test
    public void testToJSON() {
        JSONObject json = settings.toJSON();
        try {
            assertEquals("A String", json.getJSONObject("Val1").getString("value"));
            assertEquals(false, json.getJSONObject("Val2").getBoolean("value"));
            assertEquals(true, json.getJSONObject("Val3").getBoolean("value"));
            assertEquals("Second String", json.getJSONObject("Val4").getString("value"));
            assertEquals(0, json.getJSONObject("Val5").getInt("value"));
            assertEquals(-42, json.getJSONObject("Val6").getInt("value"));
            assertEquals(1234.5678, json.getJSONObject("Val7").getDouble("value"), 0.000001);

            assertEquals(-99, json.getJSONObject("Val5").getInt("min"));
            assertEquals(42, json.getJSONObject("Val5").getInt("max"));

            assertEquals("This is a test String", json.getJSONObject("Val1").getString("description"));
            assertEquals("This Boolean should be true", json.getJSONObject("Val3").getString("description"));
            assertEquals("The meaning of Life, the Universe, and Everything", json.getJSONObject("Val6").getString("description"));
            assertEquals("Double Test", json.getJSONObject("Val7").getString("description"));

            assertEquals("java.lang.String", json.getJSONObject("Val1").getString("class"));
            assertEquals("java.lang.Boolean", json.getJSONObject("Val2").getString("class"));
            assertEquals("java.lang.Boolean", json.getJSONObject("Val3").getString("class"));
            assertEquals("java.lang.String", json.getJSONObject("Val4").getString("class"));
            assertEquals("java.lang.Integer", json.getJSONObject("Val5").getString("class"));
            assertEquals("java.lang.Integer", json.getJSONObject("Val6").getString("class"));
            assertEquals("java.lang.Double", json.getJSONObject("Val7").getString("class"));
        } catch(JSONException ex) {
            fail(ex.getMessage());
        }
    }


    @Test
    public void testFromJSON() {
        JSONObject json = new JSONObject();
        try {
            JSONObject val1 = new JSONObject();
            val1.put("value", true);
            val1.put("class", "java.lang.Boolean");
            json.put("Val1", val1);
            JSONObject val2 = new JSONObject();
            val2.put("value", false);
            val2.put("class", "java.lang.Boolean");
            json.put("Val2", val2);
            JSONObject val3 = new JSONObject();
            val3.put("value", "String test");
            val3.put("description", "Description test");
            val3.put("class", "java.lang.String");
            json.put("Val3", val3);
            JSONObject val4 = new JSONObject();
            val4.put("value", 5);
            val4.put("description", "Bounded test");
            val4.put("min", 2);
            val4.put("max", 9);
            val4.put("class", "java.lang.Integer");
            json.put("Val4", val4);
        } catch(JSONException ex) {
            fail(ex.getMessage());
        }
        SettingsMap settings = new SettingsMap(json);
        assertEquals(true, settings.get("Val1"));
        assertEquals(true, settings.get("Val1", Boolean.class));
        assertEquals(false, settings.get("Val2"));
        assertEquals(false, settings.get("Val2", Boolean.class));
        assertEquals("String test", settings.get("Val3"));
        assertEquals("String test", settings.get("Val3", String.class));
        assertEquals("Description test", settings.getSetting("Val3").getDescription());
        assertEquals(5, settings.get("Val4"));
        assertEquals(5, settings.get("Val4", Integer.class).intValue());
        assertEquals("Bounded test", settings.getSetting("Val4").getDescription());
        assertEquals(2, settings.getBoundedSetting("Val4", Integer.class).getMin().intValue());
        assertEquals(9, settings.getBoundedSetting("Val4", Integer.class).getMax().intValue());
    }

}