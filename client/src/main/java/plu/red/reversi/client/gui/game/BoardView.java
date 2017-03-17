package plu.red.reversi.client.gui.game;

import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingTargetAdapter;
import plu.red.reversi.core.*;
import plu.red.reversi.core.listener.IFlipListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * The JPanel containing the board and its edges.
 */
public class BoardView extends JPanel implements MouseListener, IFlipListener {

    /** Used to help with animation */
    private FlipAnimator fAnimator;

    /**
     * This is an internal class used to manage the animation of pieces flipping over.
     */
    private class FlipAnimator extends TimingTargetAdapter {

        private ArrayList<CellState> cells;
        private PlayerColor newColor;
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

        public FlipAnimator(int startRow, int startCol,
                            int endRow, int endCol,
                            PlayerColor newColor,
                            long milliseconds)
        {
            cells = new ArrayList<CellState>();
            this.newColor = newColor;

            // Gather a list of cells to animate

            if( startRow == endRow ) {
                if( startCol > endCol ) {
                    for( int col = startCol; col >= endCol; col--) {
                        cellStates[startRow][col] = new CellState(game.getBoard().at(new BoardIndex(startRow, col)), startRow, col);
                        cells.add(cellStates[startRow][col]);
                    }
                } else {
                    for( int col = startCol; col <= endCol; col++ ) {
                        cellStates[startRow][col] = new CellState(game.getBoard().at(new BoardIndex(startRow, col)), startRow, col);
                        cells.add(cellStates[startRow][col]);
                    }
                }
            } else if( startCol == endCol ) {
                if( startRow > endRow ) {
                    for( int row = startRow; row >= endRow; row--) {
                        cellStates[row][startCol] = new CellState(game.getBoard().at(new BoardIndex(row, startCol)), row, startCol);
                        cells.add(cellStates[row][startCol]);
                    }
                } else {
                    for( int row = startRow; row <= endRow; row++) {
                        cellStates[row][startCol] = new CellState(game.getBoard().at(new BoardIndex(row, startCol)), row, startCol);
                        cells.add(cellStates[row][startCol]);
                    }
                }
            } else if( Math.abs(startRow - endRow) == Math.abs(startCol - endCol)) {
                if( startCol > endCol ) {
                    if( startRow > endRow ) {
                        for (int row = startRow, col = startCol; col >= endCol; col--, row--) {
                            cellStates[row][col] = new CellState(game.getBoard().at(new BoardIndex(row, col)), row, col);
                            cells.add(cellStates[row][col]);
                        }
                    } else {
                        for (int row = startRow, col = startCol; col >= endCol; col--, row++) {
                            cellStates[row][col] = new CellState(game.getBoard().at(new BoardIndex(row, col)), row, col);
                            cells.add(cellStates[row][col]);
                        }
                    }
                } else {
                    if( startRow > endRow ) {
                        for (int row = startRow, col = startCol; col <= endCol; col++, row--) {
                            cellStates[row][col] = new CellState(game.getBoard().at(new BoardIndex(row, col)), row, col);
                            cells.add(cellStates[row][col]);
                        }
                    } else {
                        for (int row = startRow, col = startCol; col <= endCol; col++, row++) {
                            cellStates[row][col] = new CellState(game.getBoard().at(new BoardIndex(row, col)), row, col);
                            cells.add(cellStates[row][col]);
                        }
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
        public PlayerColor cellColor;
        public int row, col;

        public boolean highlighted; // whether or not this tile should feature a reduced opacity because it is "highlighted"

        public CellState(PlayerColor cellColor, int row, int col) {
            this.cellColor = cellColor;
            this.row = row;
            this.col = col;
            this.height = 1.0f;
            this.highlighted = false;
        }
    }
    

    protected final void drawCell(Graphics g, float cellSize, int column, int row) {
        Board board = game.getBoard();
        PlayerColor player = cellStates[row][column] == null ? board.at(new BoardIndex(row, column)) : cellStates[row][column].cellColor;
        //PlayerColor player = cellStates[row][column] == null ? PlayerColor.NONE : cellStates[row][column].cellColor;
        
        if(player != PlayerColor.NONE) {
            float pad = cellSize * 0.1f;
            float x = column * cellSize;
            float cy = cellSize * (row  + 0.5f);
            float h = (cellStates[row][column] == null ? 1.0f : cellStates[row][column].height) * (cellSize - 2.0f * pad);

            Color color = player.color;

            Color actualColor = new Color(
                    color.getRed(),
                    color.getGreen(),
                    color.getBlue(),
                    cellStates[row][column] != null && cellStates[row][column].highlighted ? 128 : 255
            );

            g.setColor(actualColor);

            g.fillOval(
                    Math.round(x + pad),
                    Math.round(cy - h / 2.0f),
                    Math.round(cellSize - 2 * pad),
                    Math.round(h));
        }
    }

    protected final Game game;
    protected CellState cellStates[][];

    protected boolean showPossibleMoves;

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

        for( int row = 0; row< board.size; row++ ) {
            for( int col = 0; col < board.size; col++ ) {
                drawCell(g, cellSize, col, row);
            }
        }
    }

    /**
     * Mouse clicked event.  Determines the cell where the mouse
     * was clicked and prints the row/column to the console.
     *
     * @param e
     */
    public void mouseClicked(MouseEvent e) {
        /*
        int x = e.getX();
        int y = e.getY();

        int w = this.getWidth();
        int h = this.getHeight();

        float cellSize = (float)w / game.getBoard().size;

        int cellRow = (int)Math.floor( y / cellSize );
        int cellCol = (int)Math.floor( x / cellSize );
        System.out.printf("Cell row = %d col = %d\n", cellRow, cellCol);

        game.getCurrentPlayer().boardClicked(new BoardIndex(cellRow, cellCol));
        repaint();
        */
    }

    // This now happens in mousePressed because mouseClicked does not properly handle click and drag actions
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        int w = this.getWidth();
        int h = this.getHeight();

        float cellSize = (float)w / game.getBoard().size;

        int cellRow = (int)Math.floor( y / cellSize );
        int cellCol = (int)Math.floor( x / cellSize );
        game.statusMessage(String.format("Cell row = %d col = %d\n", cellRow, cellCol));

        game.getCurrentPlayer().boardClicked(new BoardIndex(cellRow, cellCol));
        repaint();
    }

    public void mouseReleased(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

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
                                    PlayerColor newColor,
                                    long milliseconds) {
        fAnimator = new FlipAnimator(startRow, startCol, endRow, endCol, newColor, milliseconds);
        fAnimator.start();
    }

    /**
     * Indicates that a section of tiles should be flipped.
     *
     * @param startPosition BoardIndex representing the starting tile
     * @param endPosition BoardIndex representing the ending tile
     * @param newColor PlayerRole representing the color that is being flipped to
     */
    @Override
    public void doFlip(BoardIndex startPosition, BoardIndex endPosition, PlayerColor newColor) {

        clearHighlights();

        animateFlipSequence(
                startPosition.row, startPosition.column,
                endPosition.row, endPosition.column,
                newColor,
                300);
    }

    public void highlightCells(PlayerColor color, Set<BoardIndex> indexes) {

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
