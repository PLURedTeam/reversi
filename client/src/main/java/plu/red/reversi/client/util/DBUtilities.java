package plu.red.reversi.client.util;

//import statements
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.sql.*;

/**
 * Created by Andrew on 3/9/2017.
 */
public class DBUtilities {

    //Fields
    private Connection conn; //Connection Object
    DBConnection dbConnection; //The database connector class

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

    /**
     * Creates a new game in the database
     * @param name the name of the new game
     * @return the gameID to be used for the game
     */
    public int createGame(String name) {
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
     * @param name the name of the game
     * @return true if game saved, false otherwise
     * TODO: Figure out what format to store moves in from History
     */
    public boolean saveGame(int gameID, String name, String color) {
        boolean gameSaved = false;
        int result;
        String sql = "insert into GAME values(?,?,?);";

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.clearParameters();
            stmt.setInt(1,gameID);
            stmt.setString(2,name);
            stmt.setString(3,color);
            result = stmt.executeUpdate();

            if(result > 0)
                gameSaved = true;

        } catch (SQLException e) {
            e.printStackTrace();
        }//catch

        return gameSaved;
    }//saveGame

    /**
     * Loads the game history from the database
     * @param gameID the id of the game to be loaded
     * @return
     *
     *  TODO: FINISH THIS
     */
    public String[] loadGame(int gameID) {
        String[] gameHistory = null;


        return gameHistory;
    }//loadGame

    public String[] getGames() {
        int numGames = 0;
        String[] games = null;

        try {
            //Query the database to get the number of saved games
            Statement num = conn.createStatement();
            ResultSet getNumGames = num.executeQuery("select count(*) from GAME");
            if(getNumGames.next())
                numGames = getNumGames.getInt(1);

            //Initialize the array to the number of games
            games = new String[numGames];

            //Query to get the names
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select name from GAME");

            //Fill in the array with the game names
            int i = 0;
            while(rs.next()) {
                games[i] = rs.getString(1);
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

        String sql = "insert into GAME_SETTINGS values(?,?)\n"
                + "on duplicate key\n"
                + "update game_settings = ?";

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.clearParameters();
            stmt.setInt(1,gameID);
            stmt.setString(2,settings.toString());
            stmt.setString(3,settings.toString());
            result = stmt.executeUpdate();

            if(result > 0)
                saved = true;

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
                json = new JSONObject(rs.getString(1));

        } catch (SQLException e) {
            e.printStackTrace();
        }//catch
        catch (JSONException e) {
            e.printStackTrace();
        }//catch

        return json;
    }//loadGameSettings
}//DBUtilities
