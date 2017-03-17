package plu.red.reversi.core;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import plu.red.reversi.core.player.BotPlayer;
import plu.red.reversi.core.player.NullPlayer;
import plu.red.reversi.core.util.SettingsMap;

import static org.junit.Assert.assertTrue;

public class ReversiMinimaxTest {
    private SettingsMap settingsMap;
    private Game game;

    @Before
    public void setup() {
        settingsMap = SettingsLoader.INSTANCE.loadFromJSON(new JSONObject());
        game = new Game(settingsMap);
        game.setPlayer(new NullPlayer(game, PlayerColor.WHITE));
        game.setPlayer(new NullPlayer(game, PlayerColor.BLACK));
        game.initialize();
    }

    @Test
    public void testReversiMinimax() {
        ReversiMinimax reversiMinimax = new ReversiMinimax(game, PlayerColor.WHITE, PlayerColor.WHITE);
        reversiMinimax = new ReversiMinimax(game, PlayerColor.BLACK, PlayerColor.WHITE);
        assertTrue(true);
    }

    @Test
    public void testGetBestPlay() {
        ReversiMinimax reversiMinimax = new ReversiMinimax(game, PlayerColor.WHITE, PlayerColor.WHITE);
        BoardIndex bp = reversiMinimax.getBestPlay();
        assertTrue(game.board.isValidMove(PlayerColor.WHITE, bp));
    }
}
