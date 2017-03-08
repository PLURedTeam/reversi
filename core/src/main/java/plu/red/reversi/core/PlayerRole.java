package plu.red.reversi.core;

/**
 * Created by daniel on 3/6/17.
 */
public enum PlayerRole {
    NONE, WHITE, BLACK;

    protected static final PlayerRole[] validPlayerData = new PlayerRole[]{
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
    public boolean isValid() {
        for(PlayerRole role : validPlayerData)
            if(role == this) return true; // Enums, so don't need equals()
        return false;
    }
}
