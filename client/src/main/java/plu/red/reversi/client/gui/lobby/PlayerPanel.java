package plu.red.reversi.client.gui.lobby;

import plu.red.reversi.client.gui.util.Utilities;
import plu.red.reversi.core.lobby.Lobby;
import plu.red.reversi.core.lobby.PlayerSlot;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

/**
 * Glory to the Red Team.
 *
 * PlayerPanel to represent a slot in a game lobby. Can be a local player slot, open network slot, or AI slot.
 */
public class PlayerPanel extends JPanel implements ActionListener {

    public final JLabel typeLabel;
    public final JComboBox<PlayerSlot.SlotType> typeSelect;
    protected final JPanel middlePanel;
    protected SubPanel subPanel = null;
    protected final JPanel rightPanel;

    public final Lobby lobby;
    public final PlayerSlot slot;

    public PlayerPanel(Lobby lobby, PlayerSlot slot) {
        this.lobby = lobby;
        this.slot = slot;

        this.setMaximumSize(new Dimension(10000, 64));

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        this.add(Box.createRigidArea(new Dimension(30, 0)));

        // Create the Type Label
        typeLabel = new JLabel(slot.getName());
        typeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        typeLabel.setMinimumSize(new Dimension(150, 10));
        this.add(typeLabel);

        this.add(Box.createHorizontalGlue());

        // Create the middle panel, whose type depends on what type of slot this is
        middlePanel = new JPanel();
        middlePanel.setLayout(new BorderLayout());

        switch(slot.getType()) {
            default:
            case NETWORK: // No separate Network type yet
            case LOCAL:
                subPanel = new Local();
                break;
            case AI:
                subPanel = new AI();
                break;
        }

        middlePanel.add(subPanel, BorderLayout.CENTER);
        this.add(middlePanel);

        this.add(Box.createHorizontalGlue());

        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        rightPanel.add(Box.createVerticalGlue());

        // Create the type Selector
        typeSelect = new JComboBox<>(lobby.getValidSlotTypes());
        typeSelect.setSelectedItem(slot.getType());
        typeSelect.addActionListener(this);
        rightPanel.add(typeSelect);

        rightPanel.add(Box.createVerticalGlue());

        this.add(rightPanel);

        this.add(Box.createRigidArea(new Dimension(15, 0)));

        this.setBackground(Utilities.getLessContrastColor(new Color(slot.getColor().composite)));
        this.setEnabled(!(lobby.isGameLoaded() || lobby.isNetworked()));
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        // Propogate setEnabled() to subcomponents
        if(typeSelect != null) typeSelect.setEnabled(enabled);
        if(middlePanel != null) middlePanel.setEnabled(enabled);
        if(subPanel != null) subPanel.setEnabled(enabled);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // Type Select Action
        if(e.getSource() == typeSelect) {
            slot.setType((PlayerSlot.SlotType)typeSelect.getSelectedItem());
        }
    }

    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        // Propogate setBackground() to subcomponents
        if(subPanel != null) subPanel.setBackground(color);
        if(rightPanel != null) rightPanel.setBackground(color);
        if(middlePanel != null) middlePanel.setBackground(color);
    }

    /**
     * SubPanel that represents the middle panel of the slot. Has different subclasses for different slot types.
     */
    abstract class SubPanel extends JPanel {}

    class Local extends SubPanel {}

    class AI extends SubPanel implements ChangeListener {

        public final JSlider difficultySlider;

        public AI() {
            difficultySlider = new JSlider(1, 9);
            Hashtable<Integer, JLabel> sliderLabels = new Hashtable<>();
            sliderLabels.put(1, new JLabel("Easy"));
            sliderLabels.put(5, new JLabel("Medium"));
            sliderLabels.put(9, new JLabel("Hard"));
            difficultySlider.setLabelTable(sliderLabels);
            difficultySlider.setPaintLabels(true);
            difficultySlider.setMajorTickSpacing(2);
            difficultySlider.setMinorTickSpacing(1);
            difficultySlider.setPaintTicks(true);
            difficultySlider.setMaximumSize(new Dimension(300, 40));
            difficultySlider.setValue(slot.getAIDifficulty());
            difficultySlider.addChangeListener(this);
            this.add(difficultySlider);
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            // Propogate setEnabled() to subcomponents
            if (difficultySlider != null) difficultySlider.setEnabled(enabled);
        }

        @Override
        public void setBackground(Color color) {
            super.setBackground(color);
            // Propogate setBackground() to subcomponents
            if (difficultySlider != null) difficultySlider.setBackground(color);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (e.getSource() == difficultySlider) {
                slot.setAIDifficulty(difficultySlider.getValue());
            }
        }
    }


}
