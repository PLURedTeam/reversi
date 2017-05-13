package plu.red.reversi.core;

/**
 * Created by daniel on 5/13/17.
 */
public abstract class Browser extends Coordinator {
    /**
     * Abstract Constructor. Base constructor that accepts an IMainGUI object that this Coordinator can manipulate.
     *
     * @param master Controller object that is the master Controller for this program
     * @param gui    IMainGUI object that displays for the program
     */
    public Browser(Controller master, IMainGUI gui) {
        super(master, gui);
    }
}
