package plu.red.reversi.core.reversi3d;

import org.joml.Vector2f;
import org.joml.Vector2fc;

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
            this.obj = new ObjModel3D(
                    board.getGraphics3D(),
                    board.getPipeline(),
                    ClassLoader.getSystemResourceAsStream("/models/gameover.obj"));
        }
        catch (Exception e) {
            // technically this should not happen
            e.printStackTrace();
        }

        this.freeMode = freeMode;
        start();
    }

    @Override
    public void start() {
        freeMode.setAutoRotate(true);
        freeMode.start();

        // nice savory drag
        //getCamera().beginDrag(180);
    }

    @Override
    public boolean move(float dx, float dy) {
        // endgame does not respond to motion events
        return false;
    }

    /**
     * Get the current computable position of the camera. Useful if you need pos to calculate dx, for example.
     *
     * @return the current "position" of the camera, in pixels.
     */
    @Override
    public Vector2fc getPos() {
        return freeMode.getPos();
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

        return freeMode.update(tick);
    }
}
