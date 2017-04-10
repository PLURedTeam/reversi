package plu.red.reversi.android.reversi3d;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import plu.red.reversi.android.graphics.Graphics3D;
import plu.red.reversi.android.graphics.Pipeline;
import plu.red.reversi.core.game.Board;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.util.Color;

/**
 * Created by daniel on 3/20/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public class Board3D extends ColorModel3D implements Piece3D.Piece3DListener {

    public static final float PIECE_SIZE = 0.1f;
    public static final float PIECE_BORDER_SIZE = 0.005f;
    public static final float BORDER_SIZE = 0.08f;

    public static final int ANIMATION_QUEUE_DELAY = 20;

    private Collection<Board3DListener> listeners;

    private Piece3D[] pieces;
    private Highlight3D[] highlights;

    private Deque<BoardUpdate> boardUpdates;
    private BoardUpdate currentBoardUpdate;

    private Map<Integer, Integer> scores;

    private int size;

    private Game game;

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

            Highlight3D h = (Highlight3D)highlight.clone();

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

            color = new Vector4f(0.2f, 0.2f, 0.2f, 1.0f);
        }
        else {
            color = new Vector4f(0.2f, 0.7f, 0.2f, 1.0f);
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

    protected Vector3f getBoardOrigin() {
        return new Vector3f(-PIECE_SIZE * (float)size / 2, -PIECE_SIZE * (float)size / 2, 0.001f);
    }

    public float getBoardRadius() {
        return PIECE_SIZE * size / 4;
    }

    public void clearHighlights() {
        for(int i = 0;i < highlights.length;i++) {
            removeChild(highlights[i]);
        }
    }

    public void highlightAt(BoardIndex index) {

        Vector2ic rc = toBoardCoords(index, size);

        addChild(highlights[indexFromCoord(rc, size)]);
    }

    @Override
    public boolean update(int tick) {

        boolean updated = super.update(tick);

        boolean done = false;

        if(currentBoardUpdate != null && currentBoardUpdate.triggerTick + currentBoardUpdate.duration <= tick) {
            currentBoardUpdate = null;

            done = true;
        }

        if(boardUpdates.peek() != null && boardUpdates.peek().triggerTick <= tick) {
            currentBoardUpdate = boardUpdates.poll();
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

    public int getScore(int playerId) {
        if(scores.get(playerId) == null)
            return 0;
        return scores.get(playerId);
    }

    public void animBoardUpdate(BoardIndex origin, int playerId, Collection<BoardIndex> updated) {
        int triggerTime = getLastTick();

        if(boardUpdates.peekLast() != null)
            triggerTime = boardUpdates.peekLast().triggerTick + boardUpdates.peekLast().duration + ANIMATION_QUEUE_DELAY;
        else if(currentBoardUpdate != null)
            triggerTime = currentBoardUpdate.triggerTick + currentBoardUpdate.duration + ANIMATION_QUEUE_DELAY;

        boardUpdates.add(new BoardUpdate(origin, playerId, updated, triggerTime));
    }

    public void setPiece(Game game, BoardIndex index, int playerId) {
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

    public void setBoard(Game game) {

        if(game.getBoard().size != size)
            throw new InvalidParameterException("Board is not the same size as Board3D");

        for(int r = 0;r < size;r++) {
            for(int c = 0;c < size;c++) {

                int i = indexFromCoord(new Vector2i(r, c), size);

                BoardIndex idx = fromBoardCoords(new Vector2i(r, c), size);

                setPiece(game, idx, game.getBoard().at(idx));
            }
        }

        refreshScores();

        for(Board3DListener listener : listeners)
            listener.onScoreChange(this);
    }

    public BoardIndex getIndexAtCoord(Vector2fc pos) {

        Vector2f offset = new Vector2f(getBoardOrigin().x(), getBoardOrigin().y()).add(PIECE_BORDER_SIZE, PIECE_BORDER_SIZE);

        pos.sub(offset, offset);

        return fromBoardCoords(new Vector2i(
                (int)(offset.x / PIECE_SIZE),
                (int)(offset.y / PIECE_SIZE)
        ), size);
    }

    public static BoardIndex fromBoardCoords(Vector2ic coords, int size) {
        return new BoardIndex(size - coords.x() - 1, coords.y());
    }

    public static Vector2i toBoardCoords(BoardIndex idx, int size) {
        return new Vector2i(
                size - idx.row - 1,
                idx.column
        );
    }

    public static int indexFromCoord(Vector2ic coords, int size) {
        return size * size - (size - coords.y() - 1) * size - (size - coords.x() - 1) - 1;
    }

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

    public void addListener(Board3DListener listener) {
        listeners.add(listener);
    }

    public void removeListener(Board3DListener listener) {
        listeners.remove(listener);
    }

    private class BoardUpdate {
        public BoardIndex origin;
        public int playerId;
        public Collection<BoardIndex> updates;

        public int triggerTick;

        public int duration;

        public BoardUpdate(BoardIndex origin, int playerId, Collection<BoardIndex> updates, int triggerTick) {
            this.origin = origin;
            this.playerId = playerId;
            this.updates = updates;
            this.triggerTick = triggerTick;

            duration = 0;

            for(BoardIndex idx : updates)
                duration = Math.max(duration, delayForPiece(idx) + Piece3D.ANIMATION_FLIP_DURATION);
        }

        public int delayForPiece(BoardIndex idx) {
            final float POWER = 1.3f;
            final int START_DELAY = 20; // in ticks

            return (int)(Math.pow(Math.max(Math.abs(idx.row - origin.row) - 1, Math.abs(idx.column - origin.column) - 1), POWER) * START_DELAY);
        }

        private void dispatch() {
            setPiece(game, origin, playerId);

            for(BoardIndex index : updates) {
                Vector2ic coords = toBoardCoords(index, size);

                int i = indexFromCoord(coords, size);

                boolean flipped = pieces[0].getFlippedColor() == game.getPlayer(playerId).getColor();

                // flips are delayed dramatically (see the power delay function above)
                pieces[i].animateFlip(flipped, getLastTick() + delayForPiece(index));
            }
        }
    }

    public interface Board3DListener {
        void onScoreChange(Board3D board);
        void onAnimationsDone(Board3D board);
    }
}
