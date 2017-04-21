package plu.red.reversi.core.game;

import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.listener.IGameOverListener;
import plu.red.reversi.core.util.User;

import java.util.ArrayList;

/**
 * Glory to the Red Team.
 *
 * Tournament capability for the Game, asks for a User ArrayList and starts a Tournament
 *
 */
public class Tournament {

    // ******************
    //  Member Variables
    // ******************

    private ArrayList<User> userList;
    private ArrayList<Match> matchList;

    private User winner;

    /**
     * Constructor for the Tournament
     * @param usrs is an ArrayList of User for a given Tournament, can only be powers of 2
     *
     */
    public Tournament(ArrayList<User> usrs){
        userList = usrs;
        matchList = new ArrayList<Match>(usrs.size()-1);

        //we assume the matchList will always be 3 or 7  (userList.size()-1) / 2
        int j = usrs.size()-1;
        //pair up users in the matchList
        for(int i = 0; i < j; i++) {
            matchList.add(new Match((new Pair(userList.get(i), userList.get(j))), 0));
            j--;
        }
    }

    /**
     * currentMatch finds the current match based on the id
     * what the current match is
     * @param matchID as the index in the machList array
     * @return the Match with the current ID
     *
     */
    public Match currentMatch(int matchID){
        if(matchID < 0 || matchID > matchList.size())
            throw new IndexOutOfBoundsException();
        return matchList.get(matchID);
    }

    /**
     * currentOpponents returns the two players who are competing in a given match
     * @param matchID which match
     * @return the users who are competing in a given match
     */
    public Pair currentOpponents(int matchID){
        //check out of bounds
        if(matchID < 0 || matchID > matchList.size()) {
            throw new IndexOutOfBoundsException();
        }
        //get the pair of opponents that matches the game id
        Match match = matchList.get(matchID);
        return match.usrs;
    }

    /**
     * matchNumber finds which number of match of the given match
     * @param m match looking for the number
     * @return the number of the match in the list
     */
    public int matchNumber(Match m){
        return matchList.indexOf(m);
    }

    /**
     * nextMatch
     * @param current match
     * @return returns the match after the current match passed in
     */
    public Match nextMatch(Match current){
        int i = matchList.indexOf(current);
        return matchList.get(i+1);
    }

    /**
     * This method sets the winner
     * @param u the user left in the userList
     */
    private void setWinner(User u){
        userList.set(0, u);
        winner = u;
    }

    /**
     * This method finds and returns the winner of the game
     * @return the winner -- who will be the last user left in the User array
     */
    public User getFinalWinner(){
        return userList.get(0);
    }


    /**
     *  Inner class Match to keep track of the match being played
     *
     */
    public class Match implements IGameOverListener{
        Pair usrs; //Two users for a given Match in a Tournament
        User winner;
        int score;

        /**
         * Constructor for a Match in a Tournament
         * @param p the Pair of Users
         * @param s the score of the winner
         *
         */
        public Match(Pair p, int s){
            usrs = p;
            score = s;
        }

        /**
         * This method returns the winner of the current Match
         * @return the winner
         */
        public User getMatchWinner(){ return winner; }


        /**
         * This method is used to create the pairs after the first round by modifying the userList (after the Constructor
         * has initially populated the match list) which deletes the losers
         */
        public void nextRound(ArrayList<User> usrLs){

            int j = usrLs.size()-1;
            //pair up users in the matchList
            for(int i = 0; i < j; i++) {
                matchList.add(new Match((new Pair(userList.get(i), userList.get(j))), 0));
                j--;
            }

            //delete losers
            for(int k = 0; k < matchList.size(); k++){
                userList.remove(matchList.get(k).winner);

            }
        }

        /**
         * Update the UserList every time the Game is over
         * @param player Player object representing who won, null if no one wins
         * @param score the final winning score
         */
        @Override
        public void onGameOver(Player player, int score){
            nextRound(userList);

        }
    }//end class Match

    /**
     * Static inner class Pair to keep track of the two Users in a Match
     */
    public static class Pair {
        public User first;
        public User second;

        /**
         * Constructor to initialize the first and second User in a Pair to be Used later in Match
         * @param first
         * @param second
         */
        public Pair(User first, User second) {
            this.first = first;
            this.second = second;
        }
    }//end class Pair

}

