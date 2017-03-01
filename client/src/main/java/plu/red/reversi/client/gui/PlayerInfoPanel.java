package plu.red.reversi.client.gui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class PlayerInfoPanel extends JPanel {

    private Border activeBorder;
    private Border inactiveBorder;
    private JPanel player1Panel, player2Panel;
    private JLabel player1NameLabel, player2NameLabel;
    private ScoreIcon player1Score, player2Score;
    private Color activeBackgroundColor = new Color(180, 250, 180);

    public PlayerInfoPanel() {

        final int BORDER_SIZE = 5;

        inactiveBorder = BorderFactory.createMatteBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE,
                new Color(0,0,0,0));
        activeBorder = BorderFactory.createMatteBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE,
                new Color(250,200,100));

        this.setLayout(new BorderLayout());
        player2Score = new ScoreIcon(Color.white, Color.black);
        player1Score = new ScoreIcon(Color.black, Color.white);

        // TODO: These names should be retrieved from the model
        player1NameLabel = new JLabel("Player 1 name");
        player2NameLabel = new JLabel("Player 2 name");

        player1Panel = new JPanel();
        player1Panel.add(player1Score);
        player1Panel.add(player1NameLabel);

        player2Panel = new JPanel();
        player2Panel.add(player2Score);
        player2Panel.add(player2NameLabel);

        setActivePlayer(1);

        this.add(player1Panel, BorderLayout.WEST);
        this.add(player2Panel, BorderLayout.EAST);
    }

    public void setActivePlayer( int player ){
        if( player == 1 ) {
            player1Panel.setBackground(activeBackgroundColor);
            player2Panel.setBackground(null);
            player1Panel.setBorder(activeBorder);
            player2Panel.setBorder(inactiveBorder);
        } else if (player == 2 ) {
            player2Panel.setBackground(activeBackgroundColor);
            player1Panel.setBackground(null);
            player1Panel.setBorder(inactiveBorder);
            player2Panel.setBorder(activeBorder);
        } else {
            throw new IllegalArgumentException("Invalid player: " + player);
        }
    }

    public void setScore( int player, int score ) {
        if( player == 1 ) {
            player1Score.setScoreValue(score);
        } else if (player == 2 ) {
            player2Score.setScoreValue(score);
        } else {
            throw new IllegalArgumentException("Invalid player: " + player);
        }
    }

    public void setPlayerName(int player, String name) {
        if( player == 1 ) {
            player1NameLabel.setText(name);
        } else if (player == 2 ) {
            player2NameLabel.setText(name);
        } else {
            throw new IllegalArgumentException("Invalid player: " + player);
        }
    }
}
