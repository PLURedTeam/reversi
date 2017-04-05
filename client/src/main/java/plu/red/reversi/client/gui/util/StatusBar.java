package plu.red.reversi.client.gui.util;

import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.command.StatusCommand;
import plu.red.reversi.core.listener.ICommandListener;

import javax.swing.*;
import java.awt.*;

public class StatusBar extends JPanel implements ICommandListener {

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
     * Called when a Command is being passed through Game and has been validated.
     *
     * @param cmd Command object that is being applied
     */
    @Override
    public void commandApplied(Command cmd) {
        if(cmd instanceof StatusCommand)
            setStatusMessage(((StatusCommand)cmd).message);
    }
}
