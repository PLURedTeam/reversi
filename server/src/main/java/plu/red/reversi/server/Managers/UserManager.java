package plu.red.reversi.server.Managers;

import plu.red.reversi.core.util.User;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Andrew on 3/22/2017.
 * Manages the users that are currently online.
 */
public class UserManager {

    public static UserManager INSTANCE = new UserManager();

    private ConcurrentHashMap<String, User> onlineUsers;

    /**
     * Constructor for the UserManager
     */
    public UserManager() {
        System.out.println("[USER MANAGER] USER MANAGER STARTED");
        onlineUsers = new ConcurrentHashMap<String, User>();
    }//constructor

    /**
     * Adds a user to the UserManager
     * @param user the user to be added
     */
    public void addUser(User user) {
        System.out.println("[USER MANAGER] USER LOGGED IN: " + user.getUsername());
        onlineUsers.put(user.getUsername(), user);
    }//addUser

    /**
     * Removes a user from the UserManager
     * @param user the user to be removed
     */
    public void removeUser(User user) {
        System.out.println("[USER MANAGER] USER LOGGED OUT: " + user.getUsername());
        onlineUsers.remove(user.getUsername());
        GameManager.INSTANCE.endSession(user.getSessionID());
    }//user

    /**
     * Removes a user that timed out from the user manager
     * @param sessionID the sessionID of the user that timedOut
     */
    public void timedOut(int sessionID) {
        for(String username: onlineUsers.keySet())
            if(onlineUsers.get(username).getSessionID() == sessionID) {
                onlineUsers.remove(username);
                System.out.println("[USER MANAGER] USER TIMED OUT: " + username);
            }//for
    }//timedOut

    /**
     * Checks to see if user is loggedIn
     * @param username the username to see if logged in
     * @return true it user is logged in, false otherwise
     */
    public boolean loggedIn(String username) {
        if(onlineUsers.containsKey(username)) return true;
        return false;
    }//loggedIn

    /**
     * Gathers a list of online users and returns the list
     * @return An arraylist of online users
     */
    public ArrayList<User> onlineUsers() {
        ArrayList<User> users = new ArrayList<User>();
        for(String key: onlineUsers.keySet()) {
            User u = new User();
            u.setUsername(onlineUsers.get(key).getUsername());
            u.setStatus(onlineUsers.get(key).getStatus());
            u.setRank(onlineUsers.get(key).getRank());
            users.add(u);
        }//for

        //Set sessionID and password(should already be null) to null
        for(User u: users) {
            u.setSessionID(0);
            u.setPassword("");
        }//for
        return users;
    }//onlineUsers


    /**
     * Gets the user with the associated sessionID
     * @param sessionID the sessionID of the user to be retrieved
     */
    public User getUser(int sessionID) {
        for(String username: onlineUsers.keySet())
            if(onlineUsers.get(username).getSessionID() == sessionID) {
                return onlineUsers.get(username);
            }//for

        return null;
    }//getUser

    /**
     * Changes the user's status (IN LOBBY, IN GAME)
     */
    public void setStatus(String username, String status) {
        for(String u: onlineUsers.keySet()) {
            if(u.equals(username))
                onlineUsers.get(u).setStatus(status);
        }//for
    }//setStatus



}//UserManager
