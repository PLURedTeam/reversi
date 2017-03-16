package plu.red.reversi.core.player;

/**
 * Created by daniel on 3/6/17.
 * Glory to the Red Team.
 */

import plu.red.reversi.core.BoardIndex;
import plu.red.reversi.core.Game;
import plu.red.reversi.core.PlayerColor;
import plu.red.reversi.core.ReversiMinimax;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.player.Player;
import plu.red.reversi.core.util.Looper;

/**
 * An instance of a player which the computer can play as (basically an AI)
 * It utilizes the reversi minimax algorithm to compute the best move, and then execute it.
 */
public class BotPlayer extends Player implements Looper.LooperCallback<BoardIndex> {

    private ReversiMinimax minimax;
    //private Looper.LooperCall<BoardIndex> looperCall;

    /**
     * Constructs a new BotPlayer.
     * @param game Current game obj which this player plays on.
     * @param role Bot's game role.
     * @param firstPlayer Player for the current board state.
     */
    public BotPlayer(Game game, PlayerColor role, PlayerColor firstPlayer) {
        super(game, role);
        //minimax = new ReversiMinimax(game, role, firstPlayer);
    }

    @Override
    public void nextTurn(boolean yours) {
        if(!yours) return;
        minimax = new ReversiMinimax(game, role, role);
        BoardIndex index = minimax.getBestPlay();
        game.acceptCommand(new MoveCommand(role, index));
    }

    /**
     * Called after reversi minimax has finished calculating the next best move to take
     * @param result the board index which should be played on next to minimax
     */
    @Override
    public void onLooperCallback(BoardIndex result) {
        getGame().acceptCommand(new MoveCommand(Command.Source.PLAYER, getRole(), result));
    }
}
