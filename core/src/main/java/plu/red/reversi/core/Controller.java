package plu.red.reversi.core;

import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.listener.*;

import java.util.HashSet;

/**
 * Glory to the Red Team.
 *
 * Abstract Controller Concept. The abstract Controller class is the base for all Controller-type objects. It contains
 * method stubs for Controller-specific actions as well as basic methods to be used in all Controllers.
 */
public abstract class Controller {

    // ****************
    //  Listener Logic
    // ****************

    // A Set of all available Listeners
    protected final HashSet<IListener> listenerSet = new HashSet<>();

    /**
     * Registers a <code>listener</code> to this Controller. All <code>listeners</code> that are registered to this
     * Controller will receive signals via their individual methods when certain actions happen, dependending on the
     * specific <code>listener</code>.
     *
     * @param listener Object that implements an extension of IListener
     */
    public void addListener(IListener listener) {
        listenerSet.add(listener);
    }

    /**
     * Unregisters a specified <code>listener</code> from this Controller. The <code>listener</code> object that is
     * unregistered will no longer receive signals when events happen. If the given <code>listener</code> object is
     * not currently registered to this Controller, nothing happens.
     *
     * @param listener Object that implements an extension of IListener
     */
    public void removeListener(IListener listener) {
        listenerSet.remove(listener);
    }

    /**
     * Notifies that a Command has been accepted. Iterates through and tells every ICommandListener that has been
     * registered to this Controller that a Command has been validated and accepted.
     *
     * @param cmd Command object that was accepted
     */
    protected final void notifyCommandListeners(Command cmd) {
        for(IListener listener : listenerSet) {
            if(listener instanceof ICommandListener) ((ICommandListener)listener).commandApplied(cmd);
        }
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

        // Perform the Command's action/s
        boolean successful = parseCommand(cmd);
        if(successful && cmd.source == Command.Source.CLIENT) {
            // Command Successful, propogate to Server
            // TODO: Propogate Command to the Server

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
    public final IMainGUI gui;

    /**
     * Abstract Constructor. Base constructor that accepts an IMainGUI object that this Controller can manipulate.
     *
     * @param gui IMainGUI object that displays for the program
     */
    public Controller(IMainGUI gui) {
        this.gui = gui;
    }

}
