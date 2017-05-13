package plu.red.reversi.client.gui.game;

import plu.red.reversi.client.gui.util.ChatPanel;
import plu.red.reversi.client.gui.CorePanel;
import plu.red.reversi.client.gui.MainWindow;
import plu.red.reversi.client.gui.util.PreserveAspectRatioLayout;
import plu.red.reversi.core.Client;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.SettingsLoader;

import javax.swing.*;
import java.awt.*;

/**
 * Glory to the Red Team.
 *
 * GamePanel that controls the GUI side of a Game. Acts as a sub-controller for the MainWindow, and controls all
 * in-game related GUI components.
 */
public class GamePanel extends CorePanel implements BoardView.BoardViewStateListener, GameHistoryPanel.HistoryPanelListener {

    protected final Game game;
    public Game getGame() { return game; }

    // GUI Components
    private BoardView boardView;
    private PlayerInfoPanel playerInfoPanel;
    private GameHistoryPanel historyPanel;
    private ChatPanel chatPanel;

    public GamePanel(MainWindow gui, Game game) {
        super(gui);
        this.game = game;

        this.setLayout(new BorderLayout());

        // The panel that holds the BoardView and the PlayerInfoPanel
        JPanel boardPanel = new JPanel(new BorderLayout(0, 0));

        // Other panels
        playerInfoPanel = new PlayerInfoPanel(game);
        game.addListener(playerInfoPanel);
        boardView = new BoardView(game);
        game.getGameLogic().addBoardUpdateListener(boardView);
        game.addListener(boardView);

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

        historyPanel = new GameHistoryPanel(game);
        chatPanel = new ChatPanel(Client.getInstance().getChat());
        game.addListener(chatPanel);

        // The board panel goes in the center
        this.add(boardPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(playerInfoPanel, BorderLayout.NORTH);
        rightPanel.add(historyPanel, BorderLayout.CENTER);

        this.add(rightPanel, BorderLayout.EAST);

        this.add(chatPanel, BorderLayout.SOUTH);

        boardView.setBoardViewListener(this);
        historyPanel.setListener(this);

        this.revalidate();
    }

    @Override
    public void updateGUI() {
        playerInfoPanel.updateGUI();
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

    public void cleanup() {
        // Unregister PlayerInfoPanel ISettingsListener to avoid reference leaks
        SettingsLoader.INSTANCE.removeSettingsListener(playerInfoPanel);
        game.cleanup();
    }

    @Override
    public void onBoardStateChanged(BoardView view) {
        playerInfoPanel.setCurrentBoard(view.getCurrentCache(), view.getCurrentBoard());

        historyPanel.setSelectedIndex(view.getCurrentMoveIndex());
    }

    @Override
    public void onHistoryPanelSelected() {
        boardView.setMoveIndex(historyPanel.getSelectedIndex());
    }
}
