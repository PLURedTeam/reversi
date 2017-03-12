package plu.red.reversi.client;

import plu.red.reversi.client.gui.GameWindow;
import plu.red.reversi.core.*;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.command.MoveCommand;

/**
 * Created by daniel on 3/5/17.
 * Glory to the Red Team.
 */
public class HumanPlayer extends Player {

    private GameWindow window;

    public HumanPlayer(Game game, PlayerColor role, GameWindow window) {
        super(game, role);

        this.window = window;
    }

    @Override
    public void nextTurn(boolean yours) {
        // IDK what we will do here
        /*if(yours) {
            // update the GUI to make it show that the user may now input their stuff
            // TODO: Fix the GUI to use this actual input
            window.getPlayerInfoPanel().setActivePlayer(getRole() == PlayerColor.WHITE ? 0 : 1);
            window.getPlayerInfoPanel().setScore(0, getGame().getBoard().getScore(PlayerColor.WHITE));
        }
        else {
            // set it to the player not us
            window.getPlayerInfoPanel().setActivePlayer(getRole() == PlayerColor.WHITE ? 1 : 0);
        }*/
    }

    public void issuePlay(BoardIndex index) {
        if(getGame().getCurrentPlayer() != this) {
            throw new IllegalStateException("Cannot play on game when not current player");
        }

        getGame().acceptCommand(new MoveCommand(Command.Source.PLAYER, getRole(), index));
    }
}

