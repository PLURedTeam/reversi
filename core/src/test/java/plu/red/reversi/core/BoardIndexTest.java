package plu.red.reversi.core;

import org.junit.Assert;
import org.junit.Test;
import plu.red.reversi.core.game.BoardIndex;

/**
 * Created by daniel on 3/19/17.
 */
public class BoardIndexTest {
    @Test
    public void testCoordinateString() {
        BoardIndex index = new BoardIndex(0,0);

        Assert.assertEquals("a1", index.getCoordinateString());

        index = new BoardIndex(9, 2);

        Assert.assertEquals("c10", index.getCoordinateString());

        index = new BoardIndex(2, 9);

        Assert.assertEquals("j3", index.getCoordinateString());
    }
}
