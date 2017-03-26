package plu.red.reversi.server;

import plu.red.reversi.core.util.User;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Andrew on 3/22/2017.
 */
public class UserManager {

    public static UserManager INSTANCE = new UserManager();

    HashMap<String, User> onlineUsers;


    /**
     * Constructor for the UserManager
     */
    public UserManager() {
        onlineUsers = new HashMap<String, User>();
    }//constructor

    public void addUser(User user) {
        onlineUsers.put(user.getUsername(), user);

        System.out.println("IN ADD USER, USERNAME: " + user.getUsername());

    }//addUser

    /**
     * Checks to see if user is loggedIn
     * @param username
     * @return
     */
    public boolean loggedIn(String username) {
        if(onlineUsers.containsKey(username)) return true;
        return false;
    }//loggedIn

    public void removeUser(User user) {
        onlineUsers.remove(user.getUsername());
    }//user



}//UserManager
