package plu.red.reversi.core;

import plu.red.reversi.core.command.ChatCommand;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.listener.IChatListener;
import plu.red.reversi.core.listener.ICommandListener;
import plu.red.reversi.core.listener.IListener;
import plu.red.reversi.core.listener.INetworkListener;
import plu.red.reversi.core.network.WebUtilities;
import plu.red.reversi.core.util.ChatMessage;

import java.util.HashSet;

/**
 * Glory to the Red Team.
 *
 * Abstract Coordinator Concept. The abstract Coordinator class is the base for all Coordinator-type objects. It contains
 * method stubs for Coordinator-specific actions as well as basic methods to be used in all Controllers.
 */
public abstract class Coordinator {

    // ****************
    //  Listener Logic
    // ****************

    // A Set of all available Listeners, persistent on a Coordinator level
    protected final HashSet<IListener> listenerSet = new HashSet<>();

    // A Set of all available Listeners, persistent on a global level
    protected static final HashSet<IListener> listenerSetStatic = new HashSet<>();

    /**
     * Registers a <code>listener</code> to this Coordinator. All <code>listeners</code> that are registered to this
     * Coordinator will receive signals via their individual methods when certain actions happen, depending on the
     * specific <code>listener</code>.
     *
     * @param listener Object that implements an extension of IListener
     */
    public void addListener(IListener listener) {
        listenerSet.add(listener);
    }

    /**
     * Registers a <code>listener</code> to the global list. All <code>listeners</code> that are registered to the
     * global list will receive signals whenever any Coordinators send signals. The static list will remain constant
     * even when Coordinators are swapped out of the main Controller.
     *
     * @param listener Object that implements an extension of IListener
     */
    public static void addListenerStatic(IListener listener) {
        listenerSetStatic.add(listener);
    }

    /**
     * Unregisters a specified <code>listener</code> from this Coordinator. The <code>listener</code> object that is
     * unregistered will no longer receive signals when events happen. If the given <code>listener</code> object is
     * not currently registered to this Coordinator, nothing happens.
     *
     * @param listener Object that implements an extension of IListener
     */
    public void removeListener(IListener listener) {
        listenerSet.remove(listener);
    }

    /**
     * Unregisters a specified <code>listener</code> from the global list. The <code>listener</code> object that is
     * unregistered will no longer receive signals when events happen. If the given <code>listener</code> object is not
     * currently registered to the global list, nothing happens.
     *
     * @param listener Object that implements an extension of Ilistener
     */
    public static void removeListenerStatic(IListener listener) {
        listenerSetStatic.remove(listener);
    }

    /**
     * Notifies that a Command has been accepted. Iterates through and tells every ICommandListener that has been
     * registered to this Coordinator that a Command has been validated and accepted.
     *
     * @param cmd Command object that was accepted
     */
    protected final void notifyCommandListeners(Command cmd) {
        for(IListener listener : listenerSet)
            if(listener instanceof ICommandListener) ((ICommandListener)listener).commandApplied(cmd);
        for(IListener listener : listenerSetStatic)
            if(listener instanceof ICommandListener) ((ICommandListener)listener).commandApplied(cmd);
    }

    /**
     * Notifies that a the user has logged out of the server. Iterates through and tells every INetworkListener
     * that has been registered to this handler that the loggedIn status has changed.
     *
     * @param loggedIn If the user is logged in to the server
     */
    public final void notifyLoggedInListeners(boolean loggedIn) {
        for(IListener listener : listenerSet)
            if(listener instanceof INetworkListener) ((INetworkListener)listener).onLogout(loggedIn);
        for(IListener listener : listenerSetStatic)
            if(listener instanceof INetworkListener) ((INetworkListener)listener).onLogout(loggedIn);
    }//notifyChatListeners

    /* Notifies that a ChatMessage has been received from the server. Iterates through and tells every IChatListener
     * that has been registered to this Coordinator that a ChatMessage has been received.
     *
     * @param msg ChatMessage object that was received
     */
    public final void notifyChatListeners(ChatMessage msg) {
        for(IListener listener : listenerSet)
            if(listener instanceof IChatListener) ((IChatListener)listener).onChat(msg);
        for(IListener listener : listenerSetStatic)
            if(listener instanceof IChatListener) ((IChatListener)listener).onChat(msg);
    }



    // ***************
    //  Command Logic
    // ***************

    /**
     * Accept a Command object from somewhere else in the program. After accepting, parse the <code>cmd</code> and
     * route it elsewhere if need be.
     *
     * @param cmd Commmand object to accept
     * @return True if the <code>cmd</code> was valid and accepted, false if it was rejected and no action occurred
     */
    public synchronized boolean acceptCommand(Command cmd) {

        switch(cmd.source) {
            case SERVER:
                // Were good, trust the Server (for now, may change later)
                break;
            default:
            case CLIENTSIDE_ONLY:
            case CLIENT: // Came from the Client; validate it
                if(!cmd.isValid(this)) return false;
        }

        if(cmd.source == Command.Source.CLIENT && WebUtilities.INSTANCE.loggedIn()) {
            if(cmd instanceof ChatCommand) {
                // android requires that network I/O occurs on a new thread.
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        WebUtilities.INSTANCE.sendChat(((ChatCommand)cmd).message);
                    }
                }).start();
            }
            else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        WebUtilities.INSTANCE.sendMove(cmd);
                    }
                });
            }
        }

        // Perform the Command's action/s
        boolean successful = parseCommand(cmd);
        if(successful) {
            // Notify listeners that a Command has been accepted
            notifyCommandListeners(cmd);
        }

        return successful;
    }

    /**
     * Parse Command for action. Check the type of Command given and perform an action dependant upon that type.
     *
     * @param cmd Command object to parse
     * @return True if the Command's actions were successful
     */
    protected abstract boolean parseCommand(Command cmd);



    // *************
    //  Other Logic
    // *************

    /**
     * Perform any cleanup operations that are needed, such as removing listeners that are not automatically cleaned up.
     */
    public abstract void cleanup();

    /**
     * Reference to the GUI implementing IMainGUI for displaying the program.
     */
    public IMainGUI gui;

    /**
     * Reference to the master Controller that is being used for this program.
     */
    public Controller master;

    /**
     * Abstract Constructor. Base constructor that accepts an IMainGUI object that this Coordinator can manipulate.
     *
     * @param master Controller object that is the master Controller for this program
     * @param gui IMainGUI object that displays for the program
     */
    public Coordinator(Controller master, IMainGUI gui) {
        this.master = master;
        this.gui = gui == null ? new IMainGUI.NullGUI() : gui;
    }

    /**
     * Used to tell the controller to refresh content in some way.
     * By default, nothing happens.
     */
    public void refresh() {

    }
}
