package plu.red.reversi.client.gui.game;

import plu.red.reversi.core.game.Board;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.SettingsLoader;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.command.SurrenderCommand;
import plu.red.reversi.core.listener.ICommandListener;
import plu.red.reversi.core.listener.ISettingsListener;
import plu.red.reversi.core.game.player.Player;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Glory to the Red Team.
 *
 * Panel for displaying the scores of players in a game. Arranges individual player scores in a Grid Layout with 2
 * columns and infinite rows.
 */
public class PlayerInfoPanel extends JPanel implements ICommandListener, ISettingsListener {

    private Border activeBorder;
    private Border inactiveBorder;
    private Color activeBackgroundColor = new Color(180, 250, 180);

    private Board board;

    private class PlayerPanel extends JPanel {
        public final JLabel playerName;
        public final ScoreIcon playerScore;
        public final Player player;

        public PlayerPanel(Player player, int score) {
            this.player = player;
            this.playerName = new JLabel(player.getName());
            this.playerScore = new ScoreIcon(player.getColor(), score);
            this.add(playerName);
            this.add(playerScore);
        }
    }

    private HashMap<Integer, PlayerPanel> playerPanelMap = new HashMap<>();
    private final Game game;

    public PlayerInfoPanel(Game game) {
        this.game = game;

        board = game.getBoard();

        final int BORDER_SIZE = 5;

        inactiveBorder = BorderFactory.createMatteBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE,
                new Color(0,0,0,0));
        activeBorder = BorderFactory.createMatteBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE,
                new Color(250,200,100));

        populate();

        SettingsLoader.INSTANCE.addSettingsListener(this);
    }

    protected final void populate() {
        this.removeAll();
        this.setLayout(new GridLayout(0, 2));

        // Create an individual score panel for every player in the Game
        for(Player player : game.getAllPlayers()) {
            PlayerPanel panel = new PlayerPanel(player, board.getScore(player.getID()));
            playerPanelMap.put(player.getID(), panel);
            this.add(panel);
        }

        // Set the currently active player to the correct instance
        // TODO: This should actually be set to who would play next based on the current board play state in this game.
        setActivePlayer(game.getCurrentPlayer().getID());

        this.revalidate();
        this.repaint();
    }

    public final void updateGUI() {
        populate();
    }

    protected final void setActivePlayer( int playerID ) {
        for(Map.Entry<Integer, PlayerPanel> entry : playerPanelMap.entrySet()) {
            if(entry.getKey() == playerID) {
                entry.getValue().setBackground(activeBackgroundColor);
                entry.getValue().setBorder(activeBorder);
            } else {
                entry.getValue().setBackground(null);
                entry.getValue().setBorder(inactiveBorder);
            }
        }
    }

    /**
     * Called when a Command is being passed through Game and has been validated.
     *
     * @param cmd Command object that is being applied
     */
    @Override
    public void commandApplied(Command cmd) {
        if(cmd instanceof MoveCommand || cmd instanceof SurrenderCommand) {
            // Update this GUI component
            setActivePlayer(game.getCurrentPlayer().getID());
            this.repaint();
        }
    }

    /**
     * Called when the client's settings have been changed.
     */
    @Override
    public void onClientSettingsChanged() {
        // Forces a refresh of the entire panel
        populate();
    }

    public void setCurrentBoard(Board board) {
        this.board = board;

        populate();
    }
}
