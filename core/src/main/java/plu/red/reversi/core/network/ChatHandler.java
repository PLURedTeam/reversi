package plu.red.reversi.core.network;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.MessageEvent;
import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.core.Coordinator;
import plu.red.reversi.core.listener.INetworkListener;
import plu.red.reversi.core.util.ChatMessage;

import java.io.IOException;
import java.net.URI;

/**
 * Created by Andrew on 4/13/2017.
 *
 * Handles the Server Sent Events for chat. Notifies the chat listeners when a new chat message is received
 * from the server.
 */
public class ChatHandler implements Runnable, INetworkListener, EventHandler {

    //fields
    private String baseURI; //Just temp, will change with production server
    private WebUtilities util;
    private boolean loggedIn = true;

    private EventSource es;

    /**
     * Constructor
     */
    public ChatHandler(WebUtilities u, String baseURI) {
        util = u; //Set the WebUtilities
        this.baseURI = baseURI;
        Coordinator.addListenerStatic(this); //Add the listener
    }//constructor

    /**
     * Polls the server in a new thread to get new chat messages
     * from the server
     */
    public void run() {
        System.out.println("[CHAT HANDLER]: Thread Started");

        try {
            es = new EventSource.Builder(this, new URI(baseURI + "chat"))
                    .build();

            es.start();

        } catch(Exception e) {
            // should not happen
            throw new RuntimeException();
        }

        //Coordinator.removeListenerStatic(this);
        System.out.println("[CHAT HANDLER]: Thread Finished");
    }//run

    public void onOpen() throws Exception {
        System.out.println("Chat handler has begun listening!");
    }

    public void onClosed() throws Exception {
        System.out.println("Chat handler has stopped listening!");

        Coordinator.removeListenerStatic(this);
    }

    public void onMessage(String event, MessageEvent messageEvent) throws Exception {

        if(event.equals("message")) {

            ChatMessage message;
            try {
                message = new ChatMessage().fromJSON(new JSONObject(messageEvent.getData()));
            } catch (Exception e) {
                return;
            }//catch

            //Notify listeners
            plu.red.reversi.core.Client.getInstance().getCore().notifyChatListeners(message);
            System.out.println("Got [" + message.message + "] from broadcast");
        }
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
