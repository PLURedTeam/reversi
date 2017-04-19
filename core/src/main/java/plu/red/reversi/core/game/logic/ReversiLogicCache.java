package plu.red.reversi.core.game.logic;

/**
 * Cached information specific to Reversi.
 * @see GameLogicCache
 */
public class ReversiLogicCache extends GameLogicCache {
    /**
     * Basic constructor which initializes values to their defaults.
     */
    public ReversiLogicCache() {
        super();
    }


    /**
     * Creates a copy of the current game logic which extends to the data structures, but not what they contain.
     * For example, if an array of BoardIndexes were cached, it would copy the array so both caches can be modified
     * independently, but it would not clone the board indexes with the assumption that they will only be replaced,
     * not changed directly.
     *
     * @param other The cache to copy.
     */
    public ReversiLogicCache(ReversiLogicCache other) {
        super(other);
    }


    /**
     * Creates a copy of the current game logic which extends to the data structures, but not what they contain.
     * For example, if an array of BoardIndexes were cached, it would copy the array so both caches can be modified
     * independently, but it would not clone the board indexes with the assumption that they will only be replaced,
     * not changed directly.
     *
     * @return A copy of the current GameLogicCache.
     */
    @Override
    public GameLogicCache duplicate() {
        return new ReversiLogicCache(this);
    }
}
