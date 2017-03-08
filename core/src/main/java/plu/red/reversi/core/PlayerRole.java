package plu.red.reversi.core;

/**
 * Created by daniel on 3/6/17.
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

}
