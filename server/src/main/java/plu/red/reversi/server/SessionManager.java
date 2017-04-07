package plu.red.reversi.server;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Andrew on 3/26/2017.
 */
public class SessionManager implements Runnable {

    public static SessionManager INSTANCE = new SessionManager();

    private ConcurrentHashMap<Integer, Long> sessions;
    private int sessionIncrementer = 1000;

    /**
     * Constructor for the SessionManager
     */
    public SessionManager() {
        sessions = new ConcurrentHashMap<Integer, Long>();
    }//constructor

    /**
     * Adds a session to the SessionManager
     * @return the sessionID
     */
    public int addSession() {
        int session = sessionIncrementer;
        sessions.put(session, System.currentTimeMillis());
        sessionIncrementer++;

        System.out.println("Adding Session: " + session);

        return session;
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
            System.out.println("I am looping through sessions " + System.currentTimeMillis());

            for(Integer sessionID: sessions.keySet()) {
                if(sessions.get(sessionID) < System.currentTimeMillis() - 30000) {
                    sessions.remove(sessionID);
                    UserManager.INSTANCE.timedOut(sessionID);
                    System.out.println("Removing Session: " + sessionID);
                }//if

            }//for
            try { Thread.sleep(5000);}
            catch (InterruptedException e) { e.printStackTrace();}
        }//while
    }//run
}//sessionManager
