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
    private String status;
    private int sessionID;
    private int ranking;

    public User() {}//DefaultConstructor

    //Accessors and Mutators
    public String getUsername() { return username; }
    public void setUsername(String u) { username = u;}
    public String getPassword() { return password; }
    public void setPassword(String p) { password = p; }
    public int getSessionID() { return sessionID; }
    public void setSessionID(int s) { sessionID = s; }
    public String getStatus() { return status; }
    public void setStatus(String s) { status = s; }
    public int getRank() { return ranking; }
    public void setRank(int r) { ranking = r; }
}//User
