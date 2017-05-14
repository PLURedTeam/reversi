package plu.red.reversi.server;

import org.junit.Test;
import plu.red.reversi.server.Managers.SessionManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by Andrew on 4/17/2017.
 */
public class SessionManagerTest {

    SessionManager session = new SessionManager();

    @Test
    public void addSession() throws Exception {
        int id = session.addSession();
        int id2 = session.addSession();

        assertEquals(1000, id);
        assertEquals(1001, id2);
        assertNotEquals(1001, id);
        assertNotEquals(1000, id2);

    }
}