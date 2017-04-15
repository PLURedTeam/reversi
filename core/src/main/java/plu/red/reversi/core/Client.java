package plu.red.reversi.core;

import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.core.db.DBUtilities;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.History;
import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.lobby.Lobby;

/**
 * Glory to the Red Team.
 *
 * Master Coordinator for client-side operations. The Client class controls all sub-Controllers and Models that are used
 * in the client side of the program. Main operations such as starting or loading a Game take place through this class.
 */
public class Client {

    // not public final because it needs to be initialized
    private static Client INSTANCE = null;

    /**
     * Initialize the Client Controller with a IMainGUI reference.
     *
     * @param gui IMainGUI reference to init with
     */
    public static void init(IMainGUI gui) { INSTANCE = new Client(gui); }

    /**
     * Gets the global instance of the Client controller.
     *
     * @return Global Client instance
     */
    public static Client getInstance() { return INSTANCE; }

    public final IMainGUI gui;

    protected Coordinator core = null;



    public Client(IMainGUI gui) {
        this.gui = gui;
        gui.setClient(this);
        setCore(new Lobby(gui));
    }

    public Client(IMainGUI gui, Coordinator core) {
        this.gui = gui;
        gui.setClient(this);
        setCore(core);
    }


    protected final void setCore(Coordinator core) {
        if(this.core != null) this.core.cleanup();
        this.core = core;
        gui.updateGUIMajor();
    }

    public Coordinator getCore() { return core; }




    public void createIntoLobby() {

        setCore(new Lobby(gui));
    }

    public void loadIntoLobby() {

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

        setCore(new Lobby(gui, game));
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
