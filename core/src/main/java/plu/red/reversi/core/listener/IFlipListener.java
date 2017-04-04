package plu.red.reversi.core.listener;

import plu.red.reversi.core.game.BoardIndex;

/**
 * Glory to the Red Team.
 *
 * Interface for indicating when a sequence of tiles should be flipped. Implemented by the section of GUI that controls
 * the Board View.
 */
public interface IFlipListener extends IListener {

    /**
     * Indicates that a section of tiles should be flipped.
     *
     * @param startPosition BoardIndex representing the starting tile
     * @param endPosition BoardIndex representing the ending tile
     * @param playerID Integer ID representing the Player that is being flipped to
     */
    void doFlip(BoardIndex startPosition, BoardIndex endPosition, int playerID);
}
