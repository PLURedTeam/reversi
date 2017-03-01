package plu.red.reversi.client.gui;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * This panel displays the history of moves that have taken place
 * througout the game.  It should be updated after every move.
 *
 * TODO:  This currently uses an internal class called Move, which really
 * doesn't belong here.  I suggest moving that over to a model subsystem
 * because it is something many subsystems might be interested in.
 */
public class GameHistoryPanel extends JPanel {

    private JTable historyTable;
    private ArrayList<Move> history;

    /**
     *  This is just an example table model to get you started.  It isn't
     *  complete and will need to be updated.
     *
     *  TODO: Update this to work with the rest of the system.
     */
    private class ExampleTableModel extends AbstractTableModel {
        @Override
        public String getColumnName(int column) {
            if( column == 0 ) return "#";
            else if( column == 1 ) return "Move";
            else if( column == 2 ) return "Player";
            return "";
        }

        @Override
        public int getRowCount() {
            return history.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if( rowIndex >= history.size() ) return null;

            Move m = history.get(rowIndex);
            if( columnIndex == 0 ) return "" + (rowIndex + 1);
            else if( columnIndex == 1) return m.cell;
            else if( columnIndex == 2) {
                // Should ask the model for the player name here,
                // but for now, we "hard code"
                if( m.player == 1 ) return "Player 1";
                else if(m.player == 2 ) return "Player 2";
                else return "";
            }
            return null;
        }
    }

    /**
     * This is a temporary class, just for demo purposes.  This really belongs in the model,
     * not in the GUI.  You should remove this and implement it in the model.
     *
     * TODO: Probably belongs in the model system.
     */
    private class Move {
        private String cell;
        private int player;
        public Move(String cell, int player) {
            this.cell = cell;
            this.player = player;
        }
    }

    /**
     * Construct a new history panel.  Currently, this places some example
     * history into the panel.  This should be removed.
     *
     * TODO: Implement "real" history
     */
    public GameHistoryPanel() {

        // Fill the history list with some arbitrary example data.
        // This should be stored in the model, not here.
        // TODO: move this to the model
        history = new ArrayList<Move>();
        history.add( new Move("D4", 1));
        history.add( new Move("E4", 2));
        history.add( new Move("E5", 1));
        history.add( new Move("D5", 2));

        this.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        this.setLayout(new BorderLayout());
        historyTable = new JTable(new ExampleTableModel());

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setPreferredSize(new Dimension(250,0));
        historyTable.setGridColor(new Color(220,220,220));
        historyTable.setShowGrid(true);
        historyTable.setFillsViewportHeight(true);

        TableColumnModel cmod = historyTable.getColumnModel();
        cmod.getColumn(0).setPreferredWidth(20);
        cmod.getColumn(1).setPreferredWidth(20);
        historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        JPanel borderPanel = new JPanel(new GridLayout(1,0));
        borderPanel.setBorder(BorderFactory.createTitledBorder("History"));
        borderPanel.add(scrollPane);

        this.add(borderPanel, BorderLayout.CENTER);
    }
}
