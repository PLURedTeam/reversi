package plu.red.reversi.client.database;

//import statements
import java.sql.*;

/**
 * Created by Andrew on 3/9/2017.
 */
public class ClientUtilities {

    //Fields
    private Connection conn; //Connection Object

    /**
     * Constructor for ClientUtilities class
     * Calls the dbConnection class to create a connection
     *  to the database (One will be created if none exist)
     *  and sets the conn field to the connection
     */
    public ClientUtilities() {
        ConnectDB dbConnection = new ConnectDB(); //Create the connection
        dbConnection.openDB();
        conn = dbConnection.getConn(); //Set the database connection
    }//constructor

    /**
     * Creates the user in the database
     * @param username username of the user, must be unique
     * @param password the password for the user (Stored using SHA256)
     * @return true if user created, false otherwise
     */
    public boolean createUser(String username, String password) {
        int result = 0;
        String sql = "Insert into USER values(?,?)";

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.clearParameters();
            stmt.setString(1,username);
            stmt.setString(2,password);

            result = stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }//catch

        if(result > 0)
            return true;
        else
            return false;
    }//createUser

    /**
     * Returns an array of users that are in the database
     * @return an array of users in the database
     */
    public String[] getUsers() {
        String[] users = null;
        ResultSet rs, rsSize;

        String sql = "select username from USER";
        String sizeSql = "select count(username) from USER";
        int size = 0;

        try {
            Statement stmt = conn.createStatement();

            rsSize = stmt.executeQuery(sizeSql);
            size = rsSize.getInt(1);
            users = new String[size];

            rs = stmt.executeQuery(sql);

            int i = 0;
            while(rs.next()) {
                users[i] = rs.getString(1);
                i++;
            }//while

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }//catch

        return users;
    }//getUsers

    /**
     * Deletes a user from the database
     * @param username username of the user
     * @param password password for the user
     * @return true if deleted, false otherwise
     */
    public boolean deleteUser(String username, String password) {
        int result = 0;
        String sql = "delete from USER where username=? and password=?";

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.clearParameters();
            stmt.setString(1,username);
            stmt.setString(2,password);

            result = stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }//catch

        if(result > 0)
            return true;
        else
            return false;
    }//deleteUser

    /**
     * Tests the login information against what is in the database
     * @param username the username of the user
     * @param password the password of the user
     * @return true if valid login credentials, false otherwise
     */
    public boolean login(String username, String password) {
        ResultSet result;
        boolean validLogin = false;
        String sql = "select username from USER where username=? and password=?";

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.clearParameters();
            stmt.setString(1,username);
            stmt.setString(2,password);

            result = stmt.executeQuery();

            if(result.next())
                validLogin = true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }//catch

        return validLogin;
    }//login

    /**
     *
     * @param name
     * @return
     */
    public int createGame(String name) {
        int gameID = -1;


        return gameID;
    }//createGame

    /**
     *
     * @param gameID
     * @return
     */
    public String[] loadGame(int gameID) {
        String[] gameHistory = null;


        return gameHistory;
    }//loadGame

    public String[] getGames(String username) {
        String[] games = null;

        return games;
    }//getGames

    /**
     *
     * @param gameID
     */
    public void saveGameSettings(int gameID) {

    }//saveGameSettings

    /**
     *
     * @param gameID
     */
    public void loadGameSettings(int gameID) {

    }//loadGameSettings

}//ClientUtilities
