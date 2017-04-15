package plu.red.reversi.core.listener;

/**
 * Created by Andrew on 4/14/2017.
 *
 * Interface for when the user logs out from the server
 */
public interface INetworkListener extends IListener {

    /**
     * Called when a use logs out from the server
     *
     * @param loggedIn if the user is loggedIn
     */
    void onLogout(boolean loggedIn);

}//interface
