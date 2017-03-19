package plu.red.reversi.client.gui.util;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;

/**
 * A container for static methods that are useful in various places.
 */
public class Utilities {

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

    public static Color getLessContrastColor(Color color) {
        return new Color((color.getRed()-192)*3/8+192, (color.getGreen()-192)*3/8+192, (color.getBlue()-192)*3/8+192);
    }

}
