package plu.red.reversi.android;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import plu.red.reversi.core.Game;
import plu.red.reversi.core.PlayerColor;
import plu.red.reversi.core.SettingsLoader;
import plu.red.reversi.core.game.player.BotPlayer;
import plu.red.reversi.core.game.player.NullPlayer;
import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.util.SettingsMap;

public class GameService extends Service {

    private static final String TAG = GameService.class.getSimpleName();

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

        // create a basic "default" game to get the user engaged.

        mGame = new Game(new SettingsLoader().createGameSettings());

        mGame.setPlayer(new NullPlayer(mGame, PlayerColor.BLACK));
        mGame.setPlayer(new BotPlayer(mGame, PlayerColor.WHITE, 2));

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

        public Game newGame(SettingsMap gameSettings, Player[] players) {
            mGame = new Game(gameSettings);

            for(Player p : players)
                mGame.setPlayer(p);

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
