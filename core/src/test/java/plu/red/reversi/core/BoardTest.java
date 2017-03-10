package plu.red.reversi.core;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by JChase on 3/9/17.
 */
public class BoardTest {
    private Board board; //global variabe to be used throughout the tests

    @Before
    public void setUp() throws Exception {
        SettingsMap map = new SettingsMap();
        board = new Board(8);
        Game game = new Game(map, board);
    }

    @Test
    public void getScore() throws Exception {
        assertEquals(0, board.getScore(PlayerRole.WHITE));
    }

    @Test
    public void isValidMove() throws Exception {
        BoardIndex index = new BoardIndex();
        index.row = 1;
        index.column = 1;
        assertTrue(board.isValidMove(PlayerRole.BLACK,index));
    }

    @Test
    public void getPossibleMoves() throws Exception {
        ArrayList<BoardIndex> moveList;
        moveList = board.getPossibleMoves(PlayerRole.BLACK);
        assertEquals(moveList, board.getPossibleMoves(PlayerRole.WHITE));

    }

    @Test
    public void apply() throws Exception {
        BoardIndex index;
        Command c = new Command();
        index = c.index;
        index.row=1;
        index.column=1;
        board.apply(c);
        Board board2 = new Board(board);
        board2.apply(c);
        assertEquals(board.getScore(PlayerRole.BLACK), board2.getScore(PlayerRole.WHITE));

    }

}