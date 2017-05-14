package plu.red.reversi.core.network;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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
    private OkHttpClient okh;
    private User user;
    private String baseURI; //Just temp, will change with production server
    private boolean loggedIn = true;

    /**
     * Constucts a new SessionHandler instance
     * @param okh The web client used to call the server
     * @param user The user object for the current logged in user
     */
    public SessionHandler(OkHttpClient okh, User user, String baseURI) {
        this.okh = okh;
        this.user = user;
        this.baseURI = baseURI;
        Coordinator.addListenerStatic(this); //Add the listener
    }//constructor

    /**
     * Polls the server once every 60 seconds to keep the session alive on the server
     */
    public void run() {
        System.out.println("[SESSION HANDLER]: Thread Started");
        while(loggedIn) {
            try {

                Request req = new Request.Builder()
                        .url(baseURI + "keep-session-alive/" + user.getSessionID())
                        .build();

                okh.newCall(req).execute().close();

                Thread.sleep(30000);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }//while
        System.out.println("[SESSION HANDLER]: Thread Finished");
    }//run

    @Override
    public void onLogout(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }//onLogout
}//class
