package plu.red.reversi.core.util;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by Andrew on 4/26/2017.
 */
public class GamePairTest {

    GamePair pair;

    @Before
    public void setUp() throws Exception {
        pair = new GamePair();
    }

    @Test
    public void setPlayers() throws Exception {
        User u1 = new User();
        User u2 = new User();
        ArrayList<User> list = new ArrayList<User>();
        list.add(u1);
        list.add(u2);
        pair.setPlayers(list);

        assertEquals(2, pair.getPlayers().size());
        assertNotEquals(1, pair.getPlayers().size());
        assertTrue(pair.getPlayers().contains(u1));
        assertTrue(pair.getPlayers().contains(u2));
        assertFalse(pair.getPlayers().contains(null));
    }

    @Test
    public void setStatus() throws Exception {
        pair.setStatus(GamePair.GameStatus.LOBBY);
        assertEquals(GamePair.GameStatus.LOBBY, pair.getStatus());
        assertNotEquals(GamePair.GameStatus.PLAYING, pair.getStatus());

        pair.setStatus(GamePair.GameStatus.WAITING);
        assertEquals(GamePair.GameStatus.WAITING, pair.getStatus());
        assertNotEquals(GamePair.GameStatus.PLAYING, pair.getStatus());

        pair.setStatus(GamePair.GameStatus.PLAYING);
        assertEquals(GamePair.GameStatus.PLAYING, pair.getStatus());
        assertNotEquals(GamePair.GameStatus.WAITING, pair.getStatus());
    }

    @Test
    public void setGameID() throws Exception {
        pair.setGameID(0);
        assertEquals(0, pair.getGameID());
        assertNotEquals(1, pair.getGameID());
    }

    @Test
    public void setNumPlayers() throws Exception {
        pair.setNumPlayers(2);
        assertEquals(2,pair.getNumPlayers());
        assertNotEquals(1,pair.getNumPlayers());
    }

    @Test
    public void setGameName() throws Exception {
        pair.setGameName("test");
        assertEquals("test", pair.getGameName());
        assertNotEquals("test1", pair.getGameName());
    }

    @Test
    public void getPlayers() throws Exception {
        User u1 = new User();
        User u2 = new User();
        ArrayList<User> list = new ArrayList<User>();
        list.add(u1);
        list.add(u2);
        pair.setPlayers(list);

        assertEquals(2, pair.getPlayers().size());
        assertNotEquals(1, pair.getPlayers().size());
        assertTrue(pair.getPlayers().contains(u1));
        assertTrue(pair.getPlayers().contains(u2));
        assertFalse(pair.getPlayers().contains(null));
    }

    @Test
    public void getStatus() throws Exception {
        pair.setStatus(GamePair.GameStatus.LOBBY);
        assertEquals(GamePair.GameStatus.LOBBY, pair.getStatus());
        assertNotEquals(GamePair.GameStatus.PLAYING, pair.getStatus());

        pair.setStatus(GamePair.GameStatus.WAITING);
        assertEquals(GamePair.GameStatus.WAITING, pair.getStatus());
        assertNotEquals(GamePair.GameStatus.PLAYING, pair.getStatus());

        pair.setStatus(GamePair.GameStatus.PLAYING);
        assertEquals(GamePair.GameStatus.PLAYING, pair.getStatus());
        assertNotEquals(GamePair.GameStatus.WAITING, pair.getStatus());
    }

    @Test
    public void getGameID() throws Exception {
        pair.setGameID(0);
        assertEquals(0, pair.getGameID());
        assertNotEquals(1, pair.getGameID());
    }

    @Test
    public void getNumPlayers() throws Exception {
        pair.setNumPlayers(2);
        assertEquals(2,pair.getNumPlayers());
        assertNotEquals(1,pair.getNumPlayers());
    }

    @Test
    public void getGameName() throws Exception {
        pair.setGameName("test");
        assertEquals("test", pair.getGameName());
        assertNotEquals("test1", pair.getGameName());
    }

}