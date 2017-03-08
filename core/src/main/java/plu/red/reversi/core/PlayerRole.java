package plu.red.reversi.core;

import java.util.Set;

/**
 * Glory to the Red Team.
 *
 * PlayerRole enumeration to represent different possible Player roles and colors. Generally designed to be future-proof
 * and extensible, so that it is possible to add more Player colors/types with very little effort, and in many cases no
 * changes outside this file.
 */
public enum PlayerRole {
    NONE, WHITE, BLACK;

    private static final PlayerRole[] validPlayerData = new PlayerRole[]{
            WHITE, BLACK
    };

    /**
     * Retrieves only the PlayerRole enums that are valid players.
     *
     * @return Array of PlayerRole enums that are considered valid PlayerRoles
     */
    public static PlayerRole[] validPlayers() {
        return validPlayerData;
    }

    /**
     * Determines if this PlayerRole enum is a valid player.
     *
     * @return true if this PlayerRole is in the list of PlayerRole enums considered valid, false otherwise
     */
    public final boolean isValid() {
        for(PlayerRole role : validPlayerData)
            if(role == this) return true; // Enums, so don't need equals()
        return false;
    }

    /**
     * Determines if this PlayerRole enum is a valid player and is a used player.
     *
     * @param usedPlayers Set of PlayerRoles that are currently being used
     * @return true if this PlayerRole is valid and currently used, false otherwise
     */
    public final boolean isValid(Set<PlayerRole> usedPlayers) {
        return usedPlayers.contains(this) && isValid();
    }

    /**
     * Returns an ordinal number specific to only the valid player roles.
     * Functions similarly to the default enum ordinal(), but only considers
     * valid PlayerRole enums in its ordinal numbering.
     *
     * @return Ordinal number of a valid PlayerRole, or -1 if PlayerRole is not valid.
     */
    public final int validOrdinal() {
        for(int i = 0; i < validPlayerData.length; i++)
            if(validPlayerData[i] == this) return i;
        return -1;
    }

    /**
     * Retrieves the next valid player in turn order, based on the ordering of stored valid PlayerRoles.
     *
     * @return next valid PlayerRole in turn order, or the same PlayerRole enum if the current one is not valid
     */
    public final PlayerRole getNext() {
        int o = validOrdinal();
        if(o < 0) return this;
        else {
            if(++o >= validPlayerData.length) o = 0;
            return validPlayerData[o];
        }
    }

    /**
     * Retrieves the next valid player in turn order from a set of currently used players, based on the ordering of
     * stored valid PlayerRoles.
     *
     * @param usedPlayers Set of PlayerRoles that are currently being used
     * @return next valid PlayerRole in turn order that is also currently being used, or the same PlayerRole enum if
     * the current one is not valid
     */
    public final PlayerRole getNext(Set<PlayerRole> usedPlayers) {
        if(usedPlayers.isEmpty()) return this;
        PlayerRole original = this;
        PlayerRole next = getNext();
        while(!usedPlayers.contains(next) && (next != original)) next = next.getNext();
        return next;
    }

}
