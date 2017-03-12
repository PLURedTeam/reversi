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
    public void setUp() {
        board = new Board(4);
    }

    @Test
    public void testGetScore() {
        assertEquals(2, board.getScore(PlayerRole.WHITE));
        assertEquals(2, board.getScore(PlayerRole.BLACK));
        board.apply(new CommandMove(Command.Source.PLAYER, PlayerRole.BLACK, new BoardIndex(0,0)), false);
        assertEquals(3, board.getScore(PlayerRole.BLACK));
        assertEquals(2, board.getScore(PlayerRole.WHITE));
    }

    @Test
    public void testIsValidMove() {
        assertFalse(board.isValidMove(PlayerRole.BLACK, new BoardIndex(0,0)));
        assertTrue(board.isValidMove(PlayerRole.BLACK, new BoardIndex(1, 0)));
        assertTrue(board.isValidMove(PlayerRole.BLACK, new BoardIndex(0, 1)));
        assertTrue(board.isValidMove(PlayerRole.BLACK, new BoardIndex(2, 3)));
        assertTrue(board.isValidMove(PlayerRole.BLACK, new BoardIndex(3, 2)));

    }

    @Test
    public void getPossibleMoves() {
        Board newBoard = new Board(4);
        ArrayList<BoardIndex> moveList;
        moveList = newBoard.getPossibleMoves(PlayerRole.BLACK);

        assertTrue(moveList.get(0).equals(new BoardIndex(0,1)));
        assertTrue(moveList.get(1).equals(new BoardIndex(1,0)));
        assertTrue(moveList.get(2).equals(new BoardIndex(2,3)));
        assertTrue(moveList.get(3).equals(new BoardIndex(3,2)));

    }

//    @Test
//    public void testApply() {
//        CommandMove c = new CommandMove(Command.Source.PLAYER, PlayerRole.BLACK, new BoardIndex(1, 0));
//        board.apply(c);
//        assertEquals(PlayerRole.BLACK, board.at(c.position));
//    }

}