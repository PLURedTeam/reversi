package plu.red.reversi.android;


import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import plu.red.reversi.core.network.WebUtilities;

/**
 * A simple {@link Fragment} subclass.
 */
public class MultiplayerFragment extends Fragment implements ServiceConnection {

    private GameListener mListener;

    private GameService.LocalBinder mServiceConnection;

    public MultiplayerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        System.out.println("ON INFLATE");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_multiplayer, container, false);
    }

    @Override
    public void onAttach(Context context) {
        System.out.println("ON ATTACH");
        super.onAttach(context);
        if (context instanceof GameListener) {
            mListener = (GameListener) context;

            tryLogin();
            getContext().bindService(new Intent(getContext(), GameService.class), this, 0);
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
        System.out.println("Refreshing...");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mServiceConnection = null;
    }
}
