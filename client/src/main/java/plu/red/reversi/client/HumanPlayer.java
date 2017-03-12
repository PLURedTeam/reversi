package plu.red.reversi.client;

import plu.red.reversi.client.gui.GameWindow;
import plu.red.reversi.core.*;

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
        // IDK what we will do here
        /*if(yours) {
            // update the GUI to make it show that the user may now input their stuff
            // TODO: Fix the GUI to use this actual input
            window.getPlayerInfoPanel().setActivePlayer(getRole() == PlayerRole.WHITE ? 0 : 1);
            window.getPlayerInfoPanel().setScore(0, getGame().getBoard().getScore(PlayerRole.WHITE));
        }
        else {
            // set it to the player not us
            window.getPlayerInfoPanel().setActivePlayer(getRole() == PlayerRole.WHITE ? 1 : 0);
        }*/
    }

    public void issuePlay(BoardIndex index) {
        if(getGame().getCurrentPlayer() != this) {
            throw new IllegalStateException("Cannot play on game when not current player");
        }

        getGame().acceptCommand(new CommandMove(Command.Source.PLAYER, getRole(), index));
    }
}

