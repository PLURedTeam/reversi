package plu.red.reversi.core.network;

import plu.red.reversi.core.Coordinator;
import plu.red.reversi.core.listener.INetworkListener;
import plu.red.reversi.core.util.User;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

/**
 * Created by Andrew on 4/13/2017.
 *
 * Polls the server to keep the user logged in every 60 seconds.
 */
public class SessionHandler implements Runnable, INetworkListener {

    //Fields
    private Client client;
    private User user;
    private String baseURI = "http://localhost:8080/reversi/"; //Just temp, will change with production server
    private boolean loggedIn = true;

    /**
     * Constucts a new SessionHandler instance
     * @param client The web client used to call the server
     * @param user The user object for the current logged in user
     */
    public SessionHandler(Client client, User user) {
        this.client = client;
        this.user = user;
        Coordinator.addListenerStatic(this); //Add the listener
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
