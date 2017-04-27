package plu.red.reversi.core.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Andrew on 4/26/2017.
 */
public class UserTest {

    User u;

    @Before
    public void setUp() throws Exception {
        u = new User();
    }

    @Test
    public void getUsername() throws Exception {
        u.setUsername("test");
        assertEquals("test", u.getUsername());
        assertNotEquals("test1", u.getUsername());
    }

    @Test
    public void setUsername() throws Exception {
        u.setUsername("test");
        assertEquals("test", u.getUsername());
        assertNotEquals("test1", u.getUsername());
    }

    @Test
    public void getPassword() throws Exception {
        u.setPassword("test");
        assertEquals("test", u.getPassword());
        assertNotEquals("test1", u.getPassword());
    }

    @Test
    public void setPassword() throws Exception {
        u.setPassword("test");
        assertEquals("test", u.getPassword());
        assertNotEquals("test1", u.getPassword());
    }

    @Test
    public void getSessionID() throws Exception {
        u.setSessionID(1000);
        assertEquals(1000, u.getSessionID());
        assertNotEquals(1001, u.getSessionID());
    }

    @Test
    public void setSessionID() throws Exception {
        u.setSessionID(1000);
        assertEquals(1000, u.getSessionID());
        assertNotEquals(1001, u.getSessionID());
    }

    @Test
    public void getStatus() throws Exception {
        u.setStatus("IN GAME");
        assertEquals("IN GAME", u.getStatus());
        assertNotEquals("IN LOBBY", u.getStatus());
    }

    @Test
    public void setStatus() throws Exception {
        u.setStatus("IN GAME");
        assertEquals("IN GAME", u.getStatus());
        assertNotEquals("IN LOBBY", u.getStatus());
    }

    @Test
    public void getRank() throws Exception {
        u.setRank(1);
        assertEquals(1, u.getRank());
        assertNotEquals(2, u.getRank());
    }

    @Test
    public void setRank() throws Exception {
        u.setRank(1);
        assertEquals(1, u.getRank());
        assertNotEquals(2, u.getRank());
    }

}