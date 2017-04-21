package plu.red.reversi.client.gui;

import plu.red.reversi.client.gui.game.BoardView;
import plu.red.reversi.client.gui.game.GamePanel;
import plu.red.reversi.core.Coordinator;
import plu.red.reversi.core.game.player.HumanPlayer;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.command.SurrenderCommand;
import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.reversi3d.HighlightMode;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * The main menu bar.
 */
public class ReversiMenuBar extends JMenuBar implements ActionListener {

    /** The MainWindow */
    private MainWindow gui;

    private JMenuItem newGameItem;
    private JMenuItem loadGameItem;
    private JMenuItem saveGameItem;
    private JMenuItem quitMenuItem;
    private JMenuItem surrenderMenuItem;

    private JMenuItem highlightMenuItem;
    private JMenuItem bestMoveMenuItem;

    private boolean highlighted = false;

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
        this.add(new DeveloperMenu(gui));

        //add the network menu
        this.add(new NetworkMenu(gui));

        // Add the options menu
        this.add(new OptionsMenu(gui));
    }

    private JMenu buildGameMenu() {
        JMenu menu = new JMenu("Game");
        menu.getAccessibleContext().setAccessibleDescription(
                "New game");

        newGameItem = new JMenuItem("New Game");
        newGameItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_N, InputEvent.CTRL_MASK));
        newGameItem.getAccessibleContext().setAccessibleDescription(
                "Start a new game against the computer");
        newGameItem.addActionListener(this);
        menu.add(newGameItem);

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

        JMenuItem menuItem = new JMenuItem("New Online Game");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_O, InputEvent.META_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Start a new online game and invite someone to play.");
        menu.add(menuItem);

        menuItem = new JMenuItem("Join Online Game");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_J, InputEvent.META_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Join an existing online game.");
        menu.add(menuItem);

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

        menu.addSeparator();

        surrenderMenuItem = new JMenuItem("Surrender");
        surrenderMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, InputEvent.ALT_MASK));
        surrenderMenuItem.getAccessibleContext().setAccessibleDescription(
                "Surrender the current game.");
        surrenderMenuItem.addActionListener(this);
        menu.add(surrenderMenuItem);

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

        if(e.getSource() == quitMenuItem) {
            System.exit(0);
        }

        if(e.getSource() == newGameItem) {
            gui.createNewGame();
        }

        if(e.getSource() == loadGameItem) {
            gui.loadGame();
        }

        if(e.getSource() == saveGameItem) {
            gui.saveGame();
        }
    }
}
