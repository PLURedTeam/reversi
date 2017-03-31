package plu.red.reversi.android.graphics;

import org.joml.Vector3f;

/**
 * Created by daniel on 3/22/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public interface Curve {
    Vector3f[] getTriangle(int index, int density);
}
