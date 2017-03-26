package plu.red.reversi.client.network;

import plu.red.reversi.core.util.User;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * Polls the server to determine if new information can be retrieved for chat commands,
 * board commands, and keeps the current session alive with the server. Runs in a new thread
 * that is independent of the rest of the program.
 *
 * Created by Andrew on 3/22/2017.
 */
public class PollingMachine implements Runnable {

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
        this.util = util;
        this.client = client;
        this.user = user;
    }//constructor

    /**
     * Keeps the current session alive by pinging the database to tell
     * the server that the user is still there
     */
    public void run() {

        while(util.loggedIn) {
            System.out.println("Calling keep session Alive: " + System.currentTimeMillis());

            WebTarget target = client.target( baseURI + "keep-session-alive/" + user.getSessionID());
            Response response = target.request().get();
            try {
                Thread.sleep(5000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }//while


    }//run






}//pollingMachine
