package plu.red.reversi.client.gui;

import plu.red.reversi.client.gui.util.SettingsPanel;
import plu.red.reversi.client.gui.util.Utilities;
import plu.red.reversi.core.SettingsLoader;
import plu.red.reversi.core.util.DataMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Glory to the Red Team.
 *
 * SettingsWindow that is opened when the Main wants to allow the user to change settings and options.
 */
public class SettingsWindow extends JFrame implements ActionListener, KeyListener {

    public final SettingsPanel settingsPanel;

    public final JButton acceptButton;
    public final JButton cancelButton;
    public final JButton defaultsButton;

    public SettingsWindow() {
        setTitle("Settings");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // Create our Settings Panel and populate it with options
        settingsPanel = new SettingsPanel();
        SettingsLoader.INSTANCE.loadClientSettings();
        DataMap settings = SettingsLoader.INSTANCE.getClientSettings();
        populateSettings(settings);

        // Restrict our minimum size
        this.setMinimumSize(new Dimension(350, 500));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Create and add our Scroll Pane, where the settings will be displayed
        JScrollPane scrollPane = new JScrollPane(settingsPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        Color c = Utilities.multiplyColor(getBackground(), 0.9f);
        scrollPane.setBackground(c);
        settingsPanel.setBackground(c);
        mainPanel.add(scrollPane);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Create Accept Button
        acceptButton = new JButton("Accept");
        acceptButton.setToolTipText("Save Settings and close");
        acceptButton.addActionListener(this);

        // Create Cancel Button
        cancelButton = new JButton("Cancel");
        cancelButton.setToolTipText("Close without saving");
        cancelButton.addActionListener(this);

        // Create Defaults Button
        defaultsButton = new JButton("Defaults");
        defaultsButton.setToolTipText("Reset all Settings to default values");
        defaultsButton.addActionListener(this);

        // Add the buttons to the bottom

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());

        buttonPanel.add(acceptButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        buttonPanel.add(defaultsButton);

        mainPanel.add(buttonPanel);

        this.add(mainPanel);

        this.addKeyListener(this);

        this.pack();
        this.setVisible(true);
    }

    protected final void populateSettings(DataMap settings) {

        // Clear anything remaining
        settingsPanel.clearEntries();

        // Username field
        SettingsPanel.TextFieldEntry username = new SettingsPanel.TextFieldEntry(settings, SettingsLoader.GLOBAL_USER_NAME, 12);
        username.addKeyListener(this);
        username.textField.addKeyListener(this);
        settingsPanel.addEntry(username);

        // Tell the SettingsPanel to update and revalidate
        settingsPanel.updateEntries();

        settingsPanel.addKeyListener(this);

    }

    protected final void saveAndClose() {
        DataMap settings = SettingsLoader.INSTANCE.getClientSettings();
        settingsPanel.saveSettings(settings);
        SettingsLoader.INSTANCE.saveClientSettings();
        SettingsLoader.INSTANCE.setClientSettings(settings);
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == acceptButton) {
            saveAndClose();
        } else if(e.getSource() == cancelButton) {
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        } else if(e.getSource() == defaultsButton) {
            populateSettings(SettingsLoader.checkClientDefaults(new DataMap()));
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
            saveAndClose();
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}