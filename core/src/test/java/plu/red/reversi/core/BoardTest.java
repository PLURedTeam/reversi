package plu.red.reversi.core;

import org.junit.Assert;
import org.junit.Test;
import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.command.SetCommand;
import plu.red.reversi.core.game.Board;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.logic.GameLogic;
import plu.red.reversi.core.game.logic.ReversiLogic;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import static org.junit.Assert.*;

public class BoardTest {
    @Test
    public void testApply() {
        SetCommand c = new SetCommand(0, new BoardIndex(1, 0));
        Board brd = new Board(4);
        brd.apply(c);
        assertEquals(0, brd.at(c.position));

        c = new SetCommand(1, new BoardIndex(3, 0));
        brd.apply(c);
        assertEquals(1, brd.at(c.position));
    }

    @Test
    public void testEquals() {
        Board b1 = new Board(8);
        Board b2 = new Board(b1);
        assertEquals(b1, b2);

        b1.apply(new SetCommand(1, new BoardIndex(2, 4)));
        assertNotEquals(b1, b2);
        b2.apply(new SetCommand(1, new BoardIndex(2, 4)));
        assertEquals(b1, b2);
    }

    @Test
    public void testGetTotalPieces() {
        Board b = new Board(8);
        assertEquals(0, b.getTotalPieces());

        b.apply(new SetCommand(0, new BoardIndex(0, 2)));
        assertEquals(1, b.getTotalPieces());

        b.apply(new SetCommand(1, new BoardIndex(1, 1)));
        assertEquals(2, b.getTotalPieces());

        b.apply(new SetCommand(1, new BoardIndex(0, 2)));
        assertEquals(2, b.getTotalPieces());
    }

    @Test
    public void testIterate() {
        Board b = new Board(8);
        for(BoardIndex index : b)
            assertEquals(-1, b.at(index));
    }
}
