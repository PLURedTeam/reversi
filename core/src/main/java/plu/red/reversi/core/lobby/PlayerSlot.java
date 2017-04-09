package plu.red.reversi.core.lobby;

import plu.red.reversi.core.util.Color;

/**
 * Glory to the Red Team.
 *
 * A Lobby's PlayerSlot. The PlayerSlot class represents a single non-spectator position in a game Lobby. This position
 * can be that of a local human player, an AI player, or even a networked player that is playing from a different client.
 */
public class PlayerSlot {

    /**
     * Enumeration representing a certain Slot type. The SlotType enumeration specifies what kind of Player will be
     * filling a given PlayerSlot. This can be anywhere from a local human player to an unspecified player connecting
     * from another client.
     */
    public enum SlotType {
        LOCAL,
        AI,
        NETWORK
    }

    protected SlotType type;
    protected String name;
    protected boolean nameCustom = false;
    protected Color color;
    protected int aiDifficulty = 5;

    /**
     * Reference to parent Lobby Coordinator
     */
    public final Lobby lobby;

    /**
     * Basic Constructor. Constructs a new PlayerSlot with a given <code>type</code> and <code>color</code>, and a
     * <code>name</code> generated based on that type.
     *
     * @param lobby Parent Lobby Coordinator
     * @param type SlotType <code>type</code> of this PlayerSlot
     * @param color Color <code>color</code> of this PlayerSlot
     */
    public PlayerSlot(Lobby lobby, SlotType type, Color color) {
        this.lobby = lobby;
        this.type = type;
        this.color = color;
        switch(type) {
            case LOCAL:
                this.name = "Local Player";
                break;
            case AI:
                this.name = "AI Player";
                break;
            case NETWORK:
                this.name = "Open Slot";
                break;
        }
    }

    /**
     * Full Constructor. Constructs a new PlayerSlot with a given <code>type</code>, <code>color</code> and
     * <code>name</code>.
     *
     * @param lobby Parent Lobby Coordinator
     * @param type SlotType <code>type</code> of this PlayerSlot
     * @param color Color <code>color</code> of this PlayerSlot
     * @param name String <code>name</code> of this PlayerSlot
     */
    public PlayerSlot(Lobby lobby, SlotType type, Color color, String name) {
        this.lobby = lobby;
        this.type = type;
        this.color = color;
        this.name = name;
    }

    /**
     * Type Getter. Retrieves the <code>type</code> of slot this PlayerSlot is.
     *
     * @return SlotType <code>type</code>
     */
    public SlotType getType() { return type; }

    /**
     * Type Setter. Sets the <code>type</code> of this PlayerSlot to the given <code>type</code>.
     *
     * @param type SlotType <code>type</code> to set
     */
    public void setType(SlotType type) {

        if(this.type != type) {
            switch (type) {
                case LOCAL:
                    this.name = "Local Player";
                    this.nameCustom = false;
                    break;
                case AI:
                    this.name = "AI Player";
                    break;
                case NETWORK:
                    this.name = "Open Slot";
                    break;
            }
        }
        this.type = type;

        // Spoof settings change to update automatic name generation and the GUI
        lobby.onClientSettingsChanged();
    }

    /**
     * Name Getter. Retrieves the <code>name</code> to be displayed for this PlayerSlot.
     *
     * @return String <code>name</code>
     */
    public String getName() { return name; }

    /**
     * Name Setter. Sets the <code>name</code> to be displayed for this PlayerSlot to the given <code>name</code>.
     *
     * @param name String <code>name</code> to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Name Custom Getter. Determines whether or not the name this PlayerSlot is displaying is a custom set one, or
     * an automatically determined name based upon the Client's global settings.
     *
     * @return True if this PlayerSlot's <code>name</code> is custom
     */
    public boolean isNameCustom() { return nameCustom; }

    /**
     * Name Custom Setter. Sets whether or not the name this PlayerSlot is displaying is a custom set one, or an
     * automatically determined name based upon the Client's global settings.
     * @param custom
     */
    public void setNameCustom(boolean custom) { this.nameCustom = custom; }

    /**
     * Color Getter. Retrieves the <code>color</code> to be displayed for this PlayerSlot.
     *
     * @return Color <code>color</code>
     */
    public Color getColor() { return color; }

    /**
     * Color Setter. Sets the <code>color</code> to be displayed for this PlayerSlot to the given <code>color</code>.
     *
     * @param color Color <code>color</code> to set
     */
    public void setColor(Color color) { this.color = color; }

    /**
     * AI Difficulty Getter. Retrieves the <code>aiDifficulty</code> that is used if this PlayerSlot is interpreted
     * as an AI.
     *
     * @return Integer <code>aiDifficulty</code>
     */
    public int getAIDifficulty() { return aiDifficulty; }

    /**
     * AI Difficulty Setter. Sets the <code>aiDifficulty</code> that is used if this PlayerSlot is interpreted as
     * an AI.
     * @param difficulty Integer <code>aiDifficulty</code> to set
     */
    public void setAIDifficulty(int difficulty) { this.aiDifficulty = difficulty; }
}
