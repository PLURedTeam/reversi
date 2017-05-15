package plu.red.reversi.android;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Locale;

import plu.red.reversi.core.util.ChatMessage;
import plu.red.reversi.core.util.GamePair;

/**
 * Created by daniel on 5/14/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public class MultiplayerGamesAdapter extends ArrayAdapter<GamePair> {

    /**
     * Constructor
     *
     * @param context  The current context.
     */
    public MultiplayerGamesAdapter(@NonNull Context context) {
        super(context, R.layout.fragment_list_games);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = new ViewHolder().getView();

        ViewHolder holder = (ViewHolder)convertView.getTag();
        holder.apply(getItem(position));
        return convertView;
    }

    public class ViewHolder {
        private View mView;

        private TextView mGameName;
        private TextView mGamePlayers;
        private TextView mGameType;

        public ViewHolder() {
            LayoutInflater inflater = ((LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
            mView = inflater.inflate(R.layout.fragment_list_games, null);
            mView.setTag(this);

            mGameName = (TextView)mView.findViewById(R.id.games_game_name);
            mGamePlayers = (TextView)mView.findViewById(R.id.games_game_players);
            mGameType = (TextView)mView.findViewById(R.id.games_game_type);
        }

        public void apply(GamePair game) {
            mGameName.setText(game.gameName);
            mGamePlayers.setText(String.format(Locale.getDefault(), "%d/%d", game.players.size(), game.numPlayers));
            mGamePlayers.setTextColor(game.players.size() < game.numPlayers ? Color.RED : Color.GRAY);
            mGameType.setText(game.getGameName()); // TODO
        }

        public View getView() {
            return mView;
        }
    }
}
