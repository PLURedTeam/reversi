package plu.red.reversi.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
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

import plu.red.reversi.core.Game;
import plu.red.reversi.core.util.SettingsMap;

public class GameActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GameListener {

    public static final String PREF_COMPLETED_INTRO = "completedIntro";

    FrameLayout mContentFrame;

    PlayFragment mPlayFragment;
    SingleplayerFragment mSingleplayerFragment;
    MultiplayerFragment mMultiplayerFragment;

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

        mContentFrame = (FrameLayout) findViewById(R.id.content_frame);

        navigationView.setCheckedItem(R.id.nav_play);
        // manually fire this because it does not for the method above
        onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_play));

        // start the game service
        startService(new Intent(this, GameService.class));

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
    public void onNewGame(SettingsMap gameSettings) {

    }
}
