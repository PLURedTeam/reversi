package plu.red.reversi.core.game.logic;

import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.command.SetCommand;
import plu.red.reversi.core.game.Board;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.listener.IBoardUpdateListener;
import plu.red.reversi.core.listener.IBoardUpdateListener.BoardUpdate;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * ReversiLogic is responsible for handling Reversi/Othello rules and updating the
 * board state.
 *
 * This is designed as a semi-singleton class, it has to have instances to support inheritance,
 * but you should not need to construct GameLogic class except at the beginning of a new game.
 * This class will update histroy with new changes, and update listeners as needed. Pay
 * attention to the defaults, and how it will automatically reference Game.
 *
 * This will hold a reference to board. All modifications to board (after initialization)
 * should go through this class to validate the actions with the game rules.
 *
 * For any change made to the board, the registered IBoardUpdateListeners will be updated
 * by default, and you can manually specify otherwise in each case.
 */
public class ReversiLogic extends GameLogic {
    /**
     * Constructs a new ReversiLogic Unit to be able to play a game of reversi.
     * @param game Game this logic is used for.
     */
    public ReversiLogic(Game game) {
        super(game);
    }


    /**
     * This constructor should only be used for testing.
     */
    public  ReversiLogic() {super();}


    /**
     * Constructs a new cache objcet of the appropriate subtype.
     * @return A new GameLogic cache of the appropriate subtype.
     */
    @Override
    public GameLogicCache createCache() {
        return new ReversiLogicCache();
    }


    /**
     * Make a move on the board.
     *
     * @param command Represents the move which is to be played.
     * @param board   Board to apply commands to.
     * @param notify  True if this should notify subscribed listeners.
     * @param record True if this should update the game history.
     * @throws InvalidParameterException If it is an invalid move, no move will be made.
     */
    @Override
    public GameLogic play(GameLogicCache cache, Board board, MoveCommand command, boolean notify, boolean record) throws InvalidParameterException {
        Collection<BoardIndex> indexes = calculateFlipsFromBoard(command.position, command.playerID, board);

        if(indexes.isEmpty())
            throw new InvalidParameterException("Invalid play by player " + command.playerID + " to " + command.position);

        //set tiles
        apply(cache, board, new SetCommand(command), false, false);
        for(BoardIndex index : indexes) {
            apply(cache, board, new SetCommand(command.playerID, index), false, false);
        }

        if(notify) {
            BoardUpdate update = new BoardUpdate();
            update.player = command.playerID;
            update.flipped = indexes;
            update.added.add(command.position);
            updateBoardListeners(update);
        }
        if(record) game.getHistory().addCommand(command);

        return this;
    }


    /**
     * Checks the board to see if the move attempted is valid.
     * Prefer calling play and handling an exception than checking
     * if it is valid first.
     *
     * @param command Includes player and board index.
     * @param board   Board to apply commands to.
     */
    @Override
    public boolean isValidMove(GameLogicCache cache, Board board, MoveCommand command) {
        return (
                board.at(command.position) == -1 &&
                !calculateFlipsFromBoard(command.position, command.playerID, board).isEmpty()
        );
    }


    /**
     * Find the different moves that could be made and return them.
     *
     * @param player Integer Player ID to check for
     * @param board  Board to apply commands to.
     * @return ArrayList moves
     */
    @Override
    public Set<BoardIndex> getValidMoves(GameLogicCache cache, Board board, int player) {
        //declare an array for possible moves method
        HashSet<BoardIndex> moves = new HashSet<>();

        BoardIndex index = new BoardIndex();

        //Add all valid moves to the set
        for(index.row = 0; index.row < board.size; index.row++)
            for(index.column = 0; index.column < board.size; index.column++)
                if(isValidMove(cache, board, new MoveCommand(player, index)))
                    moves.add(new BoardIndex(index)); //adds the valid move into the array of moves

        return moves;
    }


    /**
     * Returns the score of the Player ID passed in
     *
     * @param player Integer Player ID.
     * @param board  Board to apply commands to.
     * @return Score for the given player.
     */
    @Override
    public int getScore(GameLogicCache cache, Board board, int player) {
        int score = 0;
        //look for the instances of the role on the board
        for(int r = 0; r < board.size; r++)
            for(int c = 0; c < board.size; c++)
                if(board.at(new BoardIndex(r, c)) == player)
                    score++;
        return score;
    }


    /**
     * Retrieve the initial setup commands based on the specific game gameLogic.
     *
     * @param players Array of the player ids used in current game in order.
     * @param size of board for which to generate the setup commands for.
     * @return List of the moves to be made to create the initial state.
     */
    @Override
    public Collection<SetCommand> getSetupCommands(int[] players, int size) {
        LinkedList<SetCommand> list = new LinkedList<>();
        switch(players.length) {
        case 2:
            list.add(new SetCommand(players[0], new BoardIndex(size / 2 - 1,size / 2 - 1)));
            list.add(new SetCommand(players[1], new BoardIndex(size / 2 - 1,size / 2)));
            list.add(new SetCommand(players[0], new BoardIndex(size / 2,size / 2)));
            list.add(new SetCommand(players[1], new BoardIndex(size / 2,size / 2 -1)));
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
     * Figures out which index would be flipped if a piece was added to the given BoardIndex based on the current board state.
     * @param origin the board index to add a new piece on the board
     * @param playerId the player ID of the newly placed piece at the board index
     * @return the board indexes which should be flipped.
     */
    protected Collection<BoardIndex> calculateFlipsFromBoard(BoardIndex origin, int playerId, Board board) {
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
