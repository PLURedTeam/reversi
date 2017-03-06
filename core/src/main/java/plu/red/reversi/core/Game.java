package plu.red.reversi.core;

/**
 * Created by daniel on 3/5/17.
 * Glory to the Red Team.
 */
public class Game {

    private GameSettings settings;

    private Player whitePlayer;
    private Player blackPlayer;

    public Game(GameSettings settings) {
        this.settings = settings;
    }

    /**
     * Set the player reference for a game. The side that the player is registered to will be dependant upon the player's
     * role value.
     *
     * This function will overwrite any preexisting player for that role. This makes it easy for players
     * to be switched out mid-game.
     *
     * @throws UnsupportedOperationException if the player which is to be added was not initialized for this game.
     * @param player
     */
    public void setPlayer(Player player) {

        if(player.getGame() != this)
            throw new UnsupportedOperationException("Attempted to assign game to player but player not assigned to game");

        if(player.getRole() == Player.PlayerRole.WHITE)
            whitePlayer = player;
        else
            blackPlayer = player;

    }

    public Player getWhitePlayer() { return whitePlayer; }
    public Player getBlackPlayer() { return blackPlayer; }
}
