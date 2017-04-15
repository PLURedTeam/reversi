package plu.red.reversi.client.network;

import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.media.sse.EventInput;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
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
        u.addListener(this);
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

            notifyChatListeners(message);
            System.out.println("Got [" + message.message + "] from broadcast");
        }//while

        System.out.println("[CHAT HANDLER]: Thread Finished");
    }//run

    @Override
    public void onLogout(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }//onLogout





    // ****************
    //  Listener Logic
    // ****************

    // A Set of all available Listeners
    protected final HashSet<IListener> listenerSet = new HashSet<>();

    /**
     * Registers a <code>listener</code> to this Coordinator. All <code>listeners</code> that are registered to this
     * Coordinator will receive signals via their individual methods when certain actions happen, dependending on the
     * specific <code>listener</code>.
     *
     * @param listener Object that implements an extension of IListener
     */
    public void addListener(IListener listener) {
        listenerSet.add(listener);
    }

    /**
     * Unregisters a specified <code>listener</code> from this Coordinator. The <code>listener</code> object that is
     * unregistered will no longer receive signals when events happen. If the given <code>listener</code> object is
     * not currently registered to this Coordinator, nothing happens.
     *
     * @param listener Object that implements an extension of IListener
     */
    public void removeListener(IListener listener) {
        listenerSet.remove(listener);
    }

    /**
     * Notifies that a ChatMessage has been received. Iterates through and tells every IChatListener that has been
     * registered to this handler that a ChatMessage has been received.
     *
     * @param message ChatMessage received from the server
     */
    protected final void notifyChatListeners(ChatMessage message) {
        for(IListener listener : listenerSet)
            if(listener instanceof IChatListener) ((IChatListener)listener).onChat(message);
    }//notifyChatListeners




}//class
