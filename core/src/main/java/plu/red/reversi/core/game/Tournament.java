package plu.red.reversi.core.game;

import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.util.User;

import java.util.ArrayList;


/**
 * Created by JChase on 4/7/17.
 */
public class Tournament {

    // ******************
    //  Member Variables
    // ******************

    ArrayList<User> userList;
    ArrayList<Match> matchList;
    Match match;
    User winner, loser;
    int winnerScore, loserScore;

    /**
     * Constructor for the Tournament
     * @param usrs is an ArrayList of User for a given Tournament
     */
    public Tournament(ArrayList<User> usrs){
        userList = usrs;
        matchList = new ArrayList<Match>(usrs.size());
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
     */
    public static class Match{
        Pair usrs;
        History hist;
        int score1, score2;
        User winner;
        User loser;
        public Match(Pair u, History h, int s1, int s2){
            usrs = u;
            hist = h;
            score1 = s1;
            score2 = s2;

            if(score1>score2) {
                u.first = winner;
                u.second = loser;
            }else {
                u.second = winner;
                u.first = loser;
            }

            int gameID = usrs.first.getSessionID();

        }

        /**
         * This method returns the winner of the current match
         * @return the winner
         */
        public User getMatchWinner(){
            return this.winner;
        }

        /**
         * This method returns the loser of the current match
         * @return the loser
         */
        public User getMatchLoser(){
            return this.loser;
        }
    }//end class Match

    /**
     * Static inner class Pair to keep track of the two Users in a Match
     */
    public static class Pair {
        public User first;
        public User second;
        public Pair(User first, User second) {
            super();
            this.first = first;
            this.second = second;
        }
    }//end class Pair
}

