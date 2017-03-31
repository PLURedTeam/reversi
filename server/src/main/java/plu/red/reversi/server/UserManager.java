package plu.red.reversi.server;

import plu.red.reversi.core.util.User;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Andrew on 3/22/2017.
 */
public class UserManager {

    HashMap<String, User> onlineUsers;


    /**
     * Constructor for the UserManager
     */
    public UserManager() {
        onlineUsers = new HashMap<String, User>();
    }//constructor



}
