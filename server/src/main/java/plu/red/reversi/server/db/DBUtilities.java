package plu.red.reversi.server.db;

//import statements

import java.sql.*;

/**
 * Created by Andrew on 3/9/2017.
 */
public class DBUtilities {

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

        //TURN ON FOREIGN KEY CONSTRAINTS
        String sql = "PRAGMA foreign_keys = ON;";
        try {

            Statement command = conn.createStatement();
            command.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }//catch
    }//constructor

    /**
     * Calls the DBConnection class to close the database
     * This will only be used when the program closes
     */
    public void closeDB() {
        dbConnection.closeDB();
    }//closeDB



}//DBUtilities
