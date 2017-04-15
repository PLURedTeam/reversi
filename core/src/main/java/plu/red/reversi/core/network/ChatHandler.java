package plu.red.reversi.core.network;

import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.media.sse.EventInput;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import plu.red.reversi.core.Coordinator;
import plu.red.reversi.core.listener.IChatListener;
import plu.red.reversi.core.listener.IListener;
import plu.red.reversi.core.listener.INetworkListener;
import plu.red.reversi.core.util.ChatMessage;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.util.HashSet;

/**
 * Created by Andrew on 4/13/2017.
 */
public class ChatHandler implements Runnable, INetworkListener {

    //fields
    private String baseURI = "http://localhost:8080/reversi/"; //Just temp, will change with production server
    private WebUtilities util;
    private boolean loggedIn;

    /**
     * Default Constructor
     */
    public ChatHandler(WebUtilities u) {
        util = u;
        Coordinator.addListenerStatic(this);
    }//constructor

    /**
     * Polls the server in a new thread to get new chat messages
     * from the server
     */
    public void run() {
        System.out.println("[CHAT HANDLER]: Thread Started");

        //Build the client and set the target
        Client chatClient = ClientBuilder.newBuilder().register(SseFeature.class).build();
        WebTarget target = chatClient.target(baseURI + "chat/global");

        //Create the eventInput listener
        EventInput eventInput = target.request().get(EventInput.class);
        while (!eventInput.isClosed() && loggedIn) {
            final InboundEvent inboundEvent = eventInput.read();
            if (inboundEvent == null) break;

            ChatMessage message;
            try {
                message = new ChatMessage().fromJSON(new JSONObject(inboundEvent.readData(String.class)));
            } catch (Exception e) {
                continue; //Don't notify listeners
            }//catch

            plu.red.reversi.core.Client.getInstance().getCore().notifyChatListeners(message);
            System.out.println("Got [" + message.message + "] from broadcast");
        }//while

        System.out.println("[CHAT HANDLER]: Thread Finished");
    }//run

    @Override
    public void onLogout(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }//onLogout





}//class
