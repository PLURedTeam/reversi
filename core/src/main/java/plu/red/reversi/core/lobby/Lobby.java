package plu.red.reversi.core.lobby;

import plu.red.reversi.core.Controller;
import plu.red.reversi.core.Coordinator;
import plu.red.reversi.core.IMainGUI;
import plu.red.reversi.core.SettingsLoader;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.logic.GameLogic;
import plu.red.reversi.core.game.logic.GoLogic;
import plu.red.reversi.core.game.logic.ReversiLogic;
import plu.red.reversi.core.game.player.BotPlayer;
import plu.red.reversi.core.game.player.HumanPlayer;
import plu.red.reversi.core.game.player.NetworkPlayer;
import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.listener.INetworkListener;
import plu.red.reversi.core.listener.ISettingsListener;
import plu.red.reversi.core.network.WebUtilities;
import plu.red.reversi.core.util.ChatMessage;
import plu.red.reversi.core.util.Color;
import plu.red.reversi.core.util.DataMap;
import plu.red.reversi.core.util.User;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Glory to the Red Team.
 *
 * Lobby Coordinator. Object used as a Coordinator when setting up a game. A Lobby Coordinator contains all data and Model
 * objects relevant to setting up a game, and make them available to a GUI in order to display and change said data.
 */
public class Lobby extends Coordinator implements ISettingsListener, INetworkListener {

    // ******************
    //  Member Variables
    // ******************

    protected DataMap settings;
    protected ArrayList<PlayerSlot> playerSlots = new ArrayList<>();
    protected Game loadedGame;
    protected final GameLogic logic;
    protected final boolean networked;
    protected final String name;



    // ****************
    //  Member Methods
    // ****************

    public Lobby(Controller master, IMainGUI gui, GameLogic logic) { this(master, gui, logic, false, "Local Game"); }

    public Lobby(Controller master, IMainGUI gui, GameLogic logic, boolean networked, String name) {
        super(master, gui);
        this.logic = logic;
        this.networked = networked;
        this.name = name;
        this.loadedGame = null;

        // Create new settings
        this.settings = SettingsLoader.INSTANCE.createGameSettings();

        // Register as a ISettingsListener
        SettingsLoader.INSTANCE.addSettingsListener(this);

        // Self-Register Listener
        addListenerStatic(this);

        // Create Lobby Chat
       if(networked) master.getChat().create(ChatMessage.Channel.lobby(this.name));
    }

    public Lobby(Controller master, IMainGUI gui, Game loadedGame) { this(master, gui, loadedGame, false, "Loaded Game"); }

    public Lobby(Controller master, IMainGUI gui, Game loadedGame, boolean networked, String name) {
        super(master, gui);
        this.logic = loadedGame.getGameLogic();
        this.networked = networked;
        this.name = name;
        this.loadedGame = loadedGame;

        // Add our predefined Player Slots
        Player[] players = loadedGame.getAllPlayers();
        for(Player player : players) {
            PlayerSlot slot;
            if(player instanceof HumanPlayer) {
                slot = new PlayerSlot(this, PlayerSlot.SlotType.LOCAL, player.getColor(), player.getName());
            } else if(player instanceof BotPlayer) {
                slot = new PlayerSlot(this, PlayerSlot.SlotType.AI, player.getColor(), player.getName());
                slot.setAIDifficulty(((BotPlayer)player).getDifficulty());
            } else {
                throw new IllegalArgumentException("Unrecognized Player sub-class when adding Player Slots to Lobby of loaded Game");
            }
            slot.setNameCustom(true); // Stop automatic name changing when settings change
            playerSlots.add(slot);
        }

        // Update Local player names
        updateNames();

        // Load Game settings
        this.settings = loadedGame.getSettings();

        // Register as a ISettingsListener
        SettingsLoader.INSTANCE.addSettingsListener(this);

        // Self-Register Listener
        addListenerStatic(this);

        // Create Lobby Chat
        if(networked) master.getChat().create(ChatMessage.Channel.lobby(this.name));
    }

    protected Color getDefaultColorForSlot(int slotNum) {
        switch(slotNum) {
            case 0: return Color.BLACK;
            case 1: return Color.WHITE;
            case 2: return Color.BLUE;
            case 3: return Color.RED;
            case 4: return Color.YELLOW;
            case 5: return Color.GREEN;
            case 6: return Color.CYAN;
            case 7: return Color.PURPLE;
            default: return Color.GRAY;
        }
    }

    /**
     * Settings Setter. Sets this Lobby's <code>settings</code> DataMap to the given one.
     *
     * @param settings DataMap to set
     * @return Reference to this Lobby object for chain-construction
     */
    public Lobby setSettings(DataMap settings) {
        this.settings = settings;

        // Tell the GUI to refresh and repaint
        gui.updateGUIMinor();

        return this;
    }

    public void joinUser(User user) {
        for(PlayerSlot slot : playerSlots) {
            if(slot.getType() == PlayerSlot.SlotType.NETWORK && !slot.isClaimed()) {
                slot.setClaimed(true);
                slot.setName(user.getUsername());
                break;
            }
        }
        gui.updateGUIMinor();
    }

    public synchronized void removeUser(User user) {
        for(PlayerSlot slot : playerSlots) {
            if(slot.getName().equals(user.getUsername())) {
                slot.setClaimed(false);
                slot.setName("Open Slot");
                break;
            }
        }
        gui.updateGUIMinor();
    }

    /**
     * Adds a new PlayerSlot. Will add a new PlayerSlot of the <code>type</code> given if there is still room in this
     * Lobby. If the <code>type</code> is <code>LOCAL</code>, the name will be auto-generated based on client settings.
     *
     * @param type SlotType <code>type</code> of the new PlayerSlot
     * @throws IllegalArgumentException if this Lobby is already at its maximum PlayerSlot count
     */
    public void addSlot(PlayerSlot.SlotType type) throws IllegalArgumentException {

        // Check player count
        if(playerSlots.size() >= logic.maxPlayerCount())
            throw new IllegalArgumentException("Already at maximum Player count for this Lobby");

        // Count the amount of duplicate automatic local names
        int dupNum = 0;
        for(PlayerSlot slot : playerSlots) {
            if(slot.getType() == PlayerSlot.SlotType.LOCAL) dupNum++;
        }

        // Create the new slot
        PlayerSlot slot;
        switch(type) {
            case LOCAL:
                String username = "Player";
                if(WebUtilities.INSTANCE.loggedIn())
                    username = WebUtilities.INSTANCE.getUser().getUsername();
                Color color = getDefaultColorForSlot(playerSlots.size());
                dupNum++;
                if(dupNum > 1) username += dupNum;
                slot = new PlayerSlot(this, type, color, username);
                break;
            default:
                slot = new PlayerSlot(this, type, getDefaultColorForSlot(playerSlots.size()));
                break;
        }

        playerSlots.add(slot);

        // Tell the GUI to refresh and repaint
        gui.updateGUIMinor();
    }

    /**
     * Removes a PlayerSlot. If the specified <code>slot</code> exists in this Lobby, it is removed. Afterwards, the
     * Lobby is updated so that automatic name selection and the GUI can be updated.
     *
     * @param slot PlayerSlot <code>slot</code> to remove
     */
    public void removeSlot(PlayerSlot slot) {

        // Remove the slot
        playerSlots.remove(slot);
        updateNames();
    }

    /**
     * All PlayerSlots Getter. Retrieves a <code>Collection</code> view of all the PlayerSlots in this Lobby.
     *
     * @return Collection of PlayerSlots in this Lobby
     */
    public Collection<PlayerSlot> getAllSlots() { return playerSlots; }

    /**
     * Can a Game be started from this Lobby. Determines whether or not sufficient conditions are met such that the
     * Game this Lobby is preparing for can be started. Examples include having a minimum amount of Players connected.
     *
     * @return True if this Lobby is ready to start a game, False otherwise
     */
    public boolean canStart() { return logic.validPlayerCount(getClaimedCount()); }

    /**
     * Start a Game from this Lobby. If this Lobby is setting up a new Game, creates a new Game object and returns it.
     * If this Lobby is instead hosting a previously saved Game, returns the already loaded Game object.
     *
     * @return Game object to start, or <code>null</code> if this Lobby is not ready to start a Game
     */
    public Game startGame() {
        if(!canStart()) return null;

        if(loadedGame == null) {
            loadedGame = new Game(master, gui, networked, name);

            switch(logic.getType()) {
                case REVERSI: loadedGame.setLogic(new ReversiLogic(loadedGame)); break;
                case GO: loadedGame.setLogic(new GoLogic(loadedGame)); break;
            }

            // Set the settings
            settings.set(SettingsLoader.GAME_PLAYER_COUNT, getClaimedCount());
            loadedGame.setSettings(settings);

            for(PlayerSlot slot : playerSlots) {
                if(!slot.isClaimed()) continue;
                Player p;
                switch(slot.getType()) {
                    case NETWORK:
                        p = new NetworkPlayer(loadedGame, slot.getColor());
                        p.setName(slot.getName());
                        break;
                    case LOCAL:
                        p = new HumanPlayer(loadedGame, slot.getColor());
                        p.setName(slot.getName());
                        break;
                    case AI:
                        p = new BotPlayer(loadedGame, slot.getColor(), slot.getAIDifficulty());
                        p.setName(slot.getName());
                        break;
                }
            }
        }

        loadedGame.initialize();

        return loadedGame;
    }

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

        // Unregister ISettingsListener
        SettingsLoader.INSTANCE.removeSettingsListener(this);

        // Clear Lobby Chat
        master.getChat().clear(ChatMessage.Channel.lobby(this.name));

        // Self-unregister
        removeListenerStatic(this);
    }



    // ****************
    //  Getter Methods
    // ****************

    /**
     * Settings Getter. Retrieves the DataMap that this Lobby is manipulating.
     *
     * @return this Lobby's <code>settings</code> DataMap
     */
    public DataMap getSettings() { return settings; }

    /**
     * Game Loaded Checker. Determines whether or not this Lobby is currently setting up a Game that has been loaded
     * from a previous saved state.
     *
     * @return True if this Lobby is hosting a loaded Game
     */
    public boolean isGameLoaded() { return loadedGame != null; }

    /**
     * Loaded Game Getter. Retrieves the Game object for this Lobby that has been previously loaded. If no Game has
     * been loaded fro this Lobby (IE when a Game has been newly created), <code>null</code> is returned.
     *
     * @return Loaded Game object, or <code>null</code> if no Game has been loaded
     */
    public Game getLoadedGame() { return loadedGame; }

    /**
     * Networked Status Getter. Determines if this Lobby is networked; IE if the Game this Lobby is creating is an
     * online multiplayer game.
     *
     * @return <code>true</code> if this Lobby is networked, <code>false</code> otherwise
     */
    public boolean isNetworked() { return networked; }

    /**
     * Name Getter. Retrieves the <code>name</code> to associate with this Lobby and the Game it creates.
     *
     * @return String <code>name</code>
     */
    public String getName() { return name; }

    /**
     * GameLogic Getter. Retrieves the <code>logic</code> that is used by this Lobby and the Game it creates.
     *
     * @return GameLogic <code>logic</code>
     */
    public GameLogic getLogic() { return logic; }

    /**
     * Claimed Slot Count Getter. Retrieves the amount of PlayerSlots in this Lobby that are claimed/in use.
     *
     * @return Claimed Slot Count
     */
    public int getClaimedCount() {
        int count = 0;
        for(PlayerSlot slot : playerSlots)
            if(slot.isClaimed()) count++;
        return count;
    }

    /**
     * Valid SlotType Getter. Retrieves an array of the valid PlayerSlot.SlotTypes that are allowed to be selected for
     * this Lobby.
     *
     * @return Array of valid SlotTypes
     */
    public PlayerSlot.SlotType[] getValidSlotTypes() {
        ArrayList<PlayerSlot.SlotType> types = new ArrayList<>();
        types.add(PlayerSlot.SlotType.LOCAL);
        if(logic.getType() == GameLogic.Type.REVERSI)
            types.add(PlayerSlot.SlotType.AI);
        if(networked)
            types.add(PlayerSlot.SlotType.NETWORK);
        return types.toArray(new PlayerSlot.SlotType[]{});
    }

    protected void updateNames() {

        // Get the Username
        String username = "Player";
        if(WebUtilities.INSTANCE.loggedIn())
            username = WebUtilities.INSTANCE.getUser().getUsername();

        // Count duplicates (Local Players)
        int dupNum = 0;
        for(PlayerSlot slot : playerSlots) {
            if(slot.getType() == PlayerSlot.SlotType.LOCAL) {
                dupNum++;
                if(dupNum > 1) slot.setName(username + dupNum);
                else slot.setName(username);
            }
        }

        // Tell the GUI to refresh and repaint
        gui.updateGUIMinor();
    }

    /**
     * Called when the client's settings have been changed.
     */
    @Override
    public void onClientSettingsChanged() {
        updateNames();
    }

    /**
     * Called when a use logs out from the server
     *
     * @param loggedIn if the user is loggedIn
     */
    @Override
    public void onLogout(boolean loggedIn) {
        updateNames();
    }
}
