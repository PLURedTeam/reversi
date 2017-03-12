package plu.red.reversi.core;

/**
 * Created by daniel on 3/6/17.
 * Glory to the Red Team.
 */

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.LinkedList;

/**
 * Using the minimax algorithm, calculate the optimal move for a particular currentPlayer given the specified currentState.
 */
public class ReversiMinimax implements Runnable {
    private Game game;
    private ReversiNode root;
    private PlayerRole aiRole;


    private Looper.LooperCall<BoardIndex> call;
    private BoardIndex bestPlay;

    /**
     * Constructs a ReversiMinimax problem to solve
     * @param game
     * @param aiRole
     */
    public ReversiMinimax(final Game game, PlayerRole aiRole, PlayerRole nextPlay) {
        this.game = game;
        this.aiRole = aiRole;
        call = null;
        root = new ReversiNode(game.getBoard(), null, nextPlay, 0);
        bestPlay = null;
    }

    public ReversiMinimax(final Game game, PlayerRole aiRole, PlayerRole nextPlay, Looper.LooperCall<BoardIndex> call) {
        this(game, aiRole, nextPlay);
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
        if(call != null)
            call.call(getBestPlay());
    }

    public BoardIndex getBestPlay() throws IndexOutOfBoundsException {
        //check if we are already up to date
        if(bestPlay != null && root.board.equals(game.getBoard()))
            return bestPlay;

        //move root forward (may do nothing)
        root = findCurrentRoot();
        root.parent = null;

        //now find the best option using recursion
        ReversiNode i = getBestPlay(root, root.depth + 5);

        if(i == root) //Not sure if this can happen, but just in case
            throw new IndexOutOfBoundsException("Best play is root, need to regenerate tree from scratch");

        //find the move that needs to be made to get there
        bestPlay = i.moveMade;

        return bestPlay;
    }

    private ReversiNode findCurrentRoot() throws IndexOutOfBoundsException {
        ReversiNode currentRoot = root;

        //any moves which could have happened according to the board state
        LinkedList<ReversiNode> candidates = new LinkedList<>();

        //breadth-first search to find current root
        while(!currentRoot.board.equals(game.getBoard())) {

            //board is not equal yet, so check children
            if(candidates.isEmpty() || currentRoot.children.isEmpty())
                throw new IndexOutOfBoundsException("Minimax cache exceeded, need to regenerate from scratch");

            //go through all children, consider a candidate any that have moves which were made
            for(ReversiNode i : currentRoot.children)
                if(game.getBoard().at(i.moveMade).isValid())
                    candidates.add(i);

            currentRoot = candidates.poll();
        }

        return currentRoot;
    }

    /**
     * Find the best of the children to choose if our turn, and assume they choose the worst
     * on their turn.
     * @param node Current node in the tree.
     * @return A child of node which is the best state to go to.
     */
    private ReversiNode getBestPlay(ReversiNode node, int maxDepth) {
        if(node.depth >= maxDepth) {
            node.score = node.getHeuristicScore();
            return node;
        }

        //if it is us, maximize score
        boolean maximize = node.currentPlayer == aiRole;
        node.score = maximize ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        //check if children need to be generated
        if(node.children.isEmpty())
            generateChildren(node);
        if(node.children.isEmpty()) {
            //TODO: Handle setting for game not ending on no-move condition
            int basicScore = node.getBasicScore(); //our pieces - their pieces

            //END GAME CONDITION, weight the score by 2^4
            node.score = basicScore > 0 ? basicScore << 4 : basicScore >> 4;
        }

        ReversiNode choice = null;

        for(ReversiNode i : node.children) {
            ReversiNode b = getBestPlay(i, maxDepth);
            if((maximize && b.score > node.score) || (!maximize && b.score < node.score)) {
                node.score = b.score;
                choice = i;
            }
        }

        return choice;
    }

    private void generateChildren(ReversiNode node) {
        if(!node.children.isEmpty()) return;
        //need to calculate, i is current game state
        // start by getting all the possible moves for the next player
        ArrayList<BoardIndex> possible = node.getPossibleMoves();

        if(possible.isEmpty()) return;

        //go through the locations, and create new nodes for them
        for(BoardIndex l : possible) {
            Board state = new Board(game.getBoard());
            state.apply(new CommandMove(node.currentPlayer, l));
            node.children.add(new ReversiNode(state, l, node));
        }
    }


    private class ReversiNode {
        public final Board board;
        public ReversiNode parent;
        public LinkedList<ReversiNode> children;
        public PlayerRole currentPlayer;
        public int depth;
        public BoardIndex moveMade;

        public int score;


        /**
         * Initial constructor used at beginning of the game
         * @param board
         * @param parent
         * @param currentPlayer
         */
        public ReversiNode(final Board board, ReversiNode parent, PlayerRole currentPlayer, int depth) {
            this.board = board;
            this.parent = parent;
            children = new LinkedList<>();
            this.currentPlayer = currentPlayer;
            this.depth = depth;
            this.moveMade = null;
            this.score = 0;
        }

        /**
         * Constructor used when calculating the tree
         * @param board
         * @param parent
         */
        private ReversiNode(final Board board, BoardIndex moveMade, final ReversiNode parent) {
            this(board, parent, parent.currentPlayer.getNext(), parent.depth + 1);
            this.moveMade = moveMade;
        }

        /**
         * Finds the possible moves for the next player with the current board state.
         * @return An array list of possible moves for currentPlayer.
         */
        public ArrayList<BoardIndex> getPossibleMoves() {
            return board.getPossibleMoves(currentPlayer);
        }

        public int getHeuristicScore() {
            //TODO: improve by factoring in corners and edges as different weights
            return getBasicScore();
        }

        public int getBasicScore() {
            return board.getScore(aiRole) - board.getScore(aiRole.getNext());
        }
    }
}