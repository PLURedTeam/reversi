package plu.red.reversi.core.player;

/**
 * Created by daniel on 3/6/17.
 * Glory to the Red Team.
 */

import plu.red.reversi.core.Game;
import plu.red.reversi.core.PlayerColor;
import plu.red.reversi.core.ReversiMinimax;

/**
 * An instance of a player which the computer can play as (basically an AI)
 * It utilizes the reversi minimax algorithm to compute the best move, and then execute it.
 */
public class BotPlayer extends Player {
    Thread thread;
    private ReversiMinimax minimax;

    /**
     * Constructs a new BotPlayer.
     * @param game Current game obj which this player plays on.
     * @param role Bot's game role.
     * @param firstPlayer Player for the current board state.
     */
    public BotPlayer(Game game, PlayerColor role, PlayerColor firstPlayer) {
        super(game, role);
        thread = null;
        minimax = new ReversiMinimax(game, role, 10);
    }

    @Override
    public void nextTurn(boolean yours) {
        if(!yours) return;

        thread = new Thread(minimax);
        thread.start();
    }
}
