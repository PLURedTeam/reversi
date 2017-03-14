package plu.red.reversi.core;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.util.SettingsMap;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by JChase on 3/9/17.
 */
public class BoardTest {

    @Test
    public void testGetScore() {
        Board board = new Board(4);
        board.setupBoard(PlayerColor.WHITE, PlayerColor.BLACK);

        assertEquals(2, board.getScore(PlayerColor.WHITE));
        assertEquals(2, board.getScore(PlayerColor.BLACK));
        board.apply(new MoveCommand(Command.Source.PLAYER, PlayerColor.BLACK, new BoardIndex(3, 0)), true);
        assertEquals(3, board.getScore(PlayerColor.BLACK));
        assertEquals(2, board.getScore(PlayerColor.WHITE));
    }

    @Test
    public void testIsValidMove() {
        Board board = new Board(4);
        board.setupBoard(PlayerColor.WHITE, PlayerColor.BLACK);

        assertFalse(board.isValidMove(PlayerColor.BLACK, new BoardIndex(0, 0)));
        assertTrue(board.isValidMove(PlayerColor.BLACK, new BoardIndex(1, 0)));
        assertTrue(board.isValidMove(PlayerColor.BLACK, new BoardIndex(0, 1)));
        assertTrue(board.isValidMove(PlayerColor.BLACK, new BoardIndex(2, 3)));
        assertTrue(board.isValidMove(PlayerColor.BLACK, new BoardIndex(3, 2)));

    }

    @Test
    public void getPossibleMoves() {
        Board board = new Board(4);
        board.setupBoard(PlayerColor.WHITE, PlayerColor.BLACK);

        ArrayList<BoardIndex> moveList;
        moveList = board.getPossibleMoves(PlayerColor.BLACK);

        assertTrue(moveList.get(0).equals(new BoardIndex(0, 1)));
        assertTrue(moveList.get(1).equals(new BoardIndex(1, 0)));
        assertTrue(moveList.get(2).equals(new BoardIndex(2, 3)));
        assertTrue(moveList.get(3).equals(new BoardIndex(3, 2)));
    }


    @Test
    public void testApply() {
        //moving a piece in a legal index
        MoveCommand c = new MoveCommand(Command.Source.PLAYER, PlayerColor.BLACK, new BoardIndex(1, 0));
        Board brd = new Board(4);
        brd.setupBoard(PlayerColor.WHITE, PlayerColor.BLACK);
        brd.apply(c, true);
        assertEquals(PlayerColor.BLACK, brd.at(c.position));

        //trying to apply a piece on an illegal index

        assertEquals(PlayerColor.BLACK, brd.at(c.position));
    }
}
