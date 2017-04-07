package plu.red.reversi.android;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.command.MoveCommand;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 */
public class PlayFragment extends Fragment implements ServiceConnection, View.OnClickListener, GameSurfaceView.GameSurfaceViewListener {

    private GameListener mListener;

    private RelativeLayout mGameInfoPanel;
    private RelativeLayout mGameActionPanel;

    private Button mConfirmButton;
    private ImageButton mSwitchCameraButton;

    private GameSurfaceView mGameView;

    private Game mGame;

    private GameService.LocalBinder mServiceConnection;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_play, container, false);

        mGameView = (GameSurfaceView)v.findViewById(R.id.surface_game);
        mGameView.setListener(this);

        mGameInfoPanel = (RelativeLayout)v.findViewById(R.id.panel_game_info);
        mGameActionPanel = (RelativeLayout)v.findViewById(R.id.panel_game_actions);

        mSwitchCameraButton = (ImageButton)mGameActionPanel.findViewById(R.id.button_switch_camera_mode);
        mConfirmButton = (Button)mGameActionPanel.findViewById(R.id.button_confirm_move);

        mSwitchCameraButton.setOnClickListener(this);
        mConfirmButton.setOnClickListener(this);

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

        if(mServiceConnection != null) {
            mGame = mServiceConnection.getGame();
            mGameView.setGame(mGame);
        }
        else {
            getContext().bindService(new Intent(getContext(), GameService.class), this, 0);
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
        }
    }

    @Override
    public void onBoardSelected(BoardIndex index) {
        mConfirmButton.setVisibility(View.VISIBLE);
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

        // uh oh! show a message
        /*new AlertDialog.Builder(getContext())
                .setTitle(R.string.title_game_service_crashed)
                .setMessage(R.string.msg_game_service_crashed)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                })
                .show();*/
    }
}
