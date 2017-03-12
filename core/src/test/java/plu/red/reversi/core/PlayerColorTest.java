package plu.red.reversi.core;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Glory to the Red Team.
 */
public class PlayerColorTest {

    @Test
    public void testValidRoles() {
        assertEquals(false, PlayerColor.NONE.isValid());
        assertEquals(true,  PlayerColor.BLACK.isValid());
        assertEquals(true,  PlayerColor.WHITE.isValid());
    }

    @Test
    public void testValidUsedRoles() {
        Set<PlayerColor> used = new HashSet<PlayerColor>();
        used.add(PlayerColor.WHITE);
        assertEquals(false, PlayerColor.NONE.isValid(used));
        assertEquals(false, PlayerColor.BLACK.isValid(used));
        assertEquals(true,  PlayerColor.WHITE.isValid(used));
    }

    @Test
    public void testValidRoleTotals() {
        // Total Count - needs to be changed when more are added
        assertEquals(2, PlayerColor.validPlayers().length);
    }

    @Test
    public void testValidOrdinal() {
        assertTrue(PlayerColor.NONE.validOrdinal() < 0);
        assertEquals(0, PlayerColor.WHITE.validOrdinal());
        assertEquals(1, PlayerColor.BLACK.validOrdinal());
    }

    @Test
    public void testNext() {
        assertEquals(PlayerColor.NONE, PlayerColor.NONE.getNext());
        assertEquals(PlayerColor.BLACK, PlayerColor.WHITE.getNext());
        assertEquals(PlayerColor.WHITE, PlayerColor.BLACK.getNext());
    }

    @Test
    public void testUsedNext() {
        Set<PlayerColor> used = new HashSet<PlayerColor>();
        used.add(PlayerColor.BLACK);
        assertEquals(PlayerColor.NONE, PlayerColor.NONE.getNext(used));
        assertEquals(PlayerColor.BLACK, PlayerColor.WHITE.getNext(used));
        assertEquals(PlayerColor.BLACK, PlayerColor.BLACK.getNext(used));
    }


}