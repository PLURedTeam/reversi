package plu.red.reversi.server.db;

//import statements
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Andrew on 3/8/2017.
 *
 * Creates the tables for the serverside database
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
            createUserTable();
        }//else
    }//constructor

    /**
     * Creates the USER table in the database using SQL commands
     * If the SQL statement fails, will print the SQL message to
     *  the console
     */
    private void createUserTable() {
        String sql = "create table USER (\n"
                + "username varchar(50) UNIQUE,\n"
                + "password char(64),\n"
                + "PRIMARY KEY(username)\n"
                + ");";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }//catch

    }//createUserTable
}//class
