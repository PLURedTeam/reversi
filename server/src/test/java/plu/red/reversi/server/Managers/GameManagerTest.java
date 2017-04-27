package plu.red.reversi.server.Managers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import plu.red.reversi.core.util.GamePair;
import plu.red.reversi.core.util.User;

import static org.junit.Assert.*;

/**
 * Created by Andrew on 4/26/2017.
 */
public class GameManagerTest {

    GameManager games;

    @Before
    public void setUp() throws Exception {
        games = new GameManager();
    }

    @Test
    public void createGame() throws Exception {
        int gameID = games.createGame(2,"test");
        assertEquals(0,gameID);
        assertNotEquals(1,gameID);
        assertTrue(games.gameExists(0));
        assertEquals("test",games.getGame(gameID).gameName);
        assertNotEquals("test1",games.getGame(gameID).gameName);
        assertEquals(0,games.getGame(gameID).getGameID());
        assertNotEquals(1,games.getGame(gameID).getGameID());
        assertEquals(GamePair.GameStatus.LOBBY,games.getGame(gameID).getStatus());
        assertNotEquals(GamePair.GameStatus.PLAYING,games.getGame(gameID).getStatus());
    }

    @Test
    public void getGame() throws Exception {
        int gameID = games.createGame(2,"test");
        assertEquals("test",games.getGame(gameID).gameName);
        assertNull(games.getGame(2));
        assertNotEquals("test1",games.getGame(gameID).gameName);
        assertEquals(0,games.getGame(gameID).getGameID());
        assertNotEquals(1,games.getGame(gameID).getGameID());
        assertEquals(GamePair.GameStatus.LOBBY,games.getGame(gameID).getStatus());
        assertNotEquals(GamePair.GameStatus.PLAYING,games.getGame(gameID).getStatus());
    }

    @Test
    public void getGames() throws Exception {
        assertEquals(0,games.getGames().size());
        assertNotEquals(1,games.getGames().size());

        int gameID = games.createGame(2,"test");
        assertEquals(1,games.getGames().size());
        assertNotEquals(2,games.getGames().size());
    }

    @Test
    public void gameExists() throws Exception {
        int gameID = games.createGame(2,"test");
        assertTrue(games.gameExists(0));
        assertFalse(games.gameExists(1));
    }
}