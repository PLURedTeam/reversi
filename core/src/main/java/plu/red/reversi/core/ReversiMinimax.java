package plu.red.reversi.core;

/**
 * Created by daniel on 3/6/17.
 * Glory to the Red Team.
 */

import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.util.Looper;

import java.util.LinkedList;
import java.util.Set;

/**
 * Using the minimax algorithm, calculate the optimal move for a particular currentPlayer given the specified currentState.
 */
public class ReversiMinimax implements Runnable {
    private Game game;
    private ReversiNode root;
    private PlayerColor aiRole;
    private final int MAX_DEPTH;

    private BoardIndex bestPlay;

    /**
     * Constructs a ReversiMinimax problem to solve.
     * @param game Reference to current game so we can get information like board state.
     * @param aiRole Player we want to maximize (who we are).
     * @param nextPlay Person who will make the next move given current board state.
     * @param MAX_DEPTH Maximum search depth.
     */
    public ReversiMinimax(final Game game, PlayerColor aiRole, PlayerColor nextPlay, int MAX_DEPTH) {
        this.game = game;
        this.aiRole = aiRole;
        root = new ReversiNode(game.getBoard(), nextPlay);
        bestPlay = null;
        this.MAX_DEPTH = MAX_DEPTH;
    }

    /**
     * Calculate the best move in reversi by using the minimax algorithm.
     *
     * NOTE: This class implements runnable so you can export this operation to another thread quite easily.
     * The result is returned through looper on finish if one is provided.
     */
    @Override
    public void run() {
        //TODO: call game with apply command when done
    }

    /**
     * Check if the current player can make a move on the current game state.
     * Does not account for when their turn will be, assumes it is being to asked right now.
     * @return True if a move can be made, otherwise false.
     */
    public boolean canPlay() {
        updateRoot();

        if(!root.children.isEmpty())
            return true;
        if(!root.getPossibleMoves().isEmpty())
            return true;
        return false;
    }

    /**
     * Used for testing, gets the current depth of the root node.
     * @return Depth of root node since creation of cache.
     */
    public int getRootDetph() {
        return root.depth;
    }

    /**
     * Retrieve the best move as a move command.
     * @return A move command representing the best move.
     */
    public MoveCommand getBestMoveCommand() {
        return new MoveCommand(aiRole, getBestPlay());
    }

    /**
     * Finds the index of the best play.
     * @return Index of best play.
     * @throws IndexOutOfBoundsException If no moves can be made.
     */
    public BoardIndex getBestPlay() throws IndexOutOfBoundsException {
        //check if we are already up to date
        if(bestPlay != null && root.board.equals(game.getBoard()))
            return bestPlay;

        //get the current root from cached data.
        updateRoot();

        //now find the best option using recursion
        ReversiNode i = getBestPlay(root, root.depth + MAX_DEPTH);

        //System.out.println(root.children.size());
        if(i == root) { //Occurs if no more moves can be made
            throw new IndexOutOfBoundsException("No moves can be made.");
        }

        //find the move that needs to be made to get there
        bestPlay = i.moveMade;

        return bestPlay;
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
            //END GAME CONDITION, weight the score heavily (if negative it will become more negative)
            node.score = node.getBasicScore() * 16;
            return node;
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

    /**
     * Generate all the children for a given node. Basically calculate all possible
     * moves for the given state and create a new ReversiNode for it.
     * @param node Node for which the children will be generated.
     */
    private void generateChildren(ReversiNode node) {
        if(!node.children.isEmpty()) return;
        //need to calculate, i is current game state
        // start by getting all the possible moves for the next player
        Set<BoardIndex> possible = node.getPossibleMoves();

        if(possible.isEmpty()) return;

        //go through the locations, and create new nodes for them
        for(BoardIndex l : possible) {
            Board state = new Board(node.board);
            state.apply(new MoveCommand(node.currentPlayer, l));
            node.children.add(new ReversiNode(state, l, node));
        }
    }

    /**
     * Update the cached tree such that the root is at the current board state.
     */
    private void updateRoot() {
        //move root forward (may do nothing)
        try {
            ReversiNode nroot = findCurrentRoot();
            if(nroot != root) {
                bestPlay = null; //invalidate cache
                root = nroot;
                root.parent = null;
            }
        } catch(IndexOutOfBoundsException e) {
            root = new ReversiNode(game.getBoard(), game.getCurrentPlayer().getRole());
        }
    }

    /**
     * Find the root representing the current board state in the tree.
     * @return Node representing current board state.
     * @throws IndexOutOfBoundsException When the cache does not contain the current board state.
     */
    private ReversiNode findCurrentRoot() throws IndexOutOfBoundsException {
        //any moves which could have happened according to the board state
        LinkedList<ReversiNode> candidates = new LinkedList<>();
        candidates.add(root);

        ReversiNode currentRoot;
        //while the currentRoot's board does not equal the current game state
        while(!candidates.isEmpty()) {
            currentRoot = candidates.poll();

            boolean foundCandidate = false;
            //go through all children, consider a candidate any that have moves which were made
            for(ReversiNode i : currentRoot.children) {
                if(game.getBoard().at(i.moveMade).isValid()) {
                    foundCandidate = true;
                    candidates.add(i);
                }
            }

            //save on computation, since the board will not be the same if some of the currentRoot's
            // children are candidates
            if(!foundCandidate)
                if(game.getBoard().equals(currentRoot.board))
                    return currentRoot;
        }

        throw new IndexOutOfBoundsException("Minimax scoreCache exceeded, need to regenerate from scratch");
    }


    private class ReversiNode {
        public final Board board;
        public ReversiNode parent;
        public LinkedList<ReversiNode> children;
        public PlayerColor currentPlayer;
        public int depth;
        public BoardIndex moveMade;

        public int score;



        /**
         * Initial constructor used only once.
         * @param board Inital board of ReversiNodes
         * @param currentPlayer Player who's turn it is right now.
         */
        public ReversiNode(Board board, PlayerColor currentPlayer) {
            this.board = new Board(board);
            this.parent = null;
            children = new LinkedList<>();
            this.currentPlayer = currentPlayer;
            this.depth = 0;
            this.moveMade = null;
            this.score = 0;
        }

        /**
         * Constructor used when calculating the tree.
         * @param board A new board with the current game state.
         * @param parent State which yeilded this one with the moveMade.
         * @param moveMade Action taken to get to this state.
         */
        private ReversiNode(Board board, BoardIndex moveMade, final ReversiNode parent) {
            this.board = board;
            this.parent = parent;
            children = new LinkedList<>();
            this.currentPlayer = parent.currentPlayer.getNext(game.getUsedPlayers());
            this.depth = parent.depth + 1;
            this.moveMade = moveMade;
            this.score = 0;
        }

        /**
         * Finds the possible moves for the next player with the current board state.
         * @return An array list of possible moves for currentPlayer.
         */
        public Set<BoardIndex> getPossibleMoves() {
            return board.getPossibleMoves(currentPlayer);
        }

        public int getHeuristicScore() {
            //TODO: improve by factoring in corners and edges as different weights
            return getBasicScore();
        }

        public int getBasicScore() {
            return board.getScore(aiRole) - board.getScore(aiRole.getNext(game.getUsedPlayers()));
        }
    }
}