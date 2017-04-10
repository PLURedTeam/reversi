package plu.red.reversi.core.db;

//import statements
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.History;
import plu.red.reversi.core.command.*;
import plu.red.reversi.core.game.player.BotPlayer;
import plu.red.reversi.core.game.player.HumanPlayer;
import plu.red.reversi.core.game.player.NullPlayer;
import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.util.Color;

//import java.awt.*;
import java.sql.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

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
        initDB();
        cleanupGames();
    }//constructor

    private void initDB() {
        if(conn == null) {
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
        }
    }

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

        initDB();

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
     * New method for saving the game all at once
     * @param h the history of the game
     * @param p the players in the game
     * @param s the settingsMap
     * @param n the name of the game to be saved
     * @return the gameID
     */
    public int saveGame(History h, Player[] p, JSONObject s, String n) {
        int gameID;

        //Create a collection from an array
        Collection<Player> players = new HashSet<Player>();
        Collections.addAll(players,p);

        gameID = createGame();
        saveGame(gameID);
        updateGame(gameID, n);
        saveGamePlayers(gameID, players);
        saveGameSettings(gameID, s);

        for(int i = 0; i < h.getNumBoardCommands(); i++)
            saveMove(gameID, h.getBoardCommand(i));

        return gameID;
    }//saveGame


    public int saveGame(int gameID, History h, Player[] p, JSONObject s, String n) {

        //Create a collection from an array
        Collection<Player> players = new HashSet<Player>();
        Collections.addAll(players,p);

        saveGame(gameID);
        updateGame(gameID, n);
        saveGamePlayers(gameID, players);
        saveGameSettings(gameID, s);

        for(int i = 0; i < h.getNumBoardCommands(); i++)
            saveMove(gameID, h.getBoardCommand(i));

        return gameID;
    }//saveGame

    /**
     * Saves the game to the database
     * @param gameID the id of the game
     * @return true if game saved, false otherwise
     * TODO: Figure out what format to store moves in from History
     */
    public boolean saveGame(int gameID) {

        initDB();

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

    /**
     * Saves the players for a game into the database
     * @param gameID the game id for the saved game
     * @param players the collection of players in a game
     * @return true if saved, false otherwise
     */
    public boolean saveGamePlayers(int gameID, Collection<Player> players) {

        initDB();

        boolean gameSaved = false;
        int result = 0;
        String sql = "insert into PLAYERS values(?,?,?,?,?,?);";

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);

            for(Player player: players) {
                int type;
                int playerDiff = 0;

                if(player instanceof HumanPlayer)
                    type = 0;
                else if(player instanceof BotPlayer) {
                    type = 1;
                    playerDiff = ((BotPlayer) player).getDifficulty();
                } else
                    type = 2;

                stmt = conn.prepareStatement(sql);
                stmt.clearParameters();
                stmt.setInt(1, gameID);
                stmt.setInt(2, player.getID());
                stmt.setInt(3, player.getColor().composite);
                stmt.setString(4, player.getName());
                stmt.setInt(5, type);
                stmt.setInt(6, playerDiff);
                result += stmt.executeUpdate();
            }//for

            if(result > 1)
                gameSaved = true;

        } catch (SQLException e) {
            e.printStackTrace();
        }//catch

        return gameSaved;
    }//saveGamePlayers

    /**
     * Loads the players for a game from the database, and adds them to the game
     *
     * @param game the game to add the players to
     */
    public void loadGamePlayers(Game game) {

        initDB();

        String sql = "select * from PLAYERS where game_id=?";

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.clearParameters();
            stmt.setInt(1,game.getGameID());
            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                int playerID   = rs.getInt("player_id");
                Color color = new Color(rs.getInt("player_color"));
                String name = rs.getString("player_name");
                int playerType = rs.getInt("player_type");
                int playerDiff = rs.getInt("player_diff");


                Player p;
                if(playerType == 0)
                    p = new HumanPlayer(game, playerID, color);
                else if(playerType == 1)
                    p = new BotPlayer(game, playerID, color, playerDiff);
                else
                    p = new NullPlayer(game, playerID, color);

                p.setName(name);
            }//while

        } catch (SQLException e) {
            e.printStackTrace();
        }//catch
    }//loadGamePlayers


    /**
     *
     * @param gameID
     * @param name
     * @return
     */
    public boolean updateGame(int gameID, String name) {

        initDB();

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
     */
    public History loadGame(int gameID) {

        initDB();

        History h = new History();

        String sql = "select * from GAME_HISTORY where game_id=? order by move_id";

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.clearParameters();
            stmt.setInt(1,gameID);
            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                int playerID = rs.getInt("player_color");
                int row = rs.getInt("move_index_r");
                int col = rs.getInt("move_index_c");
                int type = rs.getInt("command_type");

                Command m;
                if(type == 0)
                    m = new MoveCommand(playerID, new BoardIndex(row,col));
                else
                    m = new SetCommand(playerID, new BoardIndex(row,col));

                h.addCommand(m);
            }//while

        } catch (SQLException e) {
            e.printStackTrace();
        }//catch

        return h;
    }//loadGame

    /**
     * Saves a move from a game in the database
     * @param gameID the id of the game being played
     * @param cmd the command that was played
     * @return true if saved, false otherwise
     */
    public boolean saveMove(int gameID, BoardCommand cmd) {

        initDB();

        boolean moveSaved = false;
        int result;
        int moveID = 0;
        int moveType;

        if(cmd instanceof MoveCommand)
            moveType = 0;
        else
            moveType = 1;

            //Get the values from the Command Object
            int moveIndexR = cmd.position.row;
            int moveIndexC = cmd.position.column;
            String moveSource = cmd.source.toString();
            int playerID = cmd.playerID;

            //set command = 1
            //move command = 0
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

                sql = "insert into GAME_HISTORY values(?,?,?,?,?,?,?)";
                stmt = conn.prepareStatement(sql);
                stmt.clearParameters();
                stmt.setInt(1,gameID);
                stmt.setInt(2,moveID);
                stmt.setInt(3,moveIndexR);
                stmt.setInt(4,moveIndexC);
                stmt.setString(5,moveSource);
                stmt.setInt(6,playerID);
                stmt.setInt(7,moveType);

                result = stmt.executeUpdate();

                if(result > 0)
                    moveSaved = true;

            } catch (SQLException e) {
                e.printStackTrace();
            }//catch
        return moveSaved;
    }//saveMove


    /**
     * Gets the games saved in the database
     * @return a two dimensional array (name, gameID)
     */
    public String[][] getGames() {

        initDB();

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

        initDB();

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

        initDB();

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

        initDB();

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
