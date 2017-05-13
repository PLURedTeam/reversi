package plu.red.reversi.core.reversi3d;

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
     * Called by the implementor to indicate that inputs have been received that indicate that the camera or view should
     * be transformed in some way
     * @param dx the amount of horizontal distance to transform by
     * @param dy the amount of vertical distance to transform by
     * @return if the view needs to be redrawn
     */
    public abstract boolean move(float dx, float dy);

    /**
     * Called by the implementor to indicate that a tick has passed and the view should render appropriately.
     * @param tick
     * @return if the view needs to be redrawn
     */
    public abstract boolean update(int tick);
}
