package plu.red.reversi.core;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Glory to the Red Team.
 */
public class SettingsMapTest {

    @Test
    public void testNumbers() {
        SettingsMap settings = new SettingsMap();
        assertEquals(false, settings.containsNumber("TestNumber"));
        assertEquals(0, settings.getNumber("TestNumber"));
        assertEquals(true, settings.containsNumber("TestNumber"));
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
        assertEquals("", settings.getString("TestString"));
        assertEquals(true, settings.containsString("TestString"));
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
        assertEquals(false, settings.getBoolean("TestBoolean"));
        assertEquals(true, settings.containsBoolean("TestBoolean"));
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
        assertEquals(5, settings.size());
        settings.clear();
        assertEquals(true, settings.isEmpty());
        assertEquals(0, settings.size());
    }


}