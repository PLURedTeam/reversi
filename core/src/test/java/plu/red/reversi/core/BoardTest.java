package plu.red.reversi.core;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.command.SetCommand;
import plu.red.reversi.core.util.SettingsMap;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Consumer;

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
    public void testEquals() {
        Board b1 = new Board(8);
        assertEquals(b1, new Board(b1));


        Board b2 = new Board(b1);
        LinkedList<BoardCommand> commands = Board.getSetupCommands(PlayerColor.WHITE, PlayerColor.BLACK, 8);
        b1.applyCommands(commands);
        assertNotEquals(b1, b2);

        b2.applyCommands(commands);
        assertEquals(b1, b2);

        b1.apply(new MoveCommand(PlayerColor.WHITE, new BoardIndex(2, 4)));
        assertNotEquals(b1, b2);
        b2.apply(new MoveCommand(PlayerColor.WHITE, new BoardIndex(2, 4)));
        assertEquals(b1, b2);
    }

    @Test
    public void testGetTotalPieces() {
        Board b = new Board(8);
        assertEquals(0, b.getTotalPieces());

        b.applyCommands(Board.getSetupCommands(PlayerColor.WHITE, PlayerColor.BLACK, 8));
        assertEquals(4, b.getTotalPieces());
    }

    @Test
    public void testGetPossibleMoves() {
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

    @Test
    public void testRepeatedMoves() {
        Board board = new Board(8);
        board.applyCommands(Board.getSetupCommands(PlayerColor.WHITE, PlayerColor.BLACK, 8));

        for(int x = 0; x < 15; ++x) {
            MoveCommand m = new MoveCommand(PlayerColor.WHITE, board.getPossibleMoves(PlayerColor.WHITE).iterator().next());
            assertTrue(board.isValidMove(m));
            board.apply(m);

            BoardIndex i = new BoardIndex(board.getPossibleMoves(PlayerColor.BLACK).iterator().next());
            assertTrue(board.isValidMove(PlayerColor.BLACK, i));
            board.apply(new MoveCommand(PlayerColor.BLACK, i));
        }
    }
}
