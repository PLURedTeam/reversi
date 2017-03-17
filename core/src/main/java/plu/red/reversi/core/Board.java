package plu.red.reversi.core;

/**
 * Created by daniel on 3/5/17.
 * Glory to the Red Team.
 */

import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.command.SetCommand;
import plu.red.reversi.core.listener.IFlipListener;
import plu.red.reversi.core.player.Player;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Represents the state of the board at a particular instant in time
 */
public class Board {

    // ***********
    //  Listeners
    // ***********

    // This listener exists only because of the separation of Core and Client packages, which allows for only one-way
    //  communication between classes in different modules
    protected HashSet<IFlipListener> listenerSetFlips = new HashSet<IFlipListener>();

    /**
     * Registers an IFlipListener that will have signals sent to it when Flips are applied.
     *
     * @param listener IFlipListener to register
     */
    public void addFlipListener(IFlipListener listener) {
        listenerSetFlips.add(listener);
    }

    /**
     * Unregisters an existing IFlipListener that has previously been registered. Does nothing if the specified
     * IFlipListener has not previously been registered.
     *
     * @param listener IFlipListener to unregister
     */
    public void removeFlipListener(IFlipListener listener) {
        listenerSetFlips.remove(listener);
    }



    // *********
    //  Members
    // *********

    private PlayerColor[][] board; // 2D array that represents the board
    public final int size;
    public int[] scoreCache;


    /**
     * Constructor for initializing board array
     *
     * @param sz for the size of the board
     */
    public Board(int sz){
        size = sz;
        board = new PlayerColor[sz][sz];
        scoreCache = new int[PlayerColor.validPlayerColors.length];
        Arrays.fill(scoreCache, -1);
        for(int i = 0; i < size; i++) {
            Arrays.fill(board[i], PlayerColor.NONE);
        }
    }

    /**
     * Copy constructor creating a copy of
     *
     * @param b for the board
     */
    public Board(Board b){
        size = b.size;
        scoreCache = Arrays.copyOf(b.scoreCache, b.scoreCache.length);
        board = new PlayerColor[size][size];
        for(int r = 0; r < size; r++){
            board[r] = Arrays.copyOf(b.board[r], size);
        }// end loop
    }

    /**
     * Method to setup the initial board position. Usually called from the initialization method of Game.
     * Set commands to history.
     *
     * @param game Game object this board is attached to during setup; generally used to determine player colors and size.
     */
    public static LinkedList<BoardCommand> getSetupCommands(Game game) {
        //TODO: improve; temporary setup to get used Colors
        PlayerColor color1 = null;
        PlayerColor color2 = null;
        color1 = game.getCurrentPlayer().getRole();
        color2 = color1.getNext(game.getUsedPlayers());

        return getSetupCommands(color1, color2, game.getSettings().get(SettingsLoader.GAME_BOARD_SIZE, Integer.class));
    }

    public static LinkedList<BoardCommand> getSetupCommands(PlayerColor c1, PlayerColor c2, int size) {
        LinkedList<BoardCommand> list = new LinkedList<>();
        list.add(new SetCommand(c1, new BoardIndex(size / 2 - 1,size / 2 - 1)));
        list.add(new SetCommand(c2, new BoardIndex(size / 2 - 1,size / 2)));
        list.add(new SetCommand(c2, new BoardIndex(size / 2,size / 2 -1)));
        list.add(new SetCommand(c1, new BoardIndex(size / 2,size / 2)));
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
     * @return color of the player
     * @throws IndexOutOfBoundsException if the index that is passed in is out of bounds
     */
    public final PlayerColor at(BoardIndex index) throws IndexOutOfBoundsException{
        return board[index.row][index.column];
    }

    /**
     * Returns the score of the PlayerColor object passed in
     *
     * @param role, color of the player
     * @return score, number of instances of the player on the board
     */
    public int getScore(PlayerColor role){
        int ordinal = role.validOrdinal();
        if(scoreCache[ordinal] >= 0)
            return scoreCache[ordinal];

        Arrays.fill(scoreCache, 0);
        //look for the instances of the role on the board
        for(int r = 0; r < size; r++){
            for(int c = 0; c < size; c++){
                if(board[r][c].isValid()){
                    scoreCache[board[r][c].validOrdinal()]++;
                }
            }
        }//end loop

        return scoreCache[ordinal];
    }

    /**
     * Find the total number of pieces on the board.
     * @return Total pieces on board.
     */
    public int getTotalPieces() {
        boolean valid = true;
        int sum = 0;
        for(int x = 0; x < scoreCache.length; ++x) {
            if(scoreCache[x] < 0) {
                valid = false;
                break;
            }
            sum += scoreCache[x];
        }

        if(valid) return sum;

        sum = 0;
        for(int x = 0; x < PlayerColor.validPlayerColors.length; ++x)
            sum += getScore(PlayerColor.validPlayerColors[x]);
        return sum;
    }

    /**
     * Checks the board to see if the move attempted is valid
     *
     * @param role, color of the player
     * @param index, square of the board move is attempting to be made onto
     */
    public boolean isValidMove(PlayerColor role, BoardIndex index){
        //check if the index is out of bounds
        if(index.row >= size || index.column >= size)
            return false;

        //check if the index is empty
        if(board[index.row][index.column] != PlayerColor.NONE)
            return false;

        //This loop calls the private function isValidMove for each direction
        for(int i = 0; i < 8; i++){
            int dr = i < 3 ? -1 : ( i > 4 ? 1 : 0);
            int dc = i % 3 == 0 ? -1 : ( i % 3 == 1 ? 1 : 0);
            //checks if the move is valid
            if(isValidMove(role, dr, dc, index)) {
                return true; //if the move is valid
            }
        }//end loop

        return false; //if the move is invalid
    }

    /**
     * Checks the board to see if the move attempted is valid
     * @param command Checks to see if this move is valid.
     * @return True if it is a valid move, otherwise false.
     */
    public boolean isValidMove(MoveCommand command) {
        return isValidMove(command.player, command.position);
    }

    /**
     * Private method isValidMove handles the loop to check move validity in each direction
     *
     * @param dr change in x
     * @param dc change in y
     * @return if the move is valid
     */
    private boolean isValidMove(PlayerColor role, int dr, int dc, BoardIndex i){
        try {
            PlayerColor r = board[i.row + dr][i.column + dc];
            if (r == role || !r.isValid())
                return false;
        }
        catch(IndexOutOfBoundsException e){
            return false;
        }

        for(int j = 1; j<size; j++){
            PlayerColor r= PlayerColor.NONE;
            try {
                r = board[i.row + dr * j][i.column + dc * j];
            }
            catch(IndexOutOfBoundsException e){
                return false;
            }
            //if the tile is occupied by a piece of the same color
            if(r == role) {
                return true;
            }
            //if the tile is occupied by no piece
            if(!r.isValid()) {
                return false;
            }
        }
        return false;
    }


    /**
     * Find the different moves that could be made and store them into an ArrayList
     *
     * @param color of the player
     * @return ArrayList moves
     */
    public Set<BoardIndex> getPossibleMoves(PlayerColor color ){
        //declare an array for possible moves method
        HashSet<BoardIndex> moves = new HashSet<>();

        BoardIndex indx = new BoardIndex();

        //This loop calls the function isValidMove for each direction
        for(indx.row = 0; indx.row < size; indx.row++) {
            for(indx.column = 0; indx.column < size; indx.column++) {
                //checks if the move is valid
                if (isValidMove(color, indx)) {
                    moves.add(new BoardIndex(indx)); //adds the valid move into the array of moves
                }
            }
        }//end loop
        return moves;

    }


    /**
     * Applies the move made, updating the board
     * @param c command made
     */
    public void apply(MoveCommand c) {
        //actually set the tile
        apply(new SetCommand(c));

        //flip the tiles as a result of placing this one
        for(int i = 0; i < 8; i++) {
            int dr = i < 3 ? -1 : (i > 4 ? 1 : 0);
            int dc = i % 3 == 0 ? -1 : (i % 3 == 1 ? 1 : 0);
            //Actually flip tile
            flipTiles(c, dr, dc, 1);
        }
    }

    public void apply(SetCommand c) {
        //invalidate the cache
        Arrays.fill(scoreCache, -1);
        //set the tile
        board[c.position.row][c.position.column] = c.player;
    }

    /**
     * Private helper method to check for flippable tiles in each direction flip if valid
     * @param c gets the move command, index and color
     * @param dr change in row
     * @param dc change in column
     * @param count
     * @return
     */
    private boolean flipTiles(MoveCommand c, int dr, int dc, int count) {
        PlayerColor tile = null;
        try {
            tile = board[dr * count + c.position.row][dc * count + c.position.column];
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
        if (tile == c.player) {
            // Signal our flip animation
            for(IFlipListener listener : listenerSetFlips)
                listener.doFlip(
                        c.position,
                        new BoardIndex(c.position.row + dr*(count-1), c.position.column + dc*(count-1)),
                        c.player);
            return true;
        }
        if (tile.isValid()) {
            if (flipTiles(c, dr, dc, count + 1)) {
                board[c.position.row + dr * count][c.position.column + dc * count] = c.player;
                return true;
            }
        }
        return false;
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
