package plu.red.reversi.client.gui.util;

import plu.red.reversi.client.gui.game.BoardView;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.HashMap;

/**
 * A container for static methods that are useful in various places.
 */
public class Utilities {

    public static BufferedImage TILE_IMAGE = null;

    static {
        try {
            TILE_IMAGE = ImageIO.read(BoardView.class.getResourceAsStream("/tile.png"));
        } catch(Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Draw a String centered in the middle of a Rectangle.
     *
     * @param g2d The Graphics instance.
     * @param text The String to draw.
     * @param rect The Rectangle to center the text in.
     */
    public static void drawCenteredString(Graphics2D g2d, String text, Rectangle rect) {
        Font font = g2d.getFont();
        FontRenderContext frc = g2d.getFontRenderContext();
        GlyphVector gv = font.createGlyphVector(frc, text);
        Rectangle2D box = gv.getVisualBounds();

        int x = (int)Math.round((rect.getWidth() - box.getWidth()) / 2.0 - box.getX() + rect.x);
        int y = (int)Math.round((rect.getHeight() - box.getHeight()) / 2.0 - box.getY() + rect.y);
        g2d.drawString(text, x, y);
    }

    /**
     * Performs a deep copy of a BufferedImage.
     *
     * @param bufferedImage Original BufferedImage object
     * @return New BufferedImage object that is a copy of the original
     */
    public static BufferedImage copyBufferedImage(BufferedImage bufferedImage) {
        ColorModel cm = bufferedImage.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bufferedImage.copyData(bufferedImage.getRaster().createCompatibleWritableRaster());
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    /**
     * Calculates a new Color with less Saturation and Contrast than the one given.
     *
     * @param color Old Saturated Color
     * @return New De-Saturated Color
     */
    public static Color getLessContrastColor(Color color) {
        return new Color((color.getRed()-192)*3/8+192, (color.getGreen()-192)*3/8+192, (color.getBlue()-192)*3/8+192);
    }

    private static final HashMap<Color, BufferedImage> tileCache = new HashMap<>();

    /**
     * Returns a colored tile based on the original grayscale Tile PNG Image. Once a specific color has been asked for,
     * the BufferedImage is cached for faster future lookups.
     *
     * @param color Color to tint with
     * @return Either a new BufferedImage, or a previously cached one.
     */
    public static BufferedImage getColoredTile(Color color) {
        if(tileCache.containsKey(color)) return tileCache.get(color);
        else {
            BufferedImage img = Utilities.copyBufferedImage(TILE_IMAGE);
            //BufferedImage img = new BufferedImage(TILE_IMAGE.getWidth(), TILE_IMAGE.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < img.getWidth(); x++) {
                for (int y = 0; y < img.getHeight(); y++) {
                    Color oldColor = new Color(img.getRGB(x, y));
                    Color newColor;
                    if(oldColor.getRed() + oldColor.getGreen() + oldColor.getBlue() < 32) {
                        newColor = new Color(0, 0, 0, 0);
                    } else {
                        newColor = overlayBlend(oldColor, color, true);
                        /*
                        int red = (int)((oldColor.getRed() * ( (color.getRed()) / 255.0f))*0.8f + oldColor.getRed()*0.2f);
                        int green = (int)((oldColor.getGreen() * ( (color.getGreen()) / 255.0f))*0.8f + oldColor.getGreen()*0.2f);
                        int blue = (int)((oldColor.getBlue() * ( (color.getBlue()) / 255.0f))*0.8f + oldColor.getBlue()*0.2f);
                        newColor = new Color(
                                red > 255 ? 255 : red,
                                green > 255 ? 255 : green,
                                blue > 255 ? 255 : blue,
                                oldColor.getAlpha() < 32 ? 0 : (int)(oldColor.getAlpha() * (color.getAlpha() / 255.0f))
                        );
                        */
                    }
                    img.setRGB(x, y, newColor.getRGB());
                }
            }
            tileCache.put(color, img);
            return img;
        }
    }

    /**
     * Returns an Overlay blend of two colors, optionally artificially darkening the top color in order to avoid color
     * bleedout where the resulting color is all white. In addition, unlike normal Overlay blend, the resulting Color's
     * alpha value is equal to the top color.
     *
     * @param original The base Color
     * @param top The Color to apply on top
     * @param artificialDarken Artificially darken the given top color
     * @return Blended Color value
     */
    public static Color overlayBlend(Color original, Color top, boolean artificialDarken) {

        float[] oA = original.getRGBComponents(null);
        float[] tA = top.getRGBComponents(null);

        if(artificialDarken) {
            float avg = (tA[0] + tA[1] + tA[2]) / 3.0f;
            if(avg > 0.9f) {
                for(int i = 0; i < 3; i++) tA[i] *= 0.7f;
            }
        }

        float[] nA = new float[4];

        for(int i = 0; i < 3; i++)
            nA[i] = (oA[i] < 0.5f) ?
                    (2.0f * oA[i] * tA[i]) :
                    (1.0f - 2.0f*(1.0f - oA[i])*(1.0f - tA[i]));

        nA[3] = tA[3];

        return new Color(
                Math.max(0.0f, Math.min(1.0f, nA[0])),
                Math.max(0.0f, Math.min(1.0f, nA[1])),
                Math.max(0.0f, Math.min(1.0f, nA[2])),
                Math.max(0.0f, Math.min(1.0f, nA[3])));

    }

}
