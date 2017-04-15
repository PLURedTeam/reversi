package plu.red.reversi.core;

import org.junit.Test;
import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.game.Board;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.logic.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReversiLogicTest {
    @Test
    public void testGetScore() {
        Board board = new Board(4);
        GameLogic logic = new ReversiLogic();
        logic.initBoard(new int[]{0, 1}, board, false, false);

        assertEquals(2, logic.getScore(0, board));
        assertEquals(2, logic.getScore(1, board));
        logic.play(new MoveCommand(0, new BoardIndex(3, 1)), board, false, false);
        assertEquals(4, logic.getScore(0, board));
        assertEquals(1, logic.getScore(1, board));
    }

    @Test
    public void testIsValidMove() {
        Board board = new Board(4);
        GameLogic logic = new ReversiLogic();
        logic.initBoard(new int[]{0, 1}, board, false, false);

        MoveCommand m = new MoveCommand(1, new BoardIndex(0, 0));
        assertFalse(logic.isValidMove(m, board));

        m.position.row = 1; m.position.column = 0;
        assertTrue(logic.isValidMove(m, board));

        m.position.row = 0; m.position.column = 1;
        assertTrue(logic.isValidMove(m, board));

        m.position.row = 2; m.position.column = 3;
        assertTrue(logic.isValidMove(m, board));

        m.position.row = 3; m.position.column = 2;
        assertTrue(logic.isValidMove(m, board));
    }

    @Test
    public void testGetValidMoves() {
        Board board = new Board(4);
        GameLogic logic = new ReversiLogic();
        logic.initBoard(new int[]{0, 1}, board, false, false);

        Set<BoardIndex> moveList;
        moveList = logic.getValidMoves(1, board);

        assertEquals(4, moveList.size());
        assertTrue(moveList.contains(new BoardIndex(0, 1)));
        assertTrue(moveList.contains(new BoardIndex(1, 0)));
        assertTrue(moveList.contains(new BoardIndex(2, 3)));
        assertTrue(moveList.contains(new BoardIndex(3, 2)));

    }


    @Test
    public void testPlay() {
        //moving a piece in a legal index
        MoveCommand c = new MoveCommand(Command.Source.CLIENT, 0, new BoardIndex(1, 0));

        Board board = new Board(4);
        GameLogic logic = new ReversiLogic();
        logic.initBoard(new int[]{0,1}, board, false, false);

        try {
            logic.play(c, board, false, false);
            assertTrue(false);
        } catch(IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testRepeatedPlays() {
        Board board = new Board(8);
        GameLogic logic = new ReversiLogic();
        logic.initBoard(new int[]{0, 1}, board, false, false);

        for(int x = 0; x < 15; ++x) {
            MoveCommand m = new MoveCommand(0, logic.getValidMoves(0, board).iterator().next());
            assertTrue(logic.isValidMove(m, board));
            logic.play(m, board, false, false);

            BoardIndex i = new BoardIndex(logic.getValidMoves(1, board).iterator().next());
            assertTrue(logic.isValidMove(new MoveCommand(1, i), board));
            logic.play(new MoveCommand(1, i), board, false, false);
        }
    }
}
