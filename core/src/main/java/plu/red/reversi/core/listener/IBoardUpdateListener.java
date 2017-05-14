package plu.red.reversi.core.listener;

import plu.red.reversi.core.game.BoardIndex;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
     * @param update Information about the changes made.
     * @see BoardUpdate
     */
    void onBoardUpdate(BoardUpdate update);

    /**
     * Called by Board when the state has been altered so dramatically that all board contents should
     * simply be completely refreshed.
     *
     * This is called by board when it would be more of a pain in the butt to use onBoardUpdate()
     * whenever major changes (bigger than a single movecommand or setcommand) are applied.
     *
     */
    void onBoardRefresh();


    /**
     * Board update structure which contains information relevent to onBoardUpdate calls.
     */
    public static class BoardUpdate {
        /// Player that tiles will be set to. (the new player id)
        public int player;
        /// New tiles which are put onto the board.
        public Collection<BoardIndex> added = new LinkedList<>();
        /// Tiles which change to the new player value.
        public Collection<BoardIndex> flipped = new LinkedList<>();
        /// Pieces which are taken off of the board.
        public Collection<BoardIndex> removed = new LinkedList<>();
        /// Ordered list of pieces which change position.
        public List<Jump> jumped = new LinkedList<>();

        public BoardUpdate() {}

        /**
         * Basic copy constructor. Not a deep copy.
         */
        public BoardUpdate(BoardUpdate other) {
            player = other.player;
            added = other.added;
            flipped = other.flipped;
            removed = other.removed;
            jumped = other.jumped;
        }

        public static class Jump {
            public BoardIndex from;
            public BoardIndex to;
        }
    }
}
