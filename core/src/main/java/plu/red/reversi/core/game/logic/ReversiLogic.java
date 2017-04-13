package plu.red.reversi.core.game.logic;

import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.command.SetCommand;
import plu.red.reversi.core.game.Board;
import plu.red.reversi.core.game.BoardIndex;

import java.security.InvalidParameterException;
import java.util.*;

/**
 * ReversiLogic is responsible for handling Reversi/Othello rules and updating the
 * board state.
 *
 * This will hold a reference to board. All modifications to board (after initialization)
 * should go through this class to validate the actions with the game rules.
 *
 * For any change made to the board, the registered IBoardUpdateListeners will be updated.
 */
public class ReversiLogic extends GameLogic {
    protected final HashMap<Integer, Integer> scoreCache = new HashMap<>();


    /**
     * Constructs a new ReversiLogic Unit to be able to play a game of reversi.
     * @param board Board used for this game.
     */
    public ReversiLogic(Board board) {
        super(board);
    }


    /**
     * Make a move on the board.
     * @param command Represents the move which is to be played.
     * @throws InvalidParameterException If it is an invalid move, no move will be made.
     */
    @Override
    public void play(MoveCommand command) throws InvalidParameterException {
        Collection<BoardIndex> indexes = calculateFlipsFromBoard(command.position, command.playerID);

        if(indexes.isEmpty())
            throw new InvalidParameterException("Invalid play by player " + command.playerID + " to " + command.position);

        this.invalidateCache();

        //set tiles
        board.apply(new SetCommand(command));
        for(BoardIndex index : indexes) {
            board.apply(new SetCommand(command.playerID, index));
        }

        updateBoardListeners(command.position, command.playerID, indexes);
    }


    /**
     * Checks the board to see if the move attempted is valid. Prefer calling play and
     * handling an exception than checking if it is valid first.
     *
     * @param command Includes player and board index.
     */
    @Override
    public boolean isValidMove(MoveCommand command) {
        return (
            board.at(command.position) == -1 &&
            !calculateFlipsFromBoard(command.position, command.playerID).isEmpty()
        );
    }


    /**
     * Find the different moves that could be made and return them.
     *
     * @param player Integer Player ID to check for
     * @return ArrayList moves
     */
    @Override
    public Set<BoardIndex> getValidMoves(int player) {
        //declare an array for possible moves method
        HashSet<BoardIndex> moves = new HashSet<>();

        BoardIndex index = new BoardIndex();

        //Add all valid moves to the set
        for(index.row = 0; index.row < board.size; index.row++)
            for(index.column = 0; index.column < board.size; index.column++)
                if(isValidMove(new MoveCommand(player, index)))
                    moves.add(new BoardIndex(index)); //adds the valid move into the array of moves

        return moves;
    }


    /**
     * Returns the score of the Player ID passed in
     *
     * @param player Integer Player ID
     * @return Score for the given player.
     */
    @Override
    public int getScore(int player) {
        int score = 0;
        if(validCache && scoreCache.containsKey(player))
            score = scoreCache.get(player);
        else {
            validCache = true;
            //look for the instances of the role on the board
            for(int r = 0; r < board.size; r++)
                for(int c = 0; c < board.size; c++)
                    if(board.at(new BoardIndex(r, c)) == player)
                        score++;

            scoreCache.put(player, score);
        }
        return score;
    }


    /**
     * Retrieve the initial setup commands based on the specific game logic.
     *
     * @param players Array of the player ids used in current game in order.
     * @return List of the moves to be made to create the initial state.
     */
    @Override
    public Collection<SetCommand> getSetupCommands(int[] players) {
        LinkedList<SetCommand> list = new LinkedList<>();
        final int size = board.size;
        switch(players.length) {
        case 2:
            list.add(new SetCommand(players[0], new BoardIndex(size / 2 - 1,size / 2 - 1)));
            list.add(new SetCommand(players[1], new BoardIndex(size / 2 - 1,size / 2)));
            list.add(new SetCommand(players[1], new BoardIndex(size / 2,size / 2 -1)));
            list.add(new SetCommand(players[0], new BoardIndex(size / 2,size / 2)));
            break;
        case 4:
            list.add(new SetCommand(players[0], new BoardIndex(size / 2 - 1,size / 2 - 1)));
            list.add(new SetCommand(players[0], new BoardIndex(size / 2,size / 2 + 1)));
            list.add(new SetCommand(players[0], new BoardIndex(size / 2 + 1,size / 2)));
            list.add(new SetCommand(players[1], new BoardIndex(size / 2,size / 2)));
            list.add(new SetCommand(players[1], new BoardIndex(size / 2 - 1,size / 2 - 2)));
            list.add(new SetCommand(players[1], new BoardIndex(size / 2 - 2,size / 2 - 1)));
            list.add(new SetCommand(players[2], new BoardIndex(size / 2 - 1,size / 2)));
            list.add(new SetCommand(players[2], new BoardIndex(size / 2,size / 2 - 2)));
            list.add(new SetCommand(players[2], new BoardIndex(size / 2 + 1,size / 2 - 1)));
            list.add(new SetCommand(players[3], new BoardIndex(size / 2,size / 2 - 1)));
            list.add(new SetCommand(players[3], new BoardIndex(size / 2 - 2,size / 2)));
            list.add(new SetCommand(players[3], new BoardIndex(size / 2 - 1,size / 2 + 1)));
            break;
        default:
            throw new IllegalArgumentException("Player Count must be 2 or 4");
        }

        return list;
    }


    /**
     * In the event board is changed outside of this class, this function will invalidate
     * the cache and force it to be regenerated given the current board state. Do not rely
     * on regular calls to this function.
     */
    @Override
    public void invalidateCache() {
        super.invalidateCache();
        scoreCache.clear();
    }


    /**
     * Figures out which index would be flipped if a piece was added to the given BoardIndex based on the current board state.
     * @param origin the board index to add a new piece on the board
     * @param playerId the player ID of the newly placed piece at the board index
     * @return the board indexes which should be flipped.
     */
    protected Collection<BoardIndex> calculateFlipsFromBoard(BoardIndex origin, int playerId) {
        List<BoardIndex> flipped = new LinkedList<>();

        for(int i = 0; i < 8; i++) {
            List<BoardIndex> rowFlipped = new LinkedList<>();
            int dr = i < 3 ? -1 : (i > 4 ? 1 : 0);  //change in row
            int dc = i % 3 == 0 ? -1 : (i % 3 == 1 ? 1 : 0); //change in column

            try {
                int move = 1;
                BoardIndex index = new BoardIndex(origin.row + dr, origin.column + dc);
                while(board.at(index) != playerId) {
                    if(board.at(index) == -1)
                        throw new Throwable();
                    rowFlipped.add(index);
                    move++;
                    index = new BoardIndex(origin.row + dr * move, origin.column + dc * move);
                }
                flipped.addAll(rowFlipped);
            }
            catch(Throwable e) {
                // deliberately empty
            }
        }

        return flipped;
    }
}
