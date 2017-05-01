package plu.red.reversi.core.network;

import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.media.sse.EventInput;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import plu.red.reversi.core.Controller;
import plu.red.reversi.core.Coordinator;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.listener.INetworkListener;
import plu.red.reversi.core.lobby.Lobby;
import plu.red.reversi.core.util.ChatMessage;
import plu.red.reversi.core.util.User;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

/**
 * Created by Andrew on 4/13/2017.
 */
public class GameHandler implements Runnable, INetworkListener {

    //fields
    private String baseURI = "http://localhost:8080/reversi/"; //Just temp, will change with production server
    private WebUtilities util;
    private boolean loggedIn = true;
    private int gameID;

    /**
     * Constructor
     */
    public GameHandler(WebUtilities u, int id) {
        util = u; //Set the WebUtilities
        gameID = id;
        Coordinator.addListenerStatic(this); //Add the listener
    }//constructor

    /**
     * Polls the server in a new thread to get new chat messages
     * from the server
     */
    public void run() {
        System.out.println("[GAME HANDLER]: Thread Started");

        //Build the client and set the target
        Client chatClient = ClientBuilder.newBuilder().register(SseFeature.class).build();
        WebTarget target = chatClient.target(baseURI + "game/" + gameID);

        //Create the eventInput listener
        EventInput eventInput = target.request().get(EventInput.class);
        while (!eventInput.isClosed() && loggedIn) {
            final InboundEvent inboundEvent = eventInput.read();
            if (inboundEvent == null) break;

            if(inboundEvent.getName().equals("join")) {
                User u = inboundEvent.readData(User.class);

                System.out.println("[GAME HANDLER]: " + u.getUsername() + " Connected to the Game");

                Coordinator core = Controller.getInstance().getCore();
                if(core instanceof Lobby)
                    ((Lobby)core).joinUser(u);

            } else if(inboundEvent.getName().equals("move")) {

                Command c = inboundEvent.readData(Command.class);

                //TODO: Send the move to the game to update

                System.out.println("[GAME HANDLER]: " + c.source + " moved");

            } else if(inboundEvent.getName().equals("start")) {

                Game g = inboundEvent.readData(Game.class);

                //TODO: Create the game on the client so that it can start

            } else if(inboundEvent.getName().equals("leftGame")) {
                User u = inboundEvent.readData(User.class);

                Coordinator core = Controller.getInstance().getCore();
                if(core instanceof Lobby)
                    ((Lobby)core).removeUser(u);

                System.out.println("[GAME HANDLER]: " + u.getUsername() + " left the Game");

            }//else

        }//while

        System.out.println("[GAME HANDLER]: Thread Finished");
    }//run

    /**
     * Action listener for when the user logs out.
     * @param loggedIn if the user is loggedIn
     */
    @Override
    public void onLogout(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }//onLogout

}//class
