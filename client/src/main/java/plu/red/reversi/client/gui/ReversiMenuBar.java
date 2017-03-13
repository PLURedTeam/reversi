package plu.red.reversi.client.gui;

import plu.red.reversi.client.player.HumanPlayer;
import plu.red.reversi.core.command.SurrenderCommand;
import plu.red.reversi.core.player.Player;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * The main menu bar.
 */
public class ReversiMenuBar extends JMenuBar implements ActionListener {

    /** The MainWindow */
    private MainWindow gui;

    /** Quit item */
    private JMenuItem quitMenuItem;
    private JMenuItem surrenderMenuItem;

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

        JMenuItem menuItem = new JMenuItem("New Game");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_N, ActionEvent.META_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Start a new game against the computer");
        menu.add(menuItem);

        menuItem = new JMenuItem("New Online Game");
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

        surrenderMenuItem = new JMenuItem("Surrender");
        surrenderMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        surrenderMenuItem.getAccessibleContext().setAccessibleDescription(
                "Surrender the current game.");
        surrenderMenuItem.addActionListener(this);
        menu.add(surrenderMenuItem);

        menu.addSeparator();

        quitMenuItem = new JMenuItem("Quit");
        quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_Q, ActionEvent.META_MASK));
        quitMenuItem.getAccessibleContext().setAccessibleDescription(
                "Exit Reversi.");
        quitMenuItem.addActionListener(this);
        menu.add(quitMenuItem);

        return menu;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == quitMenuItem) {
            System.exit(0);
        } else if(e.getSource() == surrenderMenuItem) {
            Player player = gui.getGamePanel().getGame().getCurrentPlayer();
            if(player instanceof HumanPlayer)
                gui.getGamePanel().getGame().acceptCommand(new SurrenderCommand(player.getRole()));
        }
    }
}
