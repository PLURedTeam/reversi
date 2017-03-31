package plu.red.reversi.android.reversi3d;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

import java.security.InvalidParameterException;

import plu.red.reversi.android.graphics.Graphics3D;
import plu.red.reversi.android.graphics.Pipeline;
import plu.red.reversi.core.Board;
import plu.red.reversi.core.BoardIndex;
import plu.red.reversi.core.PlayerColor;

/**
 * Created by daniel on 3/20/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public class Board3D extends ColorModel3D {

    public static final float PIECE_SIZE = 0.1f;
    public static final float PIECE_BORDER_SIZE = 0.005f;
    public static final float BORDER_SIZE = 0.08f;

    private Piece3D[] pieces;
    private Highlight3D[] highlights;

    private int size;

    public Board3D(Graphics3D g3d, Pipeline pipeline, int size) {

        super(g3d, pipeline);

        this.size = size;

        pieces = new Piece3D[(int)Math.pow(size, 2)];
        highlights = new Highlight3D[(int)Math.pow(size, 2)];

        Piece3D piece = new Piece3D(g3d, pipeline);
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

            Vector3f pos = new Vector3f(PIECE_SIZE * c + PIECE_SIZE / 2, PIECE_SIZE * r + PIECE_SIZE / 2, Piece3D.VERTICAL_RADIUS / 2).add(origin);

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
    public Model3D newInstance(Graphics3D g3d, Pipeline p) {
        return new Board3D(g3d, p, size);
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

        addChild(highlights[index.row * size + (size - index.column - 1)]);
    }

    public void setBoard(Board board) {

        if(board.size != size)
            throw new InvalidParameterException("Board is not the same size as Board3D");

        for(int r = 0;r < size;r++) {
            for(int c = 0;c < size;c++) {

                int i = r * size + c;

                BoardIndex idx = new BoardIndex(r, size - c - 1);

                switch (board.at(idx)) {
                    case BLACK:
                        addChild(pieces[i]);

                        pieces[i].setFlipped(false);

                        break;

                    case WHITE:
                        addChild(pieces[i]);

                        pieces[i].setFlipped(true);

                        break;

                    case NONE:
                        removeChild(pieces[i]);

                        break;
                    default:
                        System.out.println("Unrecognized player color: " + board.at(idx));
                }
            }
        }
    }

    public BoardIndex getIndexAtCoord(Vector2fc pos) {

        Vector2f offset = new Vector2f(getBoardOrigin().x(), getBoardOrigin().y()).add(PIECE_BORDER_SIZE, PIECE_BORDER_SIZE);

        pos.sub(offset, offset);

        // find row and column
        int c = (int)(offset.x / PIECE_SIZE);
        int r = (int)(offset.y / PIECE_SIZE);


        return new BoardIndex(r, size - c - 1);
    }
}
