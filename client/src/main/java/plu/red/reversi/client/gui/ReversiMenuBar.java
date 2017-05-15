package plu.red.reversi.client.gui;

import plu.red.reversi.client.gui.game.BoardView;
import plu.red.reversi.client.gui.game.GamePanel;
import plu.red.reversi.core.Browser;
import plu.red.reversi.core.Client;
import plu.red.reversi.core.Coordinator;
import plu.red.reversi.core.command.SurrenderCommand;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.player.HumanPlayer;
import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.listener.INetworkListener;
import plu.red.reversi.core.lobby.Lobby;
import plu.red.reversi.core.network.WebUtilities;
import plu.red.reversi.core.reversi3d.HighlightMode;

import javax.swing.*;
import java.awt.event.*;

/**
 * The main menu bar.
 */
public class ReversiMenuBar extends JMenuBar implements ActionListener, INetworkListener {

    /** The MainWindow */
    private MainWindow gui;

    private JMenuItem newGameItem;
    private JMenuItem loadGameItem;
    private JMenuItem saveGameItem;
    private JMenuItem quitMenuItem;
    private JMenuItem surrenderMenuItem;
    private JMenuItem leaveGameItem;
    private JMenuItem newOnlineGameItem;

    private JMenuItem highlightMenuItem;
    private JMenuItem bestMoveMenuItem;

    private boolean highlighted = false;

    public void updateEnables() {
        Coordinator core = gui.getController().getCore();
        newGameItem.setEnabled(core instanceof Browser || (core instanceof Lobby && !((Lobby)core).isNetworked()));
        loadGameItem.setEnabled(core instanceof Browser || (core instanceof Lobby && !((Lobby)core).isNetworked()));
        saveGameItem.setEnabled(core instanceof Game);
        surrenderMenuItem.setEnabled(core instanceof Game && !((Game)core).isGameOver());
        leaveGameItem.setEnabled(core instanceof Lobby || core instanceof Game);
        newOnlineGameItem.setEnabled(WebUtilities.INSTANCE.loggedIn() && core instanceof Browser);
        highlightMenuItem.setEnabled(core instanceof Game);
        bestMoveMenuItem.setEnabled(core instanceof Game);
    }

    /**
     * Constructs the menu bar
     *
     * @param gui the main MainWindow
     */
    public ReversiMenuBar(MainWindow gui) {
        this.gui = gui;

        // Build the "Game" menu
        this.add( buildGameMenu() );

        // Add the developer menu.  This should be removed when
        // the game is released
        //this.add(new DeveloperMenu(gui));

        //add the network menu
        this.add(new NetworkMenu(gui));

        // Add the options menu
        this.add(new OptionsMenu(gui));

        Coordinator.addListenerStatic(this);
    }

    private JMenu buildGameMenu() {
        JMenu menu = new JMenu("Game");
        menu.getAccessibleContext().setAccessibleDescription(
                "Game Options and Actions");

        newGameItem = new JMenuItem("New Local Game");
        newGameItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_N, InputEvent.CTRL_MASK));
        newGameItem.getAccessibleContext().setAccessibleDescription(
                "Start a new game against the computer or local players.");
        newGameItem.addActionListener(this);
        menu.add(newGameItem);

        newOnlineGameItem = new JMenuItem("New Online Game");
        newOnlineGameItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_N, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
        newOnlineGameItem.getAccessibleContext().setAccessibleDescription(
                "Start a new game against online players.");
        newOnlineGameItem.addActionListener(this);
        newOnlineGameItem.setEnabled(false);
        menu.add(newOnlineGameItem);

        loadGameItem = new JMenuItem("Load Game");
        loadGameItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_L, InputEvent.CTRL_MASK));
        loadGameItem.getAccessibleContext().setAccessibleDescription(
                "Load a previous game");
        loadGameItem.addActionListener(this);
        menu.add(loadGameItem);

        saveGameItem = new JMenuItem("Save Game");
        saveGameItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, InputEvent.CTRL_MASK));
        saveGameItem.getAccessibleContext().setAccessibleDescription(
                "Save the current game");
        saveGameItem.addActionListener(this);
        menu.add(saveGameItem);

        menu.addSeparator();

        highlightMenuItem = new JMenuItem("Toggle Show Possible Moves");
        highlightMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_H, InputEvent.CTRL_MASK));
        highlightMenuItem.getAccessibleContext().setAccessibleDescription(
                "Toggles the showing of all the places which the current player can play on the board.");
        highlightMenuItem.addActionListener(this);
        menu.add(highlightMenuItem);

        bestMoveMenuItem = new JMenuItem("Select Best Move");
        bestMoveMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_W, InputEvent.CTRL_MASK));
        bestMoveMenuItem.getAccessibleContext().setAccessibleDescription(
                "Selects and play the best move automatically.");
        bestMoveMenuItem.addActionListener(this);
        menu.add(bestMoveMenuItem);

        surrenderMenuItem = new JMenuItem("Surrender");
        surrenderMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, InputEvent.ALT_MASK));
        surrenderMenuItem.getAccessibleContext().setAccessibleDescription(
                "Surrender the current game.");
        surrenderMenuItem.addActionListener(this);
        menu.add(surrenderMenuItem);

        leaveGameItem = new JMenuItem("Leave Game");
        leaveGameItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_L, InputEvent.ALT_MASK));
        leaveGameItem.getAccessibleContext().setAccessibleDescription(
                "Leave the current game.");
        leaveGameItem.addActionListener(this);
        menu.add(leaveGameItem);

        menu.addSeparator();

        quitMenuItem = new JMenuItem("Quit");
        quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_Q, InputEvent.CTRL_MASK));
        quitMenuItem.getAccessibleContext().setAccessibleDescription(
                "Exit Reversi.");
        quitMenuItem.addActionListener(this);
        menu.add(quitMenuItem);

        return menu;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Coordinator core = gui.getController().getCore();
        if(core instanceof Game) {
            Game game = (Game)core;

            if(e.getSource() == surrenderMenuItem) {
                Player player = game.getCurrentPlayer();
                if(player instanceof HumanPlayer)
                    game.acceptCommand(new SurrenderCommand(player.getID()));
            }

            if(e.getSource() == highlightMenuItem) {

                CorePanel cp = gui.getCorePanel();
                if(cp instanceof GamePanel) {
                    BoardView bv = ((GamePanel)cp).getBoardView();

                    bv.setHighlightMode(bv.getHighlightMode() == HighlightMode.HIGHLIGHT_POSSIBLE_MOVES ?
                            HighlightMode.HIGHLIGHT_NONE :
                            HighlightMode.HIGHLIGHT_POSSIBLE_MOVES);
                }
            }

            if(e.getSource() == bestMoveMenuItem) {


                CorePanel cp = gui.getCorePanel();
                if(cp instanceof GamePanel) {
                    BoardView bv = ((GamePanel)cp).getBoardView();

                    bv.setHighlightMode(bv.getHighlightMode() == HighlightMode.HIGHLIGHT_BEST_MOVE ?
                            HighlightMode.HIGHLIGHT_NONE :
                            HighlightMode.HIGHLIGHT_BEST_MOVE);
                }
            }
        }

        if(e.getSource() == leaveGameItem) {
            if(core instanceof Game || core instanceof Lobby)
                gui.leaveGame();
        }

        if(e.getSource() == quitMenuItem) {
            gui.dispatchEvent(new WindowEvent(gui, WindowEvent.WINDOW_CLOSING));
        }

        if(e.getSource() == newGameItem) {
            gui.createNewGame();
        }

        if(e.getSource() == newOnlineGameItem) {
            gui.createNewOnlineGame();
        }

        if(e.getSource() == loadGameItem) {
            gui.loadGame();
        }

        if(e.getSource() == saveGameItem) {
            gui.saveGame();
        }
    }

    /**
     * Called when a use logs out from the server
     *
     * @param loggedIn if the user is loggedIn
     */
    @Override
    public void onLogout(boolean loggedIn) {
        SwingUtilities.invokeLater(() -> {
            newOnlineGameItem.setEnabled(loggedIn && gui.getController().getCore() instanceof Browser);
        });
    }
}
