package plu.red.reversi.android;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.Locale;

import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.History;
import plu.red.reversi.core.listener.ICommandListener;

/**
 * An ArrayAdapter to show history entries for a game.
 */
public class GameHistoryAdapter extends ArrayAdapter<BoardCommand> implements ICommandListener {

    Game mGame;

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param game the game with history which should be tracked and referenced by this adapter
     */
    public GameHistoryAdapter(@NonNull Context context, Game game) {
        super(context, R.layout.fragment_list_history);

        mGame = game;

        mGame.addListener(this);
    }

    @Override
    public int getCount() {
        return mGame.getHistory().getNumBoardCommands();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        BoardCommand cmd = mGame.getHistory().getBoardCommand(position);

        ViewHolder holder;

        if(convertView == null)
            holder = new ViewHolder(parent);
        else
            holder = (ViewHolder)convertView.getTag();

        holder.apply(position, cmd);

        return holder.getView();
    }

    /**
     * Called when a Command is being passed through Game and has been validated.
     *
     * @param cmd Command object that is being applied
     */
    @Override
    public void commandApplied(Command cmd) {
        notifyDataSetInvalidated();
    }

    /**
     * Android standard view-holder strategy
     */
    private class ViewHolder {
        TextView mHistoryIndex;
        ImageView mHistoryPlayer;
        TextView mHistoryPosition;

        View view;

        public ViewHolder(ViewGroup parent) {
            view = ((LayoutInflater)getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.fragment_list_history, null);

            mHistoryIndex = (TextView)view.findViewById(R.id.history_index);
            mHistoryPlayer = (ImageView)view.findViewById(R.id.history_player);
            mHistoryPosition = (TextView)view.findViewById(R.id.history_position);

            view.setTag(this);
        }

        /**
         * Change the view data so that it displays the data within the provided BoardCommand
         * @param position the command index. the first command is 0.
         * @param cmd the board command to show
         */
        public void apply(int position, BoardCommand cmd) {
            mHistoryIndex.setText(String.format(Locale.getDefault(), "%d", position + 1));
            mHistoryPlayer.setImageDrawable(
                    new ColorDrawable(mGame.getPlayer(cmd.playerID).getColor().composite)
            );
            mHistoryPosition.setText(cmd.position.toString());
        }

        /**
         * Get the view managed and tagged by this ViewHolder
         * @return the view managed by this view holder.
         */
        public View getView() {
            return view;
        }
    }
}
