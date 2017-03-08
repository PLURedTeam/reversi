package plu.red.reversi.core;

/**
 * Created by daniel on 3/5/17.
 * Glory to the Red Team.
 */

/**
 * Represents a single move on the reversi board
 */
public class Command {
    public PlayerRole role;
    public BoardIndex index;


    /**
     * Gets a list of all the indexes on the board which have been affected by this move
     * @note this will require the ability to see the state of the board at the point of that move for this to work, but it is required to implement this
     * @return a list of indices, which were affected by this move. Order does not matter.
     */
    public BoardIndex[] getAffectedIndices() {
        return null;
    }
}
