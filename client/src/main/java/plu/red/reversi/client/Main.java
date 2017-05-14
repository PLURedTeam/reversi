package plu.red.reversi.client;

import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingSource;
import org.jdesktop.swing.animation.timing.sources.SwingTimerTimingSource;
import plu.red.reversi.client.gui.MainWindow;
import plu.red.reversi.client.gui.browser.Browser;
import plu.red.reversi.client.gui.util.ChatLog;
import plu.red.reversi.core.Client;
import plu.red.reversi.core.Controller;
import plu.red.reversi.core.SettingsLoader;
import plu.red.reversi.core.network.WebUtilities;
import plu.red.reversi.core.util.DataMap;
import plu.red.reversi.core.util.Looper;

import javax.swing.*;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

    public static void main(String[] args)
    {
        // Set the default timing source for animation
        TimingSource ts = new SwingTimerTimingSource();
        Animator.setDefaultTimingSource(ts);
        ts.init();

        // Create the Client
        Browser browser = new Browser(null, null);
        Controller.init(new Client(
                new MainWindow(),
                new ChatLog(),
                browser,
                browser));

        // Attempt a login
        DataMap settings = SettingsLoader.INSTANCE.getClientSettings();
        if(settings.containsKey(SettingsLoader.GLOBAL_USER_NAME) && settings.containsKey(SettingsLoader.GLOBAL_USER_PASS)) {
            try {
                String username = settings.get(SettingsLoader.GLOBAL_USER_NAME, String.class);
                String password = settings.get(SettingsLoader.GLOBAL_USER_PASS, String.class);
                WebUtilities.INSTANCE.login(username, password, true);
            } catch(Exception ex) {}
        }

        // Clear any cached login info
        settings.remove(SettingsLoader.GLOBAL_USER_NAME);
        settings.remove(SettingsLoader.GLOBAL_USER_PASS);

        // looper call (feel free to move/adjust)
        // but recall that looper is actually used to call the API because it is necessary by android
        // so it is now necessary here too.
        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Looper.getLooper(Thread.currentThread()).run();
                    }
                });
            }
        }, new Date(), 100);
    }
}
