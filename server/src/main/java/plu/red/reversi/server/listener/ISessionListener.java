package plu.red.reversi.server.listener;

import plu.red.reversi.core.listener.IListener;

/**
* Created by Andrew on 4/23/2017.
*/
public interface ISessionListener extends IListener {

    /**
     * Called when a session ends on the server
     *
     * @param sessionID the session id of the user
     */
    void endSession(int sessionID);

}//interface
