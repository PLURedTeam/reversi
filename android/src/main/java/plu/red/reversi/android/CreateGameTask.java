package plu.red.reversi.android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.List;

import plu.red.reversi.core.SettingsLoader;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.logic.ReversiLogic;
import plu.red.reversi.core.game.player.HumanPlayer;
import plu.red.reversi.core.game.player.NetworkPlayer;
import plu.red.reversi.core.network.WebUtilities;
import plu.red.reversi.core.util.Color;
import plu.red.reversi.core.util.GamePair;

/**
 * Created by daniel on 5/14/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public class CreateGameTask extends AsyncTask<Void, Void, Game> {

    private Context mContext;
    private GameListener mListener;

    private String mGameName;

    private ProgressDialog mDialog;

    public CreateGameTask(Context context, GameListener listener, String name) {
        mContext = context;
        mListener = listener;
        mGameName = name;
    }

    @Override
    protected void onPreExecute() {
        mDialog = new ProgressDialog(mContext);
        mDialog.setMessage(mContext.getString(R.string.dialog_new_net_progress));
        mDialog.show();
    }

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected Game doInBackground(Void... params) {


        if(!WebUtilities.INSTANCE.createGame(2, mGameName, GamePair.GameType.REVERSI)) {
            System.out.println("Failed to create game!");
            return null;
        }

        String p2name = null;

        // wait for someone to join the game
        try {
            // TODO: This is the least efficient way to do any kind of game check, but it is the only
            // option practically available at this time, short of implementing a lobby
            // NOTE: This would not work on a large server configuation. This is just for demonstration only
            while(true) {
                List<GamePair> games = WebUtilities.INSTANCE.getOnlineGames();

                boolean good = false;

                for(GamePair game : games) {
                    if(game.gameID != WebUtilities.INSTANCE.getNetworkGameID())
                        continue;
                    System.out.println("Found game! " + game.gameID);
                    if(game.players.size() == game.numPlayers)
                        p2name = game.players.get(1).getUsername();

                    break;
                }

                if(p2name != null)
                    break;

                if(!mDialog.isShowing())
                    // call it an interrupt
                    throw new InterruptedException();

                Thread.sleep(1000);
            }
        }
        catch(InterruptedException e) {
            // try to close the game we created
            WebUtilities.INSTANCE.leaveNetworkGame();
            return null;
        }

        Game game = new Game(null, null, true, mGameName);

        game.setLogic(new ReversiLogic(game));
        game.setSettings(SettingsLoader.INSTANCE.createGameSettings());

        // create the game
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        Color p1c = new Color(prefs.getInt(SettingsFragment.KEY_P1_COLOR, Color.RED.composite));
        Color p2c = new Color(prefs.getInt(SettingsFragment.KEY_P2_COLOR, Color.BLUE.composite));

        new HumanPlayer(game, p1c).setName(WebUtilities.INSTANCE.getUser().getUsername());
        new NetworkPlayer(game, p2c).setName(p2name);

        game.initialize();

        if(!WebUtilities.INSTANCE.startGame(game)) {
            System.out.println("Start game failed!");
            WebUtilities.INSTANCE.leaveNetworkGame();
            return null;
        }

        return game;
    }

    @Override
    protected void onPostExecute(Game game) {
        mDialog.hide();

        if(game != null) {
            mListener.onNewGame(game);
        }
        else {
            Toast.makeText(mContext, R.string.error_create_net_error, Toast.LENGTH_LONG).show();
        }
    }
}
