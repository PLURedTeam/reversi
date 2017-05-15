package plu.red.reversi.android;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.network.WebUtilities;
import plu.red.reversi.core.reversi3d.HighlightMode;

public class GameActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GameListener, ServiceConnection {

    public static final String PREF_COMPLETED_INTRO = "completedIntro";
    private static final String CONTENT_FRAGMENT_TAG = "GAME_CONTENT_FRAGMENT";

    FrameLayout mContentFrame;

    PlayFragment mPlayFragment;
    SingleplayerFragment mSingleplayerFragment;
    MultiplayerFragment mMultiplayerFragment;
    SavedGamesFragment mSavesFragment;

    ListView mSubNavList;

    SettingsFragment mSettingsFragment;
    AboutFragment mAboutFragment;

    Fragment currentFragment;

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

        mSettingsFragment = new SettingsFragment();
        mAboutFragment = new AboutFragment();

        mContentFrame = (FrameLayout) findViewById(R.id.content_frame);

        mSubNavList = (ListView) findViewById(R.id.sub_nav_view);

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
    public void onDestroy() {
        super.onDestroy();

        unbindService(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if(drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }

    private void showFragment(Fragment frag) {
        mSubNavList.setAdapter(null);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, frag, CONTENT_FRAGMENT_TAG)
                .commit();

        currentFragment = frag;

        invalidateOptionsMenu();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_play) {
            showFragment(mPlayFragment);
        } else if (id == R.id.nav_singleplayer) {
            showFragment(mSingleplayerFragment);
        } else if (id == R.id.nav_multiplayer) {
            showFragment(mMultiplayerFragment);
        } else if (id == R.id.nav_saves) {
            showFragment(mSavesFragment);
        } else if (id == R.id.nav_settings) {
            showFragment(mSettingsFragment);
        } else if (id == R.id.nav_about) {
            showFragment(mAboutFragment);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {

        menu.clear();

        if(currentFragment == mPlayFragment) {
            getMenuInflater().inflate(R.menu.fragment_play_menu, menu);
        }
        else if(currentFragment == mMultiplayerFragment) {
            getMenuInflater().inflate(R.menu.fragment_multiplayer_menu, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_show_possible_moves:

                mPlayFragment.setHighlightMode(mPlayFragment.getHighlightMode() == HighlightMode.HIGHLIGHT_POSSIBLE_MOVES ?
                        HighlightMode.HIGHLIGHT_NONE :
                        HighlightMode.HIGHLIGHT_POSSIBLE_MOVES);

                return true;
            case R.id.menu_show_best_move:

                mPlayFragment.setHighlightMode(mPlayFragment.getHighlightMode() == HighlightMode.HIGHLIGHT_BEST_MOVE ?
                        HighlightMode.HIGHLIGHT_NONE :
                        HighlightMode.HIGHLIGHT_BEST_MOVE);

                return true;
            case R.id.menu_show_sub:
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

                drawer.openDrawer(GravityCompat.END);

                return true;

            case R.id.menu_create_net_game:

                final EditText et = new EditText(this);

                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_new_net_title)
                        .setMessage(R.string.dialog_new_net_message)
                        .setView(et)
                        .setPositiveButton(R.string.dialog_create_game, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new CreateGameTask(
                                        GameActivity.this,
                                        GameActivity.this,
                                        et.getText().toString()).execute();
                            }
                        })
                        .setNeutralButton(android.R.string.cancel, null)
                        .show();

            default:
                return super.onOptionsItemSelected(item);
        }
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
                                newGame(game);
                            }
                        })
                        .show();
            }
            else {
                newGame(game);
            }
        }
        else {
            // TODO: Not sure how to handle this issue; should only happen if the service connection dies really
        }
    }

    @Override
    public ListView getSlideList() {
        return mSubNavList;
    }

    @SuppressLint("ApplySharedPref")
    private void newGame(Game game) {
        mServiceConnection.setGame(game);
        showFragment(mPlayFragment);
        ((NavigationView)findViewById(R.id.nav_view)).setCheckedItem(R.id.nav_play);

        // tell the play fragment that it does not need to honor the auto follow
        // settings from the previous game

        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putBoolean(PlayFragment.PREF_AUTO_FOLLOW, true);

        // commit because sync may mess this up otherwise.
        // the app is loading right now anyway so lag created will be almost unnoticable in context
        editor.commit();
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
