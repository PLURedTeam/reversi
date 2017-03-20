package plu.red.reversi.core.db;

//import statements
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.core.BoardIndex;
import plu.red.reversi.core.Game;
import plu.red.reversi.core.History;
import plu.red.reversi.core.PlayerColor;
import plu.red.reversi.core.command.*;

import java.sql.*;
import java.util.Set;

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
            cleanupGames();
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

    /**
     * Creates a new game in the database
     * @return the gameID to be used for the game
     */
    public int createGame() {
        int gameID = 1; //Default gameID for first game

        try {
            Statement max = conn.createStatement();
            ResultSet maxID = max.executeQuery("select max(game_id) from GAME");

            if(maxID.next())
                gameID = maxID.getInt(1) + 1; //Choose largest gameID + 1

        } catch (SQLException e) {
            e.printStackTrace();
        }//catch
        return gameID;
    }//createGame

    /**
     * Saves the game to the database
     * @param gameID the id of the game
     * @return true if game saved, false otherwise
     * TODO: Figure out what format to store moves in from History
     */
    public boolean saveGame(int gameID) {
        boolean gameSaved = false;
        int result;
        String sql = "insert into GAME values(?,?,?);";

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.clearParameters();
            stmt.setInt(1,gameID);
            result = stmt.executeUpdate();

            if(result > 0)
                gameSaved = true;

        } catch (SQLException e) {
            e.printStackTrace();
        }//catch

        return gameSaved;
    }//saveGame

    public boolean updateGame(int gameID, String name) {
        boolean gameSaved = false;
        int result;
        String sql = "update GAME set name=? where game_id=?";

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.clearParameters();
            stmt.setString(1,name);
            stmt.setInt(2,gameID);
            result = stmt.executeUpdate();

            if(result > 0)
                gameSaved = true;

        } catch (SQLException e) {
            e.printStackTrace();
        }//catch

        return gameSaved;
    }//updateGame

    /**
     * Loads the game history from the database
     * @param gameID the id of the game to be loaded
     * @return
     *
     *  TODO: FINISH THIS
     */
    public History loadGame(int gameID) {
        History h = new History();

        String sql = "select * from GAME_HISTORY where game_id=?";

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.clearParameters();
            stmt.setInt(1,gameID);
            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                int color = rs.getInt("player_color");
                int row = rs.getInt("move_index_r");
                int col = rs.getInt("move_index_c");

                MoveCommand m = new MoveCommand(PlayerColor.validPlayers()[color],new BoardIndex(row,col));
                h.addCommand(m);
            }//while

        } catch (SQLException e) {
            e.printStackTrace();
        }//catch

        return h;
    }//loadGame

    public boolean saveMove(int gameID, Command command) {
        boolean moveSaved = false;
        int result;
        int moveID = 0;

        if(command instanceof MoveCommand) {
            MoveCommand cmd = (MoveCommand)command;
            //Get the values from the Command Object
            int moveIndexR = cmd.position.row;
            int moveIndexC = cmd.position.column;
            String moveSource = cmd.source.toString();
            String player = cmd.player.name;
            int color = cmd.player.validOrdinal();
            String commandType = "Move Command";

            String sql = "select max(move_id) from GAME_HISTORY where game_id=?";

            try {
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.clearParameters();
                stmt.setInt(1,gameID);
                ResultSet rs = stmt.executeQuery();

                if(rs.next())
                    moveID = rs.getInt(1) + 1;
                else {
                    System.out.println("Error adding to history, could not get moveID");
                    return false;
                }//else

                sql = "insert into GAME_HISTORY values(?,?,?,?,?,?,?,?)";
                stmt = conn.prepareStatement(sql);
                stmt.clearParameters();
                stmt.setInt(1,gameID);
                stmt.setInt(2,moveID);
                stmt.setInt(3,moveIndexR);
                stmt.setInt(4,moveIndexC);
                stmt.setString(5,moveSource);
                stmt.setString(6,player);
                stmt.setInt(7,color);
                stmt.setString(8,commandType);

                result = stmt.executeUpdate();

                if(result > 0)
                    moveSaved = true;

            } catch (SQLException e) {
                e.printStackTrace();
            }//catch

        } else if(command instanceof SurrenderCommand) {

        } else if(command instanceof SetCommand) {

        }//else
        return moveSaved;
    }//saveMove


    /**
     * Gets the games saved in the database
     * @return a two dimensional array (name, gameID)
     */
    public String[][] getGames() {
        int numGames = 0;
        String[][] games = null;

        try {
            //Query the database to get the number of saved games
            Statement num = conn.createStatement();
            ResultSet getNumGames = num.executeQuery("select count(*) from GAME");
            if(getNumGames.next())
                numGames = getNumGames.getInt(1);

            //Initialize the array to the number of games
            games = new String[numGames][2];

            //Query to get the names
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select name,game_id from GAME");

            //Fill in the array with the game names
            int i = 0;
            while(rs.next()) {
                games[i][0] = rs.getString(1);
                games[i][1] = rs.getString(2);
                i++;
            }//while
        } catch (SQLException e) {
            e.printStackTrace();
        }//catch

        return games;
    }//getGames

    /**
     * Deletes a game from the database
     * @param gameID the id of the game to be deleted
     * @return true if deleted, false otherwise
     */
    public boolean deleteGame(int gameID) {
        int result = 0;
        String sql = "delete from GAME where gameID=?";

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.clearParameters();
            stmt.setInt(1,gameID);

            result = stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }//catch

        if(result > 0)
            return true;
        else
            return false;
    }//deleteGame

    /**
     * Saves the game settings to the database
     * @param gameID the id of the game
     * @param settings the JSONObject that includes the game settings
     */
    public boolean saveGameSettings(int gameID, JSONObject settings) {
        boolean saved = false;
        int result;
        String sql;

        try {
            String key = "select game_id from GAME_SETTINGS where game_id=?";
            PreparedStatement keyStmt = conn.prepareStatement(key);
            keyStmt.clearParameters();
            keyStmt.setInt(1,gameID);
            ResultSet set = keyStmt.executeQuery();

            //If key exists update game
            if(set.next()) {
                sql = "update GAME_SETTINGS set game_settings=? where game_id=?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.clearParameters();
                stmt.setString(1, settings.toString());
                stmt.setInt(2, gameID);
                result = stmt.executeUpdate();

                if (result > 0)
                    saved = true;

            } else {
                sql = "insert into GAME_SETTINGS values(?,?)\n";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.clearParameters();
                stmt.setInt(1, gameID);
                stmt.setString(2, settings.toString());
                result = stmt.executeUpdate();

                if (result > 0)
                    saved = true;
            }//else
        } catch (SQLException e) {
            e.printStackTrace();
        }//catch
        return saved;
    }//saveGameSettings

    /**
     * Loads the game settings JSON object from the database for the game
     * @param gameID the ID of the game to get the settings
     * @return JSON object with the game settings
     */
    public JSONObject loadGameSettings(int gameID) {
        JSONObject json = null;

        String sql = "select game_settings from GAME_SETTINGS where game_id=?";

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.clearParameters();
            stmt.setInt(1,gameID);
            ResultSet rs = stmt.executeQuery();

            if(rs.next())
                json = new JSONObject(rs.getString("game_settings"));

        } catch (SQLException e) {
            e.printStackTrace();
        }//catch
        catch (JSONException e) {
            e.printStackTrace();
        }//catch

        return json;
    }//loadGameSettings

    /**
     * Cleans up the database removing games that were not saved
     * to the database (i.e. games with no name)
     */
    private void cleanupGames() {
        String sql = "delete from GAME where name IS NULL or name=''";

        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }//catch
    }//cleanupGames

}//DBUtilities
