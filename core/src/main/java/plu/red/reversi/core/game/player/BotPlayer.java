package plu.red.reversi.core.game.player;


import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.ReversiMinimax;
import plu.red.reversi.core.util.Color;
import plu.red.reversi.core.util.Looper;

/**
 * Glory to the Red Team.
 * An instance of a player which the computer can play as (basically an AI)
 * It utilizes the reversi minimax algorithm to compute the best move, and then execute it.
 */
public class BotPlayer extends Player implements Looper.LooperCallback<BoardIndex> {

    // TODO: Keep track of threads and stop them all if a Game is halted
    private Thread thread;
    private ReversiMinimax minimax;

    /**
     * New Game Constructor. Creates a BotPlayer belonging to a newly created Game object. BotPlayer is automatically registered to
     * the <code>game</code>, and an ID is retrieved from the <code>game</code>.
     *
     * @param game Game object this BotPlayer belongs to
     * @param color Color to give this BotPlayer
     * @param difficulty Integer specifying how deep the Minimax tree should search
     */
    public BotPlayer(Game game, Color color, int difficulty) {
        super(game, color);
        thread = null;
        minimax = new ReversiMinimax(game, getID(), difficulty, Looper.getLooper(Thread.currentThread()).getCall(this));
    }

    public BotPlayer(Game game, int playerID, Color color, int difficulty) {
        super(game, playerID, color);
        thread = null;
        minimax = new ReversiMinimax(game, getID(), difficulty, Looper.getLooper(Thread.currentThread()).getCall(this));
    }

    /**
     * Serial Constructor. Creates a BotPlayer belonging to an unserialized Game. BotPlayer is created with data unserialized
     * from a given JSONObject.
     *
     * @param game Game object this Player belongs to
     * @param json JSONObject storing this Player's data
     * @throws JSONException if there is a problem during unserialization
     */
    public BotPlayer(Game game, JSONObject json) throws JSONException {
        super(game, json);
        thread = null;
        int difficulty = json.getInt("difficulty");
        minimax = new ReversiMinimax(game, getID(), difficulty, Looper.getLooper(Thread.currentThread()).getCall(this));
    }

    /**
     * Serializes this Player into a JSONObject.
     *
     * @return New JSONObject from this Player
     * @throws JSONException if there is a problem during serialization
     */
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = super.toJSON();
        json.put("difficulty", this.minimax.MAX_DEPTH);
        json.put("type", 1);
        return json;
    }

    /**
     * Retrieves the Difficulty level that this BotPlayer is set to.
     *
     * @return Difficulty level
     */
    public int getDifficulty() {
        return minimax.MAX_DEPTH;
    }

    @Override
    public void nextTurn(boolean yours) {
        if(!yours) return;

        thread = new Thread(minimax);
        thread.start();
    }

    @Override
    public void onLooperCallback(BoardIndex result) {
        game.acceptCommand(new MoveCommand(getID(), result));
    }
}
