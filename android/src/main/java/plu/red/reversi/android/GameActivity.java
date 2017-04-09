package plu.red.reversi.android;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import org.jvnet.hk2.annotations.Service;

import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.util.DataMap;

public class GameActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GameListener, ServiceConnection {

    public static final String PREF_COMPLETED_INTRO = "completedIntro";

    FrameLayout mContentFrame;

    PlayFragment mPlayFragment;
    SingleplayerFragment mSingleplayerFragment;
    MultiplayerFragment mMultiplayerFragment;
    SavedGamesFragment mSavesFragment;

    GameService.LocalBinder mServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        setContentView(R.layout.activity_game);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mPlayFragment = new PlayFragment();
        mSingleplayerFragment = new SingleplayerFragment();
        mMultiplayerFragment = new MultiplayerFragment();
        mSavesFragment = new SavedGamesFragment();

        mContentFrame = (FrameLayout) findViewById(R.id.content_frame);

        navigationView.setCheckedItem(R.id.nav_play);
        // manually fire this because it does not for the method above
        onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_play));

        // start (and bind) the game service
        startService(new Intent(this, GameService.class));
        bindService(new Intent(this, GameService.class), this, 0);

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);

        if(!prefs.getBoolean(PREF_COMPLETED_INTRO, false))
            runIntro();
    }

    @Override
    public void onPause() {
        super.onPause();
        // save random little user things to shared preferences
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_play) {

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, mPlayFragment)
                    .commit();

        } else if (id == R.id.nav_singleplayer) {

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, mSingleplayerFragment)
                    .commit();

        } else if (id == R.id.nav_multiplayer) {

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, mMultiplayerFragment)
                    .commit();

        } else if (id == R.id.nav_saves) {

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, mSavesFragment)
                    .commit();

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_about) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void runIntro() {

        mPlayFragment.runIntro();


        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();

        editor.putBoolean(PREF_COMPLETED_INTRO, true);

        editor.apply();
    }

    @Override
    public void onNewGame(Game game) {
        if(mServiceConnection != null) {
            if(mServiceConnection.shouldWarnGameReplace()) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_replace_title)
                        .setMessage(R.string.dialog_replace_message)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mServiceConnection.setGame(game);

                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.content_frame, mPlayFragment)
                                        .commit();
                            }
                        })
                        .show();
            }
            else {
                mServiceConnection.setGame(game);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content_frame, mPlayFragment)
                        .commit();

                ((NavigationView)findViewById(R.id.nav_view)).setCheckedItem(R.id.nav_play);
            }
        }
        else {
            // TODO: Not sure how to handle this issue; should only happen if the service connection dies really
        }
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
        mServiceConnection = ((GameService.LocalBinder) service);
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
        mServiceConnection = null;
    }
}
