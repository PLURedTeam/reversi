package plu.red.reversi.core.game.logic;

import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.util.UnionFind;

/**
 * Cached information specific to GoLogic.
 * @see GameLogicCache
 */
public class GoLogicCache extends GameLogicCache {
    public final UnionFind<BoardIndex> groups = new UnionFind<>();
}
