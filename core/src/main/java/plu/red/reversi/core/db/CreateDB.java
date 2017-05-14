package plu.red.reversi.core.db;

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
    private Connection conn; //Connection object for the database

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
            createGameTable();
            createGameHistoryTable();
            createGameSettingsTable();
            createPlayersTable();
        }//else
    }//constructor

    /**
     * Creates the GAME table in the database using SQL commands
     * If the SQL statement fails, will print the SQL message to
     *  the console
     */
    private void createGameTable() {
        String sql = "create table GAME (\n"
                + "game_id int NOT NULL,\n"
                + "name varchar(50),\n"
                + "user_color varchar(50),\n"
                + "game_type int,\n"
                + "PRIMARY KEY(game_id)\n"
                + ");";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }//catch
    }//createGameTable

    /**
     * Creates the GAME_HISTORY table in the database using SQL commands
     * If the SQL statement fails, will print the SQL message to
     *  the console
     */
    private void createGameHistoryTable() {
        String sql = "create table GAME_HISTORY (\n"
                + "game_id int NOT NULL,\n"
                + "move_id int NOT NULL,\n"
                + "move_index_r int NOT NULL,\n"
                + "move_index_c int NOT NULL,\n"
                + "move_source varchar(50) NOT NULL,\n"
                + "player_color int NOT NULL,\n"
                + "command_type varchar(50) NOT NULL,\n"
                + "PRIMARY KEY(game_id, move_id),\n"
                + "FOREIGN KEY(game_id) references GAME(game_id)\n"
                + "ON DELETE CASCADE\n"
                + ");";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }//catch
    }//createGameHistoryTable

    /**
     * Creates the GAME_SETTINGS table in the database using SQL commands
     * If the SQL statement fails, will print the SQL message to
     *  the console
     */
    private void createGameSettingsTable() {
        String sql = "create table GAME_SETTINGS (\n"
                + "game_id int NOT NULL,\n"
                + "game_settings text NOT NULL,\n"
                + "PRIMARY KEY(game_id)\n"
                + "FOREIGN KEY(game_id) references GAME(game_id)\n"
                + "ON DELETE CASCADE\n"
                + ");";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }//catch
    }//createGameSettingsTable

    /**
     * Creates the PLAYERS table in the database using SQL commands
     * If the SQL statement fails, will print the SQL message to
     *  the console
     */
    private void createPlayersTable() {
        String sql = "create table PLAYERS (\n"
                + "game_id int NOT NULL,\n"
                + "player_id int NOT NULL,\n"
                + "player_color int NOT NULL,\n"
                + "player_name varchar(50),\n"
                + "player_type int NOT NULL,\n"
                + "player_diff int,\n"
                + "PRIMARY KEY(game_id,player_id)\n"
                + "FOREIGN KEY(game_id) references GAME(game_id)\n"
                + "ON DELETE CASCADE\n"
                + ");";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }//catch
    }//createGameSettingsTable
}//class
