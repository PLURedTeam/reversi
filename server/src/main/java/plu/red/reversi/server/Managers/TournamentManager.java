package plu.red.reversi.server.Managers;

import plu.red.reversi.core.util.GamePair;
import plu.red.reversi.core.util.User;
import plu.red.reversi.server.listener.ISessionListener;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Andrew on 3/26/2017.
 * Manages the games that are currently waiting for players and games that are currently
 * being played.
 */
public class TournamentManager implements ISessionListener {

    public static TournamentManager INSTANCE = new TournamentManager();

    private int gameCounter = 0;

    private ConcurrentHashMap<Integer, GamePair> games = new ConcurrentHashMap<Integer, GamePair>();

    /**
     * Creates a new network game
     * @param numPlayers the number of players the game can support
     * @return the id of the game created
     */
    public int createGame(int numPlayers, String name) {
        Integer gameID = gameCounter++; //Get next game id
        System.out.println("[GAME MANAGER] Creating New Game: " + gameID);
        games.put(gameID,new GamePair(gameID,numPlayers, name));
        return gameID;
    }//createGame

    /**
     * Adds a player to the specified game if the user is logged in and the game has
     * available slots
     * @param id the id of the game to add the user to
     * @param user the user being added to the game
     * @return true if added, false otherwise
     */
    public boolean addPlayer(int id, User user) {
        if(!UserManager.INSTANCE.loggedIn(user.getUsername())) return false;
        if(games.get(id).players.size() >= games.get(id).numPlayers) return false;
        System.out.println("[GAME MANAGER] Adding User: " + user.getUsername() + " to Game: " + id);
        games.get(id).players.add(user);
        return true;
    }//addPlayer

    /**
     * Removes a player from a network game
     * @param id the id of the game to remove the user from
     * @param user the user to remove from the game
     * @return true if removed, false otherwise
     */
    public boolean removePlayer(int id, User user) {
        if(!games.containsKey(id)) return false;
        for(User u: games.get(id).players) {
            if(u.getUsername().equals(user)) {
                games.get(id).players.remove(u);
                return true;
            }//if
        }//for
        return false;
    }//removeUser

    /**
     * Gets the game from the game manager if it exits
     * @param id the id of the game
     * @return GamePair of the specified game
     */
    public GamePair getGame(int id) {
        if(!games.containsKey(id)) return null;
        return games.get(id);
    }//getGame


    /**
     * Gets the games that are currently on the server and the status in which the games are
     * @return An arraylist of games on the server of type GamePair
     */
    public ArrayList<GamePair> getGames() {
        ArrayList<GamePair> gameList = new ArrayList<GamePair>();
        for(Integer i: games.keySet()) gameList.add(games.get(i));
        return gameList;
    }//getGames

    /**
     * Checks to see if the game exists with the given id
     * @param id the game id
     * @return true if game exists, false otherwise
     */
    public boolean gameExists(int id) {
        if(games.containsKey(id)) return true;
        return false;
    }//gameExists

    /**
     * Sets the status of the game to "PLAYING"
     * @param id the id of the game
     */
    public void startGame(int id) {
        games.get(id).status = GamePair.GameStatus.PLAYING;
    }//startGame

    /**
     * Listener for a session ending
     * @param sessionID the session id of the user
     */
    @Override
    public void endSession(int sessionID) {
        new Thread(new Runnable() {
            @Override
            public void run() {

            }//run
        }).start();

    }//endSession
}//gameManager
