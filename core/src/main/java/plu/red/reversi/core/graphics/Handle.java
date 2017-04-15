package plu.red.reversi.core.graphics;

/**
 * Created by daniel on 3/19/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public class Handle {
    private Graphics3D g3d;
    private Object handle;

    public Handle() {
        handle = null;
    }

    public Handle(Graphics3D g3d, Object handle) {
        this.g3d = g3d;
        this.handle = handle;
    }

    public void setHandle(Graphics3D g3d, Object handle) {
        this.g3d = g3d;
        this.handle = handle;
    }

    public Object getHandle() {
        return handle;
    }

    public Graphics3D getGraphics3D() {
        return g3d;
    }
}
