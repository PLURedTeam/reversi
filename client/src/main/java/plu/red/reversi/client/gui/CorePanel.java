package plu.red.reversi.client.gui;


import javax.swing.*;

public abstract class CorePanel extends JPanel {

    public final MainWindow gui;

    public CorePanel(MainWindow gui) {
        this.gui = gui;
    }

    public abstract void updateGUI();

    public abstract void cleanup();
}
