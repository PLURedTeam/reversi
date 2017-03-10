package plu.red.reversi.core;

/**
 * Glory to the Red Team.
 *
 * Command implementation class for a board move.
 */
public class CommandMove extends Command {

    public final PlayerRole player;
    public final BoardIndex position;

    public CommandMove(Source source, PlayerRole player, BoardIndex position) {
        super(source);
        this.player = player;
        this.position = position;
    }

    /**
     * Uses data from the Game object to determine whether or not this Command is valid. IE: Whether a move played by a
     * player is on a valid position of the board.
     *
     * @param game Game object to pull data from
     * @return true if this Command is valid, false otherwise
     */
    @Override
    public boolean isValid(Game game) {
        // Is it this Player's turn and is the position valid on the board
        return game.getCurrentPlayer().getRole() == player && game.getBoard().isValidMove(player, position);
    }
}
