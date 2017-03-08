package plu.red.reversi.core;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Glory to the Red Team.
 */
public class PlayerRoleTest {

    @Test
    public void testValidRoles() {
        assertEquals(false, PlayerRole.NONE.isValid());
        assertEquals(true,  PlayerRole.BLACK.isValid());
        assertEquals(true,  PlayerRole.WHITE.isValid());
    }

    @Test
    public void testValidUsedRoles() {
        Set<PlayerRole> used = new HashSet<PlayerRole>();
        used.add(PlayerRole.WHITE);
        assertEquals(false, PlayerRole.NONE.isValid(used));
        assertEquals(false, PlayerRole.BLACK.isValid(used));
        assertEquals(true,  PlayerRole.WHITE.isValid(used));
    }

    @Test
    public void testValidRoleTotals() {
        // Total Count - needs to be changed when more are added
        assertEquals(2, PlayerRole.validPlayers().length);
    }

    @Test
    public void testValidOrdinal() {
        assertTrue(PlayerRole.NONE.validOrdinal() < 0);
        assertEquals(0, PlayerRole.WHITE.validOrdinal());
        assertEquals(1, PlayerRole.BLACK.validOrdinal());
    }

    @Test
    public void testNext() {
        assertEquals(PlayerRole.NONE, PlayerRole.NONE.getNext());
        assertEquals(PlayerRole.BLACK, PlayerRole.WHITE.getNext());
        assertEquals(PlayerRole.WHITE, PlayerRole.BLACK.getNext());
    }

    @Test
    public void testUsedNext() {
        Set<PlayerRole> used = new HashSet<PlayerRole>();
        used.add(PlayerRole.BLACK);
        assertEquals(PlayerRole.NONE, PlayerRole.NONE.getNext(used));
        assertEquals(PlayerRole.BLACK, PlayerRole.WHITE.getNext(used));
        assertEquals(PlayerRole.BLACK, PlayerRole.BLACK.getNext(used));
    }


}