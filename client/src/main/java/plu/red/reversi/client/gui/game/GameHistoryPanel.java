package plu.red.reversi.client.gui.game;

import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.listener.ICommandListener;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;

/**
 * This panel displays the history of moves that have taken place
 * througout the game.  It should be updated after every move.
 *
 * TODO:  This currently uses an internal class called Move, which really
 * doesn't belong here.  I suggest moving that over to a model subsystem
 * because it is something many subsystems might be interested in.
 */
public class GameHistoryPanel extends JPanel implements ICommandListener, ListSelectionListener {

    private JTable historyTable;
    private CommandHistoryTableModel tableModel;

    public final Game game;

    private HistoryPanelListener listener;

    private int selectedIndex;

    private class CommandHistoryTableModel extends AbstractTableModel {
        @Override
        public String getColumnName(int column) {
            if( column == 0 ) return "#";
            else if( column == 1 ) return "Move";
            else if( column == 2 ) return "Player";
            return "";
        }

        @Override
        public int getRowCount() {
            return game.getHistory().getNumBoardCommands();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if( rowIndex >= game.getHistory().getNumBoardCommands() ) return null;

            BoardCommand command = game.getHistory().getBoardCommand(rowIndex);

                if( columnIndex == 0 ) return "" + (rowIndex + 1);
                else if( columnIndex == 1) return command.position;
                else if( columnIndex == 2) {
                    // Would be better if player was actually connecting to the actual player object here???
                    //  there is no way to get a reference to the player short of having the game, and that is bad here
                    //  since this is just supposed to show the history.

                    // Done! :P There's no reason not to keep a Game reference. The other option would be to
                    //  store the Player reference with the Command, which while doable makes it harder to
                    //  reconstruct Commands from a saved state. -James
                    return game.getPlayer(command.playerID).getName();
                }

                return null;
        }
    }

    /**
     * Construct a new history panel.  Currently, this places some example
     * history into the panel.  This should be removed.
     */
    public GameHistoryPanel(Game game) {
        this.game = game;

        game.addListener(this);

        this.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        this.setLayout(new BorderLayout());

        tableModel = new CommandHistoryTableModel();

        historyTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setPreferredSize(new Dimension(250,0));
        historyTable.setGridColor(new Color(220,220,220));
        historyTable.setShowGrid(true);
        historyTable.setFillsViewportHeight(true);

        TableColumnModel cmod = historyTable.getColumnModel();
        cmod.getColumn(0).setPreferredWidth(20);
        cmod.getColumn(1).setPreferredWidth(20);
        historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        historyTable.getSelectionModel().addListSelectionListener(this);

        JPanel borderPanel = new JPanel(new GridLayout(1,0));
        borderPanel.setBorder(BorderFactory.createTitledBorder("History"));
        borderPanel.add(scrollPane);

        this.add(borderPanel, BorderLayout.CENTER);
    }

    @Override
    public void commandApplied(Command cmd) {
        tableModel.fireTableDataChanged();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                historyTable.setRowSelectionInterval(selectedIndex, selectedIndex);
            }
        });
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int index) {
        selectedIndex = index;

        historyTable.setRowSelectionInterval(index, index);
    }

    public void setListener(HistoryPanelListener listener) {
        this.listener = listener;
    }

    @Override
    public void valueChanged(ListSelectionEvent listSelectionEvent) {

        if(selectedIndex != historyTable.getSelectedRow() && historyTable.getSelectedRow() != -1) {

            selectedIndex = historyTable.getSelectedRow();

            if(listener != null) {
                listener.onHistoryPanelSelected();
            }
        }
    }

    public interface HistoryPanelListener {
        void onHistoryPanelSelected();
    }
}
