package plu.red.reversi.core;

/**
 * Glory to the Red Team.
 *
 * Interface which describes actions the main GUI should be able to take. The IMainGUI interface is used to communicate
 * with a GUI implementation from the Client master controller.
 */
public interface IMainGUI {

    /**
     * Controller Setter. Sets what master Controller this GUI is displaying for. Usually only used by the Controller
     * class's constructor.
     *
     * @param controller Controller object to set
     */
    void setController(Controller controller);

    /**
     * GUI Display Updater. Called from a Client object when the Client object has changed significantly and the GUI
     * needs to be updated to accommodate. Example usages include when the <code>core</code> Coordinator of a Client
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
     * Load Dialog Display Method. Shows a Load Dialog to the user, which queries what Game to load from existing saved
     * Games. Can be cancelled.
     *
     * @return String name chosen, or null if the user cancelled
     */
    String showLoadDialog();

    /**
     * Information Dialog Display Method. Shows an Information Dialog to the user, displaying the given information.
     *
     * @param title String <code>title</code> of the Dialog
     * @param body String <code>body</code> of the Dialog
     */
    void showInformationDialog(String title, String body);

    /**
     * Error Dialog Display Method. Shows an Error Dialog to the user, displaying the given information.
     *
     * @param title String <code>title</code> of the Dialog
     * @param body String <code>body</code> of the Dialog
     */
    void showErrorDialog(String title, String body);

    /**
     * Query Dialog Display Method. Shows a Query Dialog to the user, displaying a question and expecting a response.
     *
     * @param title String <code>title</code> of the Dialog
     * @param body String <code>body</code> of the Dialog
     * @return String response, or <code>null</code> for no response
     */
    String showQueryDialog(String title, String body);


    public static class NullGUI implements IMainGUI {
        @Override public void setController(Controller controller) {}
        @Override public void updateGUIMajor() {}
        @Override public void updateGUIMinor() {}
        @Override public String showSaveDialog() { return null; }
        @Override public String showLoadDialog() { return null; }
        @Override public void showInformationDialog(String title, String body) {}
        @Override public void showErrorDialog(String title, String body) {}
        @Override public String showQueryDialog(String title, String body) { return null; }
    }
}
