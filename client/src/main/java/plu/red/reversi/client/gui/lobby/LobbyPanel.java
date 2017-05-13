package plu.red.reversi.client.gui.lobby;

import plu.red.reversi.client.gui.util.ChatPanel;
import plu.red.reversi.client.gui.CorePanel;
import plu.red.reversi.client.gui.MainWindow;
import plu.red.reversi.client.gui.util.SettingsPanel;
import plu.red.reversi.client.gui.util.Utilities;
import plu.red.reversi.core.*;
import plu.red.reversi.core.lobby.Lobby;
import plu.red.reversi.core.lobby.PlayerSlot;
import plu.red.reversi.core.util.DataMap;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Glory to the Red Team.
 *
 * LobbyPanel that creates the GUI for a lobby. Acts as a sub-controller for the MainWindow, and controls all of the
 * GUI components related to a game lobby.
 */
public class LobbyPanel extends CorePanel implements ActionListener, ChangeListener {

    public final Lobby lobby;

    // Swing Components
    private SettingsPanel panelSettings;
    private ChatPanel panelChat;
    private JPanel playerSelectList;
    //private ArrayList<PlayerPanel> playerSlots = new ArrayList<>();

    private JButton startButton;
    private JButton loadButton;

    public LobbyPanel(MainWindow gui, Lobby lobby) {
        super(gui);
        this.lobby = lobby;

        populate();
    }

    @Override
    public void updateGUI() {
        populate();
    }

    protected final void populate() {
        this.removeAll();

        this.setLayout(new BorderLayout());

        startButton = new JButton("Start");
        startButton.setEnabled(lobby.canStart());
        startButton.addActionListener(this);

        loadButton = new JButton("Load");
        loadButton.addActionListener(this);

        if(lobby.isNetworked())
            loadButton.setEnabled(false);


        JPanel startContainer = new JPanel();
        startContainer.setLayout(new BoxLayout(startContainer, BoxLayout.X_AXIS));
        startContainer.add(loadButton);
        startContainer.add(Box.createHorizontalGlue());
        startContainer.add(startButton);

        panelSettings = new SettingsPanel(this);
        DataMap settings = lobby.getSettings();

        // Create the Board Size setting
        SettingsPanel.SliderEntry entryBoardSize = new SettingsPanel.SliderEntry(settings, SettingsLoader.GAME_BOARD_SIZE, 8, 32);
        entryBoardSize.slider.setMajorTickSpacing(10);
        entryBoardSize.slider.setMinorTickSpacing(2);
        entryBoardSize.slider.setPaintTicks(true);
        entryBoardSize.slider.setSnapToTicks(true);
        panelSettings.addEntry(entryBoardSize);

        // Create the Turn Skipping setting
        SettingsPanel.CheckBoxEntry entryAllowTurnSkipping = new SettingsPanel.CheckBoxEntry(settings, SettingsLoader.GAME_ALLOW_TURN_SKIPPING);
        panelSettings.addEntry(entryAllowTurnSkipping);

        // Update the SettingsPanel to reflect the new settings that have been added
        panelSettings.updateEntries();

        // Enable or Disable the SettingsPanel, based on whether or not the Lobby is hosting a Game loaded from a saved state
        panelSettings.setEnabled(!lobby.isGameLoaded());

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(startContainer, BorderLayout.SOUTH);
        JScrollPane scrollPane = new JScrollPane(panelSettings,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        Color c = Utilities.multiplyColor(getBackground(), 0.9f);
        scrollPane.setBackground(c);
        panelSettings.setBackground(c);
        rightPanel.add(scrollPane);
        this.add(rightPanel, BorderLayout.EAST);

        // Create the Chat Panel
        panelChat = new ChatPanel(gui.getController().getChat());
        lobby.addListener(panelChat);
        this.add(panelChat, BorderLayout.SOUTH);

        // Create the Player Slots
        playerSelectList = new JPanel();
        this.add(playerSelectList);
        populatePlayerList();

        // Refresh and Repaint
        this.revalidate();
        this.repaint();
    }

    protected final void populatePlayerList() {

        // Clear current list
        playerSelectList.removeAll();
        playerSelectList.setLayout(new BoxLayout(playerSelectList, BoxLayout.Y_AXIS));

        // Add Player Slots
        for(PlayerSlot slot : lobby.getAllSlots()) {
            PlayerPanel pp = new PlayerPanel(lobby, slot);
            playerSelectList.add(pp);
        }

        this.revalidate();
        this.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource() == startButton) {
            gui.startGame();
        }

        if(e.getSource() == loadButton) {
            gui.loadGame();
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if(e.getSource() == panelSettings) {
            panelSettings.saveSettings(lobby.getSettings());
        }
    }

    @Override
    public void cleanup() {
        // NOOP
    }
}
