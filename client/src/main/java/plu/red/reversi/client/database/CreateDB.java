package plu.red.reversi.client.database;

import java.sql.*;

/**
 * Created by Andrew on 3/8/2017.
 */
public class CreateDB {

    private Connection conn;


    public CreateDB(Connection c) {
        conn = c;//Set the connection to the open connection in ClientUtilities

        if(conn == null)
            System.out.println("Could not connect to the database.");
        else {
            createUserTable();
            createGameTable();
            createGameHistoryTable();
            createGameSettingsTable();
        }//else
    }//constructor

    public void createUserTable() {
        String sql = "create table USER (\n"
                   + "id int NOT NULL,\n"
                   + "username varchar(50) UNIQUE,\n"
                   + "password char(64),\n"
                   + "alias varchar(50) NOT NULL,\n"
                   + "PRIMARY KEY(id)\n"
                   + ");";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }//catch
    }//createUserTable

    public void createGameTable() {
        String sql = "create table GAME (\n"
                + "game_id int NOT NULL,\n"
                + "user_id int NOT NULL,\n"
                + "name varchar(50),\n"
                + "user_color varchar(50),\n"
                + "game_score int,\n"
                + "game_won boolean,\n"
                + "PRIMARY KEY(game_id),\n"
                + "FOREIGN KEY(user_id) references USER(id)\n"
                + "ON DELETE CASCADE\n"
                + ");";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }//catch
    }//createGameTable

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
