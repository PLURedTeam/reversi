package plu.red.reversi.client.gui.util;

import plu.red.reversi.core.Coordinator;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.command.StatusCommand;
import plu.red.reversi.core.listener.ICommandListener;
import plu.red.reversi.core.listener.INetworkListener;
import plu.red.reversi.core.network.WebUtilities;
import plu.red.reversi.core.util.User;

import javax.swing.*;
import java.awt.*;

public class StatusBar extends JPanel implements ICommandListener, INetworkListener {

    private JLabel username;
    private JLabel statusMessage;

    public StatusBar() {
        this.setLayout(new BorderLayout());

        statusMessage = new JLabel("Reversi Loaded");
        statusMessage.setHorizontalAlignment(JLabel.CENTER);
        this.add(statusMessage, BorderLayout.CENTER);

        username = new JLabel("Not Logged In");
        username.setHorizontalAlignment(JLabel.LEFT);
        this.add(username, BorderLayout.WEST);

        Coordinator.addListenerStatic(this);
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

    /**
     * Called when a use logs out from the server
     *
     * @param loggedIn if the user is loggedIn
     */
    @Override
    public void onLogout(boolean loggedIn) {
        User user = WebUtilities.INSTANCE.getUser();
        if(loggedIn && user != null)
            username.setText("Username: " + user.getUsername());
        else
            username.setText("Not Logged In");
        this.repaint();
    }
}
