package plu.red.reversi.core;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.util.SettingsMap;

import java.util.ArrayList;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by JChase on 3/9/17.
 */
public class BoardTest {

    @Test
    public void testGetScore() {
        Board board = new Board(4);
        board.applyCommands(Board.getSetupCommands(PlayerColor.WHITE, PlayerColor.BLACK,4));

        assertEquals(2, board.getScore(PlayerColor.WHITE));
        assertEquals(2, board.getScore(PlayerColor.BLACK));
        board.apply(new MoveCommand(Command.Source.PLAYER, PlayerColor.BLACK, new BoardIndex(3, 0)));
        assertEquals(3, board.getScore(PlayerColor.BLACK));
        assertEquals(2, board.getScore(PlayerColor.WHITE));
    }

    @Test
    public void testIsValidMove() {
        Board board = new Board(4);
        board.applyCommands(Board.getSetupCommands(PlayerColor.WHITE, PlayerColor.BLACK,4));

        assertFalse(board.isValidMove(PlayerColor.BLACK, new BoardIndex(0, 0)));
        assertTrue(board.isValidMove(PlayerColor.BLACK, new BoardIndex(1, 0)));
        assertTrue(board.isValidMove(PlayerColor.BLACK, new BoardIndex(0, 1)));
        assertTrue(board.isValidMove(PlayerColor.BLACK, new BoardIndex(2, 3)));
        assertTrue(board.isValidMove(PlayerColor.BLACK, new BoardIndex(3, 2)));

    }

    @Test
    public void getPossibleMoves() {
        Board board = new Board(4);
        board.applyCommands(Board.getSetupCommands(PlayerColor.WHITE, PlayerColor.BLACK,4));

        Set<BoardIndex> moveList;
        moveList = board.getPossibleMoves(PlayerColor.BLACK);

        assertEquals(4, moveList.size());
        assertTrue(moveList.contains(new BoardIndex(0, 1)));
        assertTrue(moveList.contains(new BoardIndex(1, 0)));
        assertTrue(moveList.contains(new BoardIndex(2, 3)));
        assertTrue(moveList.contains(new BoardIndex(3, 2)));
    }


    @Test
    public void testApply() {
        //moving a piece in a legal index
        MoveCommand c = new MoveCommand(Command.Source.PLAYER, PlayerColor.BLACK, new BoardIndex(1, 0));
        Board brd = new Board(4);
        brd.applyCommands(Board.getSetupCommands(PlayerColor.WHITE, PlayerColor.BLACK,4));
        brd.apply(c);
        assertEquals(PlayerColor.BLACK, brd.at(c.position));

        //trying to apply a piece on an illegal index

        assertEquals(PlayerColor.BLACK, brd.at(c.position));
    }
}
