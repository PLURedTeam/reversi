package plu.red.reversi.client.gui.game;

import plu.red.reversi.client.gui.util.PreserveAspectRatioLayout;
import plu.red.reversi.core.Game;

import javax.swing.*;
import java.awt.*;


public class GamePanel extends JPanel {

    protected final Game game;
    public Game getGame() { return game; }

    // GUI Components
    private BoardView boardView;
    private PlayerInfoPanel playerInfoPanel;
    private GameHistoryPanel historyPanel;

    public GamePanel(Game game) {
        this.game = game;

        this.setLayout(new BorderLayout());

        // The panel that holds the BoardView and the PlayerInfoPanel
        JPanel boardPanel = new JPanel(new BorderLayout(0, 0));

        // Other panels
        playerInfoPanel = new PlayerInfoPanel(game);
        game.addCommandListener(playerInfoPanel);
        boardView = new BoardView(game);
        game.getBoard().addFlipListener(boardView);

        // This panel will preserve the aspect ratio of the component within it
        JPanel preserveAspectPanel = new JPanel(new PreserveAspectRatioLayout() );

        // The board and edges
        JPanel boardAndEdges = new JPanel( new BorderLayout() );
        boardAndEdges.add(BoardEdges.createTopPanel(game.getBoard().size), BorderLayout.NORTH);
        boardAndEdges.add(BoardEdges.createLeftPanel(game.getBoard().size), BorderLayout.WEST);
        boardAndEdges.add(boardView, BorderLayout.CENTER);
        boardAndEdges.setBorder(BorderFactory.createMatteBorder(0,0,BoardEdges.EDGE_HEIGHT,
                BoardEdges.EDGE_WIDTH,BoardEdges.BACKGROUND_COLOR));

        preserveAspectPanel.add(boardAndEdges);

        boardPanel.add(preserveAspectPanel, BorderLayout.CENTER);
        boardPanel.add(playerInfoPanel, BorderLayout.NORTH);

        historyPanel = new GameHistoryPanel(game);

        // The board panel goes in the center
        this.add(boardPanel, BorderLayout.CENTER);

        // History panel goes in the EAST
        this.add(historyPanel, BorderLayout.EAST);

        this.revalidate();
    }

    /**
     * Returns the PlayerInfoPanel
     * @return the PlayerInfoPanel
     */
    public PlayerInfoPanel getPlayerInfoPanel() { return playerInfoPanel; }

    /**
     * @return the BoardView
     */
    public BoardView getBoardView() { return boardView; }
}
