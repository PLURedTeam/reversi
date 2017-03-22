package plu.red.reversi.core;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.player.NullPlayer;
import plu.red.reversi.core.util.DataMap;

import java.util.Iterator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReversiMinimaxTest {
    private DataMap settingsMap;
    private Game game;

    @Before
    public void setup() {
        settingsMap = SettingsLoader.INSTANCE.loadGameSettingsFromJSON(new JSONObject());
        game = new Game();
        game.setSettings(settingsMap);
        game.setPlayer(new NullPlayer(game, PlayerColor.WHITE));
        game.setPlayer(new NullPlayer(game, PlayerColor.BLACK));
        game.initialize();
    }

    @Test
    public void testReversiMinimax() {
        ReversiMinimax reversiMinimax = new ReversiMinimax(game, PlayerColor.WHITE, 5);
        reversiMinimax = new ReversiMinimax(game, PlayerColor.BLACK, 5);
        assertTrue(true);
    }

    @Test
    public void testGetBestPlay() {
        ReversiMinimax reversiMinimax = new ReversiMinimax(game, PlayerColor.WHITE, 5);
        BoardIndex bp = reversiMinimax.getBestPlay();
        assertTrue(game.getBoard().isValidMove(PlayerColor.WHITE, bp));
    }

    @Test
    public void testGetBestPlayEndGame() {
        ReversiMinimax reversiMinimax = new ReversiMinimax(game, PlayerColor.WHITE, 4);

        //run until there are no more moves
        while(true) {
            // apply white move
            Iterator<BoardIndex> possible = game.getBoard().getPossibleMoves(PlayerColor.WHITE).iterator();
            if(!possible.hasNext()) break;

            //Have the algorithm update
            assertTrue(reversiMinimax.canPlay());
            assertTrue(game.getBoard().isValidMove(reversiMinimax.getBestMoveCommand()));

            game.getBoard().apply(new MoveCommand(PlayerColor.WHITE, possible.next()));

            //apply black move
            possible = game.getBoard().getPossibleMoves(PlayerColor.BLACK).iterator();
            if(!possible.hasNext()) continue;
            game.getBoard().apply(new MoveCommand(PlayerColor.BLACK, possible.next()));
        }

        assertFalse(reversiMinimax.canPlay());
        try {
            reversiMinimax.getBestMoveCommand();
            assertTrue(false);
        } catch(IndexOutOfBoundsException e) {
            assertTrue(true);
        }
    }
}
