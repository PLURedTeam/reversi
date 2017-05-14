package plu.red.reversi.core.network;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.MessageEvent;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.core.Controller;
import plu.red.reversi.core.Coordinator;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.listener.INetworkListener;
import plu.red.reversi.core.lobby.Lobby;
import plu.red.reversi.core.util.ChatMessage;
import plu.red.reversi.core.util.DataMap;
import plu.red.reversi.core.util.User;

import java.io.IOException;
import java.net.URI;

/**
 * Created by Andrew on 4/13/2017.
 */
public class GameHandler implements Runnable, INetworkListener, EventHandler {

    //fields
    private String baseURI; //Just temp, will change with production server
    private WebUtilities util;
    private boolean loggedIn = true;
    private int gameID;

    private EventSource es;

    /**
     * Constructor
     */
    public GameHandler(WebUtilities u, int id, String baseURI) {
        util = u; //Set the WebUtilities
        gameID = id;
        this.baseURI = baseURI;
        Coordinator.addListenerStatic(this); //Add the listener
    }//constructor

    /**
     * Polls the server in a new thread to get new chat messages
     * from the server
     */
    public void run() {
        System.out.println("[GAME HANDLER]: Thread Started");

        //Build the client and set the target
        try {
            es = new EventSource.Builder(this, new URI(baseURI + "game/" + gameID))
                    .build();

            es.start();

        } catch(Exception e) {
            // should not happen
            throw new RuntimeException();
        }

        System.out.println("[GAME HANDLER]: Thread Finished");
    }//run



    public void onOpen() throws Exception {
        System.out.println("Game handler has begun listening!");
    }

    public void onClosed() throws Exception {
        System.out.println("Game handler has stopped listening!");

        Coordinator.removeListenerStatic(this);
    }

    public void onMessage(String event, MessageEvent messageEvent) throws Exception {

        if(event.equals("join")) {

            User u = new User(new JSONObject(messageEvent.getData()));
            System.out.println("[GAME HANDLER]: " + u.getUsername() + " Connected to the Game");
            Coordinator core = Controller.getInstance().getCore();
            if(core instanceof Lobby)
                ((Lobby)core).joinUser(u);

        } else if(event.equals("move")) {

            try {
                JSONObject obj = new JSONObject(messageEvent.getData());

                Command c = Command.fromJSON(obj);

                Controller.getInstance().getCore().acceptCommand(c);
                System.out.println("[GAME HANDLER]: " + c.source + " moved");

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if(event.equals("start")) {

            if(!WebUtilities.INSTANCE.isHost()) {
                Game game;

                try {
                    JSONObject json = new JSONObject(messageEvent.getData());
                    DataMap map = new DataMap(json);

                    game = new Game(Controller.getInstance(), Controller.getInstance().gui, map);

                    Controller.getInstance().setCore(game);

                } catch (JSONException e) {
                    e.printStackTrace();
                }//catch
            }//if

            System.out.println("Game Started");

        } else if(event.equals("leftGame")) {
            User u = new User(new JSONObject(messageEvent.getData()));
            Coordinator core = Controller.getInstance().getCore();
            if(core instanceof Lobby)
                ((Lobby)core).removeUser(u);

            if(u.getHost() && !WebUtilities.INSTANCE.isHost()) {
                WebUtilities.INSTANCE.leaveNetworkGame();
                Controller.getInstance().loadNetworkBrowser();
                core.gui.showErrorDialog("User Cancelled Game", u.getUsername() + " has cancelled the Game.");
            }//if


            System.out.println("[GAME HANDLER]: " + u.getUsername() + " left the Game");

            if(core instanceof Game && util.loggedIn())
                core.gui.showInformationDialog("User Left Game", u.getUsername() + " has left the Game.");

        }//else
    }

    public void onComment(String comment) throws Exception {

    }

    public void onError(Throwable t) {
        t.printStackTrace();
    }

    /**
     * Action listener for when the user logs out.
     * @param loggedIn if the user is loggedIn
     */
    @Override
    public void onLogout(boolean loggedIn) {
        this.loggedIn = loggedIn;

        if(!loggedIn) {
            try {
                es.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }//onLogout

}//class
