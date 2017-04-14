package plu.red.reversi.client;

import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingSource;
import org.jdesktop.swing.animation.timing.sources.SwingTimerTimingSource;
import plu.red.reversi.client.gui.MainWindow;
import plu.red.reversi.core.Client;
import plu.red.reversi.core.util.Looper;

import javax.swing.*;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

    private static Client client;
    public static Client getClient() { return client; }

    public static void main(String[] args)
    {
        // Set the default timing source for animation
        TimingSource ts = new SwingTimerTimingSource();
        Animator.setDefaultTimingSource(ts);
        ts.init();

        // Create the Client
        client = new Client(new MainWindow());

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
