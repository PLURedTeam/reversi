package plu.red.reversi.core;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.ReversiMinimax;
import plu.red.reversi.core.game.logic.ReversiLogic;
import plu.red.reversi.core.game.player.NullPlayer;
import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.util.Color;
import plu.red.reversi.core.util.DataMap;

import java.util.Iterator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReversiMinimaxTest {
    private DataMap settingsMap;
    private Game game;

    private class NullGUI implements IMainGUI {
        @Override public void setClient(Client client) {}
        @Override public void updateGUIMajor() {}
        @Override public void updateGUIMinor() {}
        @Override public String showSaveDialog() { return null; }
        @Override public String showLoadDialog() { return null; }
    }

    @Before
    public void setup() {
        settingsMap = SettingsLoader.INSTANCE.loadGameSettingsFromJSON(new JSONObject());
        game = new Game(new NullGUI());
        game.setSettings(settingsMap).setLogic(new ReversiLogic(game));

        Player p;
        p = new NullPlayer(game, Color.BLACK);
        p = new NullPlayer(game, Color.WHITE);
        game.initialize();
    }

    @Test
    public void testReversiMinimax() {
        ReversiMinimax reversiMinimax = new ReversiMinimax(game, 1, 5);
        reversiMinimax = new ReversiMinimax(game, 0, 5);
        assertTrue(true);
    }

    @Test
    public void testGetBestPlay() {
        ReversiMinimax reversiMinimax = new ReversiMinimax(game, 1, 5);
        BoardIndex bp = reversiMinimax.getBestPlay();
        assertTrue(game.getGameLogic().isValidMove(new MoveCommand(1, bp)));
    }

    @Test
    public void testGetBestPlayEndGame() {
        ReversiMinimax reversiMinimax = new ReversiMinimax(game, 1, 4);

        //run until there are no more moves
        while(true) {
            // apply white move
            Iterator<BoardIndex> possible = game.getGameLogic().getValidMoves(1).iterator();
            if(!possible.hasNext()) break;

            //Have the algorithm update
            assertTrue(reversiMinimax.canPlay());
            assertTrue(game.getGameLogic().isValidMove(reversiMinimax.getBestMoveCommand()));

            game.getGameLogic().play(new MoveCommand(1, possible.next()));

            //apply black move
            possible = game.getGameLogic().getValidMoves(0).iterator();
            if(!possible.hasNext()) continue;
            game.getGameLogic().play(new MoveCommand(0, possible.next()));
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
