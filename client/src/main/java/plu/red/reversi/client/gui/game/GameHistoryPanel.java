package plu.red.reversi.client.gui.game;

import plu.red.reversi.core.Game;
import plu.red.reversi.core.History;
import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.command.SurrenderCommand;
import plu.red.reversi.core.listener.ICommandListener;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
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
public class GameHistoryPanel extends JPanel implements ICommandListener {

    private JTable historyTable;
    private CommandHistoryTableModel tableModel;

    private History gameHistory;

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
            return gameHistory.getNumBoardCommands();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if( rowIndex >= gameHistory.getNumBoardCommands() ) return null;

            BoardCommand command = gameHistory.getBoardCommand(rowIndex);

            if(command instanceof MoveCommand) {

                MoveCommand moveCommand = (MoveCommand)command;

                if( columnIndex == 0 ) return "" + (rowIndex + 1);
                else if( columnIndex == 1) return moveCommand.position;
                else if( columnIndex == 2) {
                    // TODO: Would be better if player was actually connecting to the actual player object here???
                    // there is no way to get a reference to the player short of having the game, and that is bad here
                    // since this is just supposed to show the history.
                    return moveCommand.player.name;
                }
                return null;
            }
            else {
                System.err.println("Undefined command type in command history (please implement: ");
                System.err.println(command.getClass().getName());
                return null;
            }
        }
    }

    /**
     * Construct a new history panel.  Currently, this places some example
     * history into the panel.  This should be removed.
     *
     * TODO: Implement "real" history
     */
    public GameHistoryPanel(Game game) {

        this.gameHistory = game.getHistory();

        game.addCommandListener(this);

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

        JPanel borderPanel = new JPanel(new GridLayout(1,0));
        borderPanel.setBorder(BorderFactory.createTitledBorder("History"));
        borderPanel.add(scrollPane);

        this.add(borderPanel, BorderLayout.CENTER);
    }

    @Override
    public void commandApplied(Command cmd) {
        tableModel.fireTableDataChanged();
    }
}
