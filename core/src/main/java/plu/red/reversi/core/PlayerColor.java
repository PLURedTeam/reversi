package plu.red.reversi.core;

import org.joml.Vector3f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Set;

/**
 * Glory to the Red Team.
 *
 * PlayerColor enumeration to represent different possible Player roles and colors. Generally designed to be future-proof
 * and extensible, so that it is possible to add more Player colors/types with very little effort, and in many cases no
 * changes outside this file.
 */
@Deprecated
public enum PlayerColor {

    NONE(false, null, "Non-player"),
    WHITE(true, new Vector3f(1.0f, 1.0f, 1.0f), "White"),
    BLACK(true, new Vector3f(0.0f, 0.0f, 1.0f), "Black"),
    RED(true, new Vector3f(1.0f, 0.0f, 0.0f), "Red"),
    BLUE(true, new Vector3f(0.0f, 0.0f, 1.0f), "Blue"),
    ;

    protected final boolean valid;
    protected PlayerColor next;
    protected int vOrdinal;
    public final Vector3f color;
    public final String name;

    // Constructor occurs before any static code
    PlayerColor(boolean valid, Vector3f color, String name) {
        this.valid = valid;
        this.next = this;
        this.vOrdinal = -1;
        this.color = color;
        this.name = name;
    }

    static PlayerColor[] validPlayerColors;

    // Initialize 'Next' Chain
    static {

        ArrayList<PlayerColor> tempRoles = new ArrayList<PlayerColor>();

        PlayerColor start = null;
        PlayerColor last = null;

        for(PlayerColor role : values()) {
            if(role.valid) { // Skip non-valid roles, as they self-reference
                if(last == null) {
                    start = role;
                    last = role;
                } else {
                    last.next = role;
                    last = role;
                }
                role.vOrdinal = tempRoles.size();
                tempRoles.add(role);
            }
        }

        // Finish the chain by looping back to the start
        if(last != null) last.next = start;

        // Set the Valid PlayerRoles array
        validPlayerColors = tempRoles.toArray(new PlayerColor[]{});
    }

    /**
     * Retrieves only the PlayerColor enums that are valid players.
     *
     * @return Array of PlayerColor enums that are considered valid PlayerRoles
     */
    public static PlayerColor[] validPlayers() {
        // Arrays are unmodifiable, so no need to return copy
        return validPlayerColors;
    }

    /**
     * Determines if this PlayerColor enum is a valid player.
     *
     * @return true if this PlayerColor is in the list of PlayerColor enums considered valid, false otherwise
     */
    public final boolean isValid() {
        return valid;
    }

    /**
     * Determines if this PlayerColor enum is a valid player and is a used player.
     *
     * @param usedPlayers Set of PlayerRoles that are currently being used
     * @return true if this PlayerColor is valid and currently used, false otherwise
     */
    public final boolean isValid(Set<PlayerColor> usedPlayers) {
        return usedPlayers.contains(this) && isValid();
    }

    /**
     * Returns an ordinal number specific to only the valid player roles.
     * Functions similarly to the default enum ordinal(), but only considers
     * valid PlayerColor enums in its ordinal numbering.
     *
     * @return Ordinal number of a valid PlayerColor, or -1 if PlayerColor is not valid.
     */
    public final int validOrdinal() {
        return vOrdinal;
    }

    /**
     * Retrieves the next valid player in turn order, based on the ordering of stored valid PlayerRoles.
     *
     * @return next valid PlayerColor in turn order, or the same PlayerColor enum if the current one is not valid
     */
    public final PlayerColor getNext() {
        return next;
    }

    /**
     * Retrieves the next valid player in turn order from a set of currently used players, based on the ordering of
     * stored valid PlayerRoles.
     *
     * @param usedPlayers Set of PlayerRoles that are currently being used
     * @return next valid PlayerColor in turn order that is also currently being used, or the same PlayerColor enum if
     * the current one is not valid
     */
    public final PlayerColor getNext(Set<PlayerColor> usedPlayers) {
        if(usedPlayers.isEmpty()) return this;
        PlayerColor original = this;
        PlayerColor next = getNext();
        while(!usedPlayers.contains(next) && (next != original)) next = next.getNext();
        return next;
    }
}
