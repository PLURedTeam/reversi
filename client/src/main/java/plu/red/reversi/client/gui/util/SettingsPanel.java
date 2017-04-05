package plu.red.reversi.client.gui.util;

import plu.red.reversi.core.util.DataMap;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

/**
 * Glory to the Red Team.
 *
 * SettingsPanel GUI interface to allow for changing values in a <code>DataMap</code>.
 */
public class SettingsPanel extends JPanel {

    private LinkedList<Entry> entries = new LinkedList<>();

    private ChangeListener changeListener = null;

    public SettingsPanel() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(Box.createVerticalGlue());
    }

    public SettingsPanel(ChangeListener changeListener) {
        this.changeListener = changeListener;
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(Box.createVerticalGlue());
    }

    /**
     * Adds a setting Entry to this SettingsPanel. Does not update the GUI until updateEntries() is called.
     *
     * @param entry <code>SettingsMap.Entry</code> to add
     */
    public void addEntry(Entry entry) {
        entries.add(entry);
        entry.parent = this;
    }

    /**
     * Clears all Entries from this SettingsPanel. Does not update the GUI until updateEntries() is called.
     */
    public void clearEntries() {
        entries.clear();
    }

    /**
     * Updates the GUI with all changes to Entries in this SettingsMap.
     */
    public void updateEntries() {
        this.removeAll();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        for(Entry entry : entries) this.add(entry);

        this.add(Box.createVerticalGlue());

        this.revalidate();
    }

    /**
     * Saves all changed settings in this SettingsPanel to a given <code>DataMap</code>.
     *
     * @param settings DataMap to save to
     * @return Changed DataMap that was passed in
     */
    public DataMap saveSettings(DataMap settings) {
        for(Entry entry : entries) entry.save(settings);
        return settings;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        // Propogate setEnabled() to subcomponents
        for(Entry entry : entries) entry.setEnabled(enabled);
    }

    @Override
    public void setBackground(Color c) {
        super.setBackground(c);
        // Propogate setBackground() to subcomponents
        if(entries != null) {
            for (Entry entry : entries) {
                if (entry != null) entry.setBackground(c);
            }
        }
    }


    public static abstract class Entry extends JPanel {

        public final String name;
        protected SettingsPanel parent = null;

        protected Entry(String name) {
            this.name = name;
        }

        public abstract void save(DataMap settings);
    }

    public static class SliderEntry extends Entry implements ChangeListener {

        public final JLabel label;
        public final JLabel count;
        public final JSlider slider;
        public final JPanel container;

        public SliderEntry(DataMap settings, String name, int defaultMin, int defaultMax) {
            super(name);

            int min = defaultMin;
            int max = defaultMax;

            DataMap.Setting s = settings.getSetting(name);
            if(s instanceof DataMap.BoundedSetting) {
                DataMap.BoundedSetting bs = (DataMap.BoundedSetting)s;
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

            container = new JPanel();
            container.setLayout(new BorderLayout());
            container.add(label, BorderLayout.WEST);
            container.add(count, BorderLayout.EAST);
            container.add(slider, BorderLayout.SOUTH);
            this.add(container);
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            if(slider != null) slider.setEnabled(enabled);
        }

        @Override
        public void setBackground(Color c) {
            super.setBackground(c);
            if(label != null) label.setBackground(c);
            if(count != null) count.setBackground(c);
            if(slider != null) slider.setBackground(c);
            if(container != null) container.setBackground(c);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if(e.getSource() == slider) {
                count.setText(""+slider.getValue());
                if(parent != null && parent.changeListener != null)
                    parent.changeListener.stateChanged(new ChangeEvent(parent));
            }
        }

        @Override
        public void save(DataMap settings) {
            settings.set(name, slider.getValue());
        }
    }

    public static class CheckBoxEntry extends Entry implements ActionListener {

        public final JLabel label;
        public final JCheckBox checkBox;
        public final JPanel container;

        public CheckBoxEntry(DataMap settings, String name) {
            super(name);

            label = new JLabel(name);
            label.setHorizontalAlignment(SwingConstants.LEFT);

            checkBox = new JCheckBox();
            checkBox.setHorizontalAlignment(SwingConstants.RIGHT);
            checkBox.setSelected(settings.get(name, Boolean.class));
            checkBox.addActionListener(this);

            DataMap.Setting s = settings.getSetting(name);

            label.setToolTipText(s.getDescription());
            checkBox.setToolTipText(s.getDescription());

            container = new JPanel();
            container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
            container.add(label);
            container.add(Box.createRigidArea(new Dimension(15, 0)));
            container.add(Box.createHorizontalGlue());
            container.add(checkBox);
            this.add(container);
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            if(checkBox != null) checkBox.setEnabled(enabled);
        }

        @Override
        public void setBackground(Color c) {
            super.setBackground(c);
            if(label != null) label.setBackground(c);
            if(checkBox != null) checkBox.setBackground(c);
            if(container != null) container.setBackground(c);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == checkBox) {
                if(parent != null && parent.changeListener != null)
                    parent.changeListener.stateChanged(new ChangeEvent(parent));
            }
        }

        @Override
        public void save(DataMap settings) {
            settings.set(name, checkBox.isSelected());
        }
    }

    public static class TextFieldEntry extends Entry implements DocumentListener {

        public final JLabel label;
        public final JTextField textField;
        public final JPanel container;

        public TextFieldEntry(DataMap settings, String name, int columns) {
            super(name);

            label = new JLabel(name);
            //label.setHorizontalAlignment(SwingConstants.LEFT);

            textField = new JTextField();
            //textField.setHorizontalAlignment(SwingConstants.RIGHT);
            textField.setColumns(columns);
            textField.setText(settings.get(name, String.class));
            textField.getDocument().addDocumentListener(this);

            DataMap.Setting s = settings.getSetting(name);

            label.setToolTipText(s.getDescription());
            textField.setToolTipText(s.getDescription());

            container = new JPanel();
            container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
            container.add(label);
            container.add(Box.createRigidArea(new Dimension(15, 0)));
            container.add(Box.createHorizontalGlue());
            container.add(textField);
            this.add(container);
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            if(textField != null) textField.setEnabled(enabled);
        }

        @Override
        public void setBackground(Color c) {
            super.setBackground(c);
            if(label != null) label.setBackground(c);
            if(container != null) container.setBackground(c);
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            if(parent != null && parent.changeListener != null)
                parent.changeListener.stateChanged(new ChangeEvent(parent));
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            if(parent != null && parent.changeListener != null)
                parent.changeListener.stateChanged(new ChangeEvent(parent));
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            if(parent != null && parent.changeListener != null)
                parent.changeListener.stateChanged(new ChangeEvent(parent));
        }

        @Override
        public void save(DataMap settings) {
            settings.set(name, textField.getText());
        }
    }
}
