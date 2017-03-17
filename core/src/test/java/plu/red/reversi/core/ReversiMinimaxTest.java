package plu.red.reversi.core;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.player.NullPlayer;
import plu.red.reversi.core.util.SettingsMap;

import java.util.Iterator;

import static org.junit.Assert.assertTrue;

public class ReversiMinimaxTest {
    private SettingsMap settingsMap;
    private Game game;

    @Before
    public void setup() {
        settingsMap = SettingsLoader.INSTANCE.loadGameSettingsFromJSON(new JSONObject());
        game = new Game(settingsMap);
        game.setPlayer(new NullPlayer(game, PlayerColor.WHITE));
        game.setPlayer(new NullPlayer(game, PlayerColor.BLACK));
        game.initialize();
    }

    @Test
    public void testReversiMinimax() {
        ReversiMinimax reversiMinimax = new ReversiMinimax(game, PlayerColor.WHITE, PlayerColor.WHITE, 5);
        reversiMinimax = new ReversiMinimax(game, PlayerColor.BLACK, PlayerColor.WHITE, 5);
        assertTrue(true);
    }

    @Test
    public void testGetBestPlay() {
        ReversiMinimax reversiMinimax = new ReversiMinimax(game, PlayerColor.WHITE, PlayerColor.WHITE, 5);
        BoardIndex bp = reversiMinimax.getBestPlay();
        assertTrue(game.getBoard().isValidMove(PlayerColor.WHITE, bp));
    }

    @Test
    public void testGetBestPlayFastForward() {
        //Note: This test requires the game to keep running if just choose the first
        // valid play from the board for each player.

        ReversiMinimax reversiMinimax = new ReversiMinimax(game, PlayerColor.WHITE, PlayerColor.WHITE, 4);

        //NOTE: this test needs the current player to be WHITE, because the reversi ai, when it has caching
        // issues, defers to the current player's turn as the inital state for the new tree
        for(int x = 0; x < 15; ++x) {
            MoveCommand m = new MoveCommand(PlayerColor.WHITE, game.getBoard().getPossibleMoves(PlayerColor.WHITE).iterator().next());
            assertTrue(game.getBoard().isValidMove(m));
            game.board.apply(m);

            m = new MoveCommand(PlayerColor.BLACK, game.getBoard().getPossibleMoves(PlayerColor.BLACK).iterator().next());
            assertTrue(game.getBoard().isValidMove(m));
            game.board.apply(m);

            MoveCommand move = null;
            move = reversiMinimax.getBestMoveCommand();

            assertTrue(game.getBoard().isValidMove(move));
        }

        //Verify that it has been moving the root forward
        assertTrue(reversiMinimax.getRootDetph() > 8);
    }

    @Test
    public void testGetBestPlayEndGame() {
        ReversiMinimax reversiMinimax = new ReversiMinimax(game, PlayerColor.WHITE, PlayerColor.WHITE, 4);

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
            if(!possible.hasNext()) break;
            game.getBoard().apply(new MoveCommand(PlayerColor.BLACK, possible.next()));
        }

        //assertFalse(reversiMinimax.canPlay());
        try {
            reversiMinimax.getBestMoveCommand();
            assertTrue(false);
        } catch(IndexOutOfBoundsException e) {
            assertTrue(true);
        }
    }
}
