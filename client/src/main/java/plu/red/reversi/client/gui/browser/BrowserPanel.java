package plu.red.reversi.client.gui.browser;


import plu.red.reversi.client.gui.CorePanel;
import plu.red.reversi.client.gui.MainWindow;
import plu.red.reversi.client.gui.util.ChatPanel;
import plu.red.reversi.core.browser.Browser;
import plu.red.reversi.core.network.WebUtilities;
import plu.red.reversi.core.util.ChatMessage;
import plu.red.reversi.core.util.GamePair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

/**
 * Glory to the Red Team.
 *
 * BrowserPanel that creates the GUI for browsing available games. Acts as a sub-controller for the MainWindow, and
 * controls all of the GUI components related to game browsing.
 */
public class BrowserPanel extends CorePanel implements ActionListener {

    public final Browser bowser;

    private ChatPanel panelChat;

    private JButton refreshButton;

    private BrowserPanel.BrowserCellRenderer cellRenderer = null;

    public BrowserPanel(MainWindow gui, Browser bowser) {
        super(gui);
        this.bowser = bowser;

        populate();
    }

    @Override
    public void updateGUI() { populate(); }

    protected final void populate() {
        this.removeAll();

        this.setLayout(new BorderLayout());

        // Create the Top Pane
        JPanel topPane = new JPanel();
        topPane.setLayout(new BoxLayout(topPane, BoxLayout.X_AXIS));
        topPane.add(Box.createHorizontalGlue());
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(this);
        topPane.add(refreshButton);
        topPane.add(Box.createRigidArea(new Dimension(10, 0)));
        this.add(topPane, BorderLayout.NORTH);

        // Create the Server List
        if(bowser.isConnected()) {
            JList<GamePair> list = new JList<>(bowser);
            //list.setSelectionModel(new BrowserPanel.BrowserListSelectionModel());
            cellRenderer = new BrowserPanel.BrowserCellRenderer();
            list.setCellRenderer(cellRenderer);

            //Add the listener to the JList
            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    buttonClicked(e.getPoint(), list);
                }//mouseClicked
            });

            this.add(new JScrollPane(list,
                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
                    BorderLayout.CENTER);
        } else {
            JPanel pane = new JPanel();
            pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
            pane.add(Box.createHorizontalGlue());
            pane.add(new JLabel("Failed to connect to server. Please try again later."));
            pane.add(Box.createHorizontalGlue());
            this.add(pane, BorderLayout.CENTER);
        }

        // Create the Chat Panel
        panelChat = new ChatPanel(gui.getController().getChat());
        bowser.addListener(panelChat);
        this.add(panelChat, BorderLayout.SOUTH);

        // Refresh and Repaint
        this.revalidate();
        this.repaint();
    }

    @Override
    public void cleanup() {
        // NOOP
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == refreshButton) {
            bowser.refresh();
        }
    }

    /**
     * Called when the mouse is clicked over the JList
     * @param p the point where the mouse was clicked
     * @param list the JList containing the network games
     */
    public void buttonClicked(Point p, JList<GamePair> list) {
        int index = list.locationToIndex(p);

        //Check if click is over button
        if(cellRenderer.buttons.get(index).getBounds().contains(p)) {
            System.out.println("over button");
            cellRenderer.buttons.get(index).doClick();
            GamePair game = list.getModel().getElementAt(index);
            int gameID = game.getGameID();
            WebUtilities.INSTANCE.joinGame(gameID);
        }//if

    }//buttonClicked



    private static final class BrowserListSelectionModel extends DefaultListSelectionModel {
        @Override public void setSelectionInterval(int i0, int i1) {
            super.setSelectionInterval(-1, -1);
        }
    }

    private static final class BrowserCellRenderer extends JPanel implements ListCellRenderer<GamePair> {

        public final HashMap<Integer, JButton> buttons = new HashMap<>();

        private void populate(GamePair val, int index) {
            this.removeAll();
            if(val == null) return;

            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            this.add(Box.createRigidArea(new Dimension(10, 0)));
            this.add(new JLabel(val.getGameName()));
            this.add(Box.createHorizontalGlue());
            this.add(new JLabel(val.getStatus().toString()));
            this.add(Box.createRigidArea(new Dimension(25, 0)));
            this.add(new JLabel(val.getPlayers().size() + "/" + val.getNumPlayers()));
            this.add(Box.createRigidArea(new Dimension(10, 0)));

            if(!buttons.containsKey(index)) {
                JButton button = new JButton("Join Game");
                button.putClientProperty("gameID",val.getGameID());
                buttons.put(index, button);
            }

            this.add(buttons.get(index));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends GamePair> list, GamePair value, int index, boolean isSelected, boolean cellHasFocus) {
            populate(value, index);
            return this;
        }
    }
}
