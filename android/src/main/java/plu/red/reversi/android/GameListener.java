package plu.red.reversi.android;

import android.widget.ListAdapter;
import android.widget.ListView;

import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.util.DataMap;

/**
 * Created by daniel on 3/18/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public interface GameListener {
    void onNewGame(Game game);
    ListView getSlideList();
}
