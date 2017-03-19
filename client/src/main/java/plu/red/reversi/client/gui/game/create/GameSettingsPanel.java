package plu.red.reversi.client.gui.game.create;


import plu.red.reversi.core.SettingsLoader;
import plu.red.reversi.core.util.SettingsMap;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class GameSettingsPanel extends JPanel {

    SettingsMap settings;

    // Swing Components
    SliderSetting panelBoardSize;
    CheckBoxSetting panelAllowTurnSkipping;

    public GameSettingsPanel(SettingsMap settings) {
        this.settings = settings;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        panelBoardSize = new SliderSetting(settings, SettingsLoader.GAME_BOARD_SIZE, 8, 32);
        panelBoardSize.slider.setMajorTickSpacing(10);
        panelBoardSize.slider.setMinorTickSpacing(2);
        panelBoardSize.slider.setPaintTicks(true);
        panelBoardSize.slider.setSnapToTicks(true);
        this.add(panelBoardSize);

        panelAllowTurnSkipping = new CheckBoxSetting(settings, SettingsLoader.GAME_ALLOW_TURN_SKIPPING);
        this.add(panelAllowTurnSkipping);

        this.add(Box.createVerticalGlue());
    }

    public SettingsMap getSettings() {
        settings.set(SettingsLoader.GAME_BOARD_SIZE, panelBoardSize.slider.getValue());
        settings.set(SettingsLoader.GAME_ALLOW_TURN_SKIPPING, panelAllowTurnSkipping.checkBox.isSelected());
        return settings;
    }


    class SliderSetting extends JPanel implements ChangeListener {

        public final JLabel label;
        public final JLabel count;
        public final JSlider slider;

        public SliderSetting(SettingsMap settings, String name, int defaultMin, int defaultMax) {

            int min = defaultMin;
            int max = defaultMax;

            SettingsMap.Setting s = settings.getSetting(name);
            if(s instanceof SettingsMap.BoundedSetting) {
                SettingsMap.BoundedSetting bs = (SettingsMap.BoundedSetting)s;
                if(bs.getMin() != null) min = ((Number)bs.getMin()).intValue();
                if(bs.getMax() != null) max = ((Number)bs.getMax()).intValue();
            }

            label = new JLabel(name);
            label.setHorizontalAlignment(SwingConstants.LEFT);
            slider = new JSlider(JSlider.HORIZONTAL, min, max, settings.get(name, Integer.class));
            slider.addChangeListener(this);
            count = new JLabel();
            count.setHorizontalAlignment(SwingConstants.RIGHT);
            count.setText(""+slider.getValue());

            label.setToolTipText(s.getDescription());
            count.setToolTipText(s.getDescription());
            slider.setToolTipText(s.getDescription());

            this.setLayout(new FlowLayout());

            JPanel container = new JPanel();
            container.setLayout(new BorderLayout());
            container.add(label, BorderLayout.WEST);
            container.add(count, BorderLayout.EAST);
            container.add(slider, BorderLayout.SOUTH);
            this.add(container);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if(e.getSource() == slider) {
                count.setText(""+slider.getValue());
            }
        }
    }

    class CheckBoxSetting extends JPanel {

        public final JLabel label;
        public final JCheckBox checkBox;

        public CheckBoxSetting(SettingsMap settings, String name) {

            label = new JLabel(name);
            label.setHorizontalAlignment(SwingConstants.LEFT);

            checkBox = new JCheckBox();
            checkBox.setSelected(settings.get(name, Boolean.class));

            SettingsMap.Setting s = settings.getSetting(name);

            label.setToolTipText(s.getDescription());
            checkBox.setToolTipText(s.getDescription());

            JPanel container = new JPanel();
            container.setLayout(new BorderLayout());
            container.add(label, BorderLayout.WEST);
            container.add(checkBox, BorderLayout.EAST);
            this.add(container);
        }
    }
}
