package plu.red.reversi.android;

import android.graphics.RectF;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.support.annotation.IntegerRes;
import android.util.Log;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Collection;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import plu.red.reversi.android.graphics.AndroidGraphics3D;
import plu.red.reversi.android.graphics.Graphics3D;
import plu.red.reversi.android.graphics.Pipeline;
import plu.red.reversi.android.graphics.PipelineDefinition;
import plu.red.reversi.android.graphics.PixelShader;
import plu.red.reversi.android.graphics.SimpleGLFragmentShader;
import plu.red.reversi.android.graphics.SimpleGLVertexShader;
import plu.red.reversi.android.graphics.VertexShader;
import plu.red.reversi.android.reversi3d.Board3D;
import plu.red.reversi.android.reversi3d.Camera;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.listener.IBoardUpdateListener;

/**
 * Created by daniel on 3/25/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public class GameRenderer implements GLSurfaceView.Renderer, IBoardUpdateListener {

    private static final String TAG = GameRenderer.class.getSimpleName();

    private Graphics3D g3d;

    private Pipeline mPipeline;

    private Board3D mBoard;
    private Camera mCamera;

    private float mPlayZoom;

    private int mTick;

    private Game mGame;

    private boolean mPresentationMode;
    private boolean mAutoRotate;

    private boolean mGLIsAvailable;

    public GameRenderer() {
        mTick = 0;
        mPlayZoom = 3600;

        mGLIsAvailable = false;
    }

    /**
     * Called when the surface is created or recreated.
     * <p>
     * Called when the rendering thread
     * starts and whenever the EGL context is lost. The EGL context will typically
     * be lost when the Android device awakes after going to sleep.
     * <p>
     * Since this method is called at the beginning of rendering, as well as
     * every time the EGL context is lost, this method is a convenient place to put
     * code to create resources that need to be created when the rendering
     * starts, and that need to be recreated when the EGL context is lost.
     * Textures are an example of a resource that you might want to create
     * here.
     * <p>
     * Note that when the EGL context is lost, all OpenGL resources associated
     * with that context will be automatically deleted. You do not need to call
     * the corresponding "glDelete" methods such as glDeleteTextures to
     * manually delete these lost resources.
     * <p>
     *
     * @param unused is unused
     * @param config the EGLConfig of the created surface. Can be used
     */
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        g3d = new AndroidGraphics3D();

        g3d.setClearColor(new Vector3f(0.2f, 0.2f, 0.2f));

        PipelineDefinition def = new PipelineDefinition();

        def.directionalLightCount = 2;

        VertexShader vs = new SimpleGLVertexShader(def);
        PixelShader ps = new SimpleGLFragmentShader(def);

        mPipeline = new Pipeline(def, vs, ps);

        g3d.createPipeline(mPipeline);
        g3d.setPipeline(mPipeline);

        g3d.bindPipelineUniform("fDirectionalLights[0]", mPipeline, new Vector3f(-0.6f, 0.25f, 1.0f).normalize());
        g3d.bindPipelineUniform("fDirectionalLights[1]", mPipeline, new Vector3f(0.6f, -0.25f, 1.0f).normalize());

        mCamera = new Camera();

        mGLIsAvailable = true;

        if(mGame != null)
            setGame(mGame);
    }

    /**
     * Called when the surface changed size.
     * <p>
     * Called after the surface is created and whenever
     * the OpenGL ES surface size changes.
     * <p>
     * Typically you will set your viewport here. If your camera
     * is fixed then you could also set your projection matrix here:
     * <pre class="prettyprint">
     * void onSurfaceChanged(GL10 gl, int width, int height) {
     * gl.glViewport(0, 0, width, height);
     * // for a fixed camera, set the projection too
     * float ratio = (float) width / height;
     * gl.glMatrixMode(GL10.GL_PROJECTION);
     * gl.glLoadIdentity();
     * gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
     * }
     * </pre>
     *
     * @param unused is unused
     * @param width the width of the viewport
     * @param height the height of the viewport
     */
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        g3d.setViewport(0, 0, width, height);

        mCamera.setViewport(new Vector2f(width, height));

        setPresentationMode(mPresentationMode);

        g3d.bindPipelineUniform("projectionMatrix", mPipeline, mCamera.getProjectionMatrix());
    }

    /**
     * Called to draw the current frame.
     * <p>
     * This method is responsible for drawing the current frame.
     * <p>
     * The implementation of this method typically looks like this:
     * <pre class="prettyprint">
     * void onDrawFrame(GL10 gl) {
     * gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
     * //... other gl calls to render the scene ...
     * }
     * </pre>
     *
     * @param unused is unused
     */
    @Override
    public void onDrawFrame(GL10 unused) {

        g3d.clearBuffers();
        g3d.bindPipelineUniform("viewMatrix", mPipeline, mCamera.getViewMatrix());
        g3d.bindPipelineUniform("projectionMatrix", mPipeline, mCamera.getProjectionMatrix());

        if(mGame != null) {
            mBoard.draw();
        }
    }

    public void setCameraPos(float x, float y) {

        if(!mPresentationMode) {
            mCamera.setPos(new Vector2f(x, y));
        }
        else {

            mCamera.setDir(new Vector2f(-x, y).mul(1.0f / 400));
        }
    }

    public Vector2fc getCameraPos() {

        if(mPresentationMode) {
            Vector2f f = new Vector2f(mCamera.getDir()).mul(400);
            f.x = -f.x;
            return f;
        }
        else {
            return mCamera.getPos();
        }
    }

    public void setPresentationMode(boolean present) {
        mPresentationMode = present;

        if(mCamera != null) {
            if(present) {
                mCamera.beginDrag(60);
                mCamera.setPos(new Vector2f(0, 0));
                mCamera.setDir(new Vector2f(-0.00f * (float)Math.PI, 0.15f * (float)Math.PI));
                mCamera.setZoom(2400);
            }
            else {
                mAutoRotate = false;

                mCamera.beginDrag(60);
                mCamera.setPos(new Vector2f(0, 0));
                mCamera.setDir(new Vector2f(0.00f * (float)Math.PI, 0.5f * (float)Math.PI));
                mCamera.setZoom(mPlayZoom);
            }
        }
    }

    public boolean isInPresentationMode() {
        return mPresentationMode;
    }

    public void setAutoRotate(boolean rotate) {
        if(rotate) {
            setPresentationMode(true);
            mAutoRotate = true;
        }
        else mAutoRotate = false;
    }

    public RectF getScrollBounds() {

        if(mPresentationMode)
            // return an obnoxiously large number since there is technically no limit
            return new RectF(Integer.MIN_VALUE, 0.0f, Integer.MAX_VALUE, 0.5f * (float)Math.PI * 400);

        float r = mBoard.getBoardRadius() * mCamera.getZoom();

        return new RectF(-r, -r, r, r);
    }

    public float getPlayZoom() {
        return mPlayZoom;
    }

    public void setPlayZoom(float zoom) {
        mPlayZoom = zoom;

        if(!mPresentationMode && mCamera != null)
            mCamera.setZoom(mPlayZoom);
    }

    public Camera getCamera() {
        return mCamera;
    }

    public boolean update() {

        boolean result = false;

        // it is possible for this function to be called before init. So make sure we are not processing before that.
        if(mBoard != null) {
            result = mBoard.update(++mTick);
            result = mCamera.update(mTick) || result;

            if(mAutoRotate) {

                // automatically rotate the camera's direction
                mCamera.setDir(new Vector2f(0.00175f, 0).add(mCamera.getDir()));

                return true;
            }
        }

        return result;
    }

    public void setGame(Game game) {
        this.mGame = game;

        if(mGLIsAvailable) {
            mBoard = new Board3D(g3d, mPipeline, mGame);

            float r = mBoard.getBoardRadius();

            mCamera.setMoveBounds(new Vector2f(-r, -r), new Vector2f(r, r));

            updateBoardState();
        }
    }

    public void updateBoardState() {
        if(mBoard != null) {
            System.out.println("Update board state");
            mBoard.setBoard(mGame);
        }
    }

    public void highlightBoard(BoardIndex index, Vector3fc color) {
        mBoard.highlightAt(index, color);
    }

    public void clearBoardHighlights() {
        if(mBoard != null)
            mBoard.clearHighlights();
    }

    public BoardIndex getTappedIndex(Vector2fc loc) {
        // ask the camera to convert pixels to board coord
        Vector2f res = mCamera.pixelToPosition(loc);

        System.out.println("Camera says OGL was " + res);

        return mBoard.getIndexAtCoord(res);
    }


    @Override
    public void onBoardUpdate(BoardIndex origin, int playerId, Collection<BoardIndex> updated) {
        mBoard.clearHighlights();
        mBoard.animBoardUpdate(origin, playerId, updated);
    }

    @Override
    public void onBoardRefresh() {
        mBoard.setBoard(mGame);
    }

    public Board3D getBoard() {
        return mBoard;
    }
}
