package plu.red.reversi.core;

/**
 * Created by daniel on 3/6/17.
 * Glory to the Red Team.
 */

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Using the minimax algorithm, calculate the optimal move for a particular player given the specified currentState.
 */
public class ReversiMinimax implements Runnable {

    private ReversiNode root;
    private PlayerRole role;

    /// Us if we are first, not us if we are not first
    private PlayerRole nextPlay;


    private Looper.LooperCall<BoardIndex> call;

    // Results (TODO: More stats can go here)
    private BoardIndex bestPlay;

    /**
     * Constructs a ReversiMinimax problem to solve
     * @param currentState
     * @param role
     */
    public ReversiMinimax(final Board currentState, PlayerRole role, PlayerRole nextPlay) {
        this.role = role;
        call = null;
        root = new ReversiNode(currentState, null, nextPlay);
    }

    public ReversiMinimax(final Board currentState, PlayerRole role, PlayerRole nextPlay, Looper.LooperCall<BoardIndex> call) {
        this(currentState, role, nextPlay);
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
        LinkedList<ReversiNode> work = new LinkedList<>();

        work.add(root);
        while(work.size() > 0) {
            ReversiNode t = work.poll();
            t.calculate(work);
        }

        if(call != null)
            call.call(bestPlay);
    }


    private class ReversiNode {
        public final Board board;
        public final ReversiNode parent;
        public LinkedList<ReversiNode> children;
        public PlayerRole player;
        public BoardIndex play;

        // added for readability
        public final int ourScore;
        public final int theirScore;


        /**
         * Initial constructor used at beginning of the game
         * @param board
         * @param parent
         * @param player
         */
        public ReversiNode(final Board board, final ReversiNode parent, PlayerRole player) {
            this.board = board;
            this.parent = parent;
            children = new LinkedList<>();
            this.player = player;
            ourScore = board.getScore(role);
            theirScore = board.getScore(role.invert());
            play = null;
        }

        /**
         * Constructor used when calculating the tree
         * @param board
         * @param parent
         * @param command
         */
        private ReversiNode(final Board board, final ReversiNode parent, CommandMove command) {
            this(board, parent, command.player.invert());

            if(board.getPossibleMoves(command.player.invert()).isEmpty()) {
                if(board.getPossibleMoves(command.player).isEmpty()) {
                    //END GAME
                    player = PlayerRole.NONE;
                    children = null;
                }
                player = command.player;
            }
            play = command.position;
        }

        public void calculate(LinkedList<ReversiNode> work) {
            if(!children.isEmpty()) {
                //we alreadly calculated this
                work.addAll(children);
                return;
            }

            //actually calculate
            ArrayList<BoardIndex> possible = board.getPossibleMoves(player);
            if(possible.isEmpty()) {
                //the player cannot move, skip their turn

            }
            for(BoardIndex i : possible) {
                Board b = new Board(board);
                b.apply(new CommandMove(player, i));
                children.add(new ReversiNode(b, this, player.invert()));
            }
        }
    }
}
