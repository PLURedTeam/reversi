package plu.red.reversi.client.gui.game;

import plu.red.reversi.client.gui.util.Utilities;
import plu.red.reversi.core.PlayerColor;
import plu.red.reversi.core.player.Player;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ScoreIcon extends JPanel {

    private Color chipColor, fontColor;
    //private int scoreValue;
    private static Font font = new Font("SansSerif", Font.PLAIN, 14);
    private final Player player;

    public ScoreIcon(Player player) {
        this.player = player;
        PlayerColor playerType = player.getRole();
        this.chipColor = playerType.color;
        this.fontColor = ((playerType.color.getRed() + playerType.color.getGreen() + playerType.color.getBlue()) / 3) > 128 ? Color.BLACK : Color.WHITE;
        this.setPreferredSize(new Dimension(37,37));
        this.setOpaque(false);
        //scoreValue = 0;
    }

    // TODO: Cue repaint from somewhere when score changes
    /*
    public void setScoreValue( int value ) {
        this.scoreValue = value;
        this.repaint();
    }
    */

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
        g2d.fillOval(0, 0, w, h);
        String label = "" + player.getScore();
        g2d.setColor(fontColor);
        g2d.setFont(font);
        Utilities.drawCenteredString(g2d, label, new Rectangle(0, 0, w, h));
    }

}
