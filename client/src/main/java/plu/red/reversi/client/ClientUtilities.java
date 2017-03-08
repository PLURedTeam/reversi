package plu.red.reversi.client;

//import statements
import java.sql.*;

/**
 * Created by Andrew on 3/7/2017.
 */
public class ClientUtilities {

    //fields
    private Connection conn = null; // Connection object
    private String connStatus = null;

    public ClientUtilities() {
        // Load the SQLiteSQL JDBC driver
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            connStatus = "Unable to load driver.";
        }//catch
    }//constructor

    /**
     * Open a SQLite DB connection where url, username, and password are
     * passed into the method
     * @return connection status
     */
    public String openDB() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:/clientDB.db");
        } catch (SQLException e) {
            connStatus = "Error connecting to database";
        }//catch
        if (conn != null)
            connStatus = "Successfully connected to database";
        return connStatus;
    }// openDB

    /**
     * Close the connection to the DB
     * @return the status of the database connection
     */
    public String closeDB() {
        connStatus = null;
        try {
            if (conn != null) {
                conn.close();
            }//if
            conn = null;
        } catch (SQLException e) {
            connStatus = "Failed to close database connection: " + e;
        }//catch

        if (conn == null) {
            connStatus = "Successfully disconnected from database";
        }
        return connStatus;
    }// closeDB

    /**
     * Accessor for connection object
     * @return the connection object
     */
    public Connection getConn() { return conn; }//getConn

    /**
     * Mutator for connection object
     * @param conn the connection object
     */
    public void setConn(Connection conn) { this.conn = conn; }//setConn

}//clientUtilites
