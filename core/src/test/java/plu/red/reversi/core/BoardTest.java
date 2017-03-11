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
        board.apply(new Command(PlayerRole.BLACK, new BoardIndex(0,0)), false);
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
        Board newBoard = new Board(8);
        ArrayList<BoardIndex> moveList;
        moveList = newBoard.getPossibleMoves(PlayerRole.BLACK);
        assertEquals(moveList, newBoard.getPossibleMoves(PlayerRole.WHITE));

    }

    @Test
    public void testApply() {
        Command c = new Command(PlayerRole.BLACK, new BoardIndex(0, 0));
        c.role=PlayerRole.BLACK;
        board.apply(c);
        assertEquals(PlayerRole.BLACK, board.at(c.index));
    }

}