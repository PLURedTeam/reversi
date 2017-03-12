package plu.red.reversi.client.database;

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
            System.out.println(e.getMessage());
        }//catch
    }//constructor

    /**
     * Open a SQLite DB connection
     *  Since the database is local and does not need a
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
                //Connects to the database file
                conn = DriverManager.getConnection("jdbc:sqlite:ClientDB.db");
            } else {
                //Creates the database file and connects to it
                conn = DriverManager.getConnection("jdbc:sqlite:ClientDB.db");
                CreateDB db = new CreateDB(conn); //Creates the tables in the database
            }//else
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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
