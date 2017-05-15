package plu.red.reversi.server.db;

//import statements

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Andrew on 3/9/2017.
 *
 * A set of methods to retrieve and store persistent data in the database for
 * the network server.
 *
 */
public class DBUtilities {

    //Create a singleton class so that there are no database locks
    //or race conditions while using the database. Only becomes an
    //issue when two instances try to write simultaneously.
    public static DBUtilities INSTANCE = new DBUtilities();

    //Fields
    private Connection conn; //Connection Object
    private DBConnection dbConnection; //The database connector class

    /**
     * Constructor for DBUtilities class
     * Calls the dbConnection class to create a connection
     *  to the util (One will be created if none exist)
     *  and sets the conn field to the connection
     */
    public DBUtilities() {
        dbConnection = new DBConnection(); //Create the connection
        dbConnection.openDB(); //Open the connection
        conn = dbConnection.getConn(); //Set the database connection
    }//constructor


    private void initDB() {

        try {
            if(dbConnection.getConn().isClosed()) {
                dbConnection = new DBConnection(); //Create the connection
                dbConnection.openDB(); //Open the connection
                conn = dbConnection.getConn(); //Set the database connection
            }//if
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }


    /**
     * Creates the user in the database
     * @param username username of the user, must be unique
     * @param password the password for the user (Stored using SHA256)
     * @return true if user created, false otherwise
     */
    public boolean createUser(String username, String password) {

        initDB();

        int result = 0;
        String query = "Select username from USER where username=?";
        String sql = "Insert into USER values(?,?,0,0)";

        try {
            //Check if username exists
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.clearParameters();
            pstmt.setString(1,username);
            ResultSet rs = pstmt.executeQuery();

            if(rs.next()) return false; //username exists return false

            //Create User
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.clearParameters();
            stmt.setString(1,username);
            stmt.setString(2,password);

            result = stmt.executeUpdate(); //1 if user was added to database

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }//catch

        if(result > 0)
            return true;
        else
            return false;
    }//createUser

    /**
     * Deletes a user from the database
     * @param username username of the user
     * @param password password for the user
     * @return true if deleted, false otherwise
     */
    public boolean deleteUser(String username, String password) {

        initDB();

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
     * Checks the users credentials to authenticate a user in the database for login
     * @param username the username of the player
     * @param password the password of the player
     * @return true if valid credentials, false otherwise
     */
    public boolean authenticateUser(String username, String password) {

        initDB();

        int result = 0;
        String query = "Select username from USER where username=? and password=?";

        try {
            //Check if credentials are correct
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.clearParameters();
            pstmt.setString(1,username);
            pstmt.setString(2,password);
            ResultSet rs = pstmt.executeQuery();

            if(rs.next())
                result = 1;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }//catch

        if(result > 0)
            return true;
        else
            return false;
    }//authenticateUser


    public void updateScore(String name, int score) {
        initDB();

        int result = 0;
        String query = "Select games from USER where username=?";
        String query1 = "Update USER set games=?, score=? where username='?'";

        try {
            //Check if credentials are correct
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.clearParameters();
            pstmt.setString(1,name);
            ResultSet rs = pstmt.executeQuery();

            int games = 0;

            while(rs.next())
                games = rs.getInt(1);

            pstmt.clearParameters();
            pstmt.setInt(1,games++);
            pstmt.setInt(2,score);
            pstmt.setString(3,name);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }//catch
    }//updateScore


    /**
     * Calls the DBConnection class to close the database
     * This will only be used when the program closes
     */
    public void closeDB() {
        dbConnection.closeDB();
    }//closeDB
}//DBUtilities
