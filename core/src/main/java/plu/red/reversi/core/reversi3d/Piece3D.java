package plu.red.reversi.core.reversi3d;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Collection;
import java.util.HashSet;

import plu.red.reversi.core.easing.EaseType;
import plu.red.reversi.core.easing.PolynomialEasing;
import plu.red.reversi.core.graphics.Graphics3D;
import plu.red.reversi.core.graphics.Pipeline;
import plu.red.reversi.core.util.Color;

/**
 * Created by daniel on 3/20/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public class Piece3D extends ColorModel3D {

    public static final int LONGITUDE_DETAIL = 60;
    public static final int LATITUDE_DETAIL = 20;

    public static final float HORIZONTAL_RADIUS = 0.04f;
    public static final float VERTICAL_RADIUS = 0.012f;

    public static final float ANIMATION_FLIP_JUMP = 0.2f;
    public static final float ANIMATION_ENTER_HEIGHT = 0.2f;
    public static final int ANIMATION_FLIP_ROTATIONS = 5;
    public static final int ANIMATION_ENTER_ROTATIONS = 2;
    public static final int ANIMATION_FLIP_DURATION = 60; // 1.0 seconds in ticks
    public static final int ANIMATION_ENTER_DURATION = 30; // 0.5 seconds in ticks

    public static final PolynomialEasing FLIP_EASER = new PolynomialEasing(3, EaseType.EASE_OUT);
    public static final PolynomialEasing ENTER_SCALE_EASER = new PolynomialEasing(4, EaseType.EASE_OUT);
    public static final PolynomialEasing ENTER_ROT_EASER = new PolynomialEasing(3, EaseType.EASE_OUT);
    public static final PolynomialEasing ENTER_MOVE_EASER = new PolynomialEasing(3, EaseType.EASE_IN);

    private Collection<Piece3DListener> listeners;

    private Color baseColor;
    private Color flippedColor;

    private boolean flipped;

    private int animFlipStart;
    private int animEnterStart;


    public Piece3D(Graphics3D g3d, Pipeline pipeline, Color baseColor, Color flippedColor) {
        super(g3d, pipeline);

        listeners = new HashSet<>();

        this.baseColor = baseColor;
        this.flippedColor = flippedColor;

        flipped = false;
        animFlipStart = -1;
    }

    public Piece3D(Piece3D other) {
        super(other.getGraphics3D(), other.getPipeline());

        listeners = new HashSet<>();

        this.baseColor = other.getBaseColor();
        this.flippedColor = other.getFlippedColor();

        flipped = false;
        animFlipStart = -1;
    }

    @Override
    Model3D newInstance() {
        return new Piece3D(this);
    }

    @Override
    int getSectionCount() {
        return 1;
    }

    @Override
    int getFaceCount(int sectionIndex) {
        return LONGITUDE_DETAIL * LATITUDE_DETAIL;
    }

    /**
     * Gets the coordinate for this piece at the specified spherical latitude and longitude
     * @param lat the latitude as a portion of 0.0-1.0
     * @param lng the longitude as a portion of 0.0-1.0
     * @return
     */
    private Vector3f getCurvePoint(float lat, float lng) {

        lat *= Math.PI;
        lng *= Math.PI * 2;

        double r = Math.sin(lat);

        return new Vector3f(
                (float)(r * Math.cos(lng)) * HORIZONTAL_RADIUS,
                (float)(r * Math.sin(lng)) * HORIZONTAL_RADIUS,
                (float)(Math.cos(lat)) * VERTICAL_RADIUS
        );
    }

    @Override
    Vector3f[] getFace(int sectionIndex, int faceIndex) {

        int row = faceIndex / LONGITUDE_DETAIL;
        int column = faceIndex % LONGITUDE_DETAIL;

        final float rowSize = 1.0f / LATITUDE_DETAIL;
        final float columnSize = 1.0f / LONGITUDE_DETAIL;

        float lat0 = rowSize * row;
        float lat1 = rowSize * (1 + row);

        float lng0 = column * columnSize;
        float lng1 = (column + 1) * columnSize;

        /*if(faceIndex < getFaceCount(0) / 2) {
            lng0 = -lng0;
            lng1 = -lng1;
            //lat0 = -lat0;
            //lat1 = -lat1;
        }*/

        /*if(sectionIndex == 1) {
            lng0 = -lng0;
            lng1 = -lng1;
        }*/

        if(row == 0)
            return new Vector3f[]{
                    getCurvePoint(lat0, lng0),
                    getCurvePoint(lat1, lng0),
                    getCurvePoint(lat1, lng1)
            };
        if(row == LATITUDE_DETAIL - 1)
            return new Vector3f[]{
                    getCurvePoint(lat1, lng0),
                    getCurvePoint(lat0, lng1),
                    getCurvePoint(lat0, lng0)
            };
        else
            return new Vector3f[]{
                    getCurvePoint(lat0, lng0),
                    getCurvePoint(lat1, lng0),
                    getCurvePoint(lat1, lng1),
                    getCurvePoint(lat0, lng1)
            };
    }

    @Override
    Vector4f[] getFaceColor(int sectionIndex, int faceIndex) {
        Vector4f color;

        if(faceIndex < getFaceCount(0) / 2) {
            color = baseColor.toVec4();
        }
        else {
            color = flippedColor.toVec4();
        }

        if(faceIndex < LONGITUDE_DETAIL) {
            return new Vector4f[]{
                    color,
                    color,
                    color
            };
        }
        if(getFaceCount(0) - faceIndex - 1 < LONGITUDE_DETAIL) {
            return new Vector4f[]{
                    color,
                    color,
                    color
            };
        }
        else {
            return new Vector4f[]{
                    color,
                    color,
                    color,
                    color
            };
        }
    }

    /**
     * Manually set whether or not this piece should be flipped. Automatically updates the rotation accordingly.
     * @param b is it flipped or not
     */
    public void setFlipped(boolean b) {

        flipped = b;

        if(b) {
            setRot(new Quaternionf().rotate((float)Math.PI, 0, 0));
        }
        else
            setRot(new Quaternionf().rotate(0, 0, 0));

        for(Piece3DListener listener : listeners)
            listener.onFlipped(this);
    }

    /**
     * Change the flip value to be equal to b in an animated way at the specified tick
     * @param b the new flip value. Will not animate anything if the piece is already flipped to that value.
     * @param atTick when to start animating the flip.
     */
    public void animateFlip(boolean b, int atTick) {
        if(flipped != b)
            animFlipStart = atTick;
    }

    /**
     * Animates the piece from being not visible/gone to visible
     * @param b the flip state of the piece after the animation has been completed
     * @param atTick the tick at which the piece should start animated
     */
    public void animateEnter(boolean b, int atTick) {
        setScale(new Vector3f(0));

        flipped = b;

        animEnterStart = atTick;
    }

    /**
     * Finishes any animation currently in progress immediately. The end result of the animation will
     * be the actual value applied.
     */
    public void clearAnimations() {
        // flip animations require an extra step to make sure the piece is still flipped properly.
        if(animFlipStart != -1) {
            animFlipStart = -1;

            setFlipped(!flipped);

            // make sure the piece is not transformed wierdly
            setPos(new Vector3f(
                    getPos().x(),
                    getPos().y(),
                    VERTICAL_RADIUS
            ));

            setScale(new Vector3f(1));
        }

        animEnterStart = -1;
    }

    @Override
    public boolean update(int tick) {
        super.update(tick);

        if(animFlipStart != -1 && animFlipStart < tick) {
            if(animFlipStart + ANIMATION_FLIP_DURATION < tick) {
                setFlipped(!flipped);

                setPos(new Vector3f(
                        getPos().x(),
                        getPos().y(),
                        VERTICAL_RADIUS
                ));

                animFlipStart = -1;
            }
            else {
                // we are at some intermediate state
                float tickoff = tick - animFlipStart;
                float portion = tickoff / ANIMATION_FLIP_DURATION;

                // the piece is flown up quadratically from the board
                float newz = tickoff * (ANIMATION_FLIP_DURATION - tickoff) / (float)Math.pow(ANIMATION_FLIP_DURATION / 2, 2) * ANIMATION_FLIP_JUMP;

                setPos(new Vector3f(
                        getPos().x(),
                        getPos().y(),
                        newz
                ));

                // the piece is going to flip a certain number of times before returning to the board
                Quaternionf rot = new Quaternionf();
                rot.rotateAxis((flipped ? (float)Math.PI : 0) + FLIP_EASER.ease(portion, 0.0f, (float)Math.PI * ANIMATION_FLIP_ROTATIONS, 1.0f), 0, 1, 0);

                setRot(rot);
            }

            return true;
        }

        if(animEnterStart != -1) {
            if(animEnterStart + ANIMATION_ENTER_DURATION < tick) {
                setFlipped(flipped);

                setPos(new Vector3f(
                        getPos().x(),
                        getPos().y(),
                        VERTICAL_RADIUS
                ));
            }
            else {
                float tickoff = tick - animEnterStart;
                float portion = tickoff / ANIMATION_ENTER_DURATION;

                // the piece will get bigger over the course of the animation
                setScale(new Vector3f(ENTER_SCALE_EASER.ease(portion, 0.0f, 1.0f, 1.0f)));

                // the piece will rotate cool over the course of the animation
                Quaternionf rot = new Quaternionf();
                rot.rotateAxis(ENTER_ROT_EASER.ease(
                        portion,
                        (float)Math.PI * ANIMATION_ENTER_ROTATIONS,
                        -(float)Math.PI * ANIMATION_ENTER_ROTATIONS,
                        1.0f),
                        0, 0, 1);
                rot.rotateAxis(ENTER_ROT_EASER.ease(
                        portion,
                        (float)Math.PI / 2 + (flipped ? (float)Math.PI : 0),
                        -(float)Math.PI / 2,
                        1.0f),
                        1, 0, 0);

                setRot(rot);

                // the piece will begin to drop after the first half of the animation.
                if(portion > 0.5f) {
                    setPos(new Vector3f(
                            getPos().x(),
                            getPos().y(),
                            ENTER_MOVE_EASER.ease(portion - 0.5f, ANIMATION_ENTER_HEIGHT, -ANIMATION_ENTER_HEIGHT + VERTICAL_RADIUS, 0.5f)
                    ));
                }
                else
                    setPos(new Vector3f(getPos().x(), getPos().y(), ANIMATION_ENTER_HEIGHT));
            }

            return true;
        }

        return false;
    }

    /**
     * gets the color of the top when flipped is false
     * @return the color
     */
    public Color getBaseColor() {
        return baseColor;
    }

    /**
     * Gets the color of the top when flipped is true
     * @return the color
     */
    public Color getFlippedColor() {
        return flippedColor;
    }

    /**
     * Returns whether or not the current tile is currently flipped. If an animation is in progress,
     * then it will return the end result of the animation (as opposed to the result before)
     * @return whether or not the piece is flipped
     */
    public boolean isFlipped() {
        // this can be a point of contention.
        // at this time, return the value the piece is flipping -> to for sure
        // otherwise sync issues could happen and this just makes score counting a bit simpler
        // (to the alternative of assuming it was the previous)

        if(animFlipStart != -1)
            return !flipped;

        return flipped;
    }

    /**
     * Add a new event listener
     * @param listener the listener to add
     */
    public void addListener(Piece3DListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a event listener
     * @param listener the listener to remove
     */
    public void removeListener(Piece3DListener listener) {
        listeners.remove(listener);
    }

    public interface Piece3DListener {
        /**
         * Whenever the flipped state of the piece completely changes, this method is called.
         * It executes when setFlipped() is called, or when an animation completes.
         * @param piece the piece that generated this event
         */
        void onFlipped(Piece3D piece);

        /**
         * Whenever the piece has finished animating, this method is called.
         * @param piece the piece that generated this event
         */
        void onAnimationsDone(Piece3D piece);
    }
}
