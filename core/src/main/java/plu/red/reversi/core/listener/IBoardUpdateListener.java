package plu.red.reversi.core.listener;

import java.util.Collection;

import plu.red.reversi.core.game.BoardIndex;

/**
 * Created by daniel on 4/6/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

/**
 * In order to facilitate board updates in a way that allows for better queueing/state management,
 */
public interface IBoardUpdateListener {
    /**
     * Called by Board when the state has been modified by changing a single index of the board, with
     * side effects (i.e. the tiles are now flipped).
     *
     * This method can be expected to be called once per:
     * * Move Command
     * * Set Command
     *
     * Sending tile updates in this way is useful for animation queueing on a GUI.
     *
     * @param origin The index of the board where a piece has been set to a new value
     * @param playerId the new value of the piece at BoardIndex (could be -1 to indicate the piece was removed)
     * @param updated A collection of tiles which have been updated to match origin as a result of the change at origin
     */
    void onBoardUpdate(BoardIndex origin, int playerId, Collection<BoardIndex> updated);

    /**
     * Called by Board when the state has been altered so dramatically that all board contents should
     * simply be completely refreshed.
     *
     * This is called by board when it would be more of a pain in the butt to use onBoardUpdate()
     * whenever major changes (bigger than a single movecommand or setcommand) are applied.
     *
     */
    void onBoardRefresh();
}
