package plu.red.reversi.core.game;

import plu.red.reversi.core.SettingsLoader;
import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.command.SetCommand;
import plu.red.reversi.core.listener.IBoardUpdateListener;

import java.util.*;

/**
 * Glory to the Red Team.
 *
 * Represents the state of the board at a particular instant in time
 */
public class Board {

    // ***********
    //  Listeners
    // ***********

    // This listener exists only because of the separation of Core and Client packages, which allows for only one-way
    //  communication between classes in different modules
    protected HashSet<IBoardUpdateListener> listenerBoardUpdate = new HashSet<>();

    /**
     * Registers an IFlipListener that will have signals sent to it when Flips are applied.
     *
     * @param listener IFlipListener to register
     */
    public void addBoardUpdateListener(IBoardUpdateListener listener) {
        listenerBoardUpdate.add(listener);
    }

    /**
     * Unregisters an existing IFlipListener that has previously been registered. Does nothing if the specified
     * IFlipListener has not previously been registered.
     *
     * @param listener IFlipListener to unregister
     */
    public void removeBoardUpdateListener(IBoardUpdateListener listener) {
        listenerBoardUpdate.remove(listener);
    }



    // *********
    //  Members
    // *********

    private int[][] board; // 2D array that represents the board. -1 represents an empty space.
    public final int size;
    protected final HashMap<Integer, Integer> scoreCache = new HashMap<>();


    /**
     * Basic Constructor. Constructs a new Board object with the given <code>size</code>.
     *
     * @param size Integer size of the Board
     */
    public Board(int size){
        this.size = size;
        board = new int[size][size];
        //Arrays.fill(scoreCache, -1);
        for(int i = 0; i < size; i++) {
            Arrays.fill(board[i], -1);
        }
    }

    /**
     * Copy Constructor. Constructs a new Board object that is a copy of an existing Board object.
     *
     * @param b Board object to copy
     */
    public Board(Board b){
        size = b.size;

        // Copy the board
        board = new int[size][size];
        for(int r = 0; r < size; r++){
            board[r] = Arrays.copyOf(b.board[r], size);
        }// end loop

        // Can't copy the scoreCache because of ConcurrentModificationExceptions
    }

    /**
     * Method to setup the initial board position. Usually called from the initialization method of Game.
     *
     * @param game Game object this board is attached to during setup; generally used to determine player colors and size.
     */
    public static LinkedList<BoardCommand> getSetupCommands(Game game) {
        return getSetupCommands(game.getUsedPlayers(),
                game.getSettings().get(SettingsLoader.GAME_BOARD_SIZE, Integer.class));
    }

    /**
     * Method to setup the initial board position. Usually called from the initialization method of Game.
     *
     * @param usedPlayers Array of Integer Player IDs used in this setup
     * @param size Size of the board to setup
     */
    public static LinkedList<BoardCommand> getSetupCommands(Integer[] usedPlayers, int size) {
        LinkedList<BoardCommand> list = new LinkedList<>();
        switch(usedPlayers.length) {
            case 2:
                list.add(new SetCommand(usedPlayers[0], new BoardIndex(size / 2 - 1,size / 2 - 1)));
                list.add(new SetCommand(usedPlayers[1], new BoardIndex(size / 2 - 1,size / 2)));
                list.add(new SetCommand(usedPlayers[1], new BoardIndex(size / 2,size / 2 -1)));
                list.add(new SetCommand(usedPlayers[0], new BoardIndex(size / 2,size / 2)));
                break;
            case 4:
                list.add(new SetCommand(usedPlayers[0], new BoardIndex(size / 2 - 1,size / 2 - 1)));
                list.add(new SetCommand(usedPlayers[0], new BoardIndex(size / 2,size / 2 + 1)));
                list.add(new SetCommand(usedPlayers[0], new BoardIndex(size / 2 + 1,size / 2)));
                list.add(new SetCommand(usedPlayers[1], new BoardIndex(size / 2,size / 2)));
                list.add(new SetCommand(usedPlayers[1], new BoardIndex(size / 2 - 1,size / 2 - 2)));
                list.add(new SetCommand(usedPlayers[1], new BoardIndex(size / 2 - 2,size / 2 - 1)));
                list.add(new SetCommand(usedPlayers[2], new BoardIndex(size / 2 - 1,size / 2)));
                list.add(new SetCommand(usedPlayers[2], new BoardIndex(size / 2,size / 2 - 2)));
                list.add(new SetCommand(usedPlayers[2], new BoardIndex(size / 2 + 1,size / 2 - 1)));
                list.add(new SetCommand(usedPlayers[3], new BoardIndex(size / 2,size / 2 - 1)));
                list.add(new SetCommand(usedPlayers[3], new BoardIndex(size / 2 - 2,size / 2)));
                list.add(new SetCommand(usedPlayers[3], new BoardIndex(size / 2 - 1,size / 2 + 1)));
                break;
            default:
                throw new IllegalArgumentException("Player Count must be 2 or 4");
        }

        return list;
    }

    /**
     * Apply multiple commands at once.
     * @param commands List of commands to be applied in order.
     */
    public void applyCommands(LinkedList<BoardCommand> commands) {
        for(BoardCommand c : commands) {
            if(c instanceof MoveCommand)
                apply((MoveCommand)c);
            else if(c instanceof SetCommand)
                apply((SetCommand)c);
        }
    }

    /**
     * Finds the value at a specific place of the board
     *
     * @param index being searched for
     * @return PlayerID at <code>index</code>
     * @throws IndexOutOfBoundsException if the index that is passed in is out of bounds
     */
    public final int at(BoardIndex index) throws IndexOutOfBoundsException{
        return board[index.row][index.column];
    }

    /**
     * Returns the score of the Player ID passed in
     *
     * @param player Integer Player ID
     * @return Integer score representing the number of instances of the Player ID on the board
     */
    public int getScore(int player){

        if(scoreCache.containsKey(player)) return scoreCache.get(player);

        int score = 0;
        //look for the instances of the role on the board
        for(int r = 0; r < size; r++){
            for(int c = 0; c < size; c++){
                if(board[r][c] == player){
                    score++;
                }
            }
        }//end loop

        scoreCache.put(player, score);
        return score;
    }

    /**
     * Find the total number of pieces on the board.
     * @return Total pieces on board.
     */
    public int getTotalPieces() {

        int sum = 0;
        for(int r = 0; r < size; r++){
            for(int c = 0; c < size; c++){
                if(board[r][c] >= 0){
                    sum++;
                }
            }
        }
        return sum;
    }

    /**
     * Checks the board to see if the move attempted is valid
     *
     * @param player Integer Player ID to check
     * @param index square of the board move is attempting to be made onto
     */
    public boolean isValidMove(int player, BoardIndex index){
        return at(index) == -1 && !calculateFlipsFromBoard(index, player).isEmpty();
    }

    /**
     * Checks the board to see if the move attempted is valid
     * @param command Checks to see if this move is valid.
     * @return True if it is a valid move, otherwise false.
     */
    public boolean isValidMove(MoveCommand command) {
        return isValidMove(command.playerID, command.position);
    }

    /**
     * Find the different moves that could be made and store them into an ArrayList
     *
     * @param player Integer Player ID to check for
     * @return ArrayList moves
     */
    public Set<BoardIndex> getPossibleMoves(int player ){
        //declare an array for possible moves method
        HashSet<BoardIndex> moves = new HashSet<>();

        BoardIndex indx = new BoardIndex();

        //This loop calls the function isValidMove for each position on the board
        for(indx.row = 0; indx.row < size; indx.row++) {
            for(indx.column = 0; indx.column < size; indx.column++) {
                //checks if the move is valid
                if (isValidMove(player, indx)) {
                    moves.add(new BoardIndex(indx)); //adds the valid move into the array of moves
                }
            }
        }//end loop
        return moves;

    }

    /**
     * Figures out which index would be flipped if a piece was added to the given BoardIndex based on the current board state.
     * @param origin the board index to add a new piece on the board
     * @param playerId the player ID of the newly placed piece at the board index
     * @return the board indexes which should be flipped.
     */
    public Collection<BoardIndex> calculateFlipsFromBoard(BoardIndex origin, int playerId) {

        List<BoardIndex> flipped = new LinkedList<>();

        for(int i = 0; i < 8; i++) {
            int dr = i < 3 ? -1 : (i > 4 ? 1 : 0);
            int dc = i % 3 == 0 ? -1 : (i % 3 == 1 ? 1 : 0);

            List<BoardIndex> rowFlipped = new LinkedList<>();

            try {

                BoardIndex index = new BoardIndex(origin.row + dr, origin.column + dc);
                int move = 1;

                while(at(index) != playerId) {

                    if(at(index) == -1)
                        throw new Throwable();

                    rowFlipped.add(index);

                    move++;
                    index = new BoardIndex(origin.row + dr * move, origin.column + dc * move);
                }

                flipped.addAll(rowFlipped);
            }
            catch (Throwable e) {
                // deliberately empty
            }
        }

        return flipped;
    }


    /**
     * Applies the move made, updating the board
     * @param c command made
     */
    public void apply(MoveCommand c) {
        //invalidate the entire cache
        scoreCache.clear();

        Collection<BoardIndex> indexes = calculateFlipsFromBoard(c.position, c.playerID);

        //actually set the tile
        board[c.position.row][c.position.column] = c.playerID;

        for(IBoardUpdateListener bul : listenerBoardUpdate) {
            bul.onBoardUpdate(c.position, c.playerID, indexes);
        }

        // TODO: Update specification. Current UI requires for board state to be updated after, but shoul it be before?
        for(BoardIndex index : indexes) {
            board[index.row][index.column] = c.playerID;
        }
    }

    public void apply(SetCommand c) {
        //invalidate the cache for this player
        scoreCache.remove(c.playerID);
        //set the tile
        board[c.position.row][c.position.column] = c.playerID;

        for(IBoardUpdateListener bul : listenerBoardUpdate) {
            bul.onBoardUpdate(c.position, c.playerID, new LinkedList<>());
        }
    }

    public void apply(BoardCommand c){
        if(c instanceof SetCommand)
            apply((SetCommand)c);
        else if(c instanceof  MoveCommand)
            apply((MoveCommand)c);
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof Board)) return false;
        Board b = (Board)o;

        //if the size isn't the same return false
        if(this.size != b.size)
            return false;

        for(int r = 0; r < size; r++)
            for(int c = 0; c < size; c++){
                if(board[r][c] != b.board[r][c])
                    return false;
            }
        return true;
    }
}
