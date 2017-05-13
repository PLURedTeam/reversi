package plu.red.reversi.core.util;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Created by Andrew on 3/16/2017.
 *
 * Holds the user information to pass between client and server
 * since JAX-RS will not allow me to pass two parameters using a
 * POST request
 */
public class User {
    //fields
    private String username;
    private String password;
    private String status;
    private int sessionID;
    private int rank;
    private boolean host;

    public User() {}//DefaultConstructor

    /**
     * Constructor for the user class
     * Will always be constructed on the clientside and
     * passed to the server
     * @param u username of the player
     * @param p password of the player stored in SHA256 format
     */
    public User(String u, String p) {
        username = u;
        password = p;
        rank = 100;
    }//user

    //Accessors and Mutators
    public String getUsername() { return username; }
    public void setUsername(String u) { username = u;}
    public String getPassword() { return password; }
    public void setPassword(String p) { password = p; }
    public int getSessionID() { return sessionID; }
    public void setSessionID(int s) { sessionID = s; }
    public String getStatus() { return status; }
    public void setStatus(String s) { status = s; }
    public int getRank() { return rank; }
    public void setRank(int r) { rank = r; }
    public boolean getHost() {return host;}
    public void setHost(boolean h) {host = h;}

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("password", password);
        json.put("status", status);
        json.put("sessionID", sessionID);
        json.put("ranking", rank);
        json.put("host", host);
        return json;
    }

    public User(JSONObject json) throws JSONException {
        username = json.getString("username");
        password = json.getString("password");
        status = json.getString("status");
        sessionID = json.getInt("sessionID");
        rank = json.getInt("ranking");
        host = json.getBoolean("host");
    }

}//User
