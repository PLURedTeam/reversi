package plu.red.reversi.android;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import plu.red.reversi.core.SettingsLoader;
import plu.red.reversi.core.db.DBConnection;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.player.BotPlayer;
import plu.red.reversi.core.game.player.NullPlayer;
import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.util.Color;
import plu.red.reversi.core.util.DataMap;

public class GameService extends Service {

    private static final String TAG = GameService.class.getSimpleName();

    public static final String DB_FILE_NAME = "ClientDB.db";

    public static final int GAME_NOTIFICATION_ID = 15; // it does not matter what this is set to.

    private IBinder mBinder;

    private Game mGame;

    public GameService() {
        mGame = null;
    }

    @Override
    public void onCreate() {

        super.onCreate();

        Log.d(TAG, "Game service is alive");

        mBinder = new LocalBinder();

        // NOTE: This is required here because of the singleton nature of the db connection
        // and also because code is spaghettified. case and point: Creating a game implicitly creates a db. WTP.
        try {
            DriverManager.registerDriver((Driver) Class.forName("org.sqldroid.SQLDroidDriver").newInstance());
            DBConnection.dbConnector = "jdbc:sqldroid:";
            DBConnection.dbFile = new File(getFilesDir(), DB_FILE_NAME);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        // create a basic "default" game to get the user engaged.

        mGame = new Game(null);

        SettingsLoader instace = SettingsLoader.INSTANCE;

        mGame.setSettings(instace.createGameSettings());

        new NullPlayer(mGame, Color.RED);
        new BotPlayer(mGame, Color.BLUE, 2);


        //mGame.setPlayer(new NullPlayer(mGame, Color.BLACK));
        //mGame.setPlayer(new BotPlayer(mGame, Color.WHITE, 2));

        mGame.initialize();

        onGameStateUpdate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            if(intent.getAction() == ACTIONS.SAVE_AND_EXIT) {

                stopSelf();

                return START_NOT_STICKY;
            }
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void onGameStateUpdate() {
        if(mGame != null) {
            Intent notificationIntent = new Intent(this, GameActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            Notification.Builder builder = new Notification.Builder(this)
                    .setContentTitle(getText(R.string.notification_game_in_progress))
                    //.setContentText(c.getText(R.string.notification_message))
                    // TODO: Make an app icon?
                    .setSmallIcon(R.drawable.ic_app_icon)
                    .setContentIntent(pendingIntent)
                    .setTicker(getText(R.string.notification_game_in_progress));

            // add the exit game action
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT_WATCH) {

                Intent intent = new Intent(this, GameService.class);

                intent.setAction(ACTIONS.SAVE_AND_EXIT);
                PendingIntent pi = PendingIntent.getService(this, PendingIntent.FLAG_CANCEL_CURRENT, intent, 0);

                Notification.Action action =
                        new Notification.Action.Builder(R.drawable.ic_exit,
                                getString(R.string.notification_save_end_game),
                                pi)
                                .build();

                builder.addAction(action);
            }

            Notification notification = builder.build();

            startForeground(GAME_NOTIFICATION_ID, notification);
        }
        else {
            stopForeground(true);
        }
    }

    /**
     * Implements client-side access methods for this service
     */
    public class LocalBinder extends Binder {

        public Game newGame(DataMap gameSettings, Player[] players) {
            mGame = new Game(null);

            mGame.setSettings(gameSettings);

            //for(Player p : players)
                //mGame.setPlayer(p);

            mGame.initialize();

            onGameStateUpdate();

            return mGame;
        }

        public Game getGame() {
            return mGame;
        }
    }

    public static class ACTIONS {
        public static final String SAVE_AND_EXIT = "saveAndExit";
    }
}
