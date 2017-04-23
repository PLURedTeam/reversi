package plu.red.reversi.core.game.logic;

import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.command.SetCommand;
import plu.red.reversi.core.game.Board;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.listener.IBoardUpdateListener.BoardUpdate;

import java.security.InvalidParameterException;
import java.util.*;

/**
 * GoLogic is responsible for handling the rules of Chinese Go and updating the board state.
 *
 * This is designed as a semi-singleton class, it has to have instances to support inheritance,
 * but you should not need to construct GameLogic class except at the beginning of a new game.
 * This class will update history with new changes, and update listeners as needed. Pay
 * attention to the defaults, and how it will automatically reference Game.
 *
 * This will hold a reference to board. All modifications to board (after initialization)
 * should go through this class to validate the actions with the game rules.
 *
 * For any change made to the board, the registered IBoardUpdateListeners will be updated
 * by default, and you can manually specify otherwise in each case.
 */
public class GoLogic extends GameLogic {

    /**
     * Type Getter. Retrieves the type of this GameLogic.
     *
     * @return Type of GameLogic
     */
    public Type getType() { return Type.GO; }

    /**
     * Make a move on the board.
     *
     * @param command Represents the move which is to be played.
     * @param board   Board to apply commands to.
     * @param notify  True if this should notify subscribed listeners.
     * @param record  True if this should update the game history.
     * @return This object for chaining.
     * @throws InvalidParameterException If it is an invalid move, no move will be made.
     */
    @Override
    public GameLogic play(MoveCommand command, Board board, boolean notify, boolean record) throws InvalidParameterException {
        if(!isValidMove(command)) //TODO: calculate as a result of other things to increase efficiency
            throw new InvalidParameterException("Invalid play by player " + command.playerID + " to " + command.position);

        if(!Group.discover(board, command.position, command.playerID).hasLiberty(board))
            throw new InvalidParameterException("Invalid play by player " + command.playerID + " to " + command.position);

        BoardUpdate update = new BoardUpdate();
        if(notify) update.added.add(command.position);

        //Remove any groups which no longer have liberties
        //TODO: this is painfully inefficient in every way
        Set<Group> groups = getGroups(board);
        for(Group group : groups) {
            if(group.hasLiberty(board)) continue;
            for(BoardIndex index : group) {
                apply(new SetCommand(-1, command.position), board, false, false);
                if(notify) update.removed.add(index);
            }
        }

        if(record) game.getHistory().addCommand(command);
        if(notify) updateBoardListeners(update);
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
    public boolean isValidMove(MoveCommand command, Board board) {
        if(tileHasLiberty(board, command.position)) return true;

        //TODO: use caching to speed this up
        Group group = new Group(board, command.position, command.playerID);
        return group.hasLiberty(board);
    }


    /**
     * Find the different moves that could be made and return them.
     *
     * @param player Integer Player ID to check for
     * @param board  Board to apply commands to.
     * @return ArrayList moves
     */
    @Override
    public Set<BoardIndex> getValidMoves(int player, Board board) {
        Set<BoardIndex> moves = new HashSet<>();

        for(BoardIndex i : board)
            if(isValidMove(new MoveCommand(player, i)))
                moves.add(i);

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
    public int getScore(int player, Board board) {
        return 0;
    }


    /**
     * Retrieve the initial setup commands based on the specific game gameLogic.
     *
     * @param players Array of the player ids used in current game in order.
     * @param size of board for which to generate the setup commands for.
     * @return List of the moves to be made to create the initial state.
     * @throws IllegalArgumentException If player count is invalid.
     */
    @Override
    public Collection<SetCommand> getSetupCommands(int[] players, int size) throws IllegalArgumentException {
        return new LinkedList<>();
    }


    /**
     * Assign every tile a group id value based on horizontal and vertical connections.
     * Tiles will have the same ID if they are horizontally or vertically connected,
     * and not if they are only connected by diagonal values.
     * @param board Board to search for groups on.
     * @return A board of group ids which line up with the main game board.
     */
    private Set<Group> getGroups(Board board) {
        Board groupb = new Board(board); //TODO: replace with union-find
        HashSet<Group> groups = new HashSet<>();

        int groupID = 0;
        for(BoardIndex index : board) {
            final int player = board.at(index);
            if(groupb.at(index) >= 0 || player < 0) continue;

            Group group = new Group(board, index, player);
            groupb.applyAll(group, groupID);
            groups.add(group);
            if(!group.isEmpty()) groupID++;
        }

        return groups;
    }


    /**
     * Checks if a specific tile on the board has any liberties.
     * @param board Board used in for the check.
     * @param index Index on the board to check.
     * @return Number of liberties around a single tile.
     */
    public static boolean tileHasLiberty(Board board, BoardIndex index) {
        for (int i = 0; i < 4; ++i) {
            //Get vector of direction
            final int dr = i < 2 ? -1 : 1;
            final int dc = i % 2 == 0 ? -1 : 1;

            final BoardIndex p = new BoardIndex(index.row + dr, index.column + dc);
            if(board.at(p) < 0) return true;
        }

        return false;
    }



    /**
     * This represents a grouping of tiles which are joined by horizontal or vertical connections.
     * These are used as the primary units in the Game of Go.
     *
     * TODO: Union-Find and Caching of group information.
     */
    private static class Group extends HashSet<BoardIndex> {
        /**
         * Basic constructor.
         */
        public Group() {
            super();
        }


        /**
         * Constructor which will initialize the new object to the group it discovers.
         * @param index Origin point of the search.
         * @param board Board to discover the group on.
         * @param player Player who's pieces are being analyzed
         */
        public Group(Board board, BoardIndex index, final int player) {
            super(discover(board, index, player));
        }


        /**
         * Checks if this group has at least one free spot around it. Prefer this to countLiberties
         * where possible.
         * @return True if this group has at least one liberty.
         */
        public boolean hasLiberty(Board board) {
            for(BoardIndex index : this)
                if(GoLogic.tileHasLiberty(board, index)) return true;

            return false;
        }


        /**
         * Use breadth first search to find the set of all group members given a specific starting tile.
         * Note, this will not check the first tile for player validity so it can be used to check for what group
         * there would be if a piece were played there.
         * @param index Origin point of the search.
         * @param board Board to discover the group on.
         * @param player Player who's pieces are being analyzed
         * @return A list of all members in the group, null if there are none
         */
        public static Group discover(Board board, BoardIndex index, final int player) {
            Group group = new Group();

            if(player < 0) //invalid player, so there is no group
                return group;

            Queue<BoardIndex> queue = new LinkedList<>();
            queue.add(index);

            while(!queue.isEmpty()) {
                index = queue.poll();

                //add it to the set (will not be checked for player value by intent)
                group.add(index);

                //Queue all adjacent tiles
                for(int i = 0; i < 4; i++) {
                    //Calculate direction
                    final int dr = i < 2 ? -1 : 1;
                    final int dc = i % 2 == 0 ? -1 : 1;

                    //Check boundaries
                    final int nr = index.row + dr;
                    final int nc = index.column + dc;
                    if(nr < 0 || nr >= board.size) continue;
                    if(nc < 0 || nc >= board.size) continue;

                    //Add to queue if we have not already discovered it in this search
                    BoardIndex nIndex = new BoardIndex(nr, nc);

                    //make sure it's the same player and that we have not already added it
                    if((board.at(index) == player) && (!group.contains(nIndex)))
                        queue.add(nIndex);
                }
            }
            return group;
        }
    }
}
