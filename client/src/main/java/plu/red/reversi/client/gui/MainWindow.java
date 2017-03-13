package plu.red.reversi.client.gui;

import plu.red.reversi.client.gui.game.GamePanel;
import plu.red.reversi.core.Game;

import javax.swing.*;
import java.awt.*;

/**
 * The main game window, contains all of the UI.
 */
public class MainWindow extends JFrame {

    //private Game game;
    //public Game getGame() { return game; }

    private GamePanel gamePanel;
    public GamePanel getGamePanel() { return gamePanel; }

    private StatusBar statusBar;
    public StatusBar getStatusBar() { return statusBar; }

    /**
     * Constructs a new main window for the game.
     */
    public MainWindow(Game game)
    {
        //this.game = game;
        setTitle("Reversi");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Add the menu bar
        this.setJMenuBar(new ReversiMenuBar(this));

        this.gamePanel = new GamePanel(game);
        this.add(gamePanel, BorderLayout.CENTER);

        this.statusBar = new StatusBar();
        game.addStatusListener(this.statusBar);
        this.add(statusBar, BorderLayout.SOUTH);

        this.pack();
        this.setVisible(true);
    }
}
