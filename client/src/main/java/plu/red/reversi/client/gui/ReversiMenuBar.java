package plu.red.reversi.client.gui;

import plu.red.reversi.client.player.HumanPlayer;
import plu.red.reversi.core.BoardIndex;
import plu.red.reversi.core.Game;
import plu.red.reversi.core.ReversiMinimax;
import plu.red.reversi.core.command.SurrenderCommand;
import plu.red.reversi.core.player.Player;
import plu.red.reversi.core.util.Looper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * The main menu bar.
 */
public class ReversiMenuBar extends JMenuBar implements ActionListener {

    /** The MainWindow */
    private MainWindow gui;

    private JMenuItem newGameItem;
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
    }

    private JMenu buildGameMenu() {
        JMenu menu = new JMenu("Game");
        menu.getAccessibleContext().setAccessibleDescription(
                "New game");

        newGameItem = new JMenuItem("New Game");
        newGameItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        newGameItem.getAccessibleContext().setAccessibleDescription(
                "Start a new game against the computer");
        newGameItem.addActionListener(this);
        menu.add(newGameItem);

        JMenuItem menuItem = new JMenuItem("New Online Game");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_O, ActionEvent.META_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Start a new online game and invite someone to play.");
        menu.add(menuItem);

        menuItem = new JMenuItem("Join Online Game");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_J, ActionEvent.META_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Join an existing online game.");
        menu.add(menuItem);

        menu.addSeparator();

        highlightMenuItem = new JMenuItem("Toggle Show Possible Moves");
        highlightMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_H, ActionEvent.CTRL_MASK));
        highlightMenuItem.getAccessibleContext().setAccessibleDescription(
                "Toggles the showing of all the places which the current player can play on the board.");
        highlightMenuItem.addActionListener(this);
        menu.add(highlightMenuItem);

        bestMoveMenuItem = new JMenuItem("Select Best Move");
        bestMoveMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_W, ActionEvent.CTRL_MASK));
        bestMoveMenuItem.getAccessibleContext().setAccessibleDescription(
                "Selects and play the best move automatically.");
        bestMoveMenuItem.addActionListener(this);
        menu.add(bestMoveMenuItem);

        menu.addSeparator();

        surrenderMenuItem = new JMenuItem("Surrender");
        surrenderMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, ActionEvent.ALT_MASK));
        surrenderMenuItem.getAccessibleContext().setAccessibleDescription(
                "Surrender the current game.");
        surrenderMenuItem.addActionListener(this);
        menu.add(surrenderMenuItem);

        menu.addSeparator();

        quitMenuItem = new JMenuItem("Quit");
        quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        quitMenuItem.getAccessibleContext().setAccessibleDescription(
                "Exit Reversi.");
        quitMenuItem.addActionListener(this);
        menu.add(quitMenuItem);

        return menu;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(gui.getGamePanel() != null) {
            Game game = gui.getGamePanel().getGame();

            if(e.getSource() == surrenderMenuItem) {
                Player player = game.getCurrentPlayer();
                if(player instanceof HumanPlayer)
                    gui.getGamePanel().getGame().acceptCommand(new SurrenderCommand(player.getRole()));
            }

            if(e.getSource() == highlightMenuItem) {

                if(highlighted == false) {
                    gui.getGamePanel().getBoardView().highlightCells(game.getCurrentPlayer().getRole(), game.getBoard().getPossibleMoves(game.getCurrentPlayer().getRole()));
                    highlighted = true;
                } else {
                    gui.getGamePanel().getBoardView().clearHighlights();
                    highlighted = false;
                }//else
            }

            if(e.getSource() == bestMoveMenuItem) {

                // TODO: Show a loading indicator of some kind
                // TODO: Cancel minimax result if play is performed, or disable ability to play on board
                ReversiMinimax minimax = new ReversiMinimax(game,
                        game.getCurrentPlayer().getRole(),
                        5);

                new Thread(minimax).start();
            }
        }

        if(e.getSource() == quitMenuItem) {
            System.exit(0);
        }

        if(e.getSource() == newGameItem) {
            gui.createNewGame();
        }
    }
}
