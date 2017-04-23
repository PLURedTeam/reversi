package plu.red.reversi.core;

import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.core.db.DBUtilities;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.History;
import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.lobby.Lobby;
import plu.red.reversi.core.util.ChatLog;

/**
 * Glory to the Red Team.
 *
 * Master Coordinator for client-side operations. The Client class controls all sub-Controllers and Models that are used
 * in the client side of the program. Main operations such as starting or loading a Game take place through this class.
 */
public class Client extends Controller {

    public Client(IMainGUI gui) { super(gui); }
    public Client(IMainGUI gui, Coordinator core) { super(gui, core); }

    private String queryName(boolean networked) {
        String name = "Local Game";
        if(networked) {
            name = null;
            while(name == null) {
                name = gui.showQueryDialog("Game Name", "Enter a name for the new Game Lobby:");
                if(name == null) return null; // Cancelled
                else if(name.isEmpty()) // TODO: Check for duplicate names
                    gui.showErrorDialog("Game Name", "Name Cannot be Empty");
            }
        }
        return name;
    }

    public void createIntoLobby(boolean networked) {
        String name = queryName(networked);
        if(name == null) return; // Cancelled
        setCore(new Lobby(this, gui, networked, name));
    }

    public void loadIntoLobby(boolean networked) {

        // Figure out the Game name
        String gameName = queryName(networked);
        if(gameName == null) return; // Cancelled

        // Get the name of the Game to load
        String name = gui.showLoadDialog();
        if(name == null || name.length() < 1) return; // Cancelled

        //Get the games from the DB
        String[][] games = DBUtilities.INSTANCE.getGames();
        int gameID = 0;

        //Loop through array and set gameID
        for(int i = 0; i < games.length; i++)
            if(name.equals(games[i][0]))
                gameID = Integer.parseInt(games[i][1]);

        //Loads a game from the database
        Game game = Game.loadGameFromDatabase(gui, gameID);
        game.setGameSaved(true); //Sets that the game has been saved before and has a name

        setCore(new Lobby(this, gui, game, networked, gameName));
    }

    public void startGame() throws IllegalStateException {

        // Check the state of the program
        if(!(core instanceof Lobby))
            throw new IllegalStateException("Can only start a game from a Lobby");

        setCore(((Lobby)core).startGame());
    }

    public void saveGame() throws IllegalStateException {

        // Check the state of the program
        if(!(core instanceof Game))
            throw new IllegalStateException("Can only save a Game if a Game is being played.");

        // Get information to save with
        String name = gui.showSaveDialog();
        if(name == null || name.length() < 1) return; // Cancelled
        Game game = (Game)core;
        int gameID = game.getGameID();


        History h = game.getHistory();
        Player[] p = game.getAllPlayers();
        JSONObject s = game.getSettings().toJSON();

        DBUtilities.INSTANCE.saveGame(h,p,s,name);

        game.setGameSaved(true);
    }
}
