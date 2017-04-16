package plu.red.reversi.android;

import android.content.Context;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.Scroller;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Collection;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.game.Board;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.BoardIterator;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.player.NullPlayer;
import plu.red.reversi.core.graphics.Graphics3D;
import plu.red.reversi.core.graphics.Pipeline;
import plu.red.reversi.core.graphics.PipelineDefinition;
import plu.red.reversi.core.graphics.PixelShader;
import plu.red.reversi.core.graphics.SimpleGLFragmentShader;
import plu.red.reversi.core.graphics.SimpleGLVertexShader;
import plu.red.reversi.core.graphics.VertexShader;
import plu.red.reversi.core.listener.IBoardUpdateListener;
import plu.red.reversi.core.reversi3d.Board3D;
import plu.red.reversi.core.reversi3d.Camera;
import plu.red.reversi.core.reversi3d.HighlightMode;

/**
 * Created by daniel on 3/18/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public class GameSurfaceView extends GLSurfaceView implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener, IBoardUpdateListener, Board3D.Board3DListener {


    public static final Vector3fc MOVE_SELECT_COLOR = new Vector3f(1.0f, 1.0f, 0.0f);
    public static final Vector3fc LAST_MOVE_COLOR = new Vector3f(1.0f, 0.2f, 0.2f);
    public static final Vector3fc POSSIBLE_MOVES_COLOR = new Vector3f(0.0f, 1.0f, 0.0f);

    private static final int FRAMERATE = 60;

    // expressed in inches (of screen size, later we will have DPI)
    private static final float MAX_ZOOM = 20.0f;
    private static final float MIN_ZOOM = 2.0f;

    private GameRenderer mRenderer;

    private BoardIndex mSelectedIndex;

    private Handler mUpdateTaskHandler;

    private GestureDetector mDetector;
    private ScaleGestureDetector mScaleDetector;

    private Scroller mScroller;

    private int mDpi;

    private GameSurfaceViewListener mListener;

    private Game mGame;

    private boolean mCanDoCommand;

    private boolean mAutoFollow;

    private BoardIterator mBoardIterator;

    private HighlightMode mHighlightMode;

    private boolean mPresentationMode;
    private boolean mAutoRotate;


    public GameSurfaceView(Context context) {
        super(context);

        init();
    }

    public GameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        setEGLContextClientVersion(3);

        mRenderer = new GameRenderer();
        setRenderer(mRenderer);

        setRenderMode(RENDERMODE_WHEN_DIRTY);

        // add the update task to the GL thread

        mScroller = new Scroller(getContext());

        mDetector = new GestureDetector(getContext(), this);
        mScaleDetector = new ScaleGestureDetector(getContext(), this);

        mDetector.setOnDoubleTapListener(this);

        mRenderer.setAutoRotate(true);

        mDpi = getResources().getDisplayMetrics().densityDpi;

        mRenderer.setPlayZoom(mDpi * 10.0f);

        mUpdateTaskHandler = new Handler(Looper.myLooper());

        mUpdateTaskHandler.post(new UpdateTask());
    }

    public void setListener(GameSurfaceViewListener listener) {
        mListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        mDetector.onTouchEvent(e);
        mScaleDetector.onTouchEvent(e);

        return true;
    }

    /**
     * Notified when a tap occurs with the down {@link MotionEvent}
     * that triggered it. This will be triggered immediately for
     * every down event. All other events should be preceded by this.
     *
     * @param e The down motion event.
     */
    @Override
    public boolean onDown(MotionEvent e) {
        return true; // must return true here or else other events will not be listened to.
    }

    /**
     * The user has performed a down {@link MotionEvent} and not performed
     * a move or up yet. This event is commonly used to provide visual
     * feedback to the user to let them know that their action has been
     * recognized i.e. highlight an element.
     *
     * @param e The down motion event
     */
    @Override
    public void onShowPress(MotionEvent e) {

    }

    /**
     * Notified when a tap occurs with the up {@link MotionEvent}
     * that triggered it.
     *
     * @param e The up motion event that completed the first tap
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onSingleTapUp(MotionEvent e) {

        if(!mCanDoCommand || mGame.getHistory().getNumBoardCommands() - 1 != mBoardIterator.getPos())
            return true;

        BoardIndex index = mRenderer.getTappedIndex(new Vector2f(e.getX(), e.getY()));

        // is this a valid play index?
        if(mGame.getGameLogic().getValidMoves(mGame.getCurrentPlayer().getID()).contains(index)) {

            System.out.println("Got tap at " + index);

            if(index.row < 0 || index.column < 0 || index.row >= mGame.getBoard().size || index.column >= mGame.getBoard().size)
                return false; // not a valid row index

            mSelectedIndex = index;

            if(mListener != null)
                mListener.onBoardSelected(index);
        }

        return true;
    }

    /**
     * Notified when a scroll occurs with the initial on down {@link MotionEvent} and the
     * current move {@link MotionEvent}. The distance in x and y is also supplied for
     * convenience.
     *
     * @param e1        The first down motion event that started the scrolling.
     * @param e2        The move motion event that triggered the current onScroll.
     * @param distanceX The distance along the X axis that has been scrolled since the last
     *                  call to onScroll. This is NOT the distance between {@code e1}
     *                  and {@code e2}.
     * @param distanceY The distance along the Y axis that has been scrolled since the last
     *                  call to onScroll. This is NOT the distance between {@code e1}
     *                  and {@code e2}.
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        mScroller.forceFinished(true);

        queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.setCameraPos(-distanceX + mRenderer.getCameraPos().x(), -distanceY + mRenderer.getCameraPos().y());

                requestRender();
            }
        });

        return true;
    }

    /**
     * Notified when a long press occurs with the initial on down {@link MotionEvent}
     * that trigged it.
     *
     * @param e The initial on down motion event that started the longpress.
     */
    @Override
    public void onLongPress(MotionEvent e) {

    }

    /**
     * Notified of a fling event when it occurs with the initial on down {@link MotionEvent}
     * and the matching up {@link MotionEvent}. The calculated velocity is supplied along
     * the x and y axis in pixels per second.
     *
     * @param e1        The first down motion event that started the fling.
     * @param e2        The move motion event that triggered the current onFling.
     * @param velocityX The velocity of this fling measured in pixels per second
     *                  along the x axis.
     * @param velocityY The velocity of this fling measured in pixels per second
     *                  along the y axis.
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        // Flings use math in pixels (as opposed to math based on the viewport).
        Vector2fc pos = mRenderer.getCameraPos();
        // Before flinging, aborts the current animation.
        mScroller.forceFinished(true);
        // Begins the animation

        RectF scrollBounds = mRenderer.getScrollBounds();

        //System.out.println("Start fling: " + pos.x() + ", " + pos.y() + ", " + velocityX + ", " + velocityY);

        mScroller.fling(
                // Current scroll position
                (int)pos.x(),
                (int)pos.y(),
                (int)velocityX,
                (int)velocityY,
                (int)scrollBounds.left,
                (int)scrollBounds.right,
                (int)scrollBounds.top,
                (int)scrollBounds.bottom
        );

        return true;
    }

    /**
     * Notified when a single-tap occurs.
     * <p>
     * Unlike OnGestureListener#onSingleTapUp, this
     * will only be called after the detector is confident that the user's
     * first tap is not followed by a second tap leading to a double-tap
     * gesture.
     *
     * @param e The down motion event of the single-tap.
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    /**
     * Notified when a double-tap occurs.
     *
     * @param e The down motion event of the first tap of the double-tap.
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    /**
     * Notified when an event within a double-tap gesture occurs, including
     * the down, move, and up events.
     *
     * @param e The motion event that occurred during the double-tap gesture.
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    /**
     * Responds to scaling events for a gesture in progress.
     * Reported by pointer motion.
     *
     * @param detector The detector reporting the event - use this to
     *                 retrieve extended info about event state.
     * @return Whether or not the detector should consider this event
     * as handled. If an event was not handled, the detector
     * will continue to accumulate movement until an event is
     * handled. This can be useful if an application, for example,
     * only wants to update scaling factors if the change is
     * greater than 0.01.
     */
    @Override
    public boolean onScale(ScaleGestureDetector detector) {

        float newZoom = mRenderer.getPlayZoom() * detector.getScaleFactor();

        newZoom = Math.max(MIN_ZOOM * mDpi, Math.min(MAX_ZOOM * mDpi, newZoom));

        mRenderer.setPlayZoom(newZoom);

        return true;
    }

    /**
     * Responds to the beginning of a scaling gesture. Reported by
     * new pointers going down.
     *
     * @param detector The detector reporting the event - use this to
     *                 retrieve extended info about event state.
     * @return Whether or not the detector should continue recognizing
     * this gesture. For example, if a gesture is beginning
     * with a focal point outside of a region where it makes
     * sense, onScaleBegin() may return false to ignore the
     * rest of the gesture.
     */
    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    /**
     * Responds to the end of a scale gesture. Reported by existing
     * pointers going up.
     * <p>
     * Once a scale has ended, {@link ScaleGestureDetector#getFocusX()}
     * and {@link ScaleGestureDetector#getFocusY()} will return focal point
     * of the pointers remaining on the screen.
     *
     * @param detector The detector reporting the event - use this to
     *                 retrieve extended info about event state.
     */
    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        // TODO: Some applications let you fling the scale; that might be nice here.
    }

    @Override
    public void onBoardUpdate(BoardUpdate update) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.onBoardUpdate(update);
            }
        });
    }

    @Override
    public void onBoardRefresh() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.onBoardRefresh();
            }
        });
    }

    @Override
    public void onScoreChange(Board3D board) {
        if(mListener != null)
            mListener.onBoardScoreChanged();
    }

    @Override
    public void onAnimationsDone(Board3D board) {
        // enable the ability to control again
        if(mGame.getCurrentPlayer() instanceof NullPlayer)
            setPlayerEnabled(true);
    }

    @Override
    public void onAnimationStepDone(Board3D board) {

        mBoardIterator.next();

        if(mListener != null)
            mListener.onBoardScoreChanged();
    }

    public void doHighlights() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if(mRenderer.mBoard != null) {
                    mRenderer.mBoard.clearHighlights();
                    if(mCanDoCommand) {
                        if(mHighlightMode == HighlightMode.HIGHLIGHT_POSSIBLE_MOVES) {
                            // we can use the game board because GUI will be caught up animation wise
                            for(BoardIndex index :
                                    mGame.getGameLogic().getValidMoves(mGame.getNextPlayerID(
                                            mGame.getHistory().getBoardCommand(mBoardIterator.getPos()).playerID
                                    ) ,getCurrentBoard())) {
                                mRenderer.mBoard.highlightAt(index, POSSIBLE_MOVES_COLOR);
                            }
                        }
                        else if(mHighlightMode == HighlightMode.HIGHLIGHT_BEST_MOVE) {
                            // TODO
                        }
                        BoardCommand lastMove = mGame.getHistory().getBoardCommand(mBoardIterator.getPos());
                        if (lastMove instanceof MoveCommand) {
                            mRenderer.mBoard.highlightAt(lastMove.position, LAST_MOVE_COLOR);
                        }

                        if(mSelectedIndex != null)
                            mRenderer.mBoard.highlightAt(mSelectedIndex, MOVE_SELECT_COLOR);
                    }

                    requestRender();
                }
            }
        });
    }

    public Board getCurrentBoard() {
        return mBoardIterator.board;
    }

    public int getCurrentMoveIndex() {
        return mBoardIterator.getPos();
    }

    public synchronized void setCurrentMove(int pos) {
        mBoardIterator.goTo(pos);

        if(mRenderer.mBoard != null) {
            mRenderer.mBoard.setBoard(mBoardIterator.board);
        }

        mCanDoCommand = true;

        doHighlights();
    }

    public HighlightMode getHighlightMode() {
        return mHighlightMode;
    }

    private class UpdateTask implements Runnable {

        @Override
        public void run() {

            if(mScroller.computeScrollOffset()) {
                //mRenderer.getCamera().setPos(new Vector2f(mScroller.getCurrX(), mScroller.getCurrY()));
                mRenderer.setCameraPos(mScroller.getCurrX(), mScroller.getCurrY());
                requestRender();
            }

            queueEvent(new Runnable() {
                @Override
                public void run() {

                    if(mRenderer.update()) {
                        requestRender();
                        //System.out.println("Renderer has requested render");
                    }
                }
            });

            mUpdateTaskHandler.postDelayed(this, 1000 / FRAMERATE);
        }
    }

    public void setGame(Game game) {
        mGame = game;

        mGame.getGameLogic().addBoardUpdateListener(this);

        if(mGame.getCurrentPlayer() instanceof NullPlayer)
            setPlayerEnabled(true);

        queueEvent(new Runnable() {
            @Override
            public void run() {

                mListener.onBoardScoreChanged();
            }
        });

        mBoardIterator = new BoardIterator(mGame.getHistory(), mGame.getGameLogic(), mGame.getBoard().size);

        if(mRenderer.mGLStarted) {
            mRenderer.mBoard = new Board3D(mRenderer.g3d, mRenderer.mPipeline, mGame);
            mRenderer.mBoard.addListener(this);

            mRenderer.mCamera.setMoveBounds(new Vector2f(-mRenderer.mBoard.getBoardRadius()), new Vector2f(mRenderer.mBoard.getBoardRadius()));

            doHighlights();
        }

        setCurrentMove(mAutoFollow ? mGame.getHistory().getNumBoardCommands() - 1 : 0);

    }

    public BoardIndex getCurrentSelected() {
        return mSelectedIndex;
    }

    public void disablePlayer() {
        setPlayerEnabled(false);

        mSelectedIndex = null;
    }

    private void setPlayerEnabled(boolean enabled) {

        if(mCanDoCommand != enabled) {

            mCanDoCommand = enabled;

            if(mListener != null)
                mListener.onPlayerStateChanged();
        }
    }

    public boolean isPlayerEnabled() {
        return mCanDoCommand;
    }

    public int getPlayerScore(int playerId) {
        if(mRenderer.mBoard != null)
            return mRenderer.mBoard.getScore(playerId);

        return 0;
    }

    public boolean isInPresentationMode() {
        return mPresentationMode;
    }

    public interface GameSurfaceViewListener {
        void onBoardSelected(BoardIndex index);
        void onBoardScoreChanged();
        void onPlayerStateChanged();
    }

    public void setPresentationMode(boolean present) {
        mPresentationMode = present;

        queueEvent(new Runnable() {
            @Override
            public void run() {
                if(mRenderer.mCamera != null) {
                    if(present) {
                        mRenderer.mCamera.beginDrag(60);
                        mRenderer.mCamera.setPos(new Vector2f(0, 0));
                        mRenderer.mCamera.setDir(new Vector2f(-0.00f * (float)Math.PI, 0.15f * (float)Math.PI));
                        mRenderer.mCamera.setZoom(2400);
                    }
                    else {
                        mAutoRotate = false;

                        mRenderer.mCamera.beginDrag(60);
                        mRenderer.mCamera.setPos(new Vector2f(0, 0));
                        mRenderer.mCamera.setDir(new Vector2f(0.00f * (float)Math.PI, 0.5f * (float)Math.PI));
                        mRenderer.mCamera.setZoom(mRenderer.mPlayZoom);
                    }
                }
            }
        });
    }

    public void setAutoFollow(boolean follow) {
        if(follow) {
            mAutoFollow = true;

            queueEvent(new Runnable() {
                @Override
                public void run() {
                    if(mRenderer.mBoard != null && mGame != null) {

                        mRenderer.mBoard.clearAnimations();

                        // queue up all the animations up until the end
                        BoardIterator iter = new BoardIterator(mBoardIterator);

                        System.out.println("POS: " + mBoardIterator.getPos());
                        System.out.println("Count: " + mGame.getHistory().getMoveCommandsAfter(mBoardIterator.getPos() + 1).size());
                        for(BoardCommand cmd : mGame.getHistory().getMoveCommandsAfter(mBoardIterator.getPos() + 1)) {

                            if(cmd instanceof MoveCommand) {

                                boolean preAutoFollow = mAutoFollow;

                                // hack for right now.
                                mAutoFollow = true;

                                mGame.getGameLogic().play((MoveCommand) cmd, iter.board, true, false);

                                mAutoFollow = preAutoFollow;

                                /*mRenderer.mBoard.animBoardUpdate(
                                        cmd.position,
                                        cmd.playerID,
                                        iter.board.calculateFlipsFromBoard(cmd.position, cmd.playerID)
                                );*/
                            }
                            else {
                                BoardUpdate update = new BoardUpdate();

                                update.player = cmd.playerID;
                                update.added.add(cmd.position);

                                mRenderer.mBoard.animBoardUpdate(update);
                            }

                            iter.next();
                        }
                    }

                    // just make sure the board appears to be on the latest move instead.
                    else setCurrentMove(mGame.getHistory().getNumBoardCommands() - 1);
                }
            });
        }
        else {
            mAutoFollow = false;

            queueEvent(new Runnable() {
                @Override
                public void run() {
                    mRenderer.mBoard.clearAnimations();
                }
            });
        }
    }

    public boolean isAutoFollow() {
        return mAutoFollow;
    }

    public void setHighlightMode(HighlightMode mode) {
        this.mHighlightMode = mode;

        doHighlights();
    }

    private class GameRenderer implements Renderer, IBoardUpdateListener {

        public Graphics3D g3d;

        public Pipeline mPipeline;

        public Board3D mBoard;
        public Camera mCamera;

        public float mPlayZoom;

        public int mTick;

        public boolean mGLStarted;

        public GameRenderer() {
            mTick = 0;
            mPlayZoom = 3600;
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

            def.isES = true;
            def.directionalLightCount = 2;

            VertexShader vs = new SimpleGLVertexShader(def);
            PixelShader ps = new SimpleGLFragmentShader(def);

            mPipeline = new Pipeline(def, vs, ps);

            g3d.createPipeline(mPipeline);
            g3d.setPipeline(mPipeline);

            g3d.bindPipelineUniform("fDirectionalLights[0]", mPipeline, new Vector3f(-0.6f, 0.25f, 1.0f).normalize());
            g3d.bindPipelineUniform("fDirectionalLights[1]", mPipeline, new Vector3f(0.6f, -0.25f, 1.0f).normalize());

            mCamera = new Camera();

            if(mGame != null) {
                mBoard = new Board3D(g3d, mPipeline, mGame);

                mBoard.addListener(GameSurfaceView.this);

                mBoard.setBoard(mBoardIterator.board);

                mCamera.setMoveBounds(new Vector2f(-mBoard.getBoardRadius()), new Vector2f(mBoard.getBoardRadius()));

                doHighlights();
            }

            mGLStarted = true;
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

            if(mGame != null && mBoard != null) {
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

        public BoardIndex getTappedIndex(Vector2fc loc) {
            // ask the camera to convert pixels to board coord
            Vector2f res = mCamera.pixelToPosition(loc);

            System.out.println("Camera says OGL was " + res);

            return mBoard.getIndexAtCoord(res);
        }


        @Override
        public void onBoardUpdate(BoardUpdate update) {
            if(mAutoFollow) {
                // auto follow
                if(mBoard != null) {
                    mBoard.clearHighlights();
                    mBoard.animBoardUpdate(update);
                }
            }
        }

        @Override
        public void onBoardRefresh() {
            mBoard.setBoard(mGame.getBoard());
        }
    }
}
