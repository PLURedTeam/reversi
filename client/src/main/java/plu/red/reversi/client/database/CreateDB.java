package plu.red.reversi.client.database;

//import statements
import java.sql.*;

/**
 * Created by Andrew on 3/8/2017.
 *
 * Creates the tables for the clientside database
 */
public class CreateDB {

    //fields
    private Connection conn; //Connection object for the database

    /**
     * Constructor for the CreateDB class
     * Checks the connection status and then creates the tables in the
     *  database.
     *
     * @param c The connection object to the database
     */
    public CreateDB(Connection c) {
        conn = c;//Set the connection to the open connection in ConnectDB

        if(conn == null)
            System.out.println("Could not connect to the database.");
        else {
            //Call methods to create tables
            createUserTable();
            createGameTable();
            createGameHistoryTable();
            createGameSettingsTable();
        }//else
    }//constructor

    /**
     * Creates the USER table in the database using SQL commands
     * If the SQL statement fails, will print the SQL message to
     *  the console
     */
    public void createUserTable() {
        String sql = "create table USER (\n"
                   + "username varchar(50) UNIQUE,\n"
                   + "password char(64),\n"
                   + "PRIMARY KEY(username)\n"
                   + ");";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }//catch
    }//createUserTable

    /**
     * Creates the GAME table in the database using SQL commands
     * If the SQL statement fails, will print the SQL message to
     *  the console
     */
    public void createGameTable() {
        String sql = "create table GAME (\n"
                + "game_id int NOT NULL,\n"
                + "username varchar(50) NOT NULL,\n"
                + "name varchar(50),\n"
                + "user_color varchar(50),\n"
                + "game_score int,\n"
                + "game_won boolean,\n"
                + "PRIMARY KEY(game_id),\n"
                + "FOREIGN KEY(username) references USER(username)\n"
                + "ON DELETE CASCADE\n"
                + ");";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }//catch
    }//createGameTable

    /**
     * Creates the GAME_HISTORY table in the database using SQL commands
     * If the SQL statement fails, will print the SQL message to
     *  the console
     */
    public void createGameHistoryTable() {
        String sql = "create table GAME_HISTORY (\n"
                + "game_id int NOT NULL,\n"
                + "move_id int NOT NULL,\n"
                + "move_position_x int NOT NULL,\n"
                + "move_position_y int NOT NULL,\n"
                + "PRIMARY KEY(game_id, move_id)\n"
                + "FOREIGN KEY(game_id) references GAME(game_id)\n"
                + "ON DELETE CASCADE\n"
                + ");";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }//catch
    }//createGameHistoryTable

    /**
     * Creates the GAME_SETTINGS table in the database using SQL commands
     * If the SQL statement fails, will print the SQL message to
     *  the console
     */
    public void createGameSettingsTable() {
        String sql = "create table GAME_SETTINGS (\n"
                + "game_id int NOT NULL,\n"
                + "board_size int NOT NULL,\n"
                + "board_colors varchar(20) NOT NULL,\n"
                + "PRIMARY KEY(game_id)\n"
                + "FOREIGN KEY(game_id) references GAME(game_id)\n"
                + "ON DELETE CASCADE\n"
                + ");";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }//catch
    }//createGameSettingsTable
}//class
