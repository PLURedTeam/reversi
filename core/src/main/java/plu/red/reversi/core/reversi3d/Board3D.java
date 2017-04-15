package plu.red.reversi.core.reversi3d;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import plu.red.reversi.core.game.Board;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.graphics.Graphics3D;
import plu.red.reversi.core.graphics.Pipeline;
import plu.red.reversi.core.listener.IBoardUpdateListener;
import plu.red.reversi.core.util.Color;


/**
 * Represents a board of game pieces, a square of the given size in the constructor. These pieces can be
 * set using the #setBoard or #animBoardUpdate methods.
 */
public class Board3D extends ColorModel3D implements Piece3D.Piece3DListener {

    public static final float PIECE_SIZE = 0.1f;           /// the size of a single tile on the board (including the border below)
    public static final float PIECE_BORDER_SIZE = 0.005f;  /// the size of the space between tiles on the board
    public static final float BORDER_SIZE = 0.08f;         /// the amount of extra space to put on the edge of the board around the tiles

    public static final int ANIMATION_QUEUE_DELAY = 20;    /// the amount of ticks to wait after an animation has been queued has been played

    private Collection<Board3DListener> listeners;

    private Piece3D[] pieces;                              /// an array of all the pieces on the board. Map indexes using #indexFromCoord() method.
    private Highlight3D[] highlights;                      /// an array of all the highlights which can be shown on the board.

    private Deque<BoardUpdate> boardUpdates;               /// the queue of animations which should be performed
    private BoardUpdate currentBoardUpdate;                /// the animation which is currently in progress.

    private Map<Integer, Integer> scores;                  /// a cache of the board score for each piece (by player id)

    private int size;                                      /// the size of one side of the board (the board is a square)

    private Game game;                                     /// the game object this board is showing (though not necessarily showing the latest state of)

    public Board3D(Graphics3D g3d, Pipeline pipeline, Game game) {

        super(g3d, pipeline);

        listeners = new HashSet<>();
        scores = new HashMap<>();

        this.game = game;

        this.size = game.getBoard().size;

        boardUpdates = new LinkedList<>();

        pieces = new Piece3D[(int)Math.pow(size, 2)];
        highlights = new Highlight3D[(int)Math.pow(size, 2)];

        Player[] players = game.getAllPlayers();

        Piece3D piece = new Piece3D(g3d, pipeline, players[0].getColor(), players[1].getColor());
        Highlight3D highlight = new Highlight3D(g3d, pipeline);

        // this first piece is unused, only used to make clones for all the others.
        // it will be discarded after.

        //addChild(piece);
        //pieces[0] = piece;

        Vector3f topLeft = getBoardOrigin().add(PIECE_SIZE / 2, PIECE_SIZE / 2, 0);

        Vector3fc origin = getBoardOrigin();

        for(int i = 0;i < Math.pow(size, 2);i++) {
            int r = i / size;
            int c = i % size;
            Piece3D p = (Piece3D)piece.clone();

            Vector3f pos = new Vector3f(PIECE_SIZE * c + PIECE_SIZE / 2, PIECE_SIZE * r + PIECE_SIZE / 2, Piece3D.VERTICAL_RADIUS).add(origin);

            p.setPos(pos);

            //addChild(p);
            pieces[i] = p;

            Highlight3D h = new Highlight3D(g3d, pipeline);

            h.setHeight(0.002f);

            h.highlightOn(
                    new Vector2f(PIECE_SIZE * c + PIECE_BORDER_SIZE, PIECE_SIZE * r + PIECE_BORDER_SIZE).add(new Vector2f(origin.x(), origin.y())),
                    new Vector2f(PIECE_SIZE * (c + 1) - PIECE_BORDER_SIZE, PIECE_SIZE * (r + 1) - PIECE_BORDER_SIZE).add(new Vector2f(origin.x(), origin.y()))
            );

            //if(r == c)
            //    addChild(h);
            highlights[i] = h;
        }

        // since we have the game
        // TODO: Remove argument
        setBoard(game.getBoard());
    }

    // TODO: Needs testing (even though this method will probobly never be used)
    public Board3D(Board3D other) {

        super(other.getGraphics3D(), other.getPipeline());

        this.size = other.size;

        pieces = new Piece3D[(int)Math.pow(size, 2)];
        highlights = new Highlight3D[(int)Math.pow(size, 2)];

        Vector3fc origin = getBoardOrigin();

        for(int i = 0;i < Math.pow(size, 2);i++) {
            int r = i / size;
            int c = i % size;
            Piece3D p = (Piece3D) other.pieces[i].clone();

            Vector3f pos = new Vector3f(PIECE_SIZE * c + PIECE_SIZE / 2, PIECE_SIZE * r + PIECE_SIZE / 2, Piece3D.VERTICAL_RADIUS / 2).add(origin);

            p.setPos(pos);

            //addChild(p);
            pieces[i] = p;

            Highlight3D h = (Highlight3D) other.highlights[i].clone();

            h.setHeight(0.002f);

            h.highlightOn(
                    new Vector2f(PIECE_SIZE * c + PIECE_BORDER_SIZE, PIECE_SIZE * r + PIECE_BORDER_SIZE).add(new Vector2f(origin.x(), origin.y())),
                    new Vector2f(PIECE_SIZE * (c + 1) - PIECE_BORDER_SIZE, PIECE_SIZE * (r + 1) - PIECE_BORDER_SIZE).add(new Vector2f(origin.x(), origin.y()))
            );

            //if(r == c)
            //    addChild(h);
            highlights[i] = h;
        }
    }

    @Override
    Vector4f[] getFaceColor(int sectionIndex, int faceIndex) {

        Vector4f color;

        if(sectionIndex < 6) {

            color = new Vector4f(0.15f, 0.15f, 0.15f, 1.0f);
        }
        else {
            color = new Vector4f(0.1f, 0.55f, 0.1f, 1.0f);
        }

        return new Vector4f[]{
                color,
                color,
                color,
                color
        };
    }

    @Override
    public Model3D newInstance() {
        return new Board3D(this);
    }

    @Override
    int getSectionCount() {
        return 7;
    }

    @Override
    int getFaceCount(int sectionIndex) {

        if(sectionIndex < 6)
            return 1;
        else
            return (int)Math.pow(size, 2);
    }

    @Override
    Vector3f[] getFace(int sectionIndex, int faceIndex) {

        if(sectionIndex < 6) {
            // return a cube face

            // 8 points of the cube in a more readable format
            Vector3f tdl = new Vector3f(-PIECE_SIZE * (float)size / 2 - BORDER_SIZE, -PIECE_SIZE * (float)size / 2 - BORDER_SIZE, 0);
            Vector3f tdr = new Vector3f( PIECE_SIZE * (float)size / 2 + BORDER_SIZE, -PIECE_SIZE * (float)size / 2 - BORDER_SIZE, 0);
            Vector3f tul = new Vector3f(-PIECE_SIZE * (float)size / 2 - BORDER_SIZE,  PIECE_SIZE * (float)size / 2 + BORDER_SIZE, 0);
            Vector3f tur = new Vector3f( PIECE_SIZE * (float)size / 2 + BORDER_SIZE,  PIECE_SIZE * (float)size / 2 + BORDER_SIZE, 0);
            Vector3f bdl = new Vector3f(-PIECE_SIZE * (float)size / 2 - BORDER_SIZE, -PIECE_SIZE * (float)size / 2 - BORDER_SIZE, -0.25f);
            Vector3f bdr = new Vector3f( PIECE_SIZE * (float)size / 2 + BORDER_SIZE, -PIECE_SIZE * (float)size / 2 - BORDER_SIZE, -0.25f);
            Vector3f bul = new Vector3f(-PIECE_SIZE * (float)size / 2 - BORDER_SIZE,  PIECE_SIZE * (float)size / 2 + BORDER_SIZE, -0.25f);
            Vector3f bur = new Vector3f( PIECE_SIZE * (float)size / 2 + BORDER_SIZE,  PIECE_SIZE * (float)size / 2 + BORDER_SIZE, -0.25f);

            switch(sectionIndex) {
                case 0:
                    return new Vector3f[]{tur, tul, tdl, tdr};
                case 1:
                    return new Vector3f[]{bul, bur, bdr, bdl};
                case 2:
                    return new Vector3f[]{tur, tdr, bdr, bur};
                case 3:
                    return new Vector3f[]{tdl, tul, bul, bdl};
                case 4:
                    return new Vector3f[]{tdr, tdl, bdl, bdr};
                case 5:
                    return new Vector3f[]{tul, tur, bur, bul};
            }
        }
        else {
            // return a quad for one of the checkers of the board
            int r = faceIndex / size;
            int c = faceIndex % size;

            Vector3f topLeft = getBoardOrigin().add(PIECE_BORDER_SIZE + r * PIECE_SIZE, PIECE_BORDER_SIZE + c * PIECE_SIZE, 0);

            return new Vector3f[]{
                    topLeft,
                    new Vector3f(PIECE_SIZE - PIECE_BORDER_SIZE * 2, 0, 0).add(topLeft),
                    new Vector3f(PIECE_SIZE - PIECE_BORDER_SIZE * 2, PIECE_SIZE - PIECE_BORDER_SIZE * 2, 0).add(topLeft),
                    new Vector3f(0, PIECE_SIZE - PIECE_BORDER_SIZE * 2, 0).add(topLeft)
            };
        }

        return null;
    }

    /**
     * Returns the position of the lower left corner of piece 0 on the board; the minimum coordinate of the pieces in a sense
     * @return a vector in 3d space representing the place (in board coordinates, so offset from board center, regardless of board transforms)
     */
    protected Vector3f getBoardOrigin() {
        return new Vector3f(-PIECE_SIZE * (float)size / 2, -PIECE_SIZE * (float)size / 2, 0.001f);
    }

    /**
     * Returns the distance from the center to the edge of the furthest piece away from the center
     * @return the distance in GL coordinates from the center to the edge.
     */
    public float getBoardRadius() {
        return PIECE_SIZE * size / 4;
    }

    /**
     * Disables rendering of all highlighting models
     */
    public void clearHighlights() {
        for(int i = 0;i < highlights.length;i++) {
            removeChild(highlights[i]);
        }
    }

    /**
     * Enables highlighting at the specified board index with the specified color.
     *
     * If a highlight was already present, it will be overridden by this call.
     *
     * @param index the board index to highlight
     * @param color the color of the highlighted board index.
     */
    public void highlightAt(BoardIndex index, Vector3fc color) {

        int i = indexFromCoord(toBoardCoords(index, size), size);

        highlights[i].setColor(color);

        addChild(highlights[i]);
    }

    @Override
    public boolean update(int tick) {

        boolean updated = super.update(tick);

        boolean done = false;

        if(currentBoardUpdate != null && currentBoardUpdate.triggerTick + currentBoardUpdate.duration <= tick) {
            currentBoardUpdate = null;
            done = true;

            for(Board3DListener listener : listeners) {
                listener.onAnimationStepDone(this);
            }
        }

        if(boardUpdates.peek() != null && boardUpdates.peek().triggerTick <= tick) {
            currentBoardUpdate = boardUpdates.pop();
            currentBoardUpdate.dispatch();

            // after a board update has been applied, update the cached scores
            refreshScores();

            for(Board3DListener listener : listeners)
                listener.onScoreChange(this);

            done = false;
        }

        if(done && boardUpdates.isEmpty())
            for(Board3DListener listener : listeners)
                listener.onAnimationsDone(this);

        return updated;
    }

    private void refreshScores() {

        Player[] players = game.getAllPlayers();

        if(players[0].getColor() != pieces[0].getBaseColor()) {
            Player tmp = players[0];
            players[0] = players[1];
            players[1] = tmp;
        }

        int bc = players[0].getID();
        int fc = players[1].getID();

        scores.clear();

        for(int i = 0;i < pieces.length;i++) {
            //noinspection StatementWithEmptyBody
            if(!isChild(pieces[i]));
            else if(!pieces[i].isFlipped()) {
                if(!scores.containsKey(bc))
                    scores.put(bc, 0);

                scores.put(bc, scores.get(bc) + 1);
            }
            else {
                if(!scores.containsKey(fc))
                    scores.put(fc, 0);

                scores.put(fc, scores.get(fc) + 1);
            }
        }
    }

    /**
     * Gets the currently displayed score for the specified player id
     * @param playerId the player to get the score for
     * @return the currently displayed score.
     */
    public int getScore(int playerId) {
        if(scores.get(playerId) == null)
            return 0;
        return scores.get(playerId);
    }

    /**
     * Apply an update to the board. If multiple updates have already been provided, then the update
     * will be added to a queue and executed later. Order matters in this function call!
     *
     * @param update Information about the update
     * @see IBoardUpdateListener.BoardUpdate
     */
    public void animBoardUpdate(IBoardUpdateListener.BoardUpdate update) {
        //TODO: handle all of the update types
        int triggerTime = getLastTick();

        if(boardUpdates.peekLast() != null)
            triggerTime = boardUpdates.peekLast().triggerTick + boardUpdates.peekLast().duration + ANIMATION_QUEUE_DELAY;
        else if(currentBoardUpdate != null)
            triggerTime = currentBoardUpdate.triggerTick + currentBoardUpdate.duration + ANIMATION_QUEUE_DELAY;

        boardUpdates.add(new BoardUpdate(update, triggerTime));
    }

    /**
     * Immediately stops any running animations and clears the board update queue. the board will be left in a state
     * which reflects what would be visible after the current board update animation has finished completing.
     */
    public void clearAnimations() {
        boardUpdates.clear();

        if(currentBoardUpdate != null) {
            for(BoardIndex index : currentBoardUpdate.flipped)
                pieces[indexFromCoord(toBoardCoords(index, size), size)].clearAnimations();

            currentBoardUpdate = null;

            // technically we just completed an animation step, just earlier htan expected.
            for(Board3DListener listener : listeners)
                listener.onAnimationStepDone(this);
        }
    }

    /**
     * Manually set a piece to be the specified playerId at board index
     * @param index the board index to set
     * @param playerId the new value of player id, or -1 if the piece is not supposed to be set to that.
     */
    public void setPiece(BoardIndex index, int playerId) {
        Player p = game.getPlayer(playerId);

        Color color = null;

        if(p != null)
            color = p.getColor();

        Vector2ic coords = toBoardCoords(index, size);
        int i  = indexFromCoord(coords, size);

        if(color == null) {
            removeChild(pieces[i]);
        }
        else if(color.equals(pieces[i].getBaseColor())) {
            addChild(pieces[i]);
            pieces[i].setFlipped(false);
        }
        else {
            addChild(pieces[i]);
            pieces[i].setFlipped(true);
        }
    }

    /**
     * Manually set the current board state to be equivalent to the given board.
     * @param board the data to set
     * @throws InvalidParameterException if the board size is not the same as the size used to make the board3d in constructor
     */
    public void setBoard(Board board) {

        clearAnimations();

        if(board.size != size)
            throw new InvalidParameterException("Board is not the same size as Board3D");

        for(int r = 0;r < size;r++) {
            for(int c = 0;c < size;c++) {

                int i = indexFromCoord(new Vector2i(r, c), size);

                BoardIndex idx = fromBoardCoords(new Vector2i(r, c), size);

                setPiece(idx, board.at(idx));
            }
        }

        refreshScores();

        for(Board3DListener listener : listeners)
            listener.onScoreChange(this);
    }

    /**
     * Calculate the board index which corresponds to the given G3D coordinates
     * @param pos the position in the X Y relative to the center of the board to find the center for
     * @return the board index which would be at this position
     */
    public BoardIndex getIndexAtCoord(Vector2fc pos) {

        Vector2f offset = new Vector2f(getBoardOrigin().x(), getBoardOrigin().y()).add(PIECE_BORDER_SIZE, PIECE_BORDER_SIZE);

        pos.sub(offset, offset);

        return fromBoardCoords(new Vector2i(
                (int)(offset.x / PIECE_SIZE),
                (int)(offset.y / PIECE_SIZE)
        ), size);
    }

    /**
     * Calculate the board index from the given board3d coordinate vector.
     * @param coords the coordinates to convert
     * @param size the size of the board to convert for
     * @return
     */
    public static BoardIndex fromBoardCoords(Vector2ic coords, int size) {
        return new BoardIndex(size - coords.x() - 1, coords.y());
    }

    /**
     * Calculate the board3d coordinate vector from a board index
     * @param idx the index to get the coordinate vector for
     * @param size the size of the board
     * @return the board3d coordinate vector
     */
    public static Vector2i toBoardCoords(BoardIndex idx, int size) {
        return new Vector2i(
                size - idx.row - 1,
                idx.column
        );
    }

    /**
     * Calculate the index (for pieces or highlights array) which corresponds to the given board3d coordinate vector
     * @param coords the board3d coordinate vector to get the index for
     * @param size the size of the board
     * @return the index which would contain the piece/highlight you are looking for at the given board index
     */
    public static int indexFromCoord(Vector2ic coords, int size) {
        return size * size - (size - coords.y() - 1) * size - (size - coords.x() - 1) - 1;
    }

    /**
     * Calculate the coordinate at which the given index would reside. Opposite of #indexFromCoord()
     * @param i the index of a peice or highlight
     * @param size the size of the board
     * @return a board3d coordinate vector at the specified index
     */
    public static Vector2ic indexToCoord(int i, int size) {

        i = size * size - i - 1;

        return new Vector2i(
                size - (i % size) - 1,
                size - (i / size) - 1
        );
    }

    @Override
    public void onFlipped(Piece3D piece) {
        // for right now we do not do anything with this
    }

    @Override
    public void onAnimationsDone(Piece3D piece) {
        // for right now we do not do anything with this
    }

    /**
     * Add a new event listener to this board object
     * @param listener the listener to add
     */
    public void addListener(Board3DListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove an existing event listener from this board object
     * @param listener the listener to remove
     */
    public void removeListener(Board3DListener listener) {
        listeners.remove(listener);
    }

    /**
     * Defines a future animated change which will be applied to the board. Contains a method dispatch() which actually
     * performs the update.
     *
     * Also calculates some helper variables such as duration which specify how long the animation can be expected to last,
     * which is important for animation queueing.
     *
     */
    private class BoardUpdate extends IBoardUpdateListener.BoardUpdate {
        public int triggerTick;
        public int duration;

        public BoardUpdate(IBoardUpdateListener.BoardUpdate update, int triggerTick) {
            super(update);
            this.triggerTick = triggerTick;

            duration = 0;

            for(BoardIndex idx : flipped)
                duration = Math.max(duration, delayForPiece(idx) + Piece3D.ANIMATION_FLIP_DURATION);
        }

        /**
         * Calculates the amount of time to wait from the trigger to start flipping the given piece
         * @param idx the index at which to calculate the time for (relative from the origin in this case)
         * @return
         */
        public int delayForPiece(BoardIndex idx) {
            final float POWER = 1.3f;
            final int START_DELAY = 20; // in ticks

            final BoardIndex origin = added.iterator().next();
            return (int)(Math.pow(Math.max(Math.abs(idx.row - origin.row) - 1, Math.abs(idx.column - origin.column) - 1), POWER) * START_DELAY);
        }

        /**
         * Run the animation on the board
         */
        private void dispatch() {
            final BoardIndex origin = added.iterator().next();
            setPiece(origin, player);

            for(BoardIndex index : flipped) {
                Vector2ic coords = toBoardCoords(index, size);

                int i = indexFromCoord(coords, size);

                boolean flipped = pieces[0].getFlippedColor() == game.getPlayer(player).getColor();

                // flips are delayed dramatically (see the power delay function above)
                pieces[i].animateFlip(flipped, getLastTick() + delayForPiece(index));
            }
        }
    }

    public interface Board3DListener {
        /**
         * Called when the board was updated in such a way that the scores of each of the displayed players
         * has changed
         * @param board the board which generated this event
         */
        void onScoreChange(Board3D board);

        /**
         * Called when the board has completed all animations and has reached the latest state that it knows about.
         * @param board the board which generated this event
         */
        void onAnimationsDone(Board3D board);

        /**
         * Called when a single board update has been completed. You can expect to receive exactly one of these
         * calls per call to #animBoardUpdate() after the animation has completed.
         * @param board
         */
        void onAnimationStepDone(Board3D board);
    }
}
