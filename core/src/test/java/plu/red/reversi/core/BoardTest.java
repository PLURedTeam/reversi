package plu.red.reversi.core;

import org.junit.Test;
import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.game.Board;
import plu.red.reversi.core.game.BoardIndex;

import java.util.LinkedList;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by JChase on 3/9/17.
 */
public class BoardTest {

    @Test
    public void testGetScore() {
        Board board = new Board(4);
        board.applyCommands(Board.getSetupCommands(new Integer[]{ 0, 1 }, 4));

        assertEquals(2, board.getScore(0));
        assertEquals(2, board.getScore(1));
        board.apply(new MoveCommand(Command.Source.CLIENT, 0, new BoardIndex(3, 0)));
        assertEquals(3, board.getScore(0));
        assertEquals(2, board.getScore(1));
    }

    @Test
    public void testIsValidMove() {
        Board board = new Board(4);
        board.applyCommands(Board.getSetupCommands(new Integer[]{ 0, 1 }, 4));

        assertFalse(board.isValidMove(1, new BoardIndex(0, 0)));
        assertTrue(board.isValidMove(1, new BoardIndex(1, 0)));
        assertTrue(board.isValidMove(1, new BoardIndex(0, 1)));
        assertTrue(board.isValidMove(1, new BoardIndex(2, 3)));
        assertTrue(board.isValidMove(1, new BoardIndex(3, 2)));

    }

    @Test
    public void testEquals() {
        Board b1 = new Board(8);
        assertEquals(b1, new Board(b1));


        Board b2 = new Board(b1);
        LinkedList<BoardCommand> commands = Board.getSetupCommands(new Integer[]{ 0, 1 }, 8);
        b1.applyCommands(commands);
        assertNotEquals(b1, b2);

        b2.applyCommands(commands);
        assertEquals(b1, b2);

        b1.apply(new MoveCommand(1, new BoardIndex(2, 4)));
        assertNotEquals(b1, b2);
        b2.apply(new MoveCommand(1, new BoardIndex(2, 4)));
        assertEquals(b1, b2);
    }

    @Test
    public void testGetTotalPieces() {
        Board b = new Board(8);
        assertEquals(0, b.getTotalPieces());

        b.applyCommands(Board.getSetupCommands(new Integer[]{ 0, 1 }, 8));
        assertEquals(4, b.getTotalPieces());
    }

    @Test
    public void testGetPossibleMoves() {
        Board board = new Board(4);
        board.applyCommands(Board.getSetupCommands(new Integer[]{ 0, 1 }, 4));

        Set<BoardIndex> moveList;
        moveList = board.getPossibleMoves(1);

        assertEquals(4, moveList.size());
        assertTrue(moveList.contains(new BoardIndex(0, 1)));
        assertTrue(moveList.contains(new BoardIndex(1, 0)));
        assertTrue(moveList.contains(new BoardIndex(2, 3)));
        assertTrue(moveList.contains(new BoardIndex(3, 2)));

    }


    @Test
    public void testApply() {
        //moving a piece in a legal index
        MoveCommand c = new MoveCommand(Command.Source.CLIENT, 0, new BoardIndex(1, 0));
        Board brd = new Board(4);
        brd.applyCommands(Board.getSetupCommands(new Integer[]{ 0, 1 }, 4));
        brd.apply(c);
        assertEquals(0, brd.at(c.position));

        //trying to apply a piece on an illegal index

        assertEquals(0, brd.at(c.position));
    }

    @Test
    public void testRepeatedMoves() {
        Board board = new Board(8);
        board.applyCommands(Board.getSetupCommands(new Integer[]{ 0, 1 }, 8));

        for(int x = 0; x < 15; ++x) {
            MoveCommand m = new MoveCommand(0, board.getPossibleMoves(0).iterator().next());
            assertTrue(board.isValidMove(m));
            board.apply(m);

            BoardIndex i = new BoardIndex(board.getPossibleMoves(1).iterator().next());
            assertTrue(board.isValidMove(1, i));
            board.apply(new MoveCommand(1, i));
        }
    }
}
