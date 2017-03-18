package plu.red.reversi.client;

import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingSource;
import org.jdesktop.swing.animation.timing.sources.SwingTimerTimingSource;
import plu.red.reversi.client.gui.MainWindow;
import plu.red.reversi.client.player.HumanPlayer;
import plu.red.reversi.core.Game;
import plu.red.reversi.core.PlayerColor;
import plu.red.reversi.core.SettingsLoader;
import plu.red.reversi.core.player.BotPlayer;
import plu.red.reversi.core.util.SettingsMap;

public class Main {

    public static void main(String[] args)
    {
        // Set the default timing source for animation
        TimingSource ts = new SwingTimerTimingSource();
        Animator.setDefaultTimingSource(ts);
        ts.init();

        SettingsMap settings = SettingsLoader.INSTANCE.createGameSettings();
        settings.set(SettingsLoader.GAME_BOARD_SIZE, 8);
        Game game = new Game(settings);

        HumanPlayer player1 = new HumanPlayer(game, PlayerColor.BLACK);
        //BotPlayer player1 = new BotPlayer(game, PlayerColor.BLACK, 5);
        BotPlayer player2 = new BotPlayer(game, PlayerColor.WHITE, 10);

        game.setPlayer(player1);
        game.setPlayer(player2);

        game.initialize();

        //game.nextTurn();

        // Create the MainWindow
        MainWindow window = new MainWindow(game);
    }
}
