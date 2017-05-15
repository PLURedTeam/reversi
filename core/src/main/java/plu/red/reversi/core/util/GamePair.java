package plu.red.reversi.core.util;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
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

    public enum GameType {
        REVERSI,
        GO;
    }//gameType

    public ArrayList<User> players = new ArrayList<User>(); //The players in the game
    public GameStatus status; //the status of the game
    public int gameID;
    public int numPlayers;
    public String gameName;
    public DataMap settings;
    public GameType gameType;

    /**
     * Constructor
     * Sets the game status to "LOBBY"
     */
    public GamePair(int id, int p, String name, GameType g) {
        gameID = id;
        numPlayers = p;
        status = GameStatus.LOBBY;
        gameName = name;
        gameType = g;
    }//constructor

    public GamePair(JSONObject obj) {
        try {
            gameID = obj.getInt("gameID");
            numPlayers = obj.getInt("numPlayers");
            gameName = obj.getString("gameName");
            System.out.println(obj.get("gameType").getClass());
            if(obj.getString("gameType").equals("REVERSI"))
                gameType = GameType.REVERSI;
            else
                gameType = GameType.GO;
            status = GameStatus.LOBBY;

            players = new ArrayList<>();
            JSONArray arr = obj.getJSONArray("players");

            for(int i = 0;i < arr.length();i++)
                players.add(new User(arr.getJSONObject(i)));

        } catch(Exception e) {
            // this should not happen.
            e.printStackTrace();
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("gameID", gameID);
        obj.put("numPlayers", numPlayers);
        obj.put("gameName", gameName);
        obj.put("gameType", gameType.ordinal());

        return obj;
    }

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
    public GameType getGameType() {return gameType;}
    public void setGameType(GameType g) {gameType = g;}

}//gamePair
