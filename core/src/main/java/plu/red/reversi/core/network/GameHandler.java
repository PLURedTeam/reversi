package plu.red.reversi.core.network;

import org.codehaus.jettison.json.JSONException;
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
import plu.red.reversi.core.util.DataMap;
import plu.red.reversi.core.util.User;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

/**
 * Created by Andrew on 4/13/2017.
 */
public class GameHandler implements Runnable, INetworkListener {

    //fields
    private String baseURI; //Just temp, will change with production server
    private WebUtilities util;
    private boolean loggedIn = true;
    private int gameID;

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

                try {
                    JSONObject obj = new JSONObject(inboundEvent.readData(String.class));

                    Command c = Command.fromJSON(obj);

                    Controller.getInstance().getCore().acceptCommand(c);
                    System.out.println("[GAME HANDLER]: " + c.source + " moved");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if(inboundEvent.getName().equals("start")) {

                if(!WebUtilities.INSTANCE.isHost()) {
                    Game game;

                    try {
                        JSONObject json = new JSONObject(inboundEvent.readData(String.class));
                        DataMap map = new DataMap(json);

                        game = new Game(Controller.getInstance(), Controller.getInstance().gui, map);

                        Controller.getInstance().getChat().clear(ChatMessage.Channel.lobby(game.getName()));
                        Controller.getInstance().setCore(game);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }//catch
                }//if

                System.out.println("Game Started");

            } else if(inboundEvent.getName().equals("leftGame")) {
                User u = inboundEvent.readData(User.class);
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
