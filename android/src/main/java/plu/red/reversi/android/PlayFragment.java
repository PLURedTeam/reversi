package plu.red.reversi.android;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
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
import plu.red.reversi.core.game.player.Player;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 */
public class PlayFragment extends Fragment implements ServiceConnection, View.OnClickListener, GameSurfaceView.GameSurfaceViewListener {

    public static final Vector3fc MOVE_SELECT_COLOR = new Vector3f(1.0f, 1.0f, 0.0f);
    public static final Vector3fc LAST_MOVE_COLOR = new Vector3f(1.0f, 0.2f, 0.2f);
    public static final Vector3fc POSSIBLE_MOVES_COLOR = new Vector3f(0.0f, 1.0f, 0.0f);

    private GameListener mListener;

    private RelativeLayout mGameInfoPanel;
    private RelativeLayout mGameActionPanel;

    private LinearLayout mGameScorePanel;

    private Button mConfirmButton;
    private ImageButton mSwitchCameraButton;

    private GameSurfaceView mGameView;

    private Game mGame;

    private HighlightMode mHighlightMode;

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

        mSwitchCameraButton.setOnClickListener(this);
        mConfirmButton.setOnClickListener(this);

        mHandler = new Handler(Looper.myLooper());

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof GameListener) {
            mListener = (GameListener) context;

        } else {
            throw new RuntimeException(context.toString()
                    + " must implement GameListener");
        }

        if(mServiceConnection != null && mServiceConnection.getGame() != mGame) {
            mGame = mServiceConnection.getGame();
            mGameView.setGame(mGame);

            prepareScorePanel();
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
                            mGameView.getPlayerScore(players[i].getID())
                    ));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        getContext().unbindService(this);

        mServiceConnection = null;

        mListener = null;
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

            boolean present = !mGameView.getRenderer().isInPresentationMode();

            mSwitchCameraButton.getBackground().setColorFilter(
                    present ? Color.WHITE : Color.YELLOW, PorterDuff.Mode.MULTIPLY);

            mGameView.getRenderer().setPresentationMode(present);
        }
        else if(v == mConfirmButton) {
            // move confirmed
            mGame.acceptCommand(new MoveCommand(
                    mGame.getCurrentPlayer().getID(),
                    mGameView.getCurrentSelected()
            ));

            mConfirmButton.setVisibility(View.GONE);

            // prevent the user from moving until all the animations are done
            mGameView.disablePlayer();
        }
    }

    @Override
    public void onBoardSelected(BoardIndex index) {
        doHighlights();
        mConfirmButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBoardScoreChanged() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateScorePanel();
            }
        });
    }

    @Override
    public void onPlayerStateChanged() {
        if(mGameView.isPlayerEnabled())
            doHighlights();
    }

    public void setHighlightMode(HighlightMode mode) {
        mHighlightMode = mode;

        doHighlights();
    }

    public HighlightMode getHighlightMode() {
        return mHighlightMode;
    }

    private void doHighlights() {

        mGameView.queueEvent(new Runnable() {
            @Override
            public void run() {

                if(mGameView.getRenderer().getBoard() == null)
                    return; // cant do anything yet.

                System.out.println("Time to highlight: player enabled = " + mGameView.isPlayerEnabled());

                mGameView.getRenderer().clearBoardHighlights();

                if(mGameView.isPlayerEnabled()) {
                    if(mHighlightMode == HighlightMode.HIGHLIGHT_POSSIBLE_MOVES) {
                        // we can use the game board because GUI will be caught up animation wise
                        for(BoardIndex index : mGame.getBoard().getPossibleMoves(mGame.getCurrentPlayer().getID())) {
                            mGameView.getRenderer().highlightBoard(index, POSSIBLE_MOVES_COLOR);
                        }
                    }
                    else if(mHighlightMode == HighlightMode.HIGHLIGHT_BEST_MOVE) {
                        // TODO
                    }

                    BoardCommand lastMove = mGame.getHistory().getBoardCommand(mGame.getHistory().getNumBoardCommands() - 1);

                    if(lastMove instanceof MoveCommand) {
                        mGameView.getRenderer().highlightBoard(lastMove.position, LAST_MOVE_COLOR);
                    }

                    if(mGameView.getCurrentSelected() != null)
                        mGameView.getRenderer().highlightBoard(mGameView.getCurrentSelected(), MOVE_SELECT_COLOR);

                    mGameView.requestRender();
                }
            }
        });
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

        // get the currently running game
        mGame = mServiceConnection.getGame();

        mGameView.setGame(mGame);

        prepareScorePanel();
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

    public enum HighlightMode {
        HIGHLIGHT_NONE,
        HIGHLIGHT_POSSIBLE_MOVES,
        HIGHLIGHT_BEST_MOVE;
    }
}
