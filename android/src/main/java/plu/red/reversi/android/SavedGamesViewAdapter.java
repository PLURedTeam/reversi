package plu.red.reversi.android;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Comparator;

import plu.red.reversi.core.db.DBUtilities;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.logic.ReversiLogic;

public class SavedGamesViewAdapter extends RecyclerView.Adapter<SavedGamesViewAdapter.ViewHolder> {
    private final GameListener mListener;

    private final String[][] mGames;

    public SavedGamesViewAdapter(GameListener listener) {

        mGames = DBUtilities.INSTANCE.getGames();

        Arrays.sort(mGames, new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                // sort descending
                return Integer.parseInt(o2[1]) - Integer.parseInt(o1[1]);
            }
        });

        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_savedgames, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mIdView.setText(mGames[position][1]);
        holder.mContentView.setText(mGames[position][0]);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {

                    Game game = Game.loadGameFromDatabase(null, Integer.parseInt(mGames[position][1]));
                    game.setLogic(new ReversiLogic(game));
                    game.setGameSaved(true);

                    game.initialize();

                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onNewGame(game);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mGames.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
