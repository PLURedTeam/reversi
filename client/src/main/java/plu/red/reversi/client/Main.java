package plu.red.reversi.client;

import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingSource;
import org.jdesktop.swing.animation.timing.sources.SwingTimerTimingSource;
import plu.red.reversi.client.gui.GameWindow;

public class Main {

    public static void main(String[] args)
    {
        // Set the default timing source for animation
        TimingSource ts = new SwingTimerTimingSource();
        Animator.setDefaultTimingSource(ts);
        ts.init();

        // Create the GameWindow
        new GameWindow();
    }
}
