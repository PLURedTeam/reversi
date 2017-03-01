package plu.red.reversi.client.gui;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Contains methods for building panels to be used for the top and left
 * edges of the game board.  These edges also include labels for each row and column
 * of the board.
 *
 * BoardEdges.createTopPanel()
 * BoardEdges.createLeftPanel()
 *
 */
public class BoardEdges {

    /** Height of the top edge panel */
    public static int EDGE_HEIGHT = 25;

    /** Width of the left edge panel */
    public static int EDGE_WIDTH = 25;

    /** Space in pixels between the labels and the edge of the board */
    public static int MARGIN = 5;

    private static Font FONT = new Font("SansSerif", Font.BOLD, 16);
    private static Color FONT_COLOR = new Color(50,50,50);

    /**
     * The background color for the edges.
     */
    public static Color BACKGROUND_COLOR = new Color(197, 153, 24);

    /**
     * Returns a JPanel that can be used for the top of the game board with labels
     * for each column.
     * @param size The height in pixels
     * @return A JPanel
     */
    public static JPanel createTopPanel(int size) {
        return new BoardEdgePanelTop(size);
    }

    /**
     * Returns a JPanel that can be used for the left side of the game board with labels
     * for each row.
     * @param size The width in pixels
     * @return A JPanel
     */
    public static JPanel createLeftPanel(int size) {
        return new BoardEdgePanelLeft(size);
    }

    private static class BoardEdgePanelLeft extends JPanel {

        private int size;

        public BoardEdgePanelLeft( int size )
        {
            this.size = size;
            this.setPreferredSize(new Dimension(EDGE_WIDTH, 0));
            this.setBackground(BACKGROUND_COLOR);
        }

        @Override
        public void paintComponent( Graphics g )
        {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D)g;
            Toolkit tk = Toolkit.getDefaultToolkit();
            Map map = (Map)(tk.getDesktopProperty("awt.font.desktophints"));
            if (map != null) {
                g2d.addRenderingHints(map);
            }
            g2d.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            float cellSize = (float)this.getHeight() / size;
            g2d.setColor(FONT_COLOR);
            g2d.setFont(FONT);

            for(int i = 0; i < size; i++ )
            {
                String label = "" + (i+1);
                Utilities.drawCenteredString(g2d, label, new Rectangle(0,
                        Math.round(cellSize * i), EDGE_WIDTH,
                        Math.round(cellSize) ));
            }
        }
    }

    private static class BoardEdgePanelTop extends JPanel {

        private int size;

        public BoardEdgePanelTop( int size )
        {
            this.size = size;
            this.setPreferredSize(new Dimension(0, EDGE_HEIGHT));
            this.setBackground(BACKGROUND_COLOR);
        }

        public void paintComponent( Graphics g )
        {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D)g;
            Toolkit tk = Toolkit.getDefaultToolkit();
            Map map = (Map)(tk.getDesktopProperty("awt.font.desktophints"));
            if (map != null) {
                g2d.addRenderingHints(map);
            }

            float cellSize = (float)( this.getWidth() - EDGE_WIDTH ) / size;
            g2d.setColor(FONT_COLOR);
            g2d.setFont(FONT);
            for(int i = 0; i < size; i++ )
            {
                String label = "" + (char)(i + 'A');
                Utilities.drawCenteredString(g2d, label,
                        new Rectangle(Math.round(cellSize * i + EDGE_WIDTH), 0, Math.round(cellSize), EDGE_HEIGHT));
            }
        }
    }

}

