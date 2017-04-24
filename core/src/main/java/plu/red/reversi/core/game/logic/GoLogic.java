package plu.red.reversi.core.game.logic;

import com.surelogic.Nullable;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.command.SetCommand;
import plu.red.reversi.core.game.Board;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.listener.IBoardUpdateListener.BoardUpdate;
import plu.red.reversi.core.util.UnionFind;

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
    public final static int[] VALID_PLAYER_COUNTS = {2, 3, 4, 5, 6, 7, 8};


    /**
     * Type Getter. Retrieves the type of this GameLogic.
     *
     * @return Type of GameLogic
     */
    public Type getType() { return Type.GO; }

    /**
     * Constructs a new GoLogic Unit to be able to play a game of Go.
     * @param game Game this logic is used for.
     */
    public GoLogic(Game game) {
        super(game);
    }


    /**
     * This constructor should only be used for testing.
     */
    public GoLogic() {super();}


    /**
     * Constructs a new cache objcet of the appropriate subtype.
     * @return A new GameLogic cache of the appropriate subtype.
     */
    @Override
    public GameLogicCache createCache() {
        return new GoLogicCache();
    }


    /**
     * @return The minimum number of players for the game.
     */
    @Override
    public int minPlayerCount() {
        return 2;
    }


    /**
     * @return The maximum number of players for the game.
     */
    @Override
    public int maxPlayerCount() {
        return 8;
    }


    /**
     * Returns a list of valid player counts. This list will be sorted in ascending order.
     *
     * @return A list of valid player counts in ascending order.
     */
    @Override
    public int[] validPlayerCounts() {
        return VALID_PLAYER_COUNTS;
    }


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
    public GameLogic play(GameLogicCache cache, Board board, MoveCommand command, boolean notify, boolean record) throws InvalidParameterException {
        GoLogicCache gcache = (GoLogicCache)cache;
        if(gcache == null) throw new InvalidParameterException("Incorrect cache type for Go's play.");

        //if a tile is already there, it cannot be valid
        if(board.at(command.position) >= 0)
            throw new InvalidParameterException("Invalid play by player " + command.playerID + " to " + command.position);

        //This will be updated as we progress, and set to true if it is valid.
        // Basically catch right before final actions if it is not a valid play.
        boolean validPlay = tileHasLiberty(board, command.position);

        //attempt to play the move and handle if it turns out to be invalid
        Set<BoardIndex> adjGroups = findAdjGroups(gcache, board, command.position);

        //Use these to keep track so we don't apply the change until we are 100% sure that we won't have to undo it
        BoardUpdate boardUpdate = new BoardUpdate();
        boardUpdate.player = command.playerID;
        boardUpdate.added.add(command.position);
        Collection<BoardIndex> toUnion = new LinkedList<>();
        Collection<BoardIndex> toRemove = new LinkedList<>();

        //for all the groups, check if it will be unioned or removed
        for(BoardIndex index : adjGroups) {
            if(board.at(index) == command.playerID) {
                //we will want to union with this
                toUnion.add(index);

                if(!validPlay) //if we are not sure yet that it is valid, see if this will make it valid
                    validPlay = groupHasLiberty(gcache, board, index, command.position);
            }
            else { //it is not the current player
                //if we will remove it
                if(!groupHasLiberty(gcache, board, index, command.position)) {
                    toRemove.add(index);
                    //For sure a valid play because we are removing the piece
                    validPlay = true;
                }
            }
        }

        if(!validPlay) //handle invalid play
            throw new InvalidParameterException("Invalid play by player " + command.playerID + " to " + command.position);

        //now we can apply the changes
        apply(cache, board, new SetCommand(command), false, false);
        gcache.groups.add(command.position);
        for(BoardIndex index : toUnion)
            //merge this group with all surrounding groups of this player
            gcache.groups.union(index, command.position);
        for(BoardIndex index : toRemove)
            //this will remove the set and add all that was removed to the update
            gcache.groups.removeSet(index, boardUpdate.removed);

        //actually remove the pieces from the board
        for(BoardIndex index : boardUpdate.removed)
            apply(cache, board, new SetCommand(-1, index), false, false);

        cache.addToScore(command.playerID, boardUpdate.removed.size());

        if(record)
            game.getHistory().addCommand(command);
        if(notify)
            updateBoardListeners(boardUpdate);

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
        GoLogicCache gcache = (GoLogicCache)cache;
        if(gcache == null) throw new InvalidParameterException("Incorrect cache type for Go's isValidMove.");

        //if a tile is already there, it cannot be valid
        if(board.at(command.position) >= 0) return false;

        //Process in order of least intensive to most intensive checks, and stop when we know it must be valid
        //Check if the tile has liberties
        if(tileHasLiberty(board, command.position))
            return true;

        //Check if the group it would be part of has liberties or if there will be liberties after playing (i.e. we
        // cause other pieces to be removed).
        //Could join groups, and we don't want to modify board or the disjoint-sets due to computational cost, so
        // instead find all current groups that are of the same player in adjacent tiles
        Set<BoardIndex> groups = findAdjGroups(gcache, board, command.position);
        for(BoardIndex i: groups) {
            //if it is current player, see if it has an opening
            if(board.at(i) == command.playerID)
                //check if the group has liberties excluding the current tile
                if(groupHasLiberty(gcache, board, i, command.position))
                    return true;

            else //it's an opponent, see if it will have no liberties after the move
                if(!groupHasLiberty(gcache, board, i, command.position))
                    return true;
        }

        return false;
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
        GoLogicCache c = (GoLogicCache)cache;
        if(c == null) throw new InvalidParameterException("Incorrect cache type for Go's getScore.");
        Integer score = c.score.get(player);
        return score == null ? 0 : score;
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
        return new LinkedList<>();
    }


    /**
     * Assign every tile a group id value based on horizontal and vertical connections.
     * Tiles will have the same ID if they are horizontally or vertically connected,
     * and not if they are only connected by diagonal values.
     * @param board Board to search for groups on.
     * @return A board of group ids which line up with the main game board.
     */
    private GoLogicCache getGroups(GoLogicCache cache, Board board) {
        final UnionFind<BoardIndex> groups = cache.groups;

        //if we already have groups cached, clear them before re-scanning the board
        if(!groups.isEmpty()) groups.clear();

        //scan through the board from top left to bottom right, and union with top and left tiles iff they are the
        // same player and that player is not the null player
        for(int r = 0; r < board.size; ++r) {
            for(int c = 0; c < board.size; ++c) {
                //read player
                BoardIndex t = new BoardIndex(r, c);
                int p = board.at(t);

                if(p < 0) continue; //verify they are not null
                groups.add(t); //add them

                //Check left and top; union if same player
                if(r > 0) {
                    BoardIndex t2 = new BoardIndex(r - 1, c);
                    if(board.at(t2) == p)
                        groups.union(t, t2);
                } if(r > 0 && c > 0) {
                    BoardIndex t2 = new BoardIndex(r - 1, c - 1);
                    if(board.at(t2) == p)
                        groups.union(t, t2);
                }
            }
        }

        return cache;
    }


    /**
     * Checks if a specific tile on the board has any liberties.
     * @param board Board used in for the check.
     * @param index Index on the board to check.
     * @return Number of liberties around a single tile.
     */
    private static boolean tileHasLiberty(Board board, BoardIndex index) {
        for(int i = 0; i < 4; ++i) {
            //Get vector of direction
            final int dr = getdr(i);
            final int dc = getdc(i);

            final BoardIndex p = new BoardIndex(index.row + dr, index.column + dc);
            try {
                if (board.at(p) < 0) return true;
            } catch(ArrayIndexOutOfBoundsException e) { /* do nothing */ }

        }

        return false;
    }


    private static boolean groupHasLiberty(GoLogicCache cache, Board board, BoardIndex group, @Nullable BoardIndex ignore) {
        //Go through all tiles and check their adjacent spaces for openings which are not the ignore tile
        // return true as soon as we find a valid opening.
        Set<BoardIndex> tiles = cache.groups.getSet(group);

        for(BoardIndex tile : tiles) {
            for(int i = 0; i < 4; ++i) {
                //Get vector of direction
                final int dr = getdr(i);
                final int dc = getdc(i);

                final BoardIndex t = new BoardIndex(tile.row + dr, tile.column + dc);
                try {
                    if (board.at(t) < 0 && !t.equals(ignore)) return true;
                } catch(ArrayIndexOutOfBoundsException e) { /* do nothing */ }
            }
        }
        return false;
    }


    /**
     * Find all groups adjacent to a tile given a move command of the player and the position. This will return a set
     * of all the group representatives.
     * @param board Board to search.
     * @param location The tile to scan around
     * @return A set of group representatives for all unique, adjacent groups.
     */
    private Set<BoardIndex> findAdjGroups(GoLogicCache cache, Board board, BoardIndex location) {
        //Use a tree set because it does not have to allocate an array and we have a max of 4 things to insert
        Set<BoardIndex> groups = new TreeSet<>();
        for(int i = 0; i < 4; ++i) {
            //Get vector of direction
            final int dr = getdr(i);
            final int dc = getdc(i);

            //tile to check
            BoardIndex index = new BoardIndex(
                location.row + dr,
                location.column + dc
            );

            try {
                //if there is a piece of same player, find group representative
                if(board.at(index) >= 0)
                    groups.add(cache.groups.getRep(index));
            } catch(ArrayIndexOutOfBoundsException e) { /* do nothing */ }
        }
        return groups;
    }


    private static int getdr(int i) {
        switch(i) {
            case 0: return 1;
            case 1: return -1;
            default: return 0;
        }
    }

    private static int getdc(int i) {
        switch(i) {
            case 2: return 1;
            case 3: return -1;
            default: return 0;
        }
    }
}
