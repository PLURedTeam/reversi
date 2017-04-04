package plu.red.reversi.client.gui;

import plu.red.reversi.client.gui.game.GamePanel;
import plu.red.reversi.client.gui.lobby.LobbyPanel;
import plu.red.reversi.client.gui.util.StatusBar;
import plu.red.reversi.core.*;
import plu.red.reversi.core.db.DBUtilities;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.lobby.Lobby;
import plu.red.reversi.core.util.Looper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * Glory to the Red Team.
 *
 * The main game window, acts as a Controller for the GUI. Will have one center panel at all times, whether that panel
 * is the game, a lobby screen, or a server browser.
 */
public class MainWindow extends JFrame implements WindowListener, IMainGUI {

    private CorePanel corePanel;
    public CorePanel getCorePanel() { return corePanel; }

    private StatusBar statusBar;
    public StatusBar getStatusBar() { return statusBar; }

    private SettingsWindow settingsWindow = null;
    public SettingsWindow getSettingsWindow() { return settingsWindow; }

    private Client client = null;
    public Client getClient() { return client; }

    private BorderLayout layout = new BorderLayout();

    /**
     * Constructs a new main window for the game.
     */
    public MainWindow()
    {
        //this.game = game;
        setTitle("Reversi");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.setMinimumSize(new Dimension(800, 800));
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

        this.addWindowListener(this);

        this.pack();
        this.setVisible(true);
    }

    protected final void populate(CorePanel corePanel) {

        // Cleanup the old panel
        if(this.corePanel != null) this.corePanel.cleanup();

        // Replace reference with the new panel
        this.corePanel = corePanel;

        // Remove the old center panel
        Component old = layout.getLayoutComponent(this, BorderLayout.CENTER);
        if(old != null) this.remove(old);

        // Add the new center panel
        this.add(corePanel, BorderLayout.CENTER);

        // Refresh and repaint
        this.revalidate();
        this.repaint();
    }

    /**
     * GUI Display Updater. Called from a Client object when the Client object has changed significantly and the GUI
     * needs to be updated to accommodate. Example usages include when the <code>core</code> Controller of a Client
     * object is swapped out. Causes the entire GUI to be recreated.
     */
    @Override
    public void updateGUIMajor() {
        Controller core = client.getCore();

        if(core instanceof Game) {
            populate(new GamePanel(this, (Game)core));
        } else if(core instanceof Lobby) {
            populate(new LobbyPanel(this, (Lobby)core));
        }

        core.addListener(statusBar);
    }

    /**
     * GUI Display Updater. Called from a Client object when small changes have been made in the Client and the GUI
     * needs to be updated to reflect these changes. Example usages include when a Lobby changes the amount of Player
     * Slots it has. Causes small portions of the GUI to be recalculated and redrawn.
     */
    @Override
    public void updateGUIMinor() {
        if(corePanel != null) corePanel.updateGUI();
    }

    /**
     * Client Setter. Sets what Client master controller this GUI is displaying for. Usually only used by the Client
     * class's constructor.
     *
     * @param client Client object to set
     */
    @Override
    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * Save Dialog Display Method. Shows a Save Dialog to the user, which queries what name to save a Game as. Can be
     * cancelled.
     *
     * @return String name chosen, or null if the user cancelled
     */
    @Override
    public String showSaveDialog() {
        return JOptionPane.showInputDialog(this, "Enter a name for the game","Save Game",1);
    }

    /**
     * Load Dialog Display Method. Shows a Load Dialog o the user, which queries what Game to load from existing saved
     * Games. Can be cancelled.
     *
     * @return String name chosen, or null if the user cancelled
     */
    @Override
    public String showLoadDialog() {
        String[][] games = DBUtilities.INSTANCE.getGames(); //Get the games from the DB
        String[] list = new String[games.length]; //Array for the JOptionPane

        //Convert to one dimensional array
        for(int i = 0; i < games.length; i++)
            list[i] = games[i][0];

        if(list.length < 1) {
            JOptionPane.showMessageDialog(this, "No Games Are Saved.", "Load Game", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        return (String)JOptionPane.showInputDialog(this, "Select a Game", "Load Game", JOptionPane.QUESTION_MESSAGE, null, list, list[0]);
    }

    /**
     * Creates a new game panel and starts the game
     */
    public void startGame() {
        client.startGame();
    }

    /**
     * Creates a new game panel
     */
    public void createNewGame() {
        client.createIntoLobby();
    }

    /**
     * Displays a JOptionPane to get the game information to load the game
     * Then loads and creates the game
     */
    public void loadGame() {
        client.loadIntoLobby();
    }

    /**
     * Displays a JOptionPane to get a name for the game to save to the database
     * Then saves the game in the database
     */
    public void saveGame() {
        try {
            client.saveGame();
        } catch(IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Save Game", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Opens the Main's Settings window, where they can change options and settings. If the window is already open
     * when this method is called, it simply switches focus to the already open window.
     */
    public void openClientSettings() {
        if(settingsWindow == null) {
            settingsWindow = new SettingsWindow();
            settingsWindow.addWindowListener(this);
        } else {
            settingsWindow.requestFocus();
        }
    }

    /**
     * Checks to see if the game has been saved, if not prompt the user to see if they
     * want to save the game, if yes ask for name of game and save game, else exit program
     * @param e the close window event
     */
    @Override
    public void windowClosing(WindowEvent e) {
        // Ask about saving
        if(e.getSource() == this && client.getCore() instanceof Game) {
            Game game = (Game)client.getCore();
            if(!game.getGameSaved()) { //If game has been saved, do not show prompt
                if (JOptionPane.showConfirmDialog(this,
                        "Do you want to save this game?", "Save",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                        == JOptionPane.YES_OPTION) {
                    client.saveGame();
                }
            }
        }

        // Remove SettingsWindow reference so we can open a new one
        if(e.getSource() == settingsWindow) {
            settingsWindow = null;
        }
    }

    // Unused WindowListener implemented methods - required even if they don't do anything
    @Override public void windowOpened(WindowEvent e) {}
    @Override public void windowClosed(WindowEvent e) {}
    @Override public void windowIconified(WindowEvent e) {}
    @Override public void windowDeiconified(WindowEvent e) {}
    @Override public void windowActivated(WindowEvent e) {}
    @Override public void windowDeactivated(WindowEvent e) {}
}
