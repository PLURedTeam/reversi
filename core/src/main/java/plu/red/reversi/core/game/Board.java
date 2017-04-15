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
    private int[][] board; // 2D array that represents the board. -1 represents an empty space.
    public final int size;


    /**
     * Basic Constructor. Constructs a new Board object with the given size.
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
     * Finds the value at a specific place of the board
     *
     * @param index being searched for
     * @return PlayerID at index
     * @throws IndexOutOfBoundsException if the index that is passed in is out of bounds
     */
    public int at(BoardIndex index) throws IndexOutOfBoundsException{
        return board[index.row][index.column];
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
     * Used to set a piece on the board. Do this through the Board's GameLogic class
     * if this board has been attached to one.
     * @param c Command specifying a location and its new player value.
     */
    public void apply(SetCommand c) {
        apply(c.position, c.playerID);
    }


    /**
     * Used to set a piece on the board. Do this through the Board's GameLogic class
     * if this board has been attached to one.
     * @param index Location on the board to change
     * @param value Value to set the location to
     */
    public void apply(BoardIndex index, int value) {
        board[index.row][index.column] = value;
    }


    /**
     * Apply multiple commands at once.
     * @param commands Collection of commands to be applied.
     */
    public void applyAll(Collection<SetCommand> commands) {
        for(SetCommand c : commands) apply(c);
    }


    /**
     * Set the value for multiple indices at once.
     * @param indices Places to change the value of.
     * @param value Value to change them to.
     */
    public void applyAll(Collection<BoardIndex> indices, int value) {
        for(BoardIndex index : indices) apply(index, value);
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
