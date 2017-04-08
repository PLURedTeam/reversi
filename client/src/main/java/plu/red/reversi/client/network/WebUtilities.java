package plu.red.reversi.client.network;

import org.codehaus.jettison.json.JSONArray;
import plu.red.reversi.core.util.User;

import javax.swing.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

/**
 * Created by Andrew on 3/15/2017.
 */
public class WebUtilities {

    public static WebUtilities INSTANCE = new WebUtilities();

    private Client client;
    private String baseURI = "http://localhost:8080/reversi/"; //Just temp, will change with production server
    private int sessionID;
    private User user = new User();
    private boolean loggedIn = false;

    /**
     * Constructor for WebUtilities
     * Creates a new Client Object
     */
    public WebUtilities() {
        //create the client
        client = ClientBuilder.newClient();
    }//webUtilities

    /**
     * Calls the server to authenticate the user credentials to login
     * to the server
     * @param username The players username
     * @param password The players password, should be passed in SHA256 format
     * @return true if valid credentials, false otherwise
     */
    public boolean login(String username, String password) {
        if(!loggedIn) { //Check to see if currently logged in

            //Create User
            user.setUsername(username);
            user.setPassword(password);

            try {
                //Create target and call server
                WebTarget target = client.target(baseURI + "login");
                Response response = target.request().post(Entity.json(user));

                //If invalid credentials, return false
                if (response.getStatus() == 403) return false;
                user = response.readEntity(User.class);
                sessionID = user.getSessionID();
                loggedIn = true;

                new PollingMachine(this, client, user);
                return true;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "The server is currently unreachable. Please try again later.",
                        "Login Error", 2);
                return false;
            }//catch
        } else {
            JOptionPane.showMessageDialog(null,
                    "You are currently logged in. You must logout first",
                    "Login Error", 2);
            return false;
        }//else
    }//login

    /**
     * Logs out the user from the server. Will probably only be used on
     * application exit or if we support connection to multiple servers
     */
    public boolean logout() {
        //If not logged in, return true
        if(loggedIn == false) return true;

        try {
        //Call server to logout
        WebTarget target = client.target(baseURI + "logout");
        Response response = target.request().post(Entity.json(user));

        if(response.getStatus() != 200) return false;
        loggedIn = false;
        user.setUsername(null);
        user.setPassword(null);
        user.setSessionID(-1);

        return true;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "The server is currently unreachable. Please try again later.",
                    "Logout Error",2);
            return false;
        }//catch

    }//logout

    /**
     * Calls the server to create a new network user account
     * @param username the requested username for the player
     * @param password the password for the player in SHA256 format
     * @return true if user is created, false otherwise
     */
    public boolean createUser(String username, String password) {

        //Logout the current user
        if(loggedIn) logout();

        //Create a user object
        user.setUsername(username);
        user.setPassword(password);

        try {
        WebTarget target = client.target(baseURI + "create-user");
        Response response = target.request().post(Entity.json(user));

        if(response.getStatus() == 200) {
            JOptionPane.showMessageDialog(null,
                    "Your online account was successfully created.",
                    "Online Account Created", 1);
            return true;
        }//if
        if(response.getStatus() == 406) {
            JOptionPane.showMessageDialog(null,
                    "That username already exists, please try again with a different username.",
                    "Create User Error", 2);
        }//if
        if(response.getStatus() == 500) {
            JOptionPane.showMessageDialog(null,
                    "A server error occurred. Please try again later.",
                    "Server Error", 2);
        }

        return false;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "The server is currently unreachable. Please try again later.",
                    "Create User Error",2);
            return false;
        }//catch
    }//createUser





    /**
     * Gets a list of the users currently logged in to the server
     * @return An arraylist of users logged in
     */
    public ArrayList<User> getOnlineUsers() {
        WebTarget target = client.target(baseURI + "online-users");
        Response response = target.request(MediaType.APPLICATION_JSON).get();

        ArrayList<User> users = response.readEntity(new GenericType<ArrayList<User>>(){});

        return users;
    }//getOnlineUsers

    /**
     * Returns the Users network status (logged in or not)
     * @return true if logged in, false otherwise
     */
    public boolean loggedIn() {
        return loggedIn;
    }//loggedIn
}//webUtilities
