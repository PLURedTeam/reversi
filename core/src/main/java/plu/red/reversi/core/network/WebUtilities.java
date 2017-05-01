package plu.red.reversi.core.network;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.core.Controller;
import plu.red.reversi.core.IMainGUI;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.util.ChatMessage;
import plu.red.reversi.core.util.GamePair;
import plu.red.reversi.core.util.User;

import javax.swing.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
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
    private boolean gameHost = false;
    private int networkGameID = -1;

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
        IMainGUI gui = Controller.getInstance().gui;
        if(!loggedIn) { //Check to see if currently logged in

            //Create User
            user.setUsername(username);
            user.setPassword(password);

            try {
                //Create target and call server
                WebTarget target = client.target(baseURI + "login");
                Response response = target.request().post(Entity.json(user));

                //If invalid credentials, return false
                if (response.getStatus() == 409) {
                    gui.showErrorDialog("Login Error", "That username is already logged in! Please try " +
                            "again with a different username.");
                    return false;
                }//if

                //If invalid credentials, return false
                if (response.getStatus() == 403) {
                    gui.showErrorDialog("Login Error", "That username and/or password was incorrect.");
                    return false;
                }//if

                gui.showInformationDialog("Login Successful", "Successfully logged in.");

                user = response.readEntity(User.class);
                sessionID = user.getSessionID();
                loggedIn = true;
                Controller.getInstance().getCore().notifyLoggedInListeners(loggedIn);

                //Start the session thread
                Thread session = new Thread(new SessionHandler(client, user));
                session.start();

                Thread chat = new Thread(new ChatHandler(this));
                chat.start();

                return true;
            } catch (Exception e) {
                System.err.println(e.getMessage());
                gui.showErrorDialog("Login Error", "The server is currently unreachable. Please try again later.");
                return false;
            }//catch
        } else {
            gui.showErrorDialog("Login Error", "You are currently logged in. You must logout first");
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

        IMainGUI gui = Controller.getInstance().gui;

        try {
        //Call server to logout
        WebTarget target = client.target(baseURI + "logout");
        Response response = target.request().post(Entity.json(user));

        if(response.getStatus() != 200) return false;
        loggedIn = false;
        user.setUsername(null);
        user.setPassword(null);
        user.setSessionID(-1);

        Controller.getInstance().getCore().notifyLoggedInListeners(loggedIn);

        return true;

        } catch (Exception e) {
            gui.showErrorDialog("Logout Error", "The server is currently unreachable. Please try again later.");
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

        IMainGUI gui = Controller.getInstance().gui;

        //Create a user object
        user.setUsername(username);
        user.setPassword(password);

        try {
        WebTarget target = client.target(baseURI + "create-user");
        Response response = target.request().post(Entity.json(user));

        if(response.getStatus() == 200) {
            gui.showInformationDialog("Online Account Created", "Your online account was successfully created.");
            login(username, password); // Login to simplify one step
            return true;
        }//if
        if(response.getStatus() == 406) {
            gui.showErrorDialog("Create User Error", "That username already exists, please try again with a different username.");
        }//if
        if(response.getStatus() == 500) {
            gui.showErrorDialog("Server Error", "A server error occurred. Please try again later.");
        }

        return false;

        } catch (Exception e) {
            gui.showErrorDialog("Create User Error", "The server is currently unreachable. Please try again later.");
            return false;
        }//catch
    }//createUser

    /**
     * Calls the server to delete a network user account
     * @param username the requested username for the player
     * @param password the password for the player in SHA256 format
     * @return true if user is deleted, false otherwise
     */
    public boolean deleteUser(String username, String password) {

        //Logout the current user
        if(loggedIn) logout();

        IMainGUI gui = Controller.getInstance().gui;

        //Create a user object
        user.setUsername(username);
        user.setPassword(password);

        try {
            WebTarget target = client.target(baseURI + "delete-user");
            Response response = target.request().post(Entity.json(user));

            if(response.getStatus() == 200) {
                gui.showInformationDialog("Online Account Deleted", "Your online account was successfully deleted.");
                return true;
            }//if
            if(response.getStatus() == 406) {
                gui.showErrorDialog("Delete User Error", "That username and/or password was incorrect.");
            }//if
            if(response.getStatus() == 500) {
                gui.showErrorDialog("Server Error", "A server error occurred. Please try again later.");
            }

            return false;

        } catch (Exception e) {
            gui.showErrorDialog("Delete User Error", "The server is currently unreachable. Please try again later.");
            return false;
        }//catch
    }//deleteUser

    /**
     * Gets a list of the users currently logged in to the server
     * @return An arraylist of users logged in
     */
    public ArrayList<User> getOnlineUsers() {

        IMainGUI gui = Controller.getInstance().gui;

        try {
            WebTarget target = client.target(baseURI + "online-users");
            Response response = target.request(MediaType.APPLICATION_JSON).get();

            ArrayList<User> users = response.readEntity(new GenericType<ArrayList<User>>() {
            });

            return users;
        } catch (Exception e) {
            gui.showErrorDialog("Get Online Users Error", "The server is currently unreachable. Please try again later.");
            return null;
        }//catch
    }//getOnlineUsers

    /**
     * Returns the Users network status (logged in or not)
     * @return true if logged in, false otherwise
     */
    public boolean loggedIn() {
        return loggedIn;
    }//loggedIn

    /**
     * Returns the User object when the user is loggedIn
     * @return the User object
     */
    public User getUser() {
            return user;
    }//getUser


    /**
     * Sends a chat message to the server
     * @param m the message to send to the server
     */
    public void sendChat(ChatMessage m) {
        JSONObject message = null;
        try {message = m.toJSON();}
        catch (JSONException e) {e.printStackTrace();}
        WebTarget target = client.target(baseURI + "chat");
        Response response = target.request().post(Entity.text(message.toString()));
    }//sendChat

    /**
     * Creates a new game on the server that players can join
     * @return true if game created, false otherwise
     */
    public boolean createGame(int numPlayers, String name) {
        IMainGUI gui = Controller.getInstance().gui;
        if(loggedIn && networkGameID == -1) { //Check to see if currently logged in

            try {
                //Create target and call server
                WebTarget target = client.target(baseURI + "game/create/" + numPlayers + "/" + name);
                Response response = target.request().post(Entity.json(user));

                //If invalid credentials, return false
                if (response.getStatus() == 403) {
                    gui.showErrorDialog("Create Game Error", "You must login to create a game.");
                    return false;
                }//if

                //set the game ID
                networkGameID = response.readEntity(Integer.class);
                Thread gameHandler = new Thread(new GameHandler(this,networkGameID));
                gameHost = true;
                gameHandler.start();

                return true;
            } catch (Exception e) {
                gui.showErrorDialog("Create Game Error", "The server is currently unreachable. Please try again later.");
                return false;
            }//catch
        } else {

            if(!loggedIn) {
                gui.showErrorDialog("Create Game Error", "You are not currently logged in. You must login first.");
            } else {
                gui.showErrorDialog("Create Game Error", "You can only be in one network game at a time.");
            }//else
            return false;
        }//else
    }//createGame

    /**
     * Calls the server to join a network game
     * @param gameID the gameID of the game to join
     * @return true if game joined, false otherwise
     */
    public boolean joinGame(int gameID) {
        IMainGUI gui = Controller.getInstance().gui;
        if(loggedIn) { //Check to see if currently logged in

            try {
                //Create target and call server
                WebTarget target = client.target(baseURI + "game/join/" + gameID);
                Response response = target.request().post(Entity.json(user));

                //If invalid credentials, return false
                if (response.getStatus() == 403) {
                    gui.showErrorDialog("Join Game Error", "You must login to join a game.");
                    return false;
                }//if

                //If game is gone, return false
                if (response.getStatus() == 410) {
                    gui.showErrorDialog("Join Game Error", "The requested game is no longer available.");
                    return false;
                }//if

                //set the game ID
                networkGameID = response.readEntity(Integer.class);
                Thread gameHandler = new Thread(new GameHandler(this,networkGameID));
                gameHandler.start();

                return true;
            } catch (Exception e) {
                gui.showErrorDialog("Join Game Error", "The server is currently unreachable. Please try again later.");
                return false;
            }//catch
        } else {
            gui.showErrorDialog("Join Game Error", "You are not currently logged in. You must login first");
            return false;
        }//else
    }//joinGame


    /**
     * Sends the game to the server in order to start it on the networked players application
     * @param g the game to start
     */
    public void startGame(Game g) {
        IMainGUI gui = Controller.getInstance().gui;
        if(loggedIn) { //Check to see if currently logged in

            try {

                String json = g.serialize().toJSON().toString();

                //Create target and call server
                WebTarget target = client.target(baseURI + "game/start/" + networkGameID);
                Response response = target.request().post(Entity.text(json));

                System.out.println(response.getStatus());

            } catch (Exception e) {
                e.printStackTrace();
                e.getMessage();
                gui.showErrorDialog("Start Game Error", "The server is currently unreachable. Please try again later.");
            }//catch
        } else {
            gui.showErrorDialog("Join Game Error", "You are not currently logged in. You must login first");
        }//else

    }//startGame


    /**
     * Sends a move to the server to be played
     * @param c the command to be sent to the server
     */
    public void sendMove(Command c) {
        IMainGUI gui = Controller.getInstance().gui;
        if(loggedIn) { //Check to see if currently logged in

            try {

                String json = c.toJSON().toString();

                //Create target and call server
                WebTarget target = client.target(baseURI + "game/" + networkGameID);
                Response response = target.request().post(Entity.text(json));

                System.out.println(response.getStatus());

            } catch (Exception e) {
                e.printStackTrace();
                e.getMessage();
                gui.showErrorDialog("Start Game Error", "The server is currently unreachable. Please try again later.");
            }//catch
        } else {
            gui.showErrorDialog("Join Game Error", "You are not currently logged in. You must login first");
        }//else

    }//startGame



    /**
     * Gets a list of the games currently on the server
     * @return An arraylist of GamePairs
     */
    public ArrayList<GamePair> getOnlineGames() {
        IMainGUI gui = Controller.getInstance().gui;
        try {
            WebTarget target = client.target(baseURI + "game/get-games");
            Response response = target.request(MediaType.APPLICATION_JSON).get();

            if (response.getStatus() == 404) {
                return new ArrayList<GamePair>();
            }//if

            ArrayList<GamePair> games = response.readEntity(new GenericType<ArrayList<GamePair>>() {});
            return games;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            gui.showErrorDialog("Get Online Games Error", "The server is currently unreachable. Please try again later.");
            return null;
        }//catch
    }//getOnlineUsers

    /**
     * Returns if the client is the host of the networked game
     * @return true if host, false otherwise
     */
    public boolean isHost() {
        return gameHost;
    }//isHost


}//webUtilities
