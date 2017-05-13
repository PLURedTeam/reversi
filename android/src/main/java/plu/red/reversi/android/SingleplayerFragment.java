package plu.red.reversi.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import plu.red.reversi.core.SettingsLoader;
import plu.red.reversi.core.db.DBUtilities;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.logic.ReversiLogic;
import plu.red.reversi.core.game.player.BotPlayer;
import plu.red.reversi.core.game.player.HumanPlayer;
import plu.red.reversi.core.game.player.NullPlayer;
import plu.red.reversi.core.util.Color;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GameListener} interface
 * to handle interaction events.
 */
public class SingleplayerFragment extends Fragment implements View.OnClickListener {

    private GameListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_singleplayer, container, false);

        v.findViewById(R.id.button_sp_human_vs_human).setOnClickListener(this);

        v.findViewById(R.id.button_sp_first_easy_ai).setOnClickListener(this);
        v.findViewById(R.id.button_sp_first_medium_ai).setOnClickListener(this);
        v.findViewById(R.id.button_sp_first_hard_ai).setOnClickListener(this);
        v.findViewById(R.id.button_sp_second_easy_ai).setOnClickListener(this);
        v.findViewById(R.id.button_sp_second_medium_ai).setOnClickListener(this);
        v.findViewById(R.id.button_sp_second_hard_ai).setOnClickListener(this);

        v.findViewById(R.id.button_ai_easy_easy).setOnClickListener(this);
        v.findViewById(R.id.button_ai_easy_medium).setOnClickListener(this);
        v.findViewById(R.id.button_ai_easy_hard).setOnClickListener(this);
        v.findViewById(R.id.button_ai_medium_easy).setOnClickListener(this);
        v.findViewById(R.id.button_ai_medium_medium).setOnClickListener(this);
        v.findViewById(R.id.button_ai_medium_hard).setOnClickListener(this);
        v.findViewById(R.id.button_ai_hard_easy).setOnClickListener(this);
        v.findViewById(R.id.button_ai_hard_medium).setOnClickListener(this);
        v.findViewById(R.id.button_ai_hard_hard).setOnClickListener(this);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof GameListener) {
            mListener = (GameListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {

        Game game = new Game(null);


        game.setLogic(new ReversiLogic(game));

        game.setSettings(SettingsLoader.INSTANCE.createGameSettings());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        Color p1c = new Color(prefs.getInt(SettingsFragment.KEY_P1_COLOR, Color.RED.composite));
        Color p2c = new Color(prefs.getInt(SettingsFragment.KEY_P2_COLOR, Color.BLUE.composite));


        switch(v.getId()) {
            case R.id.button_sp_human_vs_human:

                new NullPlayer(game, p2c).setName("Player 1");
                new NullPlayer(game, p1c).setName("Player 2");

                break;

            case R.id.button_sp_first_easy_ai:

                new BotPlayer(game, p2c, 2).setName("Easy Bot");
                new NullPlayer(game, p1c).setName("Player");

                break;
            case R.id.button_sp_first_medium_ai:

                new BotPlayer(game, p2c, 4).setName("Medium Bot");
                new NullPlayer(game, p1c).setName("Player");

                break;

            case R.id.button_sp_first_hard_ai:

                new BotPlayer(game, p2c, 6).setName("Hard Bot");
                new NullPlayer(game, p1c).setName("Player");

                break;

            case R.id.button_sp_second_easy_ai:

                new NullPlayer(game, p2c).setName("Player");
                new BotPlayer(game, p1c, 2).setName("Easy Bot");

                break;

            case R.id.button_sp_second_medium_ai:

                new NullPlayer(game, p2c).setName("Player");
                new BotPlayer(game, p1c, 4).setName("Medium Bot");

                break;

            case R.id.button_sp_second_hard_ai:

                new NullPlayer(game, p2c).setName("Player");
                new BotPlayer(game, p1c, 6).setName("Hard Bot");

                break;

            case R.id.button_ai_easy_easy:

                new BotPlayer(game, p2c, 2).setName("Easy Bot");
                new BotPlayer(game, p1c, 2).setName("Easy Bot");

                break;

            case R.id.button_ai_easy_medium:

                new BotPlayer(game, p2c, 2).setName("Easy Bot");
                new BotPlayer(game, p1c, 4).setName("Medium Bot");

                break;

            case R.id.button_ai_easy_hard:

                new BotPlayer(game, p2c, 2).setName("Easy Bot");
                new BotPlayer(game, p1c, 6).setName("Hard Bot");

                break;

            case R.id.button_ai_medium_easy:

                new BotPlayer(game, p2c, 4).setName("Medium Bot");
                new BotPlayer(game, p1c, 2).setName("Easy Bot");

                break;

            case R.id.button_ai_medium_medium:

                new BotPlayer(game, p2c, 4).setName("Medium Bot");
                new BotPlayer(game, p1c, 4).setName("Medium Bot");

                break;

            case R.id.button_ai_medium_hard:

                new BotPlayer(game, p2c, 4).setName("Medium Bot");
                new BotPlayer(game, p1c, 6).setName("Hard Bot");

                break;

            case R.id.button_ai_hard_easy:

                new BotPlayer(game, p2c, 6).setName("Hard Bot");
                new BotPlayer(game, p1c, 2).setName("Easy Bot");

                break;

            case R.id.button_ai_hard_medium:

                new BotPlayer(game, p2c, 6).setName("Hard Bot");
                new BotPlayer(game, p1c, 4).setName("Medium Bot");

                break;

            case R.id.button_ai_hard_hard:

                new BotPlayer(game, p2c, 6).setName("Hard Bot");
                new BotPlayer(game, p1c, 6).setName("Hard Bot");

                break;
        }

        // this is required to do (for some reason)
        game.setGameID(DBUtilities.INSTANCE.createGame());

        game.initialize();

        mListener.onNewGame(game);
    }
}
