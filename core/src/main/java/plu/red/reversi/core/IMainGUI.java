package plu.red.reversi.core;

/**
 * Glory to the Red Team.
 *
 * Interface which describes actions the main GUI should be able to take. The IMainGUI interface is used to communicate
 * with a GUI implementation from the Client master controller.
 */
public interface IMainGUI {

    /**
     * Client Setter. Sets what Client master controller this GUI is displaying for. Usually only used by the Client
     * class's constructor.
     *
     * @param client Client object to set
     */
    void setClient(Client client);

    /**
     * GUI Display Updater. Called from a Client object when the Client object has changed significantly and the GUI
     * needs to be updated to accommodate. Example usages include when the <code>core</code> Controller of a Client
     * object is swapped out. Causes the entire GUI to be recreated.
     */
    void updateGUIMajor();

    /**
     * GUI Display Updater. Called from a Client object when small changes have been made in the Client and the GUI
     * needs to be updated to reflect these changes. Example usages include when a Lobby changes the amount of Player
     * Slots it has. Causes small portions of the GUI to be recalculated and redrawn.
     */
    void updateGUIMinor();

    /**
     * Save Dialog Display Method. Shows a Save Dialog to the user, which queries what name to save a Game as. Can be
     * cancelled.
     *
     * @return String name chosen, or null if the user cancelled
     */
    String showSaveDialog();

    /**
     * Load Dialog Display Method. Shows a Load Dialog o the user, which queries what Game to load from existing saved
     * Games. Can be cancelled.
     *
     * @return String name chosen, or null if the user cancelled
     */
    String showLoadDialog();
}