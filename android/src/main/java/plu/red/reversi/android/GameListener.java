package plu.red.reversi.android;

import android.widget.ListAdapter;
import android.widget.ListView;

import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.util.DataMap;

public interface GameListener {
    void onNewGame(Game game);
    ListView getSlideList();
}
