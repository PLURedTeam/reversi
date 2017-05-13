package plu.red.reversi.client.gui;

import plu.red.reversi.client.gui.tournament.TournamentPanel;
import plu.red.reversi.core.Client;
import plu.red.reversi.core.Controller;
import plu.red.reversi.core.Coordinator;
import plu.red.reversi.core.browser.Browser;
import plu.red.reversi.core.lobby.Lobby;
import plu.red.reversi.core.network.WebUtilities;
import plu.red.reversi.core.util.ChatMessage;
import plu.red.reversi.core.util.User;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

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
    private JMenuItem logout;
    private JMenuItem viewTournament;

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

        //Create the logout menu item
        logout = new JMenuItem("Logout");
        logout.addActionListener(this);
        this.add(logout);

        //Create the Create an account menu item
        createUser = new JMenuItem("Create an online account");
        createUser.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.SHIFT_MASK));
        createUser.addActionListener(this);
        this.add(createUser);

        //Create the delete account menu item
        deleteUser = new JMenuItem("Delete my online account");
        deleteUser.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.SHIFT_MASK));
        deleteUser.addActionListener(this);
        this.add(deleteUser);

        //Create the seeRanking menu item
        seeRanking = new JMenuItem("See my ranking");
        seeRanking.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.SHIFT_MASK));
        seeRanking.addActionListener(this);
        this.add(seeRanking);

        //Create the onlineUsers menu item
        onlineUsers = new JMenuItem("See online Users");
        onlineUsers.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.SHIFT_MASK));
        onlineUsers.addActionListener(this);
        this.add(onlineUsers);

        //Create the onlineUsers menu item
        viewTournament = new JMenuItem("View Tournament");
        viewTournament.addActionListener(this);
        this.add(viewTournament);

    }//constructor

    /**
     * Responds to the users action of clicking a menu item
     * in the network menu and calls the appropriate method
     *
     * @param e the menu item that was clicked
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == createUser) {
            createUser();
        } else if (e.getSource() == deleteUser) {
            deleteUser();
        } else if (e.getSource() == seeRanking) {
            seeRanking();
        } else if (e.getSource() == login) {
            login();
        } else if (e.getSource() == onlineUsers) {
            getOnlineUsers();
        } else if (e.getSource() == logout) {
            logout();
        } else if(e.getSource() == viewTournament) {
            viewTournament();
        }//if
    }//actionPerformed

    /**
     * For testing, shows the bracket on the screen
     */
    private void viewTournament() {

        JFrame frame = new JFrame("Tournaments");
        frame.add(new TournamentPanel());
        frame.pack();
        frame.setVisible(true);


    }//viewTournament

    /**
     * Calls the webUtilities method to create the user on the server
     */
    private void createUser() {
        JTextField username = new JTextField();
        JTextField password = new JPasswordField();
        JTextField password1 = new JPasswordField();
        Object[] message = {"Username:", username, "Password:", password, "Confirm Password:", password1};

        int option = JOptionPane.showConfirmDialog(gui, message, "Create an Online Account", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {

            if(password.getText().equals("")) {
                gui.showErrorDialog("Create User Error", "Your password cannot be blank. Please Try again.");
                return;
            }//if

            if(!password.getText().equals(password1.getText())) {
                gui.showErrorDialog("Create User Error", "Your passwords did not match. Please try again.");
                return;
            }//if

            WebUtilities.INSTANCE.createUser(username.getText(), password.getText());
        }//if
    }//createUser

    /**
     * Calls the server to login with user credentials
     */
    private void login() {

        if (!WebUtilities.INSTANCE.loggedIn()) {

            JTextField username = new JTextField();
            JTextField password = new JPasswordField();
            Object[] message = {"Username:", username, "Password:", password};

            int option = JOptionPane.showConfirmDialog(gui, message, "Login", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                //Call the server to check for valid login credentials
                boolean loggedIn = WebUtilities.INSTANCE.login(username.getText(), password.getText());
            }//if

        } else {
            JOptionPane.showMessageDialog(null,
                    "You are logged in. You must logout first before you can log in to another account.",
                    "Login Error", 2);
        }//else
    }//login

    private void logout() {
        boolean loggedOut = WebUtilities.INSTANCE.logout();
        Client.getInstance().loadNetworkBrowser();
    }//logout

    /**
     * Calls the server to delete the user from the server
     */
    private void deleteUser() {
        JTextField username = new JTextField();
        JTextField password = new JPasswordField();
        Object[] message = {"Username:", username, "Password:", password};

        int option = JOptionPane.showConfirmDialog(gui, message, "Delete Your Online Account", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            WebUtilities.INSTANCE.deleteUser(username.getText(), password.getText());
        }//if
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
        //Just for testing
        ArrayList<User> users = WebUtilities.INSTANCE.getOnlineUsers();

        if (users != null) {

            Object[][] rows = new Object[users.size()][3];

            for (int i = 0; i < users.size(); i++) {
                rows[i][0] = users.get(i).getUsername();
                rows[i][1] = users.get(i).getStatus();
                rows[i][2] = users.get(i).getRank();
            }//for

            Object[] cols = {"Username", "Status", "Ranking"};
            JTable table = new JTable(rows, cols);
            table.setEnabled(false);
            JOptionPane.showMessageDialog(null, new JScrollPane(table), "Online Users", 1);
        } else {
            JOptionPane.showMessageDialog(null,
                    "There are currently 0 users online",
                    "", 1);

        }//else
    }//getOnlineUsers
}//NetworkMenu
