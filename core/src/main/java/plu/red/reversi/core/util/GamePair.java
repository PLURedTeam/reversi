package plu.red.reversi.core.util;

import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Andrew on 4/20/2017.
 *
 * Holds information pertinent to a network game
 *
 */
public class GamePair {

    /**
     * The status of a network game
     */
    public enum GameStatus {
        LOBBY,
        WAITING,
        PLAYING;
    }//gameStatus

    public ArrayList<User> players = new ArrayList<User>(); //The players in the game
    public GameStatus status; //the status of the game
    public int gameID;
    public int numPlayers;
    public String gameName;
    public DataMap settings;

    /**
     * Constructor
     * Sets the game status to "LOBBY"
     */
    public GamePair(int id, int p, String name) {
        gameID = id;
        numPlayers = p;
        status = GameStatus.LOBBY;
        gameName = name;
    }//constructor

    public GamePair() {}
    public void setPlayers(ArrayList<User> p) {players = p;}
    public void setStatus(GameStatus s) {status = s;}
    public void setGameID(int id) {gameID = id;}
    public void setNumPlayers(int p) {numPlayers = p;}
    public void setGameName(String n) {gameName = n;}
    public ArrayList<User> getPlayers() {return players;}
    public GameStatus getStatus() {return status;}
    public int getGameID() {return gameID;}
    public int getNumPlayers() {return numPlayers;}
    public String getGameName() {return gameName;}

}//gamePair
