package plu.red.reversi.core.browser;

import plu.red.reversi.core.Controller;
import plu.red.reversi.core.Coordinator;
import plu.red.reversi.core.IMainGUI;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.network.WebUtilities;
import plu.red.reversi.core.util.GamePair;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;

/**
 * Glory to the Red Team.
 *
 * Browser Coordinator. Object used as a Coordinator when browsing available games. A Browser Coordinator contains all
 * a cache of Games from the Server, that can be refreshed.
 */
public class Browser extends Coordinator implements ListModel<GamePair> {

    ArrayList<GamePair> games = null;

    public Browser(Controller master, IMainGUI gui) {
        super(master, gui);
    }

    public void refresh() {
        games = WebUtilities.INSTANCE.getOnlineGames();
        System.out.println("Refreshed: " + (games == null));
        gui.updateGUIMinor();
    }

    public boolean isConnected() { return games != null; }

    /**
     * Parse Command for action. Check the type of Command given and perform an action dependant upon that type.
     *
     * @param cmd Command object to parse
     * @return True if the Command's actions were successful
     */
    @Override
    protected boolean parseCommand(Command cmd) {
        // NOOP
        return true;
    }

    /**
     * Perform any cleanup operations that are needed, such as removing listeners that are not automatically cleaned up.
     */
    @Override
    public void cleanup() {

    }

    /**
     * Returns the length of the list.
     *
     * @return the length of the list
     */
    @Override
    public int getSize() {
        return games == null ? 0 : games.size();
    }

    /**
     * Returns the value at the specified index.
     *
     * @param index the requested index
     * @return the value at <code>index</code>
     */
    @Override
    public GamePair getElementAt(int index) {
        return games == null ? null : games.get(index);
    }

    /**
     * Adds a listener to the list that's notified each time a change
     * to the data model occurs.
     *
     * @param l the <code>ListDataListener</code> to be added
     */
    @Override
    public void addListDataListener(ListDataListener l) {

    }

    /**
     * Removes a listener from the list that's notified each time a
     * change to the data model occurs.
     *
     * @param l the <code>ListDataListener</code> to be removed
     */
    @Override
    public void removeListDataListener(ListDataListener l) {

    }
}
