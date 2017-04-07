package plu.red.reversi.android.reversi3d;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import plu.red.reversi.android.graphics.Graphics3D;
import plu.red.reversi.android.graphics.Pipeline;
import plu.red.reversi.core.util.Color;

/**
 * Created by daniel on 3/20/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public class Piece3D extends ColorModel3D {

    public static final int LONGITUDE_DETAIL = 20;
    public static final int LATITUDE_DETAIL = 10;

    public static final float HORIZONTAL_RADIUS = 0.04f;
    public static final float VERTICAL_RADIUS = 0.012f;

    private Color baseColor;
    private Color flippedColor;


    public Piece3D(Graphics3D g3d, Pipeline pipeline, Color baseColor, Color flippedColor) {
        super(g3d, pipeline);

        this.baseColor = baseColor;
        this.flippedColor = flippedColor;
    }

    public Piece3D(Piece3D other) {
        super(other.getGraphics3D(), other.getPipeline());

        this.baseColor = other.getBaseColor();
        this.flippedColor = other.getFlippedColor();
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

        lat *= Math.PI * 2;
        lng *= Math.PI * 2;

        double r = Math.cos(lat);

        return new Vector3f(
                (float)(r * Math.cos(lng)) * HORIZONTAL_RADIUS,
                (float)(r * Math.sin(lng)) * HORIZONTAL_RADIUS,
                (float)(r * Math.tan(lat)) * VERTICAL_RADIUS
        );
    }

    @Override
    Vector3f[] getFace(int sectionIndex, int faceIndex) {

        int row = faceIndex / LONGITUDE_DETAIL;
        int column = faceIndex % LONGITUDE_DETAIL;

        final float rowSize = 1.0f / LATITUDE_DETAIL;
        final float columnSize = 1.0f / LONGITUDE_DETAIL;

        float lat0 = sectionIndex == 0 ? rowSize * row : 1.0f - rowSize * row;
        float lat1 = sectionIndex == 0 ? rowSize * (1 + row) : 1.0f - rowSize * (1 + row);

        float lng0 = column * columnSize;
        float lng1 = (column + 1) * columnSize;

        System.out.println("Calculated lats: " + lat0 + ", " + lat1);
        System.out.println("Calculated lngs: " + lng0 + ", " + lng1);

        if(row == 0)
            return new Vector3f[]{
                    getCurvePoint(lat0, lng0),
                    getCurvePoint(lat1, lng1),
                    getCurvePoint(lat1, lng0)
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
            color = new Vector4f(0.9f, 0.1f, 0.1f, 1.0f);
        }
        else {
            color = new Vector4f(0.1f, 0.1f, 0.9f, 1.0f);
        }

        if(faceIndex < LONGITUDE_DETAIL) {
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

    public void setFlipped(boolean b) {
        if(b) {
            setRot(new Quaternionf().rotate((float)Math.PI, 0, 0));
        }
        else
            setRot(new Quaternionf().rotate(0, 0, 0));
    }

    public Color getBaseColor() {
        return baseColor;
    }

    public Color getFlippedColor() {
        return flippedColor;
    }
}
