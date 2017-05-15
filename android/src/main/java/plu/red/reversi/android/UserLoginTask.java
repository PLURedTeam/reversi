package plu.red.reversi.android;

/**
 * Created by daniel on 5/13/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import plu.red.reversi.core.network.WebUtilities;
import plu.red.reversi.core.util.User;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

    private final Context mContext;

    private final String mEmail;
    private final String mPassword;
    private final boolean mHashedPassword;

    private final UserLoginTaskListener mListener;

    UserLoginTask(Context context, String email, String password, boolean hashedPassword, UserLoginTaskListener listener) {
        mEmail = email;
        mPassword = password;
        mHashedPassword = hashedPassword;

        mContext = context;

        mListener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        System.out.println("Try login!");
        return WebUtilities.INSTANCE.login(mEmail, mPassword, mHashedPassword) || WebUtilities.INSTANCE.createUser(mEmail, mPassword);
    }

    @Override
    protected void onPostExecute(final Boolean success) {

        if (success) {
            // save the credentials
            User user = WebUtilities.INSTANCE.getUser();

            SharedPreferences.Editor editor = mContext.getSharedPreferences(LoginActivity.LOGIN_INFO_PREFS, Context.MODE_PRIVATE).edit();
            editor.putString(LoginActivity.KEY_LOGIN_USER, user.getUsername());
            editor.putString(LoginActivity.KEY_LOGIN_PASSWORD, user.getPassword());
            editor.apply();
        }

        if(mListener != null)
            mListener.onDone(success);
    }

    @Override
    protected void onCancelled() {
        mListener.onDone(null);
    }

    public interface UserLoginTaskListener {
        public void onDone(Boolean success);
    }
}
