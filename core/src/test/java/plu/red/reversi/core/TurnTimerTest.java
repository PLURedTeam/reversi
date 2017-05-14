package plu.red.reversi.core;

import org.junit.Test;
import plu.red.reversi.core.game.TurnTimer;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class TurnTimerTest {

    @Test
    public void testTurnTimer() {
        TurnTimer timer = new TurnTimer(300, 5);
        assertTrue(timer.getCurrentPlayer() <= 0);
        assertEquals(0, timer.getCurrentPlayerRemainingTime());
        assertTrue(timer.isPaused());
        assertEquals(300, timer.getRemainingTime(1));
        assertEquals(300, timer.getRemainingTime(2));
    }

    @Test
    public void testStartTurn() {
        TurnTimer timer = new TurnTimer(300, 5);
        try {
            timer.startTurn(0);
        } catch (TimeoutException e) {
            assertTrue(false);
        }

        assertFalse(timer.isPaused());
        assertEquals(0, timer.getCurrentPlayer());

        try {
            timer.startTurn(1);
        } catch (TimeoutException e) {
            assertTrue(false);
        }
        assertFalse(timer.isPaused());
        int t = timer.getRemainingTime(0);
        assertTrue(t > 302 && t <= 305); //fuzzy compare just in case
        t = timer.getCurrentPlayerRemainingTime();
        assertTrue(t <= 300 && t > 297);
    }

    @Test
    public void testPausing() {
        TurnTimer timer = new TurnTimer(300, 5, 0);
        timer.pause();
        assertTrue(timer.isPaused());
        timer.unpause();
        assertFalse(timer.isPaused());
    }
}
