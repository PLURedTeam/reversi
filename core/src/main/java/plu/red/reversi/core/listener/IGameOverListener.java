package plu.red.reversi.core.listener;

import plu.red.reversi.core.player.Player;

/**
 * Glory to the Red Team.
 *
 * Interface for when a Game has ended, either naturally or forcefully (but not when one is paused
 * and saved halfway through).
 */
public interface IGameOverListener {

    /**
     * Called when the Game ends and a player has won.
     * IT IS POSSIBLE FOR NO ONE TO WIN, although this happens very rarely (such as if everyone surrenders).
     *
     * @param player Player object representing who won
     * @param score the final winning score
     */
    void onGameOver(Player player, int score);
}
