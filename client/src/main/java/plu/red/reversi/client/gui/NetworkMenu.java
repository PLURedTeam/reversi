package plu.red.reversi.client.gui;

import plu.red.reversi.core.BoardIndex;
import plu.red.reversi.core.PlayerColor;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.command.MoveCommand;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * This is a menu that can be used for testing purposes.  Developers can add
 * and remove options in the menu as needed for testing.  Each menu item
 * should perform a specific test.  In production, this menu should NOT BE
 * VISIBLE to the user.
 */
public class NetworkMenu extends JMenu implements ActionListener {

    private MainWindow gui;
    private JMenuItem createUser;
    private JMenuItem deleteUser;
    private JMenuItem seeRanking;

    /**
     * Initialize the developer menu
     *
     * @param gui the main MainWindow object
     */
    public NetworkMenu(MainWindow gui) {

        this.gui = gui;
        this.setText("Network");
        this.setMnemonic(KeyEvent.VK_D);
        this.getAccessibleContext().setAccessibleDescription("Network Items");

        createUser = new JMenuItem("Create an online account" );
        createUser.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.SHIFT_MASK));
        createUser.addActionListener(this);
        this.add(createUser);

        deleteUser = new JMenuItem("Delete my online account");
        deleteUser.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.SHIFT_MASK));
        deleteUser.addActionListener(this);
        this.add(deleteUser);

        seeRanking = new JMenuItem("See my ranking");
        seeRanking.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.SHIFT_MASK));
        seeRanking.addActionListener(this);
        this.add(seeRanking);
    }//constructor

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == createUser) {
            createUser();
        } else if( e.getSource() == deleteUser) {
            deleteUser();
        } else if( e.getSource() == seeRanking) {
            seeRanking();
        }//else
    }

    private void createUser() {
    }

    private void deleteUser() {
    }

    private void seeRanking() {
    }


}
