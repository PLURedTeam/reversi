package plu.red.reversi.server;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Andrew on 3/26/2017.
 */
public class SessionManager implements Runnable {

    public static SessionManager INSTANCE = new SessionManager();

    ConcurrentHashMap<Integer, Long> sessions;

    /**
     * Constructor for the SessionManager
     */
    public SessionManager() {
        sessions = new ConcurrentHashMap<Integer, Long>();
    }//constructor

    /**
     * Adds a session to the SessionManager
     * @param sessionID the sessionID of the user
     */
    public void addSession(int sessionID) {
        sessions.put(sessionID, System.currentTimeMillis());
    }//addSession

    /**
     * Removes a session from the manager
     * @param sessionID the session id to remove
     */
    public void removeSession(int sessionID) {
        sessions.remove(sessionID);
    }//sessionID

    /**
     * Keeps a session alive
     * @param sessionID the session id of the user
     */
    public void keepSessionAlive(int sessionID) {
        sessions.put(sessionID, System.currentTimeMillis());
    }//keepSessionAlive

    /**
     * Thread that looks for expired sessions
     */
    public void run() {

        while(true) {
            for(Integer sessionID: sessions.keySet()) {
                if(sessions.get(sessionID) > System.currentTimeMillis() + 30000) {
                    sessions.remove(sessionID);
                    UserManager.INSTANCE.timedOut(sessionID);
                }//if

            }//for
            try { Thread.sleep(5000);}
            catch (InterruptedException e) { e.printStackTrace();}
        }//true
    }//run
}//sessionManager
