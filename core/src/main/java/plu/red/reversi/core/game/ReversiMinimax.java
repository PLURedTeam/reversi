package plu.red.reversi.core.game;

import plu.red.reversi.core.SettingsLoader;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.game.logic.GameLogic;
import plu.red.reversi.core.game.logic.ReversiLogic;

import java.util.Set;

//TODO: cache player score and pass it along to reduce the computation requirement
//TODO: use iterative search to improve efficiency and allow cutting it off part-way through

/**
 * Using the minimax algorithm, calculate the optimal move for a particular currentPlayer given the specified currentState.
 */
public class ReversiMinimax implements Runnable {
    private Game game;
    private int aiID;
    public final int MAX_DEPTH;

    /**
     * Constructs a ReversiMinimax problem to solve.
     * @param game Reference to current game so we can get information like board state.
     * @param aiID Player ID we want to maximize (who we are).
     * @param MAX_DEPTH Maximum search depth.
     */
    public ReversiMinimax(final Game game, int aiID, int MAX_DEPTH) {
        this.game = game;
        this.aiID = aiID;
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
        try {
            MoveCommand command = getBestMoveCommand();
            game.acceptCommand(command);
        } catch (IndexOutOfBoundsException e) {
            System.err.println("AI cannot move.");
        }
    }

    /**
     * Check if the current player can make a move on the current game state.
     * Does not account for when their turn will be, assumes it is being to asked right now.
     * @return True if a move can be made, otherwise false.
     */
    public boolean canPlay() {
        if(!game.getGameLogic().canPlay(aiID)) return false;
        return true;
    }

    /**
     * Retrieve the best move as a move command.
     * @return A move command representing the best move.
     */
    public MoveCommand getBestMoveCommand() throws IndexOutOfBoundsException {
        BoardIndex b = getBestPlay();
        if(b == null) throw new IndexOutOfBoundsException("AI Cannot move");
        return new MoveCommand(aiID, b);
    }

    /**
     * Finds the index of the best play.
     * @return Index of best play.
     * @throws IndexOutOfBoundsException If no moves can be made.
     */
    public BoardIndex getBestPlay() {
        final Board board = game.getBoard();

        final ReversiLogic logic = (ReversiLogic)game.getGameLogic();
        if(logic == null) throw new IllegalStateException("Must be a game of reversi to use reversi-minimax.");

        Set<BoardIndex> possibleMoves = logic.getValidMoves(aiID);
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        int bestScore = Integer.MIN_VALUE;

        BoardIndex bestMove = null;
        for(BoardIndex i : possibleMoves) {
            Board subBoard = new Board(board);
            logic.play(new MoveCommand(aiID, i), subBoard, false, false);

            final int childScore = getBestPlay(subBoard, game.getNextPlayerID(aiID), alpha, beta, 1);
            if(childScore > bestScore) {
                bestScore = childScore;
                bestMove = i;
                alpha = Math.max(alpha, childScore);
            }

            if(beta <= alpha) break; //will not ever happen in this location
        }
        return bestMove;
    }

    /**
     * Determines the heuristic score for a given state.
     * @param board Game state to analyze.
     * @param endgame Is this an endgame analysis?
     * @return Score for the game state.
     */
    private int heuristicScore(Board board, boolean endgame) {
        final GameLogic logic = game.getGameLogic();

        //ours - (all - ours) == ours * 2 - all
        int score = (logic.getScore(aiID, board) * 2) - board.getTotalPieces();

        if(!endgame) {
            int player = board.at(new BoardIndex(0, 0));
            if(player >= 0) score += player == aiID ? 4 : -4;

            player = board.at(new BoardIndex(board.size - 1, 0));
            if(player >= 0) score += player == aiID ? 4 : -4;

            player = board.at(new BoardIndex(0, board.size - 1));
            if(player >= 0) score += player == aiID ? 4 : -4;

            player = board.at(new BoardIndex(board.size - 1, board.size - 1));
            if(player >= 0) score += player == aiID ? 4 : -4;

            return score;
        }
        return score * 16; //weight the score
    }

    /**
     * Find the best of the children to choose if our turn, and assume they choose the worst
     * on their turn.
     * @return A child of node which is the best state to go to.
     */
    private int getBestPlay(Board board, int player, int alpha, int beta, int depth) {
        if(depth >= MAX_DEPTH)
            return heuristicScore(board, false);

        final GameLogic logic = game.getGameLogic();
        final boolean turn_skipping = game.getSettings().get(SettingsLoader.GAME_ALLOW_TURN_SKIPPING, Boolean.class);
        final int start_player = player;

        Set<BoardIndex> possibleMoves = logic.getValidMoves(player, board);
        while(possibleMoves.isEmpty()) { //can't move
            player = game.getNextPlayerID(player);
            if(player != start_player) //inc. player and make sure we have not looped
                possibleMoves = logic.getValidMoves(player, board);
            else
                return heuristicScore(board, true);
        }

        final boolean maximize = player == aiID;
        int bestScore = maximize ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for(BoardIndex i : possibleMoves) {
            Board subBoard = new Board(board);
            logic.play(new MoveCommand(player, i), subBoard, false, false);

            final int childScore = getBestPlay(subBoard, game.getNextPlayerID(player), alpha, beta, depth + 1);
            if(maximize && childScore > bestScore) {
                bestScore = childScore;
                alpha = Math.max(alpha, childScore);
            }
            else if(!maximize && childScore < bestScore) {
                bestScore = childScore;
                beta = Math.min(beta, childScore);
            }

            if(beta <= alpha) break;
        }
        return bestScore;
    }
}