package plu.red.reversi.server.Managers;

import plu.red.reversi.server.listener.ISessionListener;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Andrew on 3/26/2017.
 */
public class SessionManager implements Runnable {

    public static SessionManager INSTANCE = new SessionManager();

    private HashSet<ISessionListener> listeners = new HashSet<ISessionListener>();
    private ConcurrentHashMap<Integer, Long> sessions;
    private int sessionIncrementer = 1000;

    /**
     * Constructor for the SessionManager
     */
    public SessionManager() {
        System.out.println("[SESSION MANAGER] SESSION MANAGER STARTED");
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
        System.out.println("[SESSION MANAGER] ADDING SESSION: " + session);
        return session;
    }//addSession

    /**
     * Removes a session from the manager
     * @param sessionID the session id to remove
     */
    public void removeSession(int sessionID) {
        System.out.println("[SESSION MANAGER] REMOVING SESSION: " + sessionID);
        sessions.remove(sessionID);
        notifyListeners(sessionID);
    }//sessionID

    /**
     * Keeps a session alive
     * @param sessionID the session id of the user
     */
    public void keepSessionAlive(int sessionID) {
        sessions.put(sessionID, System.currentTimeMillis());
    }//keepSessionAlive


    /**
     * Notifies listeners that a user has logged out of the server
     *
     * @param sessionID the sessionID of the user to remove
     */
    protected final void notifyListeners(int sessionID) {
        for(ISessionListener listener : listeners) listener.endSession(sessionID);
    }//notifyListeners


    /**
     * Thread that looks for expired sessions and removes them
     */
    public void run() {

        while(true) {
            for(Integer sessionID: sessions.keySet()) {
                if(sessions.get(sessionID) < System.currentTimeMillis() - 65000) {
                    sessions.remove(sessionID);
                    UserManager.INSTANCE.timedOut(sessionID);
                    System.out.println("[SESSION MANAGER] SESSION TIMED OUT: " + sessionID);
                    removeSession(sessionID);
                }//if

            }//for
            try { Thread.sleep(5000);}
            catch (InterruptedException e) { e.printStackTrace();}
        }//while
    }//run
}//sessionManager
