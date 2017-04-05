package plu.red.reversi.core.game;

import plu.red.reversi.core.util.DataMap;

import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * Used to keep track of the times for the players.
 */
public class TurnTimer {
    private final long TURN_TIME;
    private final long TURN_INC;

    private HashMap<Integer, Long> playerTimes;
    private int currentPlayer;
    private long lastSwitch;
    private boolean paused;

    /**
     * Construct a new turn timer in a paused state.
     * @param turnTime Total time a player is allowed during the game in Seconds.
     * @param turnInc Amount of time given to a player after completing a turn in Seconds.
     */
    public TurnTimer(int turnTime, int turnInc) {
        TURN_TIME = (long)turnTime * 1000;
        TURN_INC = (long)turnInc * 1000;

        playerTimes = new HashMap<>();
        lastSwitch = 0;
        currentPlayer = -1;
        paused = true;
    }

    /**
     * Construct a new turn timer which is running.
     * @param turnTime Total time a player is allowed during the game in seconds.
     * @param turnInc Amount of time given to a player of completing a turn in seconds.
     * @param startingPlayer The player who's timer will begin counting down.
     */
    public TurnTimer(int turnTime, int turnInc, int startingPlayer) {
        this(turnTime, turnInc);

        //will only error if turnTime is <= 0
        try { startTurn(startingPlayer); } catch (TimeoutException e) { e.printStackTrace(); }
    }

    /**
     * End the last turn and start a new one for the given player. If there is no current turn, then it simply
     * starts the new players turn. This will unpause the turn timer. This will do nothing when given an invalid player.
     * @param player Player of the new turn.
     * @return reference to this object for chaining.
     * @throws TimeoutException When the new player is out of time (it also pauses the timer).
     */
    public TurnTimer startTurn(int player) throws TimeoutException {
        if(player < 0) return this;

        final long current_time = System.currentTimeMillis();

        //end last turn if it was a valid player
        if(currentPlayer >= 0) {
            Long t = playerTimes.get(currentPlayer);
            long time = t != null ? t : TURN_TIME;

            if(!paused) time -= (current_time - lastSwitch);
            time += TURN_INC;
            playerTimes.put(currentPlayer, time);
        }

        //update state information, effectively starting the new turn
        currentPlayer = player;
        lastSwitch = current_time;
        paused = false;

        if(getCurrentPlayerRemainingTime() <= 0) {
            paused = true;
            throw new TimeoutException("Player " + currentPlayer + " is out of time.");
        }

        return this;
    }

    /**
     * Get the remaining time for a player.
     * @param player A valid player.
     * @return The Player's remaining time for the game.
     */
    public int getRemainingTime(int player) {
        if(player < 0) return 0;

        Long t = playerTimes.get(player);
        long time = (t != null ? t : TURN_TIME); //default it if player is not in map
        if(!paused && player == currentPlayer)
            //current player needs to account for time since their turn began as well
            time -= (System.currentTimeMillis() - lastSwitch);

        return (int)(time / 1000);
    }

    /**
     * This returns the remaining time of the current player (or 0 if there is no current player).
     * @return Remaining time of the current player.
     */
    public int getCurrentPlayerRemainingTime() {
        return getRemainingTime(currentPlayer);
    }

    /**
     * @return The current player who's turn it is currently.
     */
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Pauses the turn timer. While paused, the turn timer will not decrement.
     */
    public void pause() {
        if(paused) return;

        final long current_time = System.currentTimeMillis();
        paused = true;
        Long t = playerTimes.get(currentPlayer);
        final long time = t != null ? t : TURN_TIME;

        playerTimes.put(currentPlayer,time - (current_time - lastSwitch));
    }

    /**
     * Resumes the turn timer. While active, the current player's timer will be decremented.
     */
    public void unpause() {
        if(!paused) return;
        paused = false;
        lastSwitch = System.currentTimeMillis();
    }

    public boolean isPaused() { return paused; }
}
