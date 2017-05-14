package plu.red.reversi.core;

import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.core.db.DBUtilities;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.History;
import plu.red.reversi.core.game.logic.GameLogic;
import plu.red.reversi.core.game.logic.GoLogic;
import plu.red.reversi.core.game.logic.ReversiLogic;
import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.lobby.Lobby;
import plu.red.reversi.core.lobby.PlayerSlot;
import plu.red.reversi.core.network.WebUtilities;
import plu.red.reversi.core.util.ChatLog;
import plu.red.reversi.core.util.GamePair;

/**
 * Glory to the Red Team.
 *
 * Master Coordinator for client-side operations. The Client class controls all sub-Controllers and Models that are used
 * in the client side of the program. Main operations such as starting or loading a Game take place through this class.
 */
public class Client extends Controller {

    public Browser browser;

    public Client(IMainGUI gui, ChatLog chat, Browser browser, Coordinator core) {
        super(gui, chat, core);

        if(browser != null) {
            this.browser = browser;
            browser.master = this;
            browser.gui = this.gui;
        }
    }

    private String queryName(boolean networked) {
        String name = "Local Game";
        if(networked) {
            name = null;
            while(name == null) {
                name = gui.showQueryDialog("Game Name", "Enter a name for the new Game Lobby:");
                if(name == null) return null; // Cancelled
                else if(name.isEmpty()) {// TODO: Check for duplicate names
                    gui.showErrorDialog("Game Name", "Name Cannot be Empty");
                    name = null;
                }
            }
        }
        return name;
    }

    public void createIntoLobby(boolean networked) {

        // Check for login status
        if(networked && !WebUtilities.INSTANCE.loggedIn()) {
            gui.showErrorDialog("Network Game", "You must be logged in to start a networked Game");
            return;
        }

        if(networked && WebUtilities.INSTANCE.inNetworkGame()) {
            gui.showErrorDialog("Network Game", "You can only be in one network game at a time.");
            return;
        }

        // Figure out the Game name
        String name = queryName(networked);
        if(name == null) return; // Cancelled

        // Figure out the Game type
        Object type = gui.showQueryDialog("Game Type", "Select a type of Game to Host", GameLogic.Type.values(), GameLogic.Type.REVERSI);
        if(type == null) return; // Cancelled
        GameLogic logic;
        switch((GameLogic.Type)type) {
            case REVERSI:   logic = new ReversiLogic();     break;
            case GO:        logic = new GoLogic();          break;
            default:        throw new IllegalArgumentException("Unknown GameLogic Type Selected");
        }

        // Figure out Player counts
        int[] ic = logic.validPlayerCounts();
        Integer[] counts = new Integer[ic.length];
        for(int i = 0; i < ic.length; i++) counts[i] = ic[i];
        Object c = gui.showQueryDialog("Player Count", "Select the amount of Players that will be playing", counts, logic.minPlayerCount());
        if(c == null) return; // Cancelled
        Integer count = (Integer)c;

        // Set the Lobby
        setCore(new Lobby(this, gui, logic, networked, name));

        // Add players
        Lobby lobby = (Lobby)core;
        lobby.addSlot(PlayerSlot.SlotType.LOCAL);
        for(int i = 1; i < count; i++)
            lobby.addSlot(networked ? PlayerSlot.SlotType.NETWORK : PlayerSlot.SlotType.LOCAL);


        GamePair.GameType gameType;

        switch((GameLogic.Type)type) {
            case REVERSI:   gameType = GamePair.GameType.REVERSI;     break;
            case GO:        gameType = GamePair.GameType.REVERSI;          break;
            default:        throw new IllegalArgumentException("Unknown GameLogic Type Selected");
        }

        // Notify Server
        if(networked) WebUtilities.INSTANCE.createGame(count, name,gameType);
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

    @Override
    public void loadNetworkBrowser() {
        setCore(browser);
        browser.refresh();
    }

    public void startGame() throws IllegalStateException {

        // Check the state of the program
        if(!(core instanceof Lobby))
            throw new IllegalStateException("Can only start a game from a Lobby");

        boolean started = false;

        //check for networked
        if(((Lobby) core).canStart()) {
            Game game = ((Lobby)core).startGame();


            if(game.isNetworked())
                started = WebUtilities.INSTANCE.startGame(game);
            else started = true;
            if(!started) {
                gui.showErrorDialog("Start Game Error", "Couldn't start game on server.");
                return;
            }

            setCore(game);

        } else {
            gui.showErrorDialog("Start Game Error", "Not enough players to start game.");
        }//else
    }

    public void saveGame() throws IllegalStateException {

        // Check the state of the program
        if(!(core instanceof Game))
            throw new IllegalStateException("Can only save a Game if a Game is being played.");

        // Get information to save with
        String name = gui.showSaveDialog();
        if(name == null || name.length() < 1) return; // Cancelled


        String[][] dbGames = DBUtilities.INSTANCE.getGames();
        boolean dup = false;

        boolean notSaved = true;

        while(notSaved) {
            for (int i = 0; i < dbGames.length; i++) {
                dup = false;
                if (name.equals(dbGames[i][0])) {
                    gui.showErrorDialog("Save Game Error", "That game name exists. Please choose another name.");
                    name = gui.showSaveDialog();
                    dup = true;
                    break;
                }//if

            }//for
            if(dup == false)
                notSaved = false;
        }


        Game game = (Game)core;
        int gameID = game.getGameID();

        int type = game.getGameLogic().getType().ordinal();



        History h = game.getHistory();
        Player[] p = game.getAllPlayers();
        JSONObject s = game.getSettings().toJSON();

        DBUtilities.INSTANCE.saveGame(h,p,s,name,type);

        game.setGameSaved(true);
    }
}
