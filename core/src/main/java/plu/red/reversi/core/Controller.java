package plu.red.reversi.core;

import plu.red.reversi.core.lobby.Lobby;
import plu.red.reversi.core.util.ChatLog;
import plu.red.reversi.core.util.ChatMessage;

/**
 * Glory to the Red Team.
 *
 * Master Controller Concept. The abstract Controller class is the base class for all Controller-type objects. It
 * contains methods and stubs for controlling the state of the program, and switching between various sub-states and
 * Coordinators.
 */
public abstract class Controller {

    private static Controller INSTANCE = null;

    /**
     * Initialize the master Controller.
     *
     * @param controller Controller to init with
     */
    public static void init(Controller controller) { INSTANCE = controller; }

    /**
     * Gets the global instance of the master Controller.
     *
     * @return Global master Controller instance
     */
    public static Controller getInstance() { return INSTANCE; }

    public final IMainGUI gui;

    protected Coordinator core = null;
    protected ChatLog chat = new ChatLog();


    public Controller(IMainGUI gui) {
        if(gui == null) this.gui = new IMainGUI.NullGUI();
        else this.gui = gui;
        chat.create(ChatMessage.Channel.GLOBAL);
        this.gui.setController(this);
        setCore(new Lobby(this, this.gui));
    }

    public Controller(IMainGUI gui, Coordinator core) {
        this.gui = gui;
        chat.create(ChatMessage.Channel.GLOBAL);
        gui.setController(this);
        setCore(core);
    }


    protected final void setCore(Coordinator core) {
        if(this.core != null) this.core.cleanup();
        this.core = core;
        gui.updateGUIMajor();
    }

    public Coordinator getCore() { return core; }

    public ChatLog getChat() { return chat; }

    public abstract void createIntoLobby(boolean networked);
    public abstract void loadIntoLobby(boolean networked);
    public abstract void startGame() throws IllegalStateException;
    public abstract void saveGame() throws IllegalStateException;
}
