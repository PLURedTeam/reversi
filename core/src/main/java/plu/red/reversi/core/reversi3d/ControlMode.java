package plu.red.reversi.core.reversi3d;

import org.joml.Vector2fc;
import plu.red.reversi.core.game.Game;

/**
 * Created by daniel on 5/12/17.
 */
public abstract class ControlMode {

    private Game game;
    private Camera camera;

    public ControlMode(Game game, Camera camera) {
        this.camera = camera;
        this.game = game;
    }

    public Camera getCamera() {
        return camera;
    }

    public Game getGame() {
        return game;
    }

    /**
     * Called when this controller is becoming the currently active controller.
     */
    public abstract void start();

    /**
     * Called by the implementor to indicate that inputs have been received that indicate that the camera or view should
     * be transformed in some way
     * @param dx the amount of horizontal distance to transform by
     * @param dy the amount of vertical distance to transform by
     * @return if the view needs to be redrawn
     */
    public abstract boolean move(float dx, float dy);

    /**
     * Get the current computable position of the camera. Useful if you need pos to calculate dx, for example.
     * @return the current "position" of the camera, in pixels.
     */
    public abstract Vector2fc getPos();

    /**
     * Called by the implementor to indicate that a tick has passed and the view should render appropriately.
     * @param tick
     * @return if the view needs to be redrawn
     */
    public abstract boolean update(int tick);
}
