package plu.red.reversi.server;

import plu.red.reversi.core.util.User;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Andrew on 3/22/2017.
 * Manages the users that are currently online.
 */
public class UserManager {

    public static UserManager INSTANCE = new UserManager();

    private HashMap<String, User> onlineUsers;

    /**
     * Constructor for the UserManager
     */
    public UserManager() {
        onlineUsers = new HashMap<String, User>();
    }//constructor

    /**
     * Adds a user to the UserManager
     * @param user the user to be added
     */
    public void addUser(User user) {
        onlineUsers.put(user.getUsername(), user);
    }//addUser

    /**
     * Removes a user from the UserManager
     * @param user the user to be removed
     */
    public void removeUser(User user) {
        onlineUsers.remove(user.getUsername());
    }//user

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
        for(String key: onlineUsers.keySet())
            users.add(onlineUsers.get(key));

        //Set sessionID and password(should already be null) to null
        for(User u: users) {
            u.setSessionID(-1);
            u.setPassword("");
        }//for
        return users;
    }//onlineUsers
}//UserManager
