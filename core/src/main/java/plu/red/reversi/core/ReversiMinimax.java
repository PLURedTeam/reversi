package plu.red.reversi.core;

/**
 * Created by daniel on 3/6/17.
 * Glory to the Red Team.
 */

/**
 * Using the minimax algorithm, calculate the optimal move for a particular player given the specified board.
 */
public class ReversiMinimax implements Runnable {

    private Board board;
    private PlayerRole role;

    private Looper.LooperCall<Board.BoardIndex> call;

    // Results (TODO: More stats can go here)
    private Board.BoardIndex bestPlay;

    /**
     * Constructs a ReversiMinimax problem to solve
     * @param board
     * @param role
     */
    public ReversiMinimax(Board board, PlayerRole role) {
        this.board = board;
        this.role = role;
        this.call = call;
    }

    public ReversiMinimax(Board board, PlayerRole role, Looper.LooperCall<Board.BoardIndex> call) {
        this(board, role);
        this.call = call;
    }

    /**
     * Calculate the best move in reversi by using the minimax algorithm.
     *
     * NOTE: This class implements runnable so you can export this operation to another thread quite easily.
     * The result is returned through looper on finish if one is provided.
     */
    @Override
    public void run() {

        // calculate the board index of the best tile
        // along the way, we should also generate helpful statistics
        // which can be optionally requested from within the class itself after completion

        if(call != null)
            call.call(bestPlay);
    }
}
