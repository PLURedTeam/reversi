package plu.red.reversi.core;

/**
 * Created by daniel on 3/5/17.
 * Glory to the Red Team.
 */

import java.util.ArrayList;

/**
 * Represents the state of the board at a particular instant in time
 */
public class Board {
    private PlayerRole[][] board;//2D array that represents the board
    public final int size;


    /**
     * Constructor for initializing board array
     */
    public Board(int sz){
        size = sz;
        board = new PlayerRole[sz][sz];
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                board[i][j] = PlayerRole.NONE;
            }
        }
        board[(size/2)-1][((size/2)-1)] = PlayerRole.WHITE;
        board[((size/2)-1)][((size/2)-1)+1] = PlayerRole.BLACK;
        board[((size/2)-1)+1][((size/2)-1)] = PlayerRole.BLACK;
        board[((size/2)-1)+1][((size/2)-1)+1] = PlayerRole.WHITE;

    }

    /**
     * Copy constructor creating a copy of
     * @param b for the board
     */
    public Board(Board b){
        size = b.size;
        board = new PlayerRole[size][size];
        for(int r = 0; r < size; r++){
            for(int c = 0; c < size; c++){
                board[r][c] = b.board[r][c];
            }
        }//end loop
    }

    /**
     * finds the value at a specific place of the board
     * @param index
     * @return role
     * @throws IndexOutOfBoundsException
     */
    final PlayerRole at(BoardIndex index) throws IndexOutOfBoundsException{
        return board[index.row][index.column];
    }

    /**
     * Returns the score of the PlayerRole object passed in
     * @param role, color of the player
     * @return score, number of instances of the player on the board
     */
    public int getScore(PlayerRole role){
        int score=0;

        //look for the instances of the role on the board
        for(int r = 0; r < size; r++){
            for(int c = 0; c < size; c++){
                if(board[r][c] == role){
                    score++;
                }
            }
        }//end loop

        return score;
    }

    /**
     * Checks the board to see if the move attempted is valid
     * @param role, color of the player
     * @param index, square of the board move is attempting to be made onto
     */
    public boolean isValidMove(PlayerRole role, BoardIndex index){
        //check if the index is out of bounds
        if(index.row >= size || index.column >= size)
            return false;

        //check if the index is empty
        if(board[index.row][index.column] != PlayerRole.NONE)
            return false;

        //This loop calls the private function isValidMove for each direction
        for(int i = 0; i < 8; i++){
            int dr = i < 3 ? -1 : ( i > 4 ? 1 : 0);
            int dc = i % 3 == 0 ? -1 : ( i % 3 == 1 ? 1 : 0);
            //checks if the move is valid
            if(isValidMove(role, dr, dc, index)) {
                return true;
            }
        }//end loop

        return false;
    }

    /**
     * Private method isValidMove handles the loop to check move validity in each direction
     * @param dr change in x
     * @param dc change in y
     * @return if the move is valid
     */
    private boolean isValidMove(PlayerRole role, int dr, int dc, BoardIndex i){
        try {
            PlayerRole r = board[i.row + dr][i.column + dc];
            if (r == role || !r.isValid())
                return false;
        }
        catch(IndexOutOfBoundsException e){
            return false;
        }

        for(int j = 1; j<size; j++){
            PlayerRole r=PlayerRole.NONE;
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
     * @param role of the player
     * @return ArrayList moves
     */
    public ArrayList<BoardIndex> getPossibleMoves(PlayerRole role ){
        //declare an array of type BoardIndex for possible moves method
        ArrayList<BoardIndex> moves = new ArrayList<BoardIndex>();

        BoardIndex indx = new BoardIndex();

        //This loop calls the function isValidMove for each direction
        for(indx.row = 0; indx.row < size; indx.row++) {
            for(indx.column = 0; indx.column < size; indx.column++) {
                //checks if the move is valid
                if (isValidMove(role, indx)) {
                    moves.add(new BoardIndex(indx)); //adds the valid move into the array of moves
                }
            }
        }//end loop
        return moves;

    }


    /**
     * Applies the move made, updating the board
     * @param c command made
     * @param flipTiles
     */
    public void apply(CommandMove c, boolean flipTiles) {
        if (flipTiles) {
            for(int i = 0; i < 8; i++) {
                int dr = i < 3 ? -1 : (i > 4 ? 1 : 0);
                int dc = i % 3 == 0 ? -1 : (i % 3 == 1 ? 1 : 0);
                //Actually flip tile
                flipTiles(c, dr, dc, 0);
            }
        }
    }

    private boolean flipTiles(CommandMove c, int dr, int dc, int count){
        PlayerRole tile = null;
        try{
            tile = board[dr*count + c.position.row][dc*count + c.position.column];
        }
        catch (ArrayIndexOutOfBoundsException e){
            return false;
        }
        if(tile == c.player)
            return true;
        if(tile.isValid()){
            if(flipTiles(c, dr, dc, count + 1)){
                board[c.position.row][c.position.column] = c.player;
                return true;
            }
        }
        return false;
    }

    /**
     * Applies a move and flips tiles
     * @param c command made
     */
    public void apply(CommandMove c) {
        apply(c, true);
    }

    public boolean equals(final Board b){
        //if the size isn't the same return false
        if(this.size != b.size)
            return false;

        for(int r = 0; r < size; r++)
            for(int c = 0; c < size; c++){
                if(board[r][c] != b.at(new BoardIndex(r, c)))
                    return false;
            }
        return true;
    }



}
