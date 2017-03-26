package plu.red.reversi.client.gui;

import plu.red.reversi.client.network.WebUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
    private JMenuItem onlineUsers;

    /**
     * Initialize the network menu
     *
     * @param gui the main MainWindow object
     */
    public NetworkMenu(MainWindow gui) {

        //Create the menu
        this.gui = gui;
        this.setText("Network");
        this.setMnemonic(KeyEvent.VK_N);
        this.getAccessibleContext().setAccessibleDescription("Network Items");

        //Create ranking menu item
        login = new JMenuItem("Login to server");
        login.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.SHIFT_MASK));
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

        //Create the seeRanking menu item
        onlineUsers = new JMenuItem("See online Users");
        onlineUsers.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.SHIFT_MASK));
        onlineUsers.addActionListener(this);
        this.add(onlineUsers);
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
            try {login();}
            catch (NoSuchAlgorithmException e1) {e1.printStackTrace();}
        } else if(e.getSource() == onlineUsers) {
            getOnlineUsers();
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
    private void login() throws NoSuchAlgorithmException {
        JTextField username = new JTextField();
        JTextField password = new JPasswordField();
        Object[] message = { "Username:", username, "Password:", password };

        int option = JOptionPane.showConfirmDialog(gui, message, "Login", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {

            //Convert the users password into SHA256 format
            MessageDigest digest = MessageDigest.getInstance("SHA-256"); //Create the MessageDigest object
            byte[] result = digest.digest(password.getText().getBytes()); //Get the byte array for the digest
            StringBuffer sb = new StringBuffer(); //String buffer to build the password string
            for(int i = 0; i < result.length; i++)
                sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1)); //Create the string

            //Call the server to check for valid login credentials
            boolean loggedIn = WebUtilities.INSTANCE.login(username.getText(),sb.toString());

            if (loggedIn) {
                System.out.println("Login successful");
            } else {
                System.out.println("login failed");
            }
        } else {
            System.out.println("Login canceled");
        }



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


    /**
     * Calls the server to get the current users online
     */
    private void getOnlineUsers() {




    }//seeRanking

}//NetworkMenu
