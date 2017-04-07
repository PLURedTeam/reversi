package plu.red.reversi.client.gui.game;

import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingTargetAdapter;
import plu.red.reversi.client.gui.util.Utilities;
import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.game.Board;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.listener.IBoardUpdateListener;
import plu.red.reversi.core.listener.ICommandListener;
import plu.red.reversi.core.listener.IGameOverListener;
import plu.red.reversi.core.game.player.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * The JPanel containing the board and its edges.
 */
public class BoardView extends JPanel implements MouseListener, IBoardUpdateListener, ICommandListener, IGameOverListener {

    /** Used to help with animation */
    private FlipAnimator fAnimator;

    /**
     * This is an internal class used to manage the animation of pieces flipping over.
     */
    private class FlipAnimator extends TimingTargetAdapter {

        private ArrayList<CellState> cells;
        private Color newColor;
        private Animator animator;

        @Override
        public void end(Animator source) {
            for( CellState c : cells ) {
                cellStates[c.row][c.col] = null;
            }
            repaint();
        }

        @Override
        public void timingEvent(Animator source, double fraction) {
            double cellChunk = 1.0 / cells.size();
            double cellChunk2 = cellChunk / 2.0;
            for(int i = 0; i < cells.size(); i++ ) {
                double cellStart = (i * cellChunk);
                double cellEnd = ((i+1) * cellChunk);
                CellState c = cells.get(i);
                if( fraction > cellStart && fraction <= cellEnd ) {
                    double h = 0.0;
                    if( fraction < cellStart + cellChunk2 ) {
                        h = 1.0 - (fraction - cellStart) / cellChunk2;
                        // Don't change cellColor, so leave this commented
                        //c.cellColor = startColor;
                    } else {
                        h = 1.0 - (cellEnd - fraction) / cellChunk2;
                        c.cellColor = newColor;
                    }
                    c.height = (float)h;
                } else if( fraction > cellEnd ) {
                    c.cellColor = newColor;
                    c.height = 1.0f;
                }
            }

            repaint();
        }

        @Override
        public void begin(Animator source) {
            for( CellState c : cells ) {
                c.height = 1.0f;
            }
            repaint();
        }
        
        // Private helper method
        private void addCellState(Game game, int r, int c) {
            CellState cell = new CellState(
                    new Color( game.getPlayer(game.getBoard().at(new BoardIndex(r, c))).getColor().composite ),
                    r, c);
            cellStates[r][c] = cell;
            cells.add(cell);
        }

        public FlipAnimator(int startRow, int startCol,
                            int endRow, int endCol,
                            Color newColor,
                            long milliseconds)
        {
            cells = new ArrayList<CellState>();
            this.newColor = newColor;

            // Gather a list of cells to animate

            if( startRow == endRow ) {
                if( startCol > endCol ) {
                    for( int col = startCol; col >= endCol; col--) addCellState(game, startRow, col);
                } else {
                    for( int col = startCol; col <= endCol; col++ ) addCellState(game, startRow, col);
                }
            } else if( startCol == endCol ) {
                if( startRow > endRow ) {
                    for( int row = startRow; row >= endRow; row--) addCellState(game, row, startCol);
                } else {
                    for( int row = startRow; row <= endRow; row++) addCellState(game, row, startCol);
                }
            } else if( Math.abs(startRow - endRow) == Math.abs(startCol - endCol)) {
                if( startCol > endCol ) {
                    if( startRow > endRow ) {
                        for (int row = startRow, col = startCol; col >= endCol; col--, row--) addCellState(game, row, col);
                    } else {
                        for (int row = startRow, col = startCol; col >= endCol; col--, row++) addCellState(game, row, col);
                    }
                } else {
                    if( startRow > endRow ) {
                        for (int row = startRow, col = startCol; col <= endCol; col++, row--) addCellState(game, row, col);
                    } else {
                        for (int row = startRow, col = startCol; col <= endCol; col++, row++) addCellState(game, row, col);
                    }
                }
            } else {
                throw new IllegalArgumentException("Start/end must define a horizontal, vertical or diagonal line.");
            }

            if( cells.size() == 0 ) {
                throw new IllegalArgumentException("No cells in given range.");
            }

            animator = new Animator.Builder()
                    .setDuration(milliseconds * cells.size(), TimeUnit.MILLISECONDS)
                    .addTarget(this).build();
        }

        public void start() {
            animator.start();
        }

    }

    
    private class CellState {
        public float height;  // Fractional height used for animation
        public Color cellColor;
        public int row, col;

        public boolean highlighted; // whether or not this tile should feature a reduced opacity because it is "highlighted"

        public CellState(Color cellColor, int row, int col) {
            this.cellColor = cellColor;
            this.row = row;
            this.col = col;
            this.height = 1.0f;
            this.highlighted = false;
        }
    }


    protected final void drawCell(Graphics g, float cellSize, int column, int row) {
        Board board = game.getBoard();
        Color color;
        if(cellStates[row][column] == null) {
            int playerID = board.at(new BoardIndex(row, column));
            if(playerID >= 0) color = new Color(game.getPlayer(playerID).getColor().composite);
            else return;
        } else color = cellStates[row][column].cellColor;

        float pad = cellSize * 0.1f;
        float x = column * cellSize;
        float cy = cellSize * (row  + 0.5f);
        float h = (cellStates[row][column] == null ? 1.0f : cellStates[row][column].height) * (cellSize - 2.0f * pad);

        Color actualColor = new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                cellStates[row][column] != null && cellStates[row][column].highlighted ? 128 : 255
        );

        g.setColor(actualColor);

        // TODO: Optimize Image Drawing/Loading/Caching
        if(Utilities.TILE_IMAGE == null) { // Couldn't load the Image for some reason
            g.fillOval(
                    Math.round(x + pad),
                    Math.round(cy - h / 2.0f),
                    Math.round(cellSize - 2 * pad),
                    Math.round(h));
        } else {
            ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g.drawImage(
                    Utilities.getColoredTile(actualColor),
                    Math.round(x + pad),
                    Math.round(cy - h / 2.0f),
                    Math.round(cellSize - 2 * pad),
                    Math.round(h),
                    null);
        }
    }

    protected final Game game;
    protected CellState cellStates[][];

    protected boolean showPossibleMoves = false;
    protected Player winningPlayer = null;
    protected int winningScore = 0;

    protected static final Font END_GAME_FONT = new Font("Sans Serif", Font.BOLD, 28);

    /**
     * Constructs a new BoardView.
     *
     * @param game Game object to pull data from
     */
    public BoardView( Game game )
    {
        this.game = game;
        this.setPreferredSize(new Dimension(500,500) );
        this.setBackground(new Color(12, 169, 18));

        resetCellStates();

        fAnimator = null;
        this.addMouseListener(this);
    }

    public void setShowPossibleMoves(boolean value) {
        showPossibleMoves = value;
        clearHighlights();
        if(showPossibleMoves) {
            int playerID = game.getCurrentPlayer().getID();
            highlightCells(new Color(game.getPlayer(playerID).getColor().composite), game.getBoard().getPossibleMoves(playerID));
        }
    }
    public boolean getShowPossibleMoves() { return showPossibleMoves; }

    private void resetCellStates() {
        int size = game.getBoard().size;
        this.cellStates = new CellState[size][size];
        for(int i = 0; i < size; i++)
            for(int j = 0; j < size; j++)
                this.cellStates[i][j] = null;
    }

    /**
     * Draws the board.
     *
     * @param g Graphics context
     */
    @Override
    public void paintComponent( Graphics g )
    {
        super.paintComponent(g);

        int w = this.getWidth();
        int h = this.getHeight();

        Board board = game.getBoard();

        float cellSize = (float)w / board.size;

        for(int i = 1; i < board.size; i++ ) {
            int pos = Math.round( i * cellSize );
            g.drawLine(pos, 0, pos, h );
            g.drawLine(0, pos, w, pos);
        }

        Graphics2D g2d = (Graphics2D)g;
        Toolkit tk = Toolkit.getDefaultToolkit();
        Map map = (Map)(tk.getDesktopProperty("awt.font.desktophints"));
        if (map != null) {
            g2d.addRenderingHints(map);
        }
        g2d.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        for( int row = 0; row< board.size; row++ ) {
            for( int col = 0; col < board.size; col++ ) {
                drawCell(g, cellSize, col, row);
            }
        }

        if(winningPlayer != null) {
            g.setFont(END_GAME_FONT);

            String displayStr = winningPlayer.getName() + " Wins!";
            Rectangle2D strBounds = g.getFontMetrics().getStringBounds(displayStr, g);
            int dispW = (int)strBounds.getWidth()+32;
            int dispH = (int)strBounds.getHeight()+32;

            g.setColor(BoardEdges.BACKGROUND_COLOR);
            g.fillRect((w-dispW)/2, (h-dispH)/2, dispW, dispH);

            g.setColor(Utilities.getLessContrastColor(new Color(winningPlayer.getColor().composite)));
            g.fillRect((w-dispW)/2+8, (h-dispH)/2+8, dispW-16, dispH-16);

            g.setColor(Color.BLACK);
            Utilities.drawCenteredString((Graphics2D)g, displayStr, new Rectangle(0, 0, w, h));
        }
    }

    @Override
    public void onGameOver(Player player, int score) {
        winningPlayer = player;
        winningScore = score;
        this.repaint();
    }

    public void mouseClicked(MouseEvent e) {}

    // This now happens in mousePressed because mouseClicked does not properly handle click and drag actions
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        int w = this.getWidth();
        int h = this.getHeight();

        float cellSize = (float)w / game.getBoard().size;

        int cellRow = (int)Math.floor( y / cellSize );
        int cellCol = (int)Math.floor( x / cellSize );
        //game.statusMessage(String.format("Cell row = %d col = %d\n", cellRow, cellCol));

        game.getCurrentPlayer().boardClicked(new BoardIndex(cellRow, cellCol));
        repaint();
    }

    // Unused MouseListener events
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    /**
     * Start an animation of a number of pieces being flipped over.
     *
     * @param startRow start row number
     * @param startCol start column number
     * @param endRow end row number
     * @param endCol end column number
     * @param newColor the color of the cells after flipping
     * @param milliseconds the time of the entire animation in milliseconds
     */
    protected final void animateFlipSequence(int startRow, int startCol,
                                    int endRow, int endCol,
                                    Color newColor,
                                    long milliseconds) {
        fAnimator = new FlipAnimator(startRow, startCol, endRow, endCol, newColor, milliseconds);
        fAnimator.start();
    }

    @Override
    public void onBoardUpdate(BoardIndex origin, int playerId, Collection<BoardIndex> updated) {

        clearHighlights();

        // TODO: The only reason this looks ugly here is because the new system will handle this problem of animation much
        // more elegantly

        // for each of the 8 directions a sequence update can happen in.
        BoardIndex[] seqMins = new BoardIndex[8];
        BoardIndex[] seqMaxs = new BoardIndex[8];

        Arrays.fill(seqMins, new BoardIndex(-1, -1));
        Arrays.fill(seqMaxs, new BoardIndex(-1, -1));

        for(BoardIndex idx : updated) {
            int dir = 4;

            if(origin.row != idx.row) {
                dir += ((idx.row - origin.row) / Math.abs(idx.row - origin.row)) * 3;
            }

            if(origin.column != idx.column)
                dir += (idx.column - origin.column) / Math.abs(idx.column - origin.column);

            if(dir > 3)
                dir--;

            if(seqMins[dir].row == -1 ||
                    Math.hypot(idx.row - origin.row, idx.column - origin.column) < Math.hypot(seqMins[dir].row - origin.row, seqMins[dir].column - origin.column))
                seqMins[dir] = idx;

            if(seqMaxs[dir].row == -1 ||
                    Math.hypot(idx.row - origin.row, idx.column - origin.column) > Math.hypot(seqMaxs[dir].row - origin.row, seqMaxs[dir].column - origin.column))
                seqMaxs[dir] = idx;
        }


        for(int i = 0;i < 8;i++) {
            if(seqMins[i].row != -1) {
                animateFlipSequence(
                        seqMins[i].row, seqMins[i].column,
                        seqMaxs[i].row, seqMaxs[i].column,
                        new Color(game.getPlayer(playerId).getColor().composite),
                        300
                );
            }
        }
    }

    @Override
    public void onBoardRefresh() {
        // TODO: Implement, because I do not see in this class how this would be done?
    }

    @Override
    public void commandApplied(Command cmd) {
        if(cmd instanceof BoardCommand) {
            clearHighlights();
            if(showPossibleMoves) {
                int playerID = game.getCurrentPlayer().getID();
                highlightCells(new Color(game.getPlayer(playerID).getColor().composite), game.getBoard().getPossibleMoves(playerID));
            }
        }
    }

    public void highlightCells(Color color, Set<BoardIndex> indexes) {

        for(BoardIndex index : indexes) {
            if(cellStates[index.row][index.column] == null) {
                cellStates[index.row][index.column] = new CellState(color, index.row, index.column);
                cellStates[index.row][index.column].highlighted = true;
            }
        }

        repaint();
    }

    public void clearHighlights() {
        for(int i = 0;i < cellStates.length;i++) {
            for(int j = 0;j < cellStates[i].length;j++) {
                if(cellStates[i][j] != null && cellStates[i][j].highlighted) {
                    cellStates[i][j] = null;
                }
            }
        }

        repaint();
    }

}
