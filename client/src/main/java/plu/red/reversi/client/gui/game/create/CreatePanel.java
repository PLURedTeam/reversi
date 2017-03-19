package plu.red.reversi.client.gui.game.create;

import plu.red.reversi.client.gui.ChatPanel;
import plu.red.reversi.client.gui.MainWindow;
import plu.red.reversi.client.player.HumanPlayer;
import plu.red.reversi.core.Game;
import plu.red.reversi.core.PlayerColor;
import plu.red.reversi.core.SettingsLoader;
import plu.red.reversi.core.player.BotPlayer;
import plu.red.reversi.core.util.SettingsMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;


public class CreatePanel extends JPanel implements ActionListener {

    public final MainWindow gui;

    // Swing Components
    GameSettingsPanel panelSettings;
    ChatPanel panelChat;
    JPanel playerSelectList;
    PlayerPanelSelect addPlayerButtons;
    ArrayList<PlayerPanel> playerSlots = new ArrayList<>();
    JButton startButton;

    public CreatePanel(MainWindow gui) {
        this.gui = gui;

        this.setLayout(new BorderLayout());

        populate(SettingsLoader.INSTANCE.createGameSettings());
    }

    protected final void populate(SettingsMap settings) {
        this.removeAll();

        startButton = new JButton("Start");
        startButton.addActionListener(this);
        JPanel startContainer = new JPanel();
        startContainer.add(startButton);
        panelSettings = new GameSettingsPanel(settings);
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(startContainer, BorderLayout.SOUTH);
        rightPanel.add(new JScrollPane(panelSettings,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        this.add(rightPanel, BorderLayout.EAST);

        panelChat = new ChatPanel();
        this.add(panelChat, BorderLayout.SOUTH);

        playerSelectList = new JPanel();
        this.add(playerSelectList);
        populatePlayerList();

        this.revalidate();
    }

    protected final void populatePlayerList() {

        // Clear current list
        playerSelectList.removeAll();
        playerSelectList.setLayout(new BoxLayout(playerSelectList, BoxLayout.Y_AXIS));

        // Add Player Slots
        for(int i = 0; i < playerSlots.size(); i++) {
            PlayerPanel slot = playerSlots.get(i);
            Color color = PlayerColor.validPlayers()[i].color;
            slot.setBackground(new Color((color.getRed()-192)*3/8+192, (color.getGreen()-192)*3/8+192, (color.getBlue()-192)*3/8+192));
            playerSelectList.add(slot);
        }

        // Keep at bottom
        addPlayerButtons = new PlayerPanelSelect(this);
        if(playerSlots.size() < PlayerColor.validPlayers().length)
            playerSelectList.add(addPlayerButtons);

        this.revalidate();
        this.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean rebuildPlayerSlots = false;

        if(e.getSource() == addPlayerButtons.bot) {
            playerSlots.add(new PlayerPanel.PlayerPanelBot(this));
            rebuildPlayerSlots = true;
        }

        if(e.getSource() == addPlayerButtons.human) {
            playerSlots.add(new PlayerPanel.PlayerPanelHuman(this));
            rebuildPlayerSlots = true;
        }

        PlayerPanel removePanel = null;
        for(PlayerPanel slot : playerSlots) {
            if(e.getSource() == slot.removeButton) {
                removePanel = slot;
                rebuildPlayerSlots = true;
            }
        }
        if(removePanel != null) playerSlots.remove(removePanel);

        if(rebuildPlayerSlots) populatePlayerList();

        if(e.getSource() == startButton) {
            attemptGameStart();
        }
    }

    protected final void attemptGameStart() {
        SettingsMap settings = panelSettings.getSettings();
        settings.set(SettingsLoader.GAME_PLAYER_COUNT, playerSlots.size());
        Game game = new Game(settings);

        for(int i = 0; i < playerSlots.size(); i++) {
            PlayerPanel slot = playerSlots.get(i);
            PlayerColor color = PlayerColor.validPlayers()[i];
            if(slot instanceof PlayerPanel.PlayerPanelBot)
                game.setPlayer(new BotPlayer(game, color, ((PlayerPanel.PlayerPanelBot)slot).difficultySlider.getValue()*2));
            else if(slot instanceof PlayerPanel.PlayerPanelHuman)
                game.setPlayer(new HumanPlayer(game, color));
        }

        game.initialize();

        gui.startGame(game);
    }

    public static class PlayerPanelSelect extends JPanel {

        public final JButton human;
        public final JButton network;
        public final JButton bot;

        public PlayerPanelSelect(ActionListener buttonListener) {
            this.setMaximumSize(new Dimension(10000, 36));

            human = new JButton("Add Local Player");
            human.setVerticalAlignment(SwingConstants.CENTER);
            human.setHorizontalAlignment(SwingConstants.CENTER);
            human.addActionListener(buttonListener);
            network = new JButton("Add Networked Player");
            network.setVerticalAlignment(SwingConstants.CENTER);
            network.setHorizontalAlignment(SwingConstants.CENTER);
            network.addActionListener(buttonListener);
            bot = new JButton("Add AI Player");
            bot.setVerticalAlignment(SwingConstants.CENTER);
            bot.setHorizontalAlignment(SwingConstants.CENTER);
            bot.addActionListener(buttonListener);

            this.add(human);
            this.add(network);
            this.add(bot);
        }
    }
}
