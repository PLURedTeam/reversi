package plu.red.reversi.client;

import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingSource;
import org.jdesktop.swing.animation.timing.sources.SwingTimerTimingSource;
import plu.red.reversi.client.gui.MainWindow;
import plu.red.reversi.core.Client;

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
    }
}
