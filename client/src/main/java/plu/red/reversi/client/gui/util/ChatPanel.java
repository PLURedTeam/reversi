package plu.red.reversi.client.gui.util;


import plu.red.reversi.core.Client;
import plu.red.reversi.core.command.ChatCommand;
import plu.red.reversi.core.listener.IChatListener;
import plu.red.reversi.core.listener.INetworkListener;
import plu.red.reversi.core.network.WebUtilities;
import plu.red.reversi.core.util.ChatLog;
import plu.red.reversi.core.util.ChatMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ChatPanel extends JPanel implements KeyListener, ActionListener, IChatListener, INetworkListener {

    public final JList<ChatMessage> chatHistoryList;
    public final ChatLog chatLog;
    public final JTextField chatEntryField;
    public final JButton chatEntryButton;

    public final String channel;

    public ChatPanel(String channel) {
        this.channel = channel;

        // Create the History
        this.chatLog = new ChatLog();
        this.chatHistoryList = new JList<>(this.chatLog);
        this.chatHistoryList.setSelectionModel(new DefaultListSelectionModel() {
            @Override public void setSelectionInterval(int i0, int i1) {
                super.setSelectionInterval(-1, -1);
            }
        });
        this.chatHistoryList.setCellRenderer(new ChatCellRenderer());

        // Create the Entry Field
        this.chatEntryField = new JTextField();
        this.chatEntryField.addKeyListener(this);

        // Create the Entry Button
        this.chatEntryButton = new JButton("Chat");
        this.chatEntryButton.addActionListener(this);
        this.chatEntryButton.setEnabled(WebUtilities.INSTANCE.loggedIn());

        this.setLayout(new BorderLayout());
        this.add(new JScrollPane(chatHistoryList,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.NORTH);
        this.add(chatEntryField, BorderLayout.CENTER);
        this.add(chatEntryButton, BorderLayout.EAST);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getSource() == chatEntryField && e.getKeyCode() == KeyEvent.VK_ENTER) {
            if(chatEntryButton.isEnabled()) chatEntryButton.doClick();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == chatEntryButton) {
            if(chatEntryField.getText().length() > 0) {
                ChatMessage message = new ChatMessage(ChatMessage.Channel.GLOBAL, WebUtilities.INSTANCE.getUser().getUsername(), chatEntryField.getText());
                chatEntryField.setText("");
                Client.getInstance().getCore().acceptCommand(new ChatCommand(message));
            }
        }
    }

    @Override
    public void onChat(ChatMessage message) {
        if(message.channel.equals(channel))
            addChat(message);
    }

    @Override
    public void onLogout(boolean loggedIn) {
        chatEntryButton.setEnabled(loggedIn);
        this.revalidate();
        this.repaint();
    }

    void addChat(ChatMessage message) {
        chatLog.add(message);
        chatHistoryList.ensureIndexIsVisible(chatLog.getSize()-1);
    }

    protected static final class ChatCellRenderer extends JPanel implements ListCellRenderer<ChatMessage> {

        private void populate(ChatMessage msg, boolean isSmall) {
            if(msg == null) return;

            this.removeAll();

            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            if(isSmall) {
                JLabel timelabel = new JLabel("[...]");
                timelabel.setToolTipText(msg.getTimeString());
                this.add(timelabel);
            } else this.add(new JLabel("[" + msg.getTimeString() + "]"));

            JLabel namelabel = new JLabel(msg.username);
            namelabel.setForeground(new Color(msg.usercolor.composite));
            this.add(namelabel);

            this.add(new JLabel(": " + msg.message));

            this.add(Box.createHorizontalGlue());
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ChatMessage> list, ChatMessage value, int index, boolean isSelected, boolean cellHasFocus) {
            //setFont(list.getFont());
            //setOpaque(true);
            populate(value, list.getWidth() < 150);
            return this;
        }
    }
}
