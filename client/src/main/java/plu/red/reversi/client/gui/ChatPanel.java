package plu.red.reversi.client.gui;


import plu.red.reversi.core.listener.IChatListener;
import plu.red.reversi.core.util.ChatLog;
import plu.red.reversi.core.util.ChatMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ChatPanel extends JPanel implements KeyListener, ActionListener, IChatListener {

    public final JList<ChatMessage> chatHistoryList;
    public final ChatLog chatLog;
    public final JTextField chatEntryField;
    public final JButton chatEntryButton;

    public ChatPanel() {

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

        this.setLayout(new BorderLayout());
        this.add(new JScrollPane(chatHistoryList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.NORTH);
        this.add(chatEntryField, BorderLayout.CENTER);
        this.add(chatEntryButton, BorderLayout.EAST);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getSource() == chatEntryField && e.getKeyCode() == KeyEvent.VK_ENTER) {
            chatEntryButton.doClick();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == chatEntryButton) {
            if(chatEntryField.getText().length() > 0) {
                addChat(new ChatMessage(ChatMessage.Channel.GLOBAL, "A User", chatEntryField.getText()));
                chatEntryField.setText("");
            }
        }
    }

    @Override
    public void onChat(ChatMessage message) {
        addChat(message);
    }

    void addChat(ChatMessage message) {
        chatLog.add(message);
        chatHistoryList.ensureIndexIsVisible(chatLog.getSize()-1);
    }

    protected static final class ChatCellRenderer extends JLabel implements ListCellRenderer<ChatMessage> {

        @Override
        public Component getListCellRendererComponent(JList<? extends ChatMessage> list, ChatMessage value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(value.toHTMLString());
            setFont(list.getFont());
            setOpaque(true);
            return this;
        }
    }
}
