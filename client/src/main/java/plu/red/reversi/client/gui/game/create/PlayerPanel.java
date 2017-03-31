package plu.red.reversi.client.gui.game.create;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

public class PlayerPanel extends JPanel implements ActionListener {

    public final JLabel typeLabel;
    public final JButton removeButton;
    public final JComboBox<String> typeSelect;
    protected final JPanel middlePanel;
    protected SubPanel subPanel = null;
    protected final JPanel rightPanel;

    public enum SlotType {
        LOCAL,
        NETWORK,
        AI
    }

    protected SlotType slotType = SlotType.LOCAL;

    public PlayerPanel(ActionListener buttonListener) {
        this.setMaximumSize(new Dimension(10000, 64));

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        this.add(Box.createRigidArea(new Dimension(30, 0)));

        typeLabel = new JLabel("Local Player");
        typeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        typeLabel.setMinimumSize(new Dimension(150, 10));
        this.add(typeLabel);

        this.add(Box.createHorizontalGlue());

        middlePanel = new JPanel();
        middlePanel.setLayout(new BorderLayout());
        subPanel = new SubPanel.Local();
        middlePanel.add(subPanel, BorderLayout.CENTER);
        this.add(middlePanel);

        this.add(Box.createHorizontalGlue());

        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        // TODO: Make Player Slot Remove Button use an Icon
        removeButton = new JButton("Remove");
        removeButton.addActionListener(buttonListener);
        removeButton.setVerticalAlignment(SwingConstants.CENTER);
        removeButton.setHorizontalAlignment(SwingConstants.CENTER);
        rightPanel.add(removeButton);

        rightPanel.add(Box.createVerticalGlue());

        typeSelect = new JComboBox<>(new String[]{"Local", "Network", "AI"});
        typeSelect.addActionListener(this);
        rightPanel.add(typeSelect);

        this.add(rightPanel);

        this.add(Box.createRigidArea(new Dimension(15, 0)));
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if(removeButton != null) removeButton.setEnabled(enabled);
        if(typeSelect != null) typeSelect.setEnabled(enabled);
        if(middlePanel != null) middlePanel.setEnabled(enabled);
        if(subPanel != null) subPanel.setEnabled(enabled);
    }

    public SubPanel getSubPanel() { return subPanel; }

    public SlotType getType() { return slotType; }

    public void setType(SlotType type) {
        this.slotType = type;
        Color bg = subPanel.getBackground();
        middlePanel.removeAll();

        switch(type) {
            case NETWORK:
            case LOCAL: {
                typeLabel.setText("Local Player");
                subPanel = new SubPanel.Local();
                typeSelect.setSelectedItem("Local");
            } break;
            case AI: {
                typeLabel.setText("AI Player");
                subPanel = new SubPanel.AI();
                typeSelect.setSelectedItem("AI");
            } break;
            default: break;
        }

        subPanel.setBackground(bg);
        subPanel.setEnabled(middlePanel.isEnabled());

        middlePanel.add(subPanel, BorderLayout.CENTER);

        this.revalidate();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == typeSelect) {
            switch((String)typeSelect.getSelectedItem()) {
                default:
                case "Local":
                    setType(SlotType.LOCAL);
                    break;
                case "AI":
                    setType(SlotType.AI);
                    break;
            }
        }
    }

    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        if(subPanel != null) subPanel.setBackground(color);
        if(rightPanel != null) rightPanel.setBackground(color);
        if(middlePanel != null) middlePanel.setBackground(color);
    }

    public static abstract class SubPanel extends JPanel {

        public static class Local extends SubPanel {

        }

        public static class AI extends SubPanel {

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
                this.add(difficultySlider);
            }

            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                if(difficultySlider != null) difficultySlider.setEnabled(enabled);
            }

            @Override
            public void setBackground(Color color) {
                super.setBackground(color);
                if(difficultySlider != null) difficultySlider.setBackground(color);
            }
        }
    }


}
