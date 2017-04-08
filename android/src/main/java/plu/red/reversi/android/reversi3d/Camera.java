package plu.red.reversi.android.reversi3d;

import org.joml.Intersectionf;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import plu.red.reversi.android.easing.EaseType;
import plu.red.reversi.android.easing.Easing;
import plu.red.reversi.android.easing.PolynomialEasing;

/**
 * Created by daniel on 3/27/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public class Camera {

    /**
     * Since the camera uses an ortho projection, having the radius here is only necessary to ensure
     * that the board at no point will be behind the board.
     * So I set it to an arbitrary value which should always be within clip coordinates
     */
    private static final float CAMERA_RADIUS = 10.0f;

    private CameraState targetState;
    private CameraState currentState;
    private CameraState dragState;

    private int lastTick;

    private Easing easer;
    private int dragStartTick;
    private int dragFinishTick;

    private Matrix4f viewMatrix;
    private Matrix4f projectionMatrix;

    /**
     * The maximum bounds, usually equal to the calculated size of the board in pixels.
     */
    private Vector2f moveExtentBottomLeft;
    private Vector2f moveExtentTopRight;

    private Vector3f[] tmp;

    public Camera() {

        targetState = new CameraState();

        currentState = targetState;
        dragState = targetState;

        viewMatrix = new Matrix4f();
        projectionMatrix = new Matrix4f();

        easer = new PolynomialEasing(3, EaseType.EASE_OUT);
        dragFinishTick = 0;

        tmp = new Vector3f[]{new Vector3f(), new Vector3f(), new Vector3f()};
    }

    public Camera beginDrag(int ticks) {
        dragStartTick = lastTick;
        dragFinishTick = lastTick + ticks;

        if(dragState != targetState) {
            // previous drag is still in progress--set target state equal to current state to disable camera jump
            cancelDrag();
        }

        dragState = new CameraState(targetState);
        currentState = new CameraState(targetState);

        return this;
    }

    public Camera cancelDrag() {
        targetState = currentState;
        dragState = currentState;

        return this;
    }

    public boolean update(int tick) {

        lastTick = tick;

        if(dragState != targetState) {
            if(tick > dragFinishTick) {
                dragState = targetState;
                currentState = targetState;
            }
            else {
                currentState.interpolateBetween(dragState, targetState, easer.ease(tick - dragStartTick, 0.0f, 1.0f, dragFinishTick - dragStartTick));
            }

            recalculateViewMatrix();
            recalculateProjectionMatrix();

            return true;
        }

        return false;
    }

    /**
     * Set the width and height of the current view window, so that the projection matrix can be calculated correctly
     * @param viewport the width and height of the current view
     */
    public Camera setViewport(Vector2fc viewport) {
        targetState.viewport.set(viewport);

        recalculateProjectionMatrix();

        return this;
    }

    public Camera setZoom(float zoom) {
        targetState.zoom = zoom;

        recalculateProjectionMatrix();

        return this;
    }

    public float getZoom() {
        return targetState.zoom;
    }

    public Camera setPos(Vector2fc pos) {
        targetState.cameraPos.set(pos);

        targetState.cameraPos.x = Math.max(moveExtentBottomLeft.x * targetState.zoom, targetState.cameraPos.x);
        targetState.cameraPos.x = Math.min(moveExtentTopRight.x * targetState.zoom, targetState.cameraPos.x);
        targetState.cameraPos.y = Math.max(moveExtentBottomLeft.y * targetState.zoom, targetState.cameraPos.y);
        targetState.cameraPos.y = Math.min(moveExtentTopRight.y * targetState.zoom, targetState.cameraPos.y);

        recalculateViewMatrix();

        return this;
    }

    public Vector2f getPos() {
        return targetState.cameraPos;
    }

    public Camera setDir(Vector2fc dir) {
        targetState.cameraDir.set(dir);

        targetState.cameraDir.x = targetState.cameraDir.x % (2 * (float)Math.PI);

        targetState.cameraDir.y = (float)Math.max(0, Math.min(Math.PI / 2, targetState.cameraDir.y));

        recalculateViewMatrix();

        return this;
    }

    public Vector2f getDir() {
        return targetState.cameraDir;
    }

    /**
     * Set the furthest extent in which the camera's focus can be located from the center.
     * All coordinates are in GL coordinates
     * @param bottomLeft the furthest negative XY the camera can be
     * @param topRight the furthest positive XY the camera can be
     */
    public Camera setMoveBounds(Vector2fc bottomLeft, Vector2fc topRight) {
        moveExtentBottomLeft.set(bottomLeft);
        moveExtentTopRight.set(topRight);

        return this;
    }

    public Vector3f getRealCameraPos() {

        float offxy = CAMERA_RADIUS * (float)Math.cos(currentState.cameraDir.y);
        float offz = CAMERA_RADIUS * (float)Math.sin(currentState.cameraDir.y);

        float cx = (float)Math.cos(currentState.cameraDir.x - Math.PI / 2); // we sub PI over 2 here because otherwise the calcs will not be relative to negative y
        float cy = (float)Math.sin(currentState.cameraDir.x - Math.PI / 2);

        return getRealCameraCenter().sub(offxy * cx, offxy * cy, -offz);
    }

    public Vector3f getRealCameraCenter() {
        float vx = currentState.cameraPos.x * 2 / currentState.zoom;
        float vy = -currentState.cameraPos.y * 2 / currentState.zoom;

        return new Vector3f(vx, vy, 0);
    }

    public Vector3f getRealCameraUp() {
        float cx = (float)Math.cos(currentState.cameraDir.x - Math.PI / 2); // we sub PI over 4 here because otherwise the calcs will not be relative to negative y
        float cy = (float)Math.sin(currentState.cameraDir.x - Math.PI / 2);
        return new Vector3f(cx, cy, 1);
    }

    private void recalculateViewMatrix() {

        tmp[0] = getRealCameraPos();

        // determine "look at"
        tmp[1] = getRealCameraCenter();

        // up is usually set to (0,0,1), but it is possible that the camera will be looking straight
        // down. If that is the case, we should also provide some component there too.

        tmp[2] = getRealCameraUp();

        viewMatrix.setLookAt(tmp[0], tmp[1], tmp[2]);
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    private void recalculateProjectionMatrix() {
        projectionMatrix.setOrtho(
                -currentState.viewport.x / currentState.zoom,
                 currentState.viewport.x / currentState.zoom,
                -currentState.viewport.y / currentState.zoom,
                 currentState.viewport.y / currentState.zoom,
                 0.01f, 20.0f);
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    // TODO: This function could be made more efficient I am sure
    public Vector2f pixelToPosition(Vector2fc pixel) {
        Vector2f result = new Vector2f(pixel);

        // calculate GL offset based on center
        result.sub(currentState.viewport.x / 2, currentState.viewport.y / 2);
        result.mul(1.0f / currentState.zoom * 2);

        // now figure out what the offset would be in the world based on the current camera angling
        Vector3f cameraDir = getRealCameraCenter().sub(getRealCameraPos()).normalize();

        Vector3f left = getRealCameraUp().cross(cameraDir);
        Vector3f up = new Vector3f();
        cameraDir.cross(left, up);

        Vector3f p = getRealCameraPos().sub(left.mul(result.x)).sub(up.mul(result.y));

        float pt = Intersectionf.intersectRayPlane(p, cameraDir, new Vector3f(0, 0, currentState.height), new Vector3f(0, 0, 1), 0.0001f);

        if(pt == -1)
            System.out.println("Something was wrong with the camera calculations because they are indecisive!");

        p.add(cameraDir.mul(pt));

        return result.set(p.x, p.y);
    }

    private class CameraState {

        public CameraState() {
            cameraPos = new Vector2f();
            cameraDir = new Vector2f();

            zoom = 2400;
            height = 0;

            viewport = new Vector2f();

            moveExtentBottomLeft = new Vector2f();
            moveExtentTopRight = new Vector2f();
        }

        public CameraState(CameraState other) {
            cameraPos = new Vector2f(other.cameraPos);
            cameraDir = new Vector2f(other.cameraDir);

            zoom = other.zoom;
            height = other.height;

            viewport = new Vector2f(other.viewport);
        }

        /**
         * The camera look at on the board, in pixels (because pixels make it easy to map exactly to gesture controls)
         */
        private Vector2f cameraPos;

        /**
         * The direction of the camera--the x component represents left-right, and the y component represents up/down
         * Changing the up/down will alter the altitude of the camera to continue facingthe cameraPos point on the board.
         * Both values are in radians.
         */
        private Vector2f cameraDir;

        /**
         * Specifies the number of pixels per 1 GL unit--increasing this value effectively makes the board look larger.
         */
        private float zoom;

        /**
         * The width and height of the screen, specified in pixels
         */
        private Vector2f viewport;

        /**
         * Configures the height of the camera above the board. Usually this should be set to 0.
         * Right now it is used to run the app intro, but other imaginative things could be done as well.
         * Also it is the last dimension of movement which the camera could use.
         */
        private float height;

        public void interpolateBetween(CameraState start, CameraState end, float portion) {

            //System.out.println("Running interpolation!");

            start.cameraPos.lerp(end.cameraPos, portion, cameraPos);
            start.cameraDir.lerp(end.cameraDir, portion, cameraDir);
            start.viewport.lerp(end.viewport, portion, viewport);
            zoom = (end.zoom - start.zoom) * portion + start.zoom;
            height = (end.height - start.height) * portion + start.height;
        }
    }
}
