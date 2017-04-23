package plu.red.reversi.core.game.logic;

import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.util.UnionFind;

/**
 * Cached information specific to GoLogic.
 * @see GameLogicCache
 */
public class GoLogicCache extends GameLogicCache {
    public final UnionFind<BoardIndex> groups;


    /**
     * Basic constructor which initializes values to their defaults.
     */
    public GoLogicCache() {
        super();
        groups = new UnionFind<>();
    }


    /**
     * Creates a copy of the current game logic which extends to the data structures, but not what they contain.
     * For example, if an array of BoardIndexes were cached, it would copy the array so both caches can be modified
     * independently, but it would not clone the board indexes with the assumption that they will only be replaced,
     * not changed directly.
     *
     * @param other The cache to copy.
     */
    public GoLogicCache(GoLogicCache other) {
        super(other);
        groups = new UnionFind<>(other.groups);
    }


    /**
     * Invalidate the cache. Clears it out and forces it to be regenerated. Use this if the board is changed outside
     * of a game logic function.
     */
    public void invalidate() {
        super.invalidate();
        groups.clear();
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
        return new GoLogicCache(this);
    }
}
