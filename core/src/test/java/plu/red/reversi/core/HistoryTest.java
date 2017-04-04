package plu.red.reversi.core;

import org.junit.Before;
import org.junit.Test;
import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.History;

import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class HistoryTest {
    private History history;

    @Before
    public void setup() {
        history = new History();
    }

    @Test
    public void testHistory() {
        assertEquals(0, history.getNumBoardCommands());
    }

    @Test
    public void testHistoryHistory() {
        History h2 = new History(history);

        history.addCommand(new MoveCommand(1, new BoardIndex(0, 0)));

        assertEquals(0, h2.getNumBoardCommands());
    }

    @Test
    public void testGetBoardCommand() {
        try {
            history.getBoardCommand(0);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}

        MoveCommand c = new MoveCommand(1, new BoardIndex(1, 2));
        history.addCommand(c);
        assertEquals(history.getBoardCommand(0), c);
    }

    @Test
    public void testGetMoveCommandsUntil() {
        LinkedList<BoardCommand> t = history.getMoveCommandsUntil(0);

        try {
            t = history.getMoveCommandsUntil(1);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}

        history.addCommand(new MoveCommand(0, new BoardIndex(1, 2)));
        history.addCommand(new MoveCommand(1, new BoardIndex(3, 5)));
        history.addCommand(new MoveCommand(0, new BoardIndex(2, 0)));

        assertEquals(1, history.getMoveCommandsUntil(1).size());
        assertEquals(3, history.getMoveCommandsUntil(3).size());

        assertEquals(5, ((MoveCommand)history.getMoveCommandsUntil(3).get(1)).position.column);
    }
}
