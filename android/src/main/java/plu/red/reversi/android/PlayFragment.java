package plu.red.reversi.android;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Locale;

import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.game.player.HumanPlayer;
import plu.red.reversi.core.game.player.NullPlayer;
import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.reversi3d.HighlightMode;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 */
public class PlayFragment extends Fragment implements ServiceConnection, View.OnClickListener, GameSurfaceView.GameSurfaceViewListener, AdapterView.OnItemClickListener {

    public static final String PREF_AUTO_FOLLOW = "play_autoFollow";

    private GameListener mListener;

    private RelativeLayout mGameInfoPanel;
    private RelativeLayout mGameActionPanel;

    private LinearLayout mGameScorePanel;

    private Button mConfirmButton;

    private Button mPlayForwardButton;
    private Button mPlayPrevButton;
    private Button mPlayNextButton;

    private ImageButton mSwitchCameraButton;

    private GameSurfaceView mGameView;

    private Game mGame;

    private GameService.LocalBinder mServiceConnection;

    private Handler mHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_play, container, false);

        mGameView = (GameSurfaceView)v.findViewById(R.id.surface_game);
        mGameView.setListener(this);

        mGameInfoPanel = (RelativeLayout)v.findViewById(R.id.panel_game_info);
        mGameScorePanel = (LinearLayout)mGameInfoPanel.findViewById(R.id.panel_player_scores);

        mGameActionPanel = (RelativeLayout)v.findViewById(R.id.panel_game_actions);
        mSwitchCameraButton = (ImageButton)mGameActionPanel.findViewById(R.id.button_switch_camera_mode);
        mConfirmButton = (Button)mGameActionPanel.findViewById(R.id.button_confirm_move);

        mPlayForwardButton = (Button)mGameActionPanel.findViewById(R.id.button_play_forward);
        mPlayNextButton = (Button)mGameActionPanel.findViewById(R.id.button_play_next);
        mPlayPrevButton = (Button)mGameActionPanel.findViewById(R.id.button_play_prev);


        mSwitchCameraButton.setOnClickListener(this);
        mConfirmButton.setOnClickListener(this);
        mPlayForwardButton.setOnClickListener(this);
        mPlayNextButton.setOnClickListener(this);
        mPlayPrevButton.setOnClickListener(this);

        mHandler = new Handler(Looper.myLooper());

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof GameListener) {
            mListener = (GameListener) context;

            if(mGameView != null)
                mGameView.setListener(this);

        } else {
            throw new RuntimeException(context.toString()
                    + " must implement GameListener");
        }

        if(mServiceConnection != null && mServiceConnection.getGame() != mGame) {
            refreshGame();
        }
        else {
            getContext().bindService(new Intent(getContext(), GameService.class), this, 0);
        }
    }

    private void prepareScorePanel() {
        mGameScorePanel.removeAllViewsInLayout();

        Player[] players = mGame.getAllPlayers();

        for(Player player : players) {

            LinearLayout layout = new LinearLayout(getContext());
            layout.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(10, 10, 10 ,10);

            layout.setLayoutParams(params);
            layout.setTag(player);

            ImageView image = new ImageView(getContext());
            image.setImageDrawable(new ColorDrawable(player.getColor().composite));
            image.setMinimumWidth(50);
            image.setMinimumHeight(50);
            layout.addView(image);

            params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(10, 0, 0, 0);

            TextView name = new TextView(getContext());
            name.setText(player.getName());
            name.setTextColor(Color.WHITE);
            name.setLayoutParams(params);
            layout.addView(name);

            TextView score = new TextView(getContext());
            score.setText(String.format(
                    Locale.getDefault(),
                    "%d",
                    mGameView.getPlayerScore(player.getID())));
            score.setTextColor(Color.WHITE);
            score.setTypeface(Typeface.DEFAULT_BOLD);
            score.setLayoutParams(params);
            layout.addView(score);

            mGameScorePanel.addView(layout);
        }
    }

    private void updateScorePanel() {

        Player[] players = mGame.getAllPlayers();

        for(int i = 0;i < players.length;i++) {
            LinearLayout layout = (LinearLayout)mGameScorePanel.findViewWithTag(players[i]);

            ((TextView)layout.getChildAt(2))
                    .setText(String.format(
                            Locale.getDefault(),
                            "%d",
                            // TODO: Historical game logic scores have been borked.
                            mGame.getGameLogic().getScore(
                                    mGameView.getCurrentCache(),
                                    mGameView.getCurrentBoard(),
                                    players[i].getID()
                            )
                    ));

            if(mGame.getCurrentPlayer() == players[i])
                mGameScorePanel.findViewWithTag(players[i]).setBackgroundColor(Color.argb(64, 255, 255, 0));
            else
                mGameScorePanel.findViewWithTag(players[i]).setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void updateActionPanel() {

        System.out.println("update action panel");

        mConfirmButton.setVisibility(View.GONE);
        mPlayForwardButton.setVisibility(View.GONE);
        mPlayNextButton.setVisibility(View.GONE);
        mPlayPrevButton.setVisibility(View.GONE);

        if(!mGameView.isAutoFollow()) {
            mPlayForwardButton.setVisibility(View.VISIBLE);
            if(mGameView.getCurrentMoveIndex() < mGame.getHistory().getNumBoardCommands() - 1)
                mPlayNextButton.setVisibility(View.VISIBLE);
            if(mGameView.getCurrentMoveIndex() > 0)
                mPlayPrevButton.setVisibility(View.VISIBLE);

        }

        if(mGameView.getCurrentSelected() != null && mGameView.getCurrentMoveIndex() == mGame.getHistory().getNumBoardCommands() - 1) {
            mConfirmButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        getContext().unbindService(this);

        mServiceConnection = null;

        mListener = null;

        // save whether or not we should auto follow based on if we were auto following before
        SharedPreferences.Editor prefs = getActivity().getPreferences(Context.MODE_PRIVATE).edit();

        prefs.putBoolean(PREF_AUTO_FOLLOW, mGameView.isAutoFollow());

        prefs.apply();
    }

    public void runIntro() {

    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if(v == mSwitchCameraButton) {

            boolean present = !mGameView.isInPresentationMode();

            mSwitchCameraButton.getBackground().setColorFilter(
                    present ? Color.WHITE : Color.YELLOW, PorterDuff.Mode.MULTIPLY);

            mGameView.setPresentationMode(present);
        }
        else if(v == mConfirmButton) {
            // move confirmed
            System.out.println("Setting board clicked for current player");
            mGame.getCurrentPlayer().boardClicked(mGameView.getCurrentSelected());

            mGameView.clearCurrentSelected();

            updateActionPanel();

            // prevent the user from moving until all the animations are done
            mGameView.setPlayerEnabled(mGame.getCurrentPlayer() instanceof HumanPlayer);
        }
        else if(v == mPlayForwardButton) {
            mGameView.setAutoFollow(true);
            updateActionPanel();
        }
        else if(v == mPlayNextButton) {
            mGameView.setCurrentMove(mGameView.getCurrentMoveIndex() + 1);
            updateActionPanel();
        }
        else if(v == mPlayPrevButton) {
            mGameView.setCurrentMove(mGameView.getCurrentMoveIndex() - 1);
            updateActionPanel();
        }
    }

    @Override
    public void onBoardSelected(BoardIndex index) {
        doHighlights();
        updateActionPanel();
    }

    @Override
    public void onBoardStateChanged() {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mListener != null) {
                    mListener.getSlideList().setSelection(mGameView.getCurrentMoveIndex());
                    updateScorePanel();
                }
            }
        });

        mServiceConnection.setMoveIndex(mGameView.getCurrentMoveIndex());

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateActionPanel();
            }
        });
    }

    @Override
    public void onPlayerStateChanged() {
        if(mGameView.isPlayerEnabled())
            doHighlights();
    }

    public void setHighlightMode(HighlightMode mode) {
        mGameView.setHighlightMode(mode);
    }

    public HighlightMode getHighlightMode() {
        return mGameView.getHighlightMode();
    }

    private void doHighlights() {
        mGameView.doHighlights();
    }

    private void refreshGame() {
        // get the currently running game
        mGame = mServiceConnection.getGame();

        mGameView.setGame(mGame);
        mGameView.setCurrentMove(mServiceConnection.getMoveIndex());
        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        mGameView.setAutoFollow(prefs.getBoolean(PREF_AUTO_FOLLOW, true));
        mGameView.setPlayerEnabled(mGame.getCurrentPlayer() instanceof HumanPlayer);

        prepareScorePanel();
        updateActionPanel();

        mListener.getSlideList().setAdapter(new GameHistoryAdapter(getContext(), mGame));
        mListener.getSlideList().setOnItemClickListener(this);
    }



    /**
     * The slideList has been clicked...
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this
     *                 will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mGameView.setCurrentMove(position);

        updateActionPanel();
    }

    /**
     * Called when a connection to the Service has been established, with
     * the {@link IBinder} of the communication channel to the
     * Service.
     *
     * @param name    The concrete component name of the service that has
     *                been connected.
     * @param service The IBinder of the Service's communication channel,
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        mServiceConnection = ((GameService.LocalBinder)service);

        refreshGame();
    }

    /**
     * Called when a connection to the Service has been lost.  This typically
     * happens when the process hosting the service has crashed or been killed.
     * This does <em>not</em> remove the ServiceConnection itself -- this
     * binding to the service will remain active, and you will receive a call
     * to {@link #onServiceConnected} when the Service is next running.
     *
     * @param name The concrete component name of the service whose
     *             connection has been lost.
     */
    @Override
    public void onServiceDisconnected(ComponentName name) {

        getActivity().finish();
    }
}
