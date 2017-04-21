package plu.red.reversi.client.gui;

import plu.red.reversi.core.Coordinator;
import plu.red.reversi.core.game.Game;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * This is a menu that can be used for testing purposes.  Developers can add
 * and remove options in the menu as needed for testing.  Each menu item
 * should perform a specific test.  In production, this menu should NOT BE
 * VISIBLE to the user.
 */
public class DeveloperMenu extends JMenu implements ActionListener {

    private MainWindow gui;
    private JMenuItem testFlipAnimItem;
    private JMenuItem changePlayer1NameItem;
    private JMenuItem swapActivePlayerItem;
    private JMenuItem endGameItem;

    private JMenuItem testServerItem;

    /**
     * Initialize the developer menu
     *
     * @param gui the main MainWindow object
     */
    public DeveloperMenu(MainWindow gui) {

        this.gui = gui;
        this.setText("Developer");
        this.setMnemonic(KeyEvent.VK_D);
        this.getAccessibleContext().setAccessibleDescription("Developer options");

        testFlipAnimItem = new JMenuItem("Test flip animation" );
        testFlipAnimItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.ALT_MASK));
        testFlipAnimItem.addActionListener(this);
        this.add(testFlipAnimItem);

        changePlayer1NameItem = new JMenuItem("Change Player 1 Name");
        changePlayer1NameItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_MASK));
        changePlayer1NameItem.addActionListener(this);
        this.add(changePlayer1NameItem);

        swapActivePlayerItem = new JMenuItem("Swap Active Player");
        swapActivePlayerItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.ALT_MASK));
        swapActivePlayerItem.addActionListener(this);
        this.add(swapActivePlayerItem);

        testServerItem = new JMenuItem("Test Server");
        testServerItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_MASK));
        testServerItem.addActionListener(this);
        this.add(testServerItem);

        endGameItem = new JMenuItem("Force End the Game");
        endGameItem.addActionListener(this);
        this.add(endGameItem);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == testFlipAnimItem) {
            testFlip();
        } else if( e.getSource() == changePlayer1NameItem) {
            changePlayer1Name();
        } else if( e.getSource() == swapActivePlayerItem ) {
            swapActivePlayer();
        } else if( e.getSource() == testServerItem ) {
            testServer();
        } else if( e.getSource() == endGameItem) {
            endGame();
        }
    }

    @Deprecated
    private void testFlip() {
        /*
        gui.getGamePanel().getBoardView().doFlip(new BoardIndex(5, 1), new BoardIndex(1, 5),
                PlayerColor.WHITE);
        gui.getGamePanel().getBoardView().doFlip(new BoardIndex(5, 1), new BoardIndex(5, 6),
                PlayerColor.WHITE);
                */
    }

    @Deprecated
    private void changePlayer1Name() {
        String newName = JOptionPane.showInputDialog(gui, "New name");
        //gui.getPlayerInfoPanel().setPlayerName(1, newName);
    }

    private void swapActivePlayer() {
        Coordinator core = gui.getController().getCore();
        if(core instanceof Game) {
            Game game = (Game)core;
            game.nextTurn();
        }
    }

    @Deprecated
    private void testServer() {
        // Moved to NetworkMenu
    }

    private void endGame() {
        Coordinator core = gui.getController().getCore();
        if(core instanceof Game) {
            Game game = (Game)core;
            game.endGame();
        }
    }
}
