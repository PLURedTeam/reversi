package plu.red.reversi.client.gui.game.create;

import plu.red.reversi.client.gui.ChatPanel;
import plu.red.reversi.client.gui.MainWindow;
import plu.red.reversi.client.gui.util.Utilities;
import plu.red.reversi.core.player.HumanPlayer;
import plu.red.reversi.core.Game;
import plu.red.reversi.core.PlayerColor;
import plu.red.reversi.core.SettingsLoader;
import plu.red.reversi.core.player.BotPlayer;
import plu.red.reversi.core.player.Player;
import plu.red.reversi.core.util.DataMap;

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
    JButton loadButton;

    // If null, creating a new game, otherwise loading a game
    Game loadedGame = null;

    public CreatePanel(MainWindow gui) {
        this.gui = gui;

        populate(SettingsLoader.INSTANCE.createGameSettings());
    }

    public CreatePanel(MainWindow gui, Game loadedGame) {
        this.gui = gui;
        this.loadedGame = loadedGame;

        for(PlayerColor color : PlayerColor.validPlayers()) {
            Player player = loadedGame.getPlayer(color);
            if(player != null) {
                PlayerPanel panel = new PlayerPanel(this);
                if(player instanceof BotPlayer) {
                    panel.setType(PlayerPanel.SlotType.AI);
                    PlayerPanel.SubPanel sp = panel.getSubPanel();
                    if(sp instanceof PlayerPanel.SubPanel.AI)
                        ((PlayerPanel.SubPanel.AI)sp).difficultySlider.setValue(((BotPlayer)player).getDifficulty());
                } else {
                    panel.setType(PlayerPanel.SlotType.LOCAL);
                }
                playerSlots.add(panel);
            }
        }

        populate(loadedGame.getSettings());
    }

    protected final void populate(DataMap settings) {
        this.removeAll();

        this.setLayout(new BorderLayout());

        startButton = new JButton("Start");
        startButton.addActionListener(this);

        loadButton = new JButton("Load");
        loadButton.addActionListener(this);

        JPanel startContainer = new JPanel();
        startContainer.setLayout(new BoxLayout(startContainer, BoxLayout.X_AXIS));
        startContainer.add(loadButton);
        startContainer.add(Box.createHorizontalGlue());
        startContainer.add(startButton);

        panelSettings = new GameSettingsPanel(settings);
        panelSettings.setEnabled(loadedGame == null);
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
            slot.setBackground(Utilities.getLessContrastColor(color));
            playerSelectList.add(slot);
            slot.setEnabled(loadedGame == null);
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

        if(e.getSource() == addPlayerButtons.add) {
            playerSlots.add(new PlayerPanel(this));
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

        if(e.getSource() == loadButton) {
            gui.loadGame();
        }
    }

    protected final void attemptGameStart() {

        if(loadedGame == null) {

            // Create a new Game
            loadedGame = new Game();

            // Add Settings to the Game
            DataMap settings = panelSettings.getSettings();
            settings.set(SettingsLoader.GAME_PLAYER_COUNT, playerSlots.size());
            loadedGame.setSettings(settings);

            // Add Players to the Game
            for (int i = 0; i < playerSlots.size(); i++) {
                PlayerPanel.SubPanel slot = playerSlots.get(i).getSubPanel();
                PlayerColor color = PlayerColor.validPlayers()[i];
                if (slot instanceof PlayerPanel.SubPanel.AI)
                    loadedGame.setPlayer(new BotPlayer(loadedGame, color, ((PlayerPanel.SubPanel.AI) slot).difficultySlider.getValue()));
                else if (slot instanceof PlayerPanel.SubPanel.Local)
                    loadedGame.setPlayer(new HumanPlayer(loadedGame, color));
            }
        }

        loadedGame.initialize();

        gui.startGame(loadedGame);
    }

    public static class PlayerPanelSelect extends JPanel {

        public final JButton add;
        //public final JButton network;
        //public final JButton bot;

        public PlayerPanelSelect(ActionListener buttonListener) {
            this.setMaximumSize(new Dimension(10000, 36));

            add = new JButton("Add Player");
            add.setVerticalAlignment(SwingConstants.CENTER);
            add.setHorizontalAlignment(SwingConstants.CENTER);
            add.addActionListener(buttonListener);

            /*
            network = new JButton("Add Networked Player");
            network.setVerticalAlignment(SwingConstants.CENTER);
            network.setHorizontalAlignment(SwingConstants.CENTER);
            network.addActionListener(buttonListener);

            bot = new JButton("Add AI Player");
            bot.setVerticalAlignment(SwingConstants.CENTER);
            bot.setHorizontalAlignment(SwingConstants.CENTER);
            bot.addActionListener(buttonListener);
            */

            this.add(add);
            //this.add(network);
            //this.add(bot);
        }
    }
}
