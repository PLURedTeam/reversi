package plu.red.reversi.client.gui;

import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.client.gui.game.GamePanel;
import plu.red.reversi.client.gui.game.create.CreatePanel;
import plu.red.reversi.core.Game;
import plu.red.reversi.core.History;
import plu.red.reversi.core.db.DBUtilities;
import plu.red.reversi.core.util.Looper;
import plu.red.reversi.core.util.SettingsMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The main game window, contains all of the UI.
 */
public class MainWindow extends JFrame {

    private GamePanel gamePanel = null;
    public GamePanel getGamePanel() { return gamePanel; }

    private StatusBar statusBar;
    public StatusBar getStatusBar() { return statusBar; }

    private BorderLayout layout = new BorderLayout();

    /**
     * Constructs a new main window for the game.
     */
    public MainWindow()
    {
        //this.game = game;
        setTitle("Reversi");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.setMinimumSize(new Dimension(800, 600));
        this.setLayout(layout);

        // Add the menu bar
        this.setJMenuBar(new ReversiMenuBar(this));

        this.statusBar = new StatusBar();
        this.add(statusBar, BorderLayout.SOUTH);

        Timer timer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Looper.getLooper(Thread.currentThread()).run();
            }
        });

        populate(new CreatePanel(this));

        this.pack();
        this.setVisible(true);
    }

    protected final void populate(Component centerComponent) {
        Component old = layout.getLayoutComponent(this, BorderLayout.CENTER);
        if(old != null) this.remove(old);

        this.add(centerComponent, BorderLayout.CENTER);

        this.revalidate();
        this.repaint();
    }

    public void startGame(Game game) {
        this.gamePanel = new GamePanel(game);
        game.addStatusListener(this.statusBar);
        populate(this.gamePanel);
    }

    public void createNewGame() {
        this.gamePanel = null;
        populate(new CreatePanel(this));
    }

    public void loadGame() {
        String[][] games = DBUtilities.INSTANCE.getGames();
        String[] list = new String[games.length];
        int gameID = 0;

        //Convert to one dimensional array
        for(int i = 0; i < games.length; i++)
            list[i] = games[i][0];

        String input = null;
        if(games.length > 0)
            input = (String)JOptionPane.showInputDialog(this,"Select a Game","Load Game",JOptionPane.QUESTION_MESSAGE,null,list,list[0]);
        else {
            JOptionPane.showMessageDialog(this, "You do not have any saved games");
            return;
        }

        //Loop through array and set gameID
        for(int i = 0; i < games.length; i++)
            if(input.equals(games[i][0]))
                gameID = Integer.parseInt(games[i][1]);

        History h = DBUtilities.INSTANCE.loadGame(gameID);
        JSONObject obj = DBUtilities.INSTANCE.loadGameSettings(gameID);
        SettingsMap map = new SettingsMap(obj);

        this.gamePanel = null;
        populate(new CreatePanel(this, map, h, gameID));
    }
}
