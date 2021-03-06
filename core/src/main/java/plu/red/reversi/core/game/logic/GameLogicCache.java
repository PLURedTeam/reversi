package plu.red.reversi.core.game.logic;

import java.util.HashMap;

/**
 * This is a cache used to store information relevant to a specific game. The main cache will need to be created
 * when the game is, and then pass it along either directly or indirectly. The Game class will store its cache
 * which will automatically be read if no alternative is specified.
 */
public abstract class GameLogicCache {
    public final HashMap<Integer, Integer> score;


    /**
     * Basic constructor which initializes values to their defaults.
     */
    public GameLogicCache() {
        score = new HashMap<>();
    }


    /**
     * Creates a copy of the current game logic which extends to the data structures, but not what they contain.
     * For example, if an array of BoardIndexes were cached, it would copy the array so both caches can be modified
     * independently, but it would not clone the board indexes with the assumption that they will only be replaced,
     * not changed directly.
     *
     * @param other The cache to copy.
     */
    public GameLogicCache(GameLogicCache other) {
        this.score = new HashMap<>(other.score);
    }


    /**
     * Invalidate the cache. Clears it out and forces it to be regenerated. Use this if the board is changed outside
     * of a game logic function.
     */
    public void invalidate() {
        score.clear();
    }


    /**
     * Creates a copy of the current game logic which extends to the data structures, but not what they contain.
     * For example, if an array of BoardIndexes were cached, it would copy the array so both caches can be modified
     * independently, but it would not clone the board indexes with the assumption that they will only be replaced,
     * not changed directly.
     *
     * @return A copy of the current GameLogicCache.
     */
    public abstract GameLogicCache duplicate();


    /**
     * Add, or subtract, a value from the score of a player. This handles the null case where there is not a player
     * in the score set and will add them. This will do nothing if the player id is < 0
     * @param player The player who's score is to be changed.
     * @param amount Amount (+/-) the score should be changed by.
     * @return True if the cache was modified, false otherwise.
     */
    public boolean addToScore(int player, int amount) {
        if(player < 0) return false;
        Integer value = score.get(player);
        //put the new value in, treating the old one as 0 if it was not in the cache
        score.put(player, (value == null ? 0 : value) + amount);
        return true;
    }
}