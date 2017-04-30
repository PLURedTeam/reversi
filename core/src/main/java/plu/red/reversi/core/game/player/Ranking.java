package plu.red.reversi.core.game.player;

import plu.red.reversi.core.util.User;

import java.util.ArrayList;


/**
 * Created by JChase on 4/30/17.
 */
public class Ranking {

    private ArrayList<User> userList;
    private int size;

    /**
     * Constructor to initialize the ArrayList and size
     * @param usrList
     */
    public Ranking(ArrayList<User> usrList){
        userList = usrList;
        size = userList.size();
    }

    /**
     * This method should be called any time a User wins a game
     */
    public void increaseRank(User u){
        //get the current spot of the User in the list
        int currentSpot = userList.indexOf(u);

        //increase the User's rank by one
        u.setRank(u.getRank()+1);

        //check if their new rank is high enough to replace their predecessor in the list
        if(u.getRank() > userList.get(currentSpot).getRank()) {
            userList.remove(u);
            userList.add(currentSpot - 1, u);
        }

    }//end method increaseRank

    /**
     * This method should be called any time a User loses a game
     */
    public void decreaseRank(User u){
        //get the current spot of the User in the list
        int currentSpot = userList.indexOf(u);

        //decrease the User's rank by one
        u.setRank(u.getRank()-1);

        //check if their new rank is low enough to move them down the list
        if(u.getRank() < userList.get(currentSpot).getRank()) {
            userList.remove(u);
            userList.add(currentSpot + 1, u);
        }

    }//end method decreaseRank



}
