package plu.red.reversi.android;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.ArrayList;

import plu.red.reversi.core.network.WebUtilities;
import plu.red.reversi.core.util.GamePair;

/**
 * A simple {@link Fragment} subclass.
 */
public class MultiplayerFragment extends Fragment implements ServiceConnection, AdapterView.OnItemClickListener {

    private GameListener mListener;
    private GameService.LocalBinder mServiceConnection;
    private MultiplayerGamesAdapter mSlideAdapter;
    private GetGamesTask mGetTask;

    public MultiplayerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mGetTask = new GetGamesTask();
        mSlideAdapter = new MultiplayerGamesAdapter(getContext());

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_multiplayer, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof GameListener) {
            mListener = (GameListener) context;

            tryLogin();
            getContext().bindService(new Intent(getContext(), GameService.class), this, 0);

            mListener.getSlideList().setAdapter(mSlideAdapter);
            mListener.getSlideList().setOnItemClickListener(this);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    private void tryLogin() {
        System.out.println("Login?");

        if(!WebUtilities.INSTANCE.loggedIn()) {
            // the user needs to log in. Try to do it here first.
            final ProgressDialog dialog = new ProgressDialog(getContext());
            dialog.setMessage(getString(R.string.login_trying));
            dialog.show();

            SharedPreferences prefs = getContext().getSharedPreferences(LoginActivity.LOGIN_INFO_PREFS, Context.MODE_PRIVATE);
            new UserLoginTask(
                    getContext(),
                    prefs.getString(LoginActivity.KEY_LOGIN_USER, ""),
                    prefs.getString(LoginActivity.KEY_LOGIN_PASSWORD, ""),
                    true,
                    new UserLoginTask.UserLoginTaskListener() {
                        @Override
                        public void onDone(Boolean success) {
                            dialog.dismiss();

                            if(success)
                                refresh();
                            else {
                                Intent intent = new Intent(getContext(), LoginActivity.class);
                                startActivity(intent);
                            }
                        }
                    }
            ).execute();
        }
        else refresh();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

        getContext().unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mServiceConnection = (GameService.LocalBinder)service;
    }

    public void refresh() {
        System.out.println("Refreshing network...");

        mGetTask.execute();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mServiceConnection = null;
    }

    /**
     * Callback method to be invoked when an item in this AdapterView has
     * been clicked.
     * <p>
     * Implementers can call getItemAtPosition(position) if they need
     * to access the data associated with the selected item.
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this
     *                 will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // show a dialog "are you sure you want to join this multiplayer game?
        // then, actually show a progress dialog which is ok for right now
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_join_game_title)
                .setMessage(R.string.dialog_join_game_message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(!WebUtilities.INSTANCE.joinGame(mSlideAdapter.getItem(position).gameID)) {
                            Toast.makeText(getContext(), R.string.error_join_game, Toast.LENGTH_LONG).show();
                            return;
                        }

                        final ProgressDialog d = new ProgressDialog(getContext());
                        d.setMessage(getString(R.string.dialog_wait_start));
                        d.show();

                        // move to game screen when game is started
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private class GetGamesTask extends AsyncTask<Void, Void, ArrayList<GamePair>> {

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param voids The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected ArrayList<GamePair> doInBackground(Void... voids) {
            return WebUtilities.INSTANCE.getOnlineGames();
        }

        @Override
        protected void onPostExecute(ArrayList<GamePair> games) {
            mSlideAdapter.clear();
            mSlideAdapter.addAll(games);
        }
    }
}
