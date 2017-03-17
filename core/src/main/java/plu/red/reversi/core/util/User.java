package plu.red.reversi.core.util;

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
    private int sessionID;

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
    }//user

    //Accessors and Mutators
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public int getSessionID() {return sessionID; }
    public void setSessionID(int s) { sessionID = s; }
    public void setPassword(String p) { password = p; }
}//User
