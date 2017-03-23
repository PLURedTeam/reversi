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
 * This is a menu that displays the network items for the game. This menu
 * contains all items pertaining to the users interaction with the server
 * not including game play
 */
public class NetworkMenu extends JMenu implements ActionListener {

    //Fields
    private MainWindow gui;
    private JMenuItem login;
    private JMenuItem createUser;
    private JMenuItem deleteUser;
    private JMenuItem seeRanking;

    /**
     * Initialize the network menu
     *
     * @param gui the main MainWindow object
     */
    public NetworkMenu(MainWindow gui) {

        //Create the menu
        this.gui = gui;
        this.setText("Network");
        this.setMnemonic(KeyEvent.VK_D);
        this.getAccessibleContext().setAccessibleDescription("Network Items");

        //Create ranking menu item
        login = new JMenuItem("See my ranking");
        login.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.SHIFT_MASK));
        login.addActionListener(this);
        this.add(login);

        //Create the Create an account menu item
        createUser = new JMenuItem("Create an online account" );
        createUser.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.SHIFT_MASK));
        createUser.addActionListener(this);
        this.add(createUser);

        //Create the delete account menu item
        deleteUser = new JMenuItem("Delete my online account");
        deleteUser.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.SHIFT_MASK));
        deleteUser.addActionListener(this);
        this.add(deleteUser);

        //Create the seeRanking menu item
        seeRanking = new JMenuItem("See my ranking");
        seeRanking.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.SHIFT_MASK));
        seeRanking.addActionListener(this);
        this.add(seeRanking);
    }//constructor

    /**
     * Responds to the users action of clicking a menu item
     * in the network menu and calls the appropriate method
     * @param e the menu item that was clicked
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == createUser) {
            createUser();
        } else if(e.getSource() == deleteUser) {
            deleteUser();
        } else if(e.getSource() == seeRanking) {
            seeRanking();
        } else if(e.getSource() == login) {
            login();
        }//else
    }//actionPerformed

    /**
     * Calls the webUtilities method to create the user on the server
     */
    private void createUser() {

    }//createUser

    /**
     * Calls the server to login with user credentials
     */
    private void login() {

    }//login

    /**
     * Calls the server to delete the user from the server
     */
    private void deleteUser() {
    }//deleteUser

    /**
     * Calls the server to get the current users global ranking
     */
    private void seeRanking() {

    }//seeRanking
}//NetworkMenu
