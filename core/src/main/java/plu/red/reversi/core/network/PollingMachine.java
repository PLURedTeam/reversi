package plu.red.reversi.core.network;

import org.glassfish.jersey.media.sse.EventInput;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import plu.red.reversi.core.listener.IChatListener;
import plu.red.reversi.core.util.User;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * Polls the server to determine if new information can be retrieved for chat commands,
 * board commands, and keeps the current session alive with the server. Runs in a new thread
 * that is independent of the rest of the program.
 *
 * Created by Andrew on 3/22/2017.
 */
public class PollingMachine {

    //Fields
    private WebUtilities util;
    private Client client;
    private User user;
    private String baseURI = "http://localhost:8080/reversi/"; //Just temp, will change with production server

    /**
     * Constucts a new PollingMachine instance
     * @param util
     * @param client
     * @param user
     */
    public PollingMachine(WebUtilities util, Client client, User user) {
        //set fields for user
        this.util = util;
        this.client = client;
        this.user = user;
        //start polling threads
        new Thread(new SessionPoll()).start();
        new Thread(new ChatPoll()).start();
        new Thread(new GamePoll()).start();
    }//constructor

    /**
     * Polls the server in a new thread to keep the current users
     * session active on the server
     */
    private class SessionPoll implements Runnable {
        public void run() {
            while(util.loggedIn()) {
                System.out.println("I am polling for the session!!!");
                WebTarget target = client.target( baseURI + "keep-session-alive/" + user.getSessionID());
                Response response = target.request().get();
                try { Thread.sleep(5000);}
                catch (InterruptedException e) {e.printStackTrace(); }
            }//while
            System.out.println("Session Thread is Dead!");
        }//run
    }//SessionPoll

    /**
     * Polls the server in a new thread to get new chat messages
     * from the server
     */
    private class ChatPoll implements Runnable {
        public void run() {

            System.out.println("The chat poll is running");


            Client chatClient = ClientBuilder.newBuilder().register(SseFeature.class).build();
            WebTarget target = chatClient.target(baseURI + "chat/global");

            EventInput eventInput = target.request().get(EventInput.class);

            System.out.println("Before the fucking loop");
            System.out.println("EventInput: " + eventInput.isClosed());

            while(!eventInput.isClosed()) {
                System.out.println("I am in the loop");

                final InboundEvent inboundEvent = eventInput.read();

                System.out.println("I got a god damn message");

                if(inboundEvent == null) break;
                System.out.println("Got " + inboundEvent.readData(String.class) + " from broadcast");
            }//while

            System.out.println("OUT OF THE LOOP");



        }//run
    }//ChatPoll

    /**
     * Polls the server in a new thread to get new moves
     * that have been played during a game
     */
    private class GamePoll implements Runnable {
        public void run() {

        }//run
    }//GamePoll

}//pollingMachine
