package plu.red.reversi.client.gui;

import plu.red.reversi.core.*;
import plu.red.reversi.core.command.*;
import plu.red.reversi.core.player.Player;
import plu.red.reversi.core.listener.ICommandListener;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PlayerInfoPanel extends JPanel implements ICommandListener {

    private Border activeBorder;
    private Border inactiveBorder;
    //private JPanel player1Panel, player2Panel;
    //private JLabel player1NameLabel, player2NameLabel;
    //private ScoreIcon player1Score, player2Score;
    private Color activeBackgroundColor = new Color(180, 250, 180);

    private class PlayerPanel extends JPanel {
        public final JLabel playerName;
        public final ScoreIcon playerScore;
        public final Player player;

        public PlayerPanel(Player player) {
            this.player = player;
            this.playerName = new JLabel(player.getName());
            this.playerScore = new ScoreIcon(player);
            this.add(playerName);
            this.add(playerScore);
        }
    }

    private HashMap<PlayerColor, PlayerPanel> playerPanelMap = new HashMap<PlayerColor, PlayerPanel>();
    private final Game game;

    public PlayerInfoPanel(Game game) {
        this.game = game;

        final int BORDER_SIZE = 5;

        inactiveBorder = BorderFactory.createMatteBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE,
                new Color(0,0,0,0));
        activeBorder = BorderFactory.createMatteBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE,
                new Color(250,200,100));

        this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        for(PlayerColor player : game.getUsedPlayers()) {
            PlayerPanel panel = new PlayerPanel(game.getPlayer(player));
            playerPanelMap.put(player, panel);
            this.add(panel);
        }

        setActivePlayer(game.getCurrentPlayer().getRole());
    }

    protected final void setActivePlayer( PlayerColor player ) {
        for(Map.Entry<PlayerColor, PlayerPanel> entry : playerPanelMap.entrySet()) {
            if(entry.getKey() == player) {
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
        if(cmd instanceof MoveCommand) {
            // Update this GUI component
            setActivePlayer(game.getCurrentPlayer().getRole());
            this.repaint();
        }
    }
}
