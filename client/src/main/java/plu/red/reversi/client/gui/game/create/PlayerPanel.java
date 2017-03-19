package plu.red.reversi.client.gui.game.create;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Hashtable;

public abstract class PlayerPanel extends JPanel {

    public final JButton removeButton;

    public PlayerPanel(ActionListener buttonListener) {
        this.setMaximumSize(new Dimension(10000, 64));

        // TODO: Make Player Slot Remove Button use an Icon
        removeButton = new JButton("Remove");
        removeButton.addActionListener(buttonListener);
        removeButton.setVerticalAlignment(SwingConstants.CENTER);

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    }

    public static class PlayerPanelBot extends PlayerPanel {

        public final JSlider difficultySlider;

        public PlayerPanelBot(ActionListener buttonListener) {
            super(buttonListener);

            this.add(Box.createRigidArea(new Dimension(30, 0)));

            JLabel label = new JLabel("AI Player");
            label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
            label.setMinimumSize(new Dimension(150, 10));
            this.add(label);

            this.add(Box.createHorizontalGlue());

            difficultySlider = new JSlider(1, 5);
            Hashtable<Integer, JLabel> sliderLabels = new Hashtable<>();
            sliderLabels.put(1, new JLabel("Easy"));
            sliderLabels.put(3, new JLabel("Medium"));
            sliderLabels.put(5, new JLabel("Hard"));
            difficultySlider.setLabelTable(sliderLabels);
            difficultySlider.setPaintLabels(true);
            difficultySlider.setMajorTickSpacing(2);
            difficultySlider.setMinorTickSpacing(1);
            difficultySlider.setPaintTicks(true);
            difficultySlider.setMaximumSize(new Dimension(300, 40));
            this.add(difficultySlider);

            this.add(Box.createHorizontalGlue());

            this.add(removeButton);

            this.add(Box.createRigidArea(new Dimension(15, 0)));
        }

        @Override
        public void setBackground(Color color) {
            super.setBackground(color);
            if(difficultySlider != null) difficultySlider.setBackground(color);
        }
    }

    public static class PlayerPanelHuman extends PlayerPanel {

        public PlayerPanelHuman(ActionListener buttonListener) {
            super(buttonListener);

            this.add(Box.createRigidArea(new Dimension(30, 0)));

            JLabel label = new JLabel("Human Player");
            label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
            label.setMinimumSize(new Dimension(150, 10));
            this.add(label);

            this.add(Box.createHorizontalGlue());

            this.add(removeButton);

            this.add(Box.createRigidArea(new Dimension(15, 0)));
        }
    }


}
