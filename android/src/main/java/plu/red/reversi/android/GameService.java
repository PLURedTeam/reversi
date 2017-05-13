package plu.red.reversi.android;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import plu.red.reversi.core.SettingsLoader;
import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.db.DBConnection;
import plu.red.reversi.core.db.DBUtilities;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.logic.ReversiLogic;
import plu.red.reversi.core.game.player.BotPlayer;
import plu.red.reversi.core.game.player.HumanPlayer;
import plu.red.reversi.core.game.player.NullPlayer;
import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.listener.ICommandListener;
import plu.red.reversi.core.listener.IListener;
import plu.red.reversi.core.util.Color;
import plu.red.reversi.core.util.DataMap;

public class GameService extends Service implements ICommandListener {

    private static final String TAG = GameService.class.getSimpleName();

    public static final String DB_FILE_NAME = "ClientDB.db";

    public static final int GAME_NOTIFICATION_ID = 15; // it does not matter what this is set to.

    private LocalBinder mBinder;

    private Game mGame;
    private int mMoveIndex;

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
        Game game = new Game(null, null);

        SettingsLoader instance = SettingsLoader.INSTANCE;

        game.setSettings(instance.createGameSettings());

        new BotPlayer(game, Color.BLUE, 2).setName("Bot 2");
        new BotPlayer(game, Color.RED, 2).setName("Bot 1");

        game.setLogic(new ReversiLogic(game));

        game.initialize();

        mBinder.setGame(game);

        onGameStateUpdate();



        final Handler handler = new Handler(Looper.myLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                plu.red.reversi.core.util.Looper.getLooper(Thread.currentThread()).run();

                handler.postDelayed(this, 100);
            }
        }, 100);
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
     * Called when a Command is being passed through Game and has been validated.
     *
     * @param cmd Command object that is being applied
     */
    @Override
    public void commandApplied(Command cmd) {

        if(!mGame.isInitialized()) {
            System.out.println("You idiot.");
        }

        if(!mGame.getGameSaved()) {

            boolean shouldSave = false;

            // only save game if one of the players is not a bot player
            for(Player p : mGame.getAllPlayers())
                shouldSave = shouldSave || !(p instanceof BotPlayer);

            if(shouldSave) {
                DBUtilities.INSTANCE.saveGame(mGame.getHistory(),
                        mGame.getAllPlayers(),
                        mGame.getSettings().toJSON(),
                        SimpleDateFormat.getDateTimeInstance().format(new Date())
                );

                mGame.setGameSaved(true);
            }
        }
        else {
            if(cmd instanceof BoardCommand)
                DBUtilities.INSTANCE.saveMove(mGame.getGameID(), (BoardCommand)cmd);
        }
    }

    /**
     * Implements client-side access methods for this service
     */
    public class LocalBinder extends Binder {

        public Game setGame(Game game) {

            if(mGame != null) {
                mGame.removeListener(GameService.this);
            }

            mGame = game;

            mGame.addListener(GameService.this);

            if(mGame.isGameOver()) {
                // start with the beginning by default
                mMoveIndex = 0;
            }
            else
                mMoveIndex = mGame.getHistory().getNumBoardCommands() - 1;

            return mGame;
        }

        public int getMoveIndex() {
            return mMoveIndex;
        }

        public void setMoveIndex(int index) {
            mMoveIndex = index;
        }

        public boolean shouldWarnGameReplace() {
            boolean should = mGame.getGameSaved();

            // only save game if one of the players is not a bot player
            for(Player p : mGame.getAllPlayers())
                should = should || !(p instanceof BotPlayer);

            return should;
        }

        public Game getGame() {
            return mGame;
        }
    }

    public static class ACTIONS {
        public static final String SAVE_AND_EXIT = "saveAndExit";
    }
}
