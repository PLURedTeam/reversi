package plu.red.reversi.client.gui;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ChatPanel extends JPanel implements KeyListener, ActionListener {

    public final JList<String> chatHistoryList;
    public final DefaultListModel<String> chatHistory;
    public final JTextField chatEntryField;
    public final JButton chatEntryButton;

    public ChatPanel() {

        // Create the History
        this.chatHistory = new DefaultListModel<>();
        this.chatHistoryList = new JList<>(this.chatHistory);
        this.chatHistoryList.setSelectionModel(new DefaultListSelectionModel() {
            @Override public void setSelectionInterval(int i0, int i1) {
                super.setSelectionInterval(-1, -1);
            }
        });

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
                chatHistory.addElement(chatEntryField.getText());
                chatEntryField.setText("");
            }
        }
    }
}
