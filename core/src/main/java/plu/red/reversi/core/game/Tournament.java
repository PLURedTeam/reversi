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
    private double rounds;

    /**
     * Constructor for the Tournament
     * @param usrs is an ArrayList of User for a given Tournament, can only be powers of 2
     *
     */
    public Tournament(ArrayList<User> usrs) throws IllegalArgumentException {
        //base case: only continue if usrs is an acceptable size
        int j = usrs.size();

        //use bitwise operation to check if the list is a power of 2
        if((j & -j) != j)
            throw new IllegalArgumentException();

        j--; //alter this value by 1 to use this as an index value

        //initialize fields
        userList = usrs;
        matchList = new ArrayList<Match>(usrs.size()-1);
        rounds = Math.sqrt((double)j);

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
    public void setFinalWinner(User u){
        userList.set(0, u);
        winner = u;
    }

    /**
     * This method finds and returns the winner of the Tournament
     * @return the winner -- who will be the last user left in the User array
     */
    public User getFinalWinner(){
        return userList.get(0);
    }

    /**
     *  Inner class Match to keep track of the match being played
     *  Utilizes the IGameOverListener, to be notified when the game is finished
     *  so that there can be new matches based on the winner of the initial round
     */
    public class Match implements IGameOverListener{
        private Pair usrs; //Two users for a given Match in a Tournament
        private User winner;
        private int score;

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
         * This method is used to create the pairs after the first round
         * by modifying the userList (after the Constructor
         * has initially populated the match list) which deletes the losers, this method will be called
         * in onGameOver
         */
        public void nextRound(ArrayList<User> usrLs){
            //update userList
            userList = usrLs;

            //delete losers from the userList so that the winners can be put into a Match for the next round
            for(int k = 0; k < matchList.size(); k++){
                if(matchList.get(k).usrs.first == winner)
                    userList.remove(matchList.get(k).usrs.second);
                else
                    userList.remove(matchList.get(k).usrs.first);
            }

            //save the new
            int j = userList.size()-1;
            //pair up users in the matchList
            for(int i = 0; i < j; i++) {
                matchList.add(new Match((new Pair(userList.get(i), userList.get(j))), 0));
                j--;
            }

            //keeping track of the round
            rounds--;

            //check to see if this is the final game, if so, set the final winner
            if(rounds == 1)
                setFinalWinner(winner);


        }

        /**
         * Update the UserList every time the Game is over
         * @param player Player object representing who won, null if no one wins
         * //TODO: use the Player to be able to see which user has won
         * @param score the final winning score
         */
        @Override
        public void onGameOver(Player player, int score){
            nextRound(userList);
        }

        @Override
        public boolean equals(Object o){
            if(!(o instanceof Match))
                return false;
            //if the Match does not have the same Users, return false
            if(this.usrs != ((Match) o).usrs )
                return false;
            else
                return true;
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

