package plu.red.reversi.core.game;

import org.junit.Before;
import org.junit.Test;
import plu.red.reversi.core.Client;
import plu.red.reversi.core.Controller;
import plu.red.reversi.core.IMainGUI;
import plu.red.reversi.core.SettingsLoader;
import plu.red.reversi.core.game.logic.GameLogic;
import plu.red.reversi.core.game.logic.ReversiLogic;
import plu.red.reversi.core.game.player.HumanPlayer;
import plu.red.reversi.core.util.Color;
import plu.red.reversi.core.util.DataMap;

import static org.junit.Assert.*;


public class GameTest {

    Game game;

    @Before
    public void setup() {
        Controller.init(new Client(null, null, null, null));
        game = new Game(Controller.getInstance(), new IMainGUI.NullGUI());

        game.setLogic(new ReversiLogic(game));

        DataMap settings = SettingsLoader.INSTANCE.createGameSettings();
        settings.set(SettingsLoader.GAME_PLAYER_COUNT, 2);
        game.setSettings(settings);

        new HumanPlayer(game, Color.BLACK);
        new HumanPlayer(game, Color.WHITE);

        game.initialize();
    }

    @Test
    public void testSerialize() {

        assertEquals(2, game.getPlayerCount());
        assertTrue(game.isInitialized());
        assertTrue(game.isHost());
        assertEquals(GameLogic.Type.REVERSI, game.getGameLogic().getType());

        DataMap data = game.serialize();
        Game copy = new Game(Controller.getInstance(), new IMainGUI.NullGUI(), data);

        assertEquals(2, copy.getPlayerCount());
        assertTrue(copy.isInitialized());
        assertFalse(copy.isHost());
        assertEquals(GameLogic.Type.REVERSI, copy.getGameLogic().getType());

        assertEquals(game.isGameOver(),             copy.isGameOver());
        assertEquals(game.getGameID(),              copy.getGameID());
        assertEquals(game.getGameSaved(),           copy.getGameSaved());
        assertEquals(game.getGameLogic().getType(), copy.getGameLogic().getType());

    }

}