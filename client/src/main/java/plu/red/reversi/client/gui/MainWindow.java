package plu.red.reversi.client.gui;

import plu.red.reversi.client.gui.browser.BrowserPanel;
import plu.red.reversi.client.gui.game.GamePanel;
import plu.red.reversi.client.gui.lobby.LobbyPanel;
import plu.red.reversi.client.gui.util.StatusBar;
import plu.red.reversi.core.*;
import plu.red.reversi.client.gui.browser.Browser;
import plu.red.reversi.core.db.DBUtilities;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.lobby.Lobby;
import plu.red.reversi.core.network.WebUtilities;
import plu.red.reversi.core.util.GamePair;
import plu.red.reversi.core.util.Looper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

/**
 * Glory to the Red Team.
 *
 * The main game window, acts as a Coordinator for the GUI. Will have one center panel at all times, whether that panel
 * is the game, a lobby screen, or a server browser.
 */
public class MainWindow extends JFrame implements WindowListener, IMainGUI {

    private CorePanel corePanel;
    public CorePanel getCorePanel() { return corePanel; }

    private StatusBar statusBar;
    public StatusBar getStatusBar() { return statusBar; }

    private SettingsWindow settingsWindow = null;
    public SettingsWindow getSettingsWindow() { return settingsWindow; }

    private Controller master = null;
    public Controller getController() { return master; }

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
     * needs to be updated to accommodate. Example usages include when the <code>core</code> Coordinator of a Client
     * object is swapped out. Causes the entire GUI to be recreated.
     */
    @Override
    public void updateGUIMajor() {
        Coordinator core = master.getCore();

        if(core instanceof Game) {
            populate(new GamePanel(this, (Game)core));
        } else if(core instanceof Lobby) {
            populate(new LobbyPanel(this, (Lobby)core));
        } else if(core instanceof Browser) {
            populate(new BrowserPanel(this, (Browser)core));
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
     * Controller Setter. Sets what master Controller this GUI is displaying for. Usually only used by the Controller
     * class's constructor.
     *
     * @param controller Controller object to set
     */
    @Override
    public void setController(Controller controller) {
        this.master = controller;
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
     * Information Dialog Display Method. Shows an Information Dialog to the user, displaying the given information.
     *
     * @param title String <code>title</code> of the Dialog
     * @param body  String <code>body</code> of the Dialog
     */
    @Override
    public void showInformationDialog(String title, String body) {
        JOptionPane.showMessageDialog(this, body, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Error Dialog Display Method. Shows an Error Dialog to the user, displaying the given information.
     *
     * @param title String <code>title</code> of the Dialog
     * @param body  String <code>body</code> of the Dialog
     */
    @Override
    public void showErrorDialog(String title, String body) {
        JOptionPane.showMessageDialog(this, body, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Query Dialog Display Method. Shows a Query Dialog to the user, displaying a question and expecting a response.
     *
     * @param title String <code>title</code> of the Dialog
     * @param body  String <code>body</code> of the Dialog
     * @return String response, or <code>null</code> for no response
     */
    @Override
    public String showQueryDialog(String title, String body) {
        return JOptionPane.showInputDialog(this, body, title, JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Query Dialog Display Method. Shows a Query Dialog with a list of options to the user, displaying a question and
     * expecting a response.
     *
     * @param title        String <code>title</code> of the Dialog
     * @param body         String <code>body</code> of the Dialog
     * @param values       Object array of values to choose from
     * @param defaultValue Default Object value to start selected
     * @return Object value that was selected, or <code>null</code> for no response
     */
    @Override
    public Object showQueryDialog(String title, String body, Object[] values, Object defaultValue) {
        return JOptionPane.showInputDialog(this, body, title, JOptionPane.QUESTION_MESSAGE, null, values, defaultValue);
    }

    /**
     * Creates a new game panel and starts the game
     */
    public void startGame() {
        master.startGame();
    }

    /**
     * Creates a new local game panel
     */
    public void createNewGame() {
        master.createIntoLobby(false);
    }

    public void createNewOnlineGame() {
        master.createIntoLobby(true);
    }

    /**
     * Displays a JOptionPane to get the game information to load the game
     * Then loads and creates the game
     */
    public void loadGame() {
        master.loadIntoLobby(false);
    }

    /**
     * Displays a JOptionPane to get a name for the game to save to the database
     * Then saves the game in the database
     */
    public void saveGame() {
        try {
            master.saveGame();
        } catch(IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Save Game", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Calls the server to create a new network game
     */
    @Deprecated
    public void createNetworkGame(String name) {
        Object[] numPlayers = {2,4};
        int p = (int)JOptionPane.showInputDialog(this, "Number of Players", "Create Network Game", JOptionPane.QUESTION_MESSAGE, null, numPlayers, numPlayers[0]);

        //TODO: Add logic to call server and set up game on client side

        //WebUtilities.INSTANCE.createGame(p);
    }//createNetworkGame


    public void leaveNetworkGame() {
       WebUtilities.INSTANCE.leaveNetworkGame();
       Client.getInstance().loadNetworkBrowser();
    }//


    /**
     * Calls the server to get a list of available online games to join
     */
    public void joinNetworkGame() {
        //Just for testing
        ArrayList<GamePair> games = WebUtilities.INSTANCE.getOnlineGames();

        if (games != null) {
            Object[][] rows = new Object[games.size()][4];

            for (int i = 0; i < games.size(); i++) {
                rows[i][0] = games.get(i).gameName;
                rows[i][1] = games.get(i).players.size() + " / " + games.get(i).numPlayers;
                rows[i][2] = games.get(i).status;
            }//for

            Object[] cols = {"Game Name", "Number of Players", "Status"};
            JTable table = new JTable(rows, cols);
            JOptionPane.showMessageDialog(null, new JScrollPane(table), "Online Users", 1);
        } else {
            JOptionPane.showMessageDialog(null,
                    "There are currently 0 games available",
                    "", 1);

        }//else

    }//joinNetworkGame

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

        //Logout from the server
        if(e.getSource() == this)
            WebUtilities.INSTANCE.logout();

        // Ask about saving
        if(e.getSource() == this && master.getCore() instanceof Game) {
            Game game = (Game) master.getCore();
            if(!game.getGameSaved() && !game.isNetworked()) { //If game has been saved, do not show prompt
                if (JOptionPane.showConfirmDialog(this,
                        "Do you want to save this game?", "Save",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                        == JOptionPane.YES_OPTION) {
                    master.saveGame();
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
