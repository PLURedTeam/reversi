package plu.red.reversi.server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import plu.red.reversi.core.util.User;

import static org.junit.Assert.*;

/**
 * Created by Andrew on 4/17/2017.
 */
public class UserManagerTest {

    UserManager manager;

    @Before
    public void setUp() throws Exception {
        manager = new UserManager();
    }

    @Test
    public void addUser() throws Exception {
        User u = new User();
        u.setUsername("Test");

        manager.addUser(u);
        assertEquals("Test", manager.onlineUsers().get(0).getUsername());
        assertNotEquals("FALSE", manager.onlineUsers().get(0).getUsername());
    }

    @Test
    public void removeUser() throws Exception {
        User u = new User();
        u.setUsername("Test");

        manager.addUser(u);
        assertEquals(1, manager.onlineUsers().size());
        assertNotEquals(0, manager.onlineUsers().size());
        manager.removeUser(u);
        assertEquals(0, manager.onlineUsers().size());
        assertNotEquals(1, manager.onlineUsers().size());
    }

    @Test
    public void timedOut() throws Exception {
        User u = new User();
        u.setUsername("Test");
        u.setSessionID(1000);

        manager.addUser(u);
        assertEquals(1, manager.onlineUsers().size());
        assertNotEquals(0, manager.onlineUsers().size());
        assertTrue(manager.loggedIn("Test"));
        manager.timedOut(1000);
        assertFalse(manager.loggedIn("Test"));
    }

    @Test
    public void loggedIn() throws Exception {
        User u = new User();
        u.setUsername("Test");

        manager.addUser(u);
        assertTrue(manager.loggedIn("Test"));
        assertFalse(manager.loggedIn("FALSE"));
    }

    @Test
    public void onlineUsers() throws Exception {
        User u = new User();
        u.setUsername("Test");
        u.setSessionID(1000);

        User u1 = new User();
        u1.setUsername("Test1");
        u1.setSessionID(1000);

        manager.addUser(u);
        manager.addUser(u1);
        assertEquals(2, manager.onlineUsers().size());
        assertNotEquals(1, manager.onlineUsers().size());
    }

}