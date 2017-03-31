package plu.red.reversi.server.db;

//import statements

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Andrew on 3/8/2017.
 *
 * Creates the tables for the clientside database
 */
public class CreateDB {

    //fields
    private Connection conn; //Connection object for the util

    /**
     * Constructor for the CreateDB class
     * Checks the connection status and then creates the tables in the
     *  database.
     *
     * @param c The connection object to the database
     */
    public CreateDB(Connection c) {
        conn = c;//Set the connection to the open connection in DBConnection

        if(conn == null)
            System.out.println("Could not connect to the database.");
        else {
            //Call methods to create tables
            //TODO: Create methods for creating the server database
        }//else
    }//constructor

}//class
