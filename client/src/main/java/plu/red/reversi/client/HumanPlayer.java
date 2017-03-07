package plu.red.reversi.client;

import plu.red.reversi.client.gui.GameWindow;
import plu.red.reversi.core.Game;
import plu.red.reversi.core.Player;
import plu.red.reversi.core.PlayerRole;

/**
 * Created by daniel on 3/5/17.
 * Glory to the Red Team.
 */
public class HumanPlayer extends Player {

    private GameWindow window;

    public HumanPlayer(Game game, PlayerRole role, GameWindow window) {
        super(game, role);

        this.window = window;
    }

    @Override
    public void nextTurn(boolean yours) {
        if(yours) {
            // update the GUI to make it show that the user may now input their stuff
            // TODO: Fix the GUI to use this actual input
            window.getPlayerInfoPanel().setActivePlayer(getRole() == PlayerRole.WHITE ? 0 : 1);

            // TODO: get previous command and update the board with nice animations

            // TODO: start listening for input
        }
        else {
            // set it to the player not us
            window.getPlayerInfoPanel().setActivePlayer(getRole() == PlayerRole.WHITE ? 1 : 0);

            // TODO: Stop listening to input
        }

    }
}
