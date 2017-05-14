package plu.red.reversi.client.gui.util;


import com.vdurmont.emoji.EmojiManager;
import plu.red.reversi.core.Client;
import plu.red.reversi.core.command.ChatCommand;
import plu.red.reversi.core.listener.IChatListener;
import plu.red.reversi.core.listener.INetworkListener;
import plu.red.reversi.core.network.WebUtilities;
import plu.red.reversi.core.util.ChatMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class ChatPanel extends JPanel implements KeyListener, ActionListener, IChatListener, INetworkListener {

    private class TabLabel extends JLabel {
        @Override
        public Dimension getMaximumSize() {
            return new Dimension(150, 30);
        }
        @Override
        public Dimension getPreferredSize() {
            Dimension dim = super.getPreferredSize();
            return new Dimension(Math.min(150, dim.width), dim.height);
        }
        public TabLabel(String text) {
            super(text);
        }
    }

    //public final JList<ChatMessage> chatHistoryList;
    public final JTabbedPane tabPane;
    protected ArrayList<JList<ChatMessage>> tabList;
    protected plu.red.reversi.core.util.ChatLog chat;
    public final JTextField chatEntryField;
    public final JButton chatEntryButton;

    private final JPanel emojiPanel;

    private final JButton emojiGrinning;
    private final JButton emojiFrowning;
    private final JButton emojiZipped;
    private final JButton emojiAngry;
    private final JButton emojiTongue;
    private final JButton emojiEyeroll;
    private final JButton emojiConfused;
    private final JButton emojiInnocent;
    private final JButton emojiHourglass;

    private void setupEmojiButton(JButton button) {
        button.addActionListener(this);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        //button.setContentAreaFilled(false);
        button.setBackground(Utilities.multiplyColor(this.getBackground(), 0.9f));
        emojiPanel.add(button);
    }

    public ChatPanel(plu.red.reversi.core.util.ChatLog chat) {

        // Create the Tabbed Pane
        tabPane = new JTabbedPane();
        tabList = new ArrayList<>();

        // Create the Entry Field
        this.chatEntryField = new JTextField();
        this.chatEntryField.addKeyListener(this);

        // Create the Entry Button
        this.chatEntryButton = new JButton("Chat");
        this.chatEntryButton.addActionListener(this);
        this.chatEntryButton.setEnabled(WebUtilities.INSTANCE.loggedIn());
        if(!this.chatEntryButton.isEnabled())
            this.chatEntryButton.setToolTipText("Login to Chat");

        emojiGrinning   = new JButton(EmojiManager.getForAlias(":grinning:").getUnicode());
        emojiFrowning   = new JButton(EmojiManager.getForAlias(":frowning:").getUnicode());
        emojiZipped     = new JButton(EmojiManager.getForAlias(":zipper_mouth:").getUnicode());
        emojiAngry      = new JButton(EmojiManager.getForAlias(":angry:").getUnicode());
        emojiTongue     = new JButton(EmojiManager.getForAlias(":stuck_out_tongue:").getUnicode());
        emojiEyeroll    = new JButton(EmojiManager.getForAlias(":eye_roll:").getUnicode());
        emojiConfused   = new JButton(EmojiManager.getForAlias(":confused:").getUnicode());
        emojiInnocent   = new JButton(EmojiManager.getForAlias(":innocent:").getUnicode());
        emojiHourglass  = new JButton(EmojiManager.getForAlias(":hourglass_flowing_sand:").getUnicode());

        emojiPanel = new JPanel();
        setupEmojiButton(emojiGrinning);
        setupEmojiButton(emojiFrowning);
        setupEmojiButton(emojiZipped);
        setupEmojiButton(emojiAngry);
        setupEmojiButton(emojiTongue);
        setupEmojiButton(emojiEyeroll);
        setupEmojiButton(emojiConfused);
        setupEmojiButton(emojiInnocent);
        setupEmojiButton(emojiHourglass);

        this.setLayout(new BorderLayout());
        this.add(tabPane, BorderLayout.NORTH);
        this.add(chatEntryField, BorderLayout.CENTER);
        this.add(chatEntryButton, BorderLayout.EAST);
        this.add(emojiPanel, BorderLayout.SOUTH);

        setChat(chat);
    }

    public void setChat(plu.red.reversi.core.util.ChatLog chat) {
        this.chat = chat;
        tabPane.removeAll();
        tabList.clear();
        for(ChatLog.ChannelLog channel : ((ChatLog)chat)) {
            JList<ChatMessage> list = new JList<>(channel);
            list.setSelectionModel(new ChatListSelectionModel());
            list.setCellRenderer(new ChatCellRenderer());
            tabPane.add(channel.channel, new JScrollPane(list,
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
            tabList.add(list);
        }
        for(int i = 0; i < tabPane.getTabCount(); i++) {
            JList<ChatMessage> list = tabList.get(i);
            JLabel tabLabel = new TabLabel(((ChatLog.ChannelLog)list.getModel()).channel);
            tabPane.setTabComponentAt(i, tabLabel);
            list.ensureIndexIsVisible(list.getModel().getSize()-1);
        }
        this.revalidate();
        this.repaint();
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
                int i = tabPane.getSelectedIndex();
                if(i >= 0) {
                    ChatMessage message = new ChatMessage(((ChatLog.ChannelLog)tabList.get(i).getModel()).channel, WebUtilities.INSTANCE.getUser().getUsername(), chatEntryField.getText());
                    chatEntryField.setText("");
                    Client.getInstance().getCore().acceptCommand(new ChatCommand(message));
                }
            }
        }

        if(e.getSource() == emojiGrinning)
            chatEntryField.setText(chatEntryField.getText() + ":grinning:");
        if(e.getSource() == emojiFrowning)
            chatEntryField.setText(chatEntryField.getText() + ":frowning:");
        if(e.getSource() == emojiZipped)
            chatEntryField.setText(chatEntryField.getText() + ":zipper_mouth:");
        if(e.getSource() == emojiAngry)
            chatEntryField.setText(chatEntryField.getText() + ":angry:");
        if(e.getSource() == emojiTongue)
            chatEntryField.setText(chatEntryField.getText() + ":stuck_out_tongue:");
        if(e.getSource() == emojiEyeroll)
            chatEntryField.setText(chatEntryField.getText() + ":eye_roll:");
        if(e.getSource() == emojiConfused)
            chatEntryField.setText(chatEntryField.getText() + ":confused:");
        if(e.getSource() == emojiInnocent)
            chatEntryField.setText(chatEntryField.getText() + ":innocent:");
        if(e.getSource() == emojiHourglass)
            chatEntryField.setText(chatEntryField.getText() + ":hourglass_flowing_sand:");
    }

    @Override
    public void onChat(ChatMessage message) {
        addChat(message);
    }

    @Override
    public void onLogout(boolean loggedIn) {
        chatEntryButton.setEnabled(loggedIn);
        chatEntryButton.setToolTipText(loggedIn ? null : "Login to Chat");
        this.revalidate();
        this.repaint();
    }

    void addChat(ChatMessage message) {
        chat.offer(message);
        int index = tabPane.getSelectedIndex();
        if(index > -1) {
            JList<ChatMessage> list = tabList.get(index);
            if(list.getModel() == ((ChatLog)chat).get(message.channel))
                list.ensureIndexIsVisible(list.getModel().getSize()-1);
        }
        this.repaint();
    }

    private static final class ChatListSelectionModel extends DefaultListSelectionModel {
        @Override public void setSelectionInterval(int i0, int i1) {
            super.setSelectionInterval(-1, -1);
        }
    }

    private static final class ChatCellRenderer extends JPanel implements ListCellRenderer<ChatMessage> {

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
