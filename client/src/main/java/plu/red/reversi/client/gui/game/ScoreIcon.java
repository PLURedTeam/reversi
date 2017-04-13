package plu.red.reversi.client.gui.game;

import plu.red.reversi.client.gui.util.Utilities;
import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.util.Map;

public class ScoreIcon extends JPanel {

    private Color chipColor, fontColor;
    private static Font font = new Font("SansSerif", Font.PLAIN, 14);

    private int score;

    public ScoreIcon(plu.red.reversi.core.util.Color color, int score) {

        this.score = score;

        this.chipColor = new Color(color.composite);
        this.fontColor = ((color.red + color.green + color.blue) / 3) > 128 ? Color.BLACK : Color.WHITE;
        this.setPreferredSize(new Dimension(37,37));
        this.setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int w = this.getWidth();
        int h = this.getHeight();

        Graphics2D g2d = (Graphics2D)g;
        Toolkit tk = Toolkit.getDefaultToolkit();
        Map map = (Map)(tk.getDesktopProperty("awt.font.desktophints"));
        if (map != null) {
            g2d.addRenderingHints(map);
        }
        g2d.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        g2d.setColor(chipColor);
        if(Utilities.TILE_IMAGE == null)
            g2d.fillOval(0, 0, w, h);
        else
            g2d.drawImage(
                Utilities.getColoredTile(chipColor),
                0, 0, w, h, null);
        String label = "" + score;
        g2d.setColor(fontColor);
        g2d.setFont(font);
        Utilities.drawCenteredString(g2d, label, new Rectangle(0, 0, w, h));
    }

    public void setScore(int score) {
        this.score = score;

        repaint();
    }
}
