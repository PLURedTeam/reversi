package plu.red.reversi.core;

/**
 * Created by daniel on 3/6/17.
 * Glory to the Red Team.
 */

import plu.red.reversi.core.util.Looper;

/**
 * An instance of a player which the computer can play as (basically an AI)
 * It utilizes the reversi minimax algorithm to compute the best move, and then execute it.
 */
public class BotPlayer extends Player implements Looper.LooperCallback<BoardIndex> {

    private ReversiMinimax minimax;
    private Looper.LooperCall<BoardIndex> looperCall;

    public BotPlayer(Game game, PlayerColor role) {
        super(game, role);

        // for later
        //minimax = new ReversiMinimax(getGame().board, getRole());
        looperCall = Looper.getLooper(Thread.currentThread()).getCall(this);
    }

    @Override
    public void nextTurn(boolean yours) {
        // TODO: In the future, we should simply update the minimax algorithm with the performed move.
        // for right now the act of reinit will be ok for what we are doing now, however.
        minimax = new ReversiMinimax(getGame(), getRole(), getRole().getNext(), looperCall);

        new Thread(minimax).start();
    }

    /**
     * Called after reversi minimax has finished calculating the next best move to take
     * @param result the board index which should be played on next to minimax
     */
    @Override
    public void onLooperCallback(BoardIndex result) {
        getGame().acceptCommand(new CommandMove(Command.Source.PLAYER, getRole(), result));
    }
}
