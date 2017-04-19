package plu.red.reversi.core.game.logic;

import java.util.HashMap;

/**
 * This is a cache used to store information relevant to a specific game. The main cache will need to be created
 * when the game is, and then pass it along either directly or indirectly. The Game class will store its cache
 * which will automatically be read if no alternative is specified.
 */
public abstract class GameLogicCache {
    public final HashMap<Integer, Integer> score = new HashMap<>();
}