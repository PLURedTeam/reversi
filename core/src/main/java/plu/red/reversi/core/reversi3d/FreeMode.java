package plu.red.reversi.core.reversi3d;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import plu.red.reversi.core.game.Game;

/**
 * Created by daniel on 5/12/17.
 */
public class FreeMode extends ControlMode {

    private Board3D board;

    private float zoom;
    private boolean autoRotate;

    public FreeMode(Game game, Camera camera, Board3D board, int zoom) {
        super(game, camera);

        this.board = board;

        start();
    }

    @Override
    public void start() {
        // very slow drag begins here
        getCamera().setMoveBounds(new Vector2f(Integer.MIN_VALUE), new Vector2f(Integer.MAX_VALUE));
        getCamera().beginDrag(30);
        getCamera().setPos(new Vector2f());
        getCamera().setDir(new Vector2f(0.0f, 0.15f * (float)Math.PI));
        setZoom(zoom);
    }

    @Override
    public boolean move(float dx, float dy) {
        Vector2fc pos = new Vector2f(getPos());
        Vector2fc newp = new Vector2f(
                pos.x() + dx,
                -pos.y() - dy).mul(1.0f / (zoom / 6));
        getCamera().setDir(newp);

        return true;
    }

    /**
     * Get the current computable position of the camera. Useful if you need pos to calculate dx, for example.
     *
     * @return the current "position" of the camera, in pixels.
     */
    @Override
    public Vector2fc getPos() {
        Vector2f f = new Vector2f(getCamera().getDir()).mul(zoom / 6);
        f.y = -f.y;
        return f;
    }

    @Override
    public boolean update(int tick) {
        boolean updated = false;

        if(autoRotate) {
            getCamera().setDir(new Vector2f(0.0000005f * zoom, 0).add(getCamera().getDir()));
            updated = true;
        }

        updated = board.update(tick) || updated;

        updated = getCamera().update(tick) || updated;

        return updated;
    }

    public void setAutoRotate(boolean autoRotate) {
        getCamera().beginDrag(30);
        this.autoRotate = autoRotate;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
        getCamera().setZoom(zoom);
    }

    public boolean isAutoRotating() {
        return autoRotate;
    }
}
