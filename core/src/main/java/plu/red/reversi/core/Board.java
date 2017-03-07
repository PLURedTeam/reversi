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
    private PlayerRole[][] board;
    public final int size;


    /**
     * Constructor for initializing board array
     */
    public Board(int sz){
        size = sz;
        board = new PlayerRole[sz][sz];
    }

    /**
     * Copy constructor creating a copy of
     */
    public Board(Board b){
        size = b.size;
        PlayerRole[][] copyBoard = new PlayerRole[size][size];
        for(int r = 0; r < size; r++){
            for(int c = 0; c < size; c++){
                copyBoard[r][c] = board[r][c];
            }
        }//end loop
    }

    /**
     * Returns the score of the PlayerRole object passed in
     * @param role, color of the player
     * @return score, number of instances of the player on the board
     */
    int getScore(PlayerRole role){
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
     * Checks the board to see if the move is valid
     * @param role, color of the player
     * @param index, square of the board move is attempting to be made onto
     */
    boolean isValidMove(PlayerRole role, BoardIndex index){
        //check if the index is out of bounds
        if(index.row >= size || index.column >= size)
            return false;

        //This loop calls the private function isValidMove for each direction
        for(int i = 0; i < 8; i++){
            int dx = i < 3 ? -1 : ( i > 4 ? 1 : 0);
            int dy = i % 3 == 0 ? -1 : ( i % 3 == 1 ? 1 : 0);
            //checks if the move is valid
            if(isValidMove(role, index, dx, dy))
                return true;
        }//end loop
        return false;
    }

    /**
     * Private method isValidMove handles the loop to check
     * @param i for the BoardIndex
     * @param dx change in x
     * @param dy change in y
     * @return if the move is valid
     */
    private boolean isValidMove(PlayerRole role, BoardIndex i, int dx, int dy){
        if(board[dx][dy] == role)
            return false;

        for(int j = 1; j<size; j++){
            try {
                //if the tile is occupied by a piece of the same color
                if(board[dx*j][dy*j] == role) {
                    return true;
                }
                //if the tile is occupied by no piece
                if(board[dx*j][dy*j] == PlayerRole.NONE) {
                    return false;
                }
                //if the tile is occupied by a piece of
            }
            catch(IndexOutOfBoundsException e){
                System.err.println("IndexOutOfBoundsException: " + e.getMessage());
            }
        }

        return false;
    }


    /**
     * Find the different moves that could be made
     * @param role of the player
     * @return ArrayList list
     */
    ArrayList getPossibleMoves(PlayerRole role){
        ArrayList<BoardIndex> moves = new ArrayList<BoardIndex>();

        return moves;

    }



}
