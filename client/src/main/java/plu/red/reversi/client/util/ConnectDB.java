package plu.red.reversi.client.util;

//import statements
import java.io.File;
import java.sql.*;

/**
 * Created by Andrew on 3/7/2017.
 * Creates and manages the connection object to the database
 */
public class ConnectDB {

    //fields
    private Connection conn = null; // Connection object
    private String connStatus = null; //Status of the connection

    /**
     * Constructor for the ConnectDB class
     * Loads the JDBC driver from the SQLite dependency and
     *  prints the exception to the console if the driver could
     *  not be loaded
     */
    public ConnectDB() {
        // Load the SQLite JDBC driver
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }//catch
    }//constructor

    /**
     * Open a SQLite DB connection
     *  Since the util is local and does not need a
     *   username or password none of those parameters
     *   are passed.
     *
     *  If the database does not exist, one will be created
     *   with the name ClientDB.db and the CreateDB class will
     *   be called to create the tables.
     *
     *  If the connection to the database fails the SQL message
     *   will be printed to the console.
     *
     * NOT COMPLETE, STILL HAVE TO FIGURE OUT WHAT DIRECTORY TO CREATE THE FILE IN
     *
     * @return connection status
     */
    public String openDB() {
        try {
            //Testing to see if db file exists
            File file = new File("ClientDB.db");

            if(file.exists()) {
                //Connects to the util file
                conn = DriverManager.getConnection("jdbc:sqlite:ClientDB.db");
            } else {
                //Creates the util file and connects to it
                conn = DriverManager.getConnection("jdbc:sqlite:ClientDB.db");
                CreateDB db = new CreateDB(conn); //Creates the tables in the database
            }//else
        } catch (SQLException e) {
            e.printStackTrace();
        }//catch
        if (conn != null)
            connStatus = "Successfully connected to database";
        return connStatus;
    }// openDB

    /**
     * Close the connection to the DB
     * This is important, without closing the database
     *  it can become locked for editing the next time
     *  that the database is opened. Also it may prevent
     *  the process from being terminated.
     * @return the status of the util connection
     */
    public String closeDB() {
        connStatus = null; //Set the status to null
        try {
            if (conn != null) {
                conn.close(); //Try to close the connection
            }//if
            conn = null;
        } catch (SQLException e) {
            connStatus = "Failed to close database connection: " + e;
        }//catch

        if (conn == null) {
            connStatus = "Successfully disconnected from database";
        }//if
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
