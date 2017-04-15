package plu.red.reversi.core.network;

import plu.red.reversi.core.Coordinator;
import plu.red.reversi.core.listener.INetworkListener;
import plu.red.reversi.core.util.User;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

/**
 * Created by Andrew on 4/13/2017.
 */
public class SessionHandler implements Runnable, INetworkListener {

    //Fields
    private WebUtilities util;
    private Client client;
    private User user;
    private String baseURI = "http://localhost:8080/reversi/"; //Just temp, will change with production server
    private boolean loggedIn;

    /**
     * Constucts a new SessionHandler instance
     * @param util
     * @param client
     * @param user
     */
    public SessionHandler(WebUtilities util, Client client, User user) {
        //set fields for user
        this.util = util;
        this.client = client;
        this.user = user;
        Coordinator.addListenerStatic(this);
    }//constructor

    /**
     * Polls the server once every 60 seconds to keep the session alive on the server
     */
    public void run() {
        System.out.println("[SESSION HANDLER]: Thread Started");
        while(loggedIn) {
            WebTarget target = client.target( baseURI + "keep-session-alive/" + user.getSessionID());
            target.request().get();
            try { Thread.sleep(60000);}
            catch (InterruptedException e) {e.printStackTrace(); }
        }//while
        System.out.println("[SESSION HANDLER]: Thread Finished");
    }//run

    @Override
    public void onLogout(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }//onLogout
}//class
