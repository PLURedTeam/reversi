package plu.red.reversi.client.gui;


import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Glory to the Red Team.
 *
 * Menu bar for displaying selections such as changing Main settings and seeing an About screen.
 */
public class OptionsMenu extends JMenu implements ActionListener {

    public final MainWindow gui;

    private JMenuItem settingsItem;
    private JMenuItem aboutItem;

    public OptionsMenu(MainWindow gui) {
        this.gui = gui;

        this.setText("Options");
        this.setMnemonic(KeyEvent.VK_O);
        this.getAccessibleContext().setAccessibleDescription("Options and Settings");

        settingsItem = new JMenuItem("Settings");
        settingsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_MASK));
        settingsItem.addActionListener(this);
        this.add(settingsItem);

        this.addSeparator();

        aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(this);
        this.add(aboutItem);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == settingsItem) {
            //SettingsWindow settingsWindow = new SettingsWindow();
            gui.openClientSettings();
        } else if(e.getSource() == aboutItem) {
            // NOOP
            // TODO: Create an About window
        }
    }
}
