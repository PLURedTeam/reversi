package plu.red.reversi.core.reversi3d;

import org.joml.Vector2f;
import plu.red.reversi.core.game.Game;

/**
 * Created by daniel on 5/12/17.
 */
public class FreeMode extends ControlMode {

    private int startTick = -1;
    private int lastTick = -1;
    private int zoom;
    private boolean autoRotate;

    public FreeMode(Game game, Camera camera, int zoom) {
        super(game, camera);

        // very slow drag begins here
        getCamera().beginDrag(180);
        getCamera().setPos(new Vector2f());
    }

    @Override
    public boolean move(float dx, float dy) {


        return true;
    }

    @Override
    public boolean update(int tick) {
        lastTick = tick;

        if(autoRotate) {
            //
        }

        return false;
    }

    public void setAutoRotate(boolean autoRotate) {
        this.autoRotate = autoRotate;
        startTick = lastTick;

        getCamera().beginDrag(30);
    }

    public boolean isAutoRotating() {
        return autoRotate;
    }
}
