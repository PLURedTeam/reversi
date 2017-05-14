package plu.red.reversi.core;

import org.junit.Test;
import plu.red.reversi.core.command.SetCommand;
import plu.red.reversi.core.game.Board;
import plu.red.reversi.core.game.BoardIndex;

import java.util.Iterator;

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

        Board b = new Board(3);

        // check the 4 corners are iterated properly
        b.apply(new SetCommand(1, new BoardIndex(0, 0)));
        b.apply(new SetCommand(2, new BoardIndex(0, 2)));
        b.apply(new SetCommand(3, new BoardIndex(2, 0)));
        b.apply(new SetCommand(4, new BoardIndex(2, 2)));

        Iterator<BoardIndex> iter = b.iterator();

        assertEquals(1, b.at(iter.next()));
        assertEquals(-1, b.at(iter.next()));
        assertEquals(2, b.at(iter.next()));
        assertEquals(-1, b.at(iter.next()));
        assertEquals(-1, b.at(iter.next()));
        assertEquals(-1, b.at(iter.next()));
        assertEquals(3, b.at(iter.next()));
        assertEquals(-1, b.at(iter.next()));
        assertEquals(4, b.at(iter.next()));

        assertFalse(iter.hasNext());
    }
}
