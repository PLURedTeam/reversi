package plu.red.reversi.client;

import org.codehaus.jettison.json.JSONObject;
import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingSource;
import org.jdesktop.swing.animation.timing.sources.SwingTimerTimingSource;
import plu.red.reversi.client.gui.MainWindow;
import plu.red.reversi.client.player.HumanPlayer;
import plu.red.reversi.core.Game;
import plu.red.reversi.core.PlayerColor;
import plu.red.reversi.core.SettingsLoader;
import plu.red.reversi.core.player.BotPlayer;
import plu.red.reversi.core.player.Player;
import plu.red.reversi.core.util.SettingsMap;

public class Main {

    public static void main(String[] args)
    {
        // Set the default timing source for animation
        TimingSource ts = new SwingTimerTimingSource();
        Animator.setDefaultTimingSource(ts);
        ts.init();

        SettingsMap settings = SettingsLoader.INSTANCE.loadFromJSON(new JSONObject());
        Game game = new Game(settings);

        HumanPlayer p1 = new HumanPlayer(game, PlayerColor.WHITE);
        Player p2 = new BotPlayer(game, PlayerColor.BLACK, PlayerColor.WHITE);

        game.setPlayer(p1);
        game.setPlayer(p2);

        game.initialize();

        // Create the MainWindow
        MainWindow window = new MainWindow(game);
    }
}
