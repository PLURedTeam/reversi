package plu.red.reversi.core;

import org.junit.Test;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.game.Board;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.logic.GameLogic;
import plu.red.reversi.core.game.logic.GameLogicCache;
import plu.red.reversi.core.game.logic.GoLogic;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GoLogicTest {
    @Test
    public void testGetScore() {
        GameLogic logic = new GoLogic();
        Board board = new Board(4);
        GameLogicCache cache = logic.createCache();
        logic.initBoard(cache, board, new int[]{0, 1}, false, false);

        assertEquals(0, logic.getScore(cache, board, 0));
        assertEquals(0, logic.getScore(cache, board, 1));

        logic.play(cache, board, new MoveCommand(0, new BoardIndex(0, 0)), false, false);
        logic.play(cache, board, new MoveCommand(1, new BoardIndex(0, 1)), false, false);
        logic.play(cache, board, new MoveCommand(1, new BoardIndex(1, 0)), false, false);

        assertEquals(-1, board.at(new BoardIndex(0, 0)));
        assertEquals(1, logic.getScore(cache, board, 1));
    }

//    @Test
//    public void testIsValidMove() {
//        Board board = new Board(4);
//        GameLogic logic = new ReversiLogic();
//        GameLogicCache cache = logic.createCache();
//        logic.initBoard(cache, board, new int[]{0, 1}, false, false);
//
//        MoveCommand m = new MoveCommand(1, new BoardIndex(0, 0));
//        assertFalse(logic.isValidMove(cache, board, m));
//
//        m.position.row = 1; m.position.column = 0;
//        assertTrue(logic.isValidMove(cache, board, m));
//
//        m.position.row = 0; m.position.column = 1;
//        assertTrue(logic.isValidMove(cache, board, m));
//
//        m.position.row = 2; m.position.column = 3;
//        assertTrue(logic.isValidMove(cache, board, m));
//
//        m.position.row = 3; m.position.column = 2;
//        assertTrue(logic.isValidMove(cache, board, m));
//    }
//
//    @Test
//    public void testGetValidMoves() {
//        Board board = new Board(4);
//        GameLogic logic = new ReversiLogic();
//        GameLogicCache cache = logic.createCache();
//        logic.initBoard(cache, board, new int[]{0, 1}, false, false);
//
//        Set<BoardIndex> moveList;
//        moveList = logic.getValidMoves(cache, board, 1);
//
//        assertEquals(4, moveList.size());
//        assertTrue(moveList.contains(new BoardIndex(0, 1)));
//        assertTrue(moveList.contains(new BoardIndex(1, 0)));
//        assertTrue(moveList.contains(new BoardIndex(2, 3)));
//        assertTrue(moveList.contains(new BoardIndex(3, 2)));
//
//    }
//
//
//    @Test
//    public void testPlay() {
//        //moving a piece in a legal index
//        MoveCommand c = new MoveCommand(Command.Source.CLIENT, 0, new BoardIndex(1, 0));
//
//        Board board = new Board(4);
//        GameLogic logic = new ReversiLogic();
//        GameLogicCache cache = logic.createCache();
//        logic.initBoard(cache, board, new int[]{0,1}, false, false);
//
//        try {
//            logic.play(cache, board, c, false, false);
//            assertTrue(false);
//        } catch(IllegalArgumentException e) {
//            assertTrue(true);
//        }
//    }
//
//    @Test
//    public void testRepeatedPlays() {
//        Board board = new Board(8);
//        GameLogic logic = new ReversiLogic();
//        GameLogicCache cache = logic.createCache();
//        logic.initBoard(cache, board, new int[]{0, 1}, false, false);
//
//        for(int x = 0; x < 15; ++x) {
//            MoveCommand m = new MoveCommand(0, logic.getValidMoves(cache, board, 0).iterator().next());
//            assertTrue(logic.isValidMove(cache, board, m));
//            logic.play(cache, board, m, false, false);
//
//            BoardIndex i = new BoardIndex(logic.getValidMoves(cache, board, 1).iterator().next());
//            assertTrue(logic.isValidMove(cache, board, new MoveCommand(1, i)));
//            logic.play(cache, board, new MoveCommand(1, i), false, false);
//        }
//    }
}
