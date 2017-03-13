package plu.red.reversi.client.gui;


import plu.red.reversi.core.listener.IStatusListener;

import javax.swing.*;
import java.awt.*;

public class StatusBar extends JPanel implements IStatusListener {

    JLabel statusMessage;

    public StatusBar() {
        this.setLayout(new BorderLayout());

        statusMessage = new JLabel("Reversi Loaded");
        statusMessage.setHorizontalAlignment(JLabel.CENTER);
        this.add(statusMessage, BorderLayout.CENTER);
    }

    public String getStatusMessage() { return statusMessage.getText(); }
    public void setStatusMessage(String msg) { statusMessage.setText(msg); this.repaint(); }


    /**
     * Called when a status message is produced and passed around.
     *
     * @param message String representing the message
     */
    @Override
    public void onStatusMessage(String message) {
        setStatusMessage(message);
    }
}
