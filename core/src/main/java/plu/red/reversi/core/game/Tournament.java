package plu.red.reversi.core.game;

import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.listener.IGameOverListener;
import plu.red.reversi.core.util.User;

import java.util.ArrayList;

/**
 * Created by JChase on 4/7/17.
 */
public class Tournament {

    // ******************
    //  Member Variables
    // ******************

    private ArrayList<User> userList;
    private ArrayList<Match> matchList;

    private User winner, loser;
    private int winnerScore, loserScore;

    /**
     * Constructor for the Tournament
     * @param usrs is an ArrayList of User for a given Tournament, can only be powers of 2
     *
     */
    public Tournament(ArrayList<User> usrs){
        userList = usrs;
        matchList = new ArrayList<Match>(usrs.size()-1);

        //we assume the matchList will always be (userList.size()-1) / 2
        int j = usrs.size()-1;
        //pair up users in the matchList
        for(int i = 0; i < j; i++) {
            matchList.add(new Match((new Pair(userList.get(i), userList.get(j))), 0, 0));
            j--;
        }

        //delete losers from the userList
        for(int k = 0; k < matchList.size(); k++){
            if( matchList.get(k).score1 > matchList.get(k).score2 )
                userList.remove(matchList.get(k).usrs.second);
            else
                userList.remove(matchList.get(k).usrs.first);
        }

        this.worker(userList);
    }

    /**
     * This method is used to modify the userList after the Constructor has initially populated
     * the match list
     */
    public void worker(ArrayList<User> usrLs){

        int j = usrLs.size()-1;
        //pair up users in the matchList
        for(int i = 0; i < j; i++) {
            matchList.add(new Match((new Pair(userList.get(i), userList.get(j))), 0, 0));
            j--;
        }

        //delete losers
        for(int k = 0; k < matchList.size(); k++){
            if( matchList.get(k).score1 > matchList.get(k).score2 )
                userList.remove(matchList.get(k).usrs.second);
            else
                userList.remove(matchList.get(k).usrs.first);

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
    public void setWinner(User u){
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
     * This method sets the loser a match
     * @param u loser in match
     */
    public void setLoser(User u){
        loser = u;
    }

    /**
     * This method returns the loser of the tournament
     * @return the loser
     */
    public User getLoser(){
        return loser;
    }

    /**
     * This method sets the winner, loser and their scores
     * @param winner1
     * @param loser1
     * @param winScore1
     * @param loseScore1
     */
    public void completedGame(User winner1, User loser1, int winScore1, int loseScore1){
        setWinner(winner1);
        setLoser(loser1);
        winnerScore = winScore1;
        loserScore = loseScore1;
    }

    /**
     * Static inner class Match to keep track of the match being played
     *
     */
    public static class Match implements IGameOverListener {
        Pair usrs;
        User usr;
        User winner;
        User loser;
        int score1, score2;

        /**
         * Constructor for a match
         * @param u
         * @param s1
         * @param s2
         */
        public Match(Pair u, int s1, int s2){
            usrs = u;
            score1=s1;
            score2=s2;

        }


        /**
         * This method returns the winner of the current match
         * @return the winner
         */
        public User getMatchWinner(){ return winner; }

        /**
         * This method returns the loser of the current match
         * @return the loser of the individual match
         */
        public User getMatchLoser(){
            return loser;
        }


        @Override
        public void onGameOver(Player player, int score){

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

