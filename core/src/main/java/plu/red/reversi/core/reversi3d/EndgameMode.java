package plu.red.reversi.core.reversi3d;

import org.joml.Vector2f;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.player.Player;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by daniel on 5/12/17.
 */
public class EndgameMode extends ControlMode {

    private Board3D board;
    private ObjModel3D obj;
    private int startTick = -1;
    private FreeMode freeMode;

    public EndgameMode(Game game, Camera camera, Board3D board, FreeMode freeMode) {
        super(game, camera);

        this.board = board;

        try {
            URL url = ClassLoader.getSystemResource("models/gameover.obj");
            File f = new File(url.toURI());

            this.obj = new ObjModel3D(board.getGraphics3D(), board.getPipeline(), f);
        }
        catch (Exception e) {
            // technically this should not happen
            e.printStackTrace();
        }

        this.freeMode = freeMode;
        freeMode.setAutoRotate(true);

        // nice savory drag
        camera.beginDrag(180);
    }

    @Override
    public boolean move(float dx, float dy) {
        // endgame does not respond to motion events
        return false;
    }

    @Override
    public boolean update(int tick) {
        if(startTick == -1) {
            startTick = tick;

            // find out who the winner(s)
            Set<Player> winners = new HashSet<>();
            for(Player player : getGame().getAllPlayers()) {
                if(winners.isEmpty() || winners.iterator().next().getScore() < player.getScore()) {
                    winners.clear();
                    winners.add(player);
                }
                else if(winners.iterator().next().getScore() == player.getScore()) {
                    // a tie of winners
                    winners.add(player);
                }
            }

            board.animBlackout(winners.size() > 1 ? null : winners.iterator().next().getColor());
        }



        board.update(tick);

        return true;
    }
}
