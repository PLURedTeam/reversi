package plu.red.reversi.core.reversi3d;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import plu.red.reversi.core.game.Game;

/**
 * Created by daniel on 5/12/17.
 */
public class FlatMode extends ControlMode {
    private Board3D board;
    private float zoom;

    public FlatMode(Game game, Camera camera, Board3D board, float zoom) {
        super(game, camera);

        this.board = board;

        start();
    }

    public void start() {
        // very slow drag begins here
        getCamera().setMoveBounds(new Vector2f(-board.getBoardRadius()), new Vector2f(board.getBoardRadius()));
        getCamera().beginDrag(30);
        getCamera().setPos(new Vector2f());
        getCamera().setDir(new Vector2f(-0, 0.5f * (float)Math.PI));
        setZoom(zoom);
    }

    @Override
    public boolean move(float dx, float dy) {
        getCamera().setPos(
                new Vector2f(
                        getCamera().getPos().x + dx,
                        getCamera().getPos().y + dy)
        );
        return true;
    }

    /**
     * Get the current computable position of the camera. Useful if you need pos to calculate dx, for example.
     *
     * @return the current "position" of the camera, in pixels.
     */
    @Override
    public Vector2fc getPos() {
        return getCamera().getPos();
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
        getCamera().setZoom(zoom);
    }

    @Override
    public boolean update(int tick) {
        boolean updated = false;
        updated = board.update(tick) || updated;
        updated = getCamera().update(tick) || updated;

        return updated;
    }
}
