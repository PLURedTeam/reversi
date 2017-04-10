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

import java.util.Collection;

import plu.red.reversi.android.reversi3d.Board3D;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.game.player.NullPlayer;
import plu.red.reversi.core.listener.IBoardUpdateListener;
import plu.red.reversi.core.listener.ICommandListener;

/**
 * Created by daniel on 3/18/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public class GameSurfaceView extends GLSurfaceView implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener, IBoardUpdateListener, Board3D.Board3DListener {

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

        if(!mCanDoCommand)
            return true;

        BoardIndex index = mRenderer.getTappedIndex(new Vector2f(e.getX(), e.getY()));

        // is this a valid play index?
        if(mGame.getBoard().getPossibleMoves(mGame.getCurrentPlayer().getID()).contains(index)) {


            System.out.println("Got tap at " + index);

            if(index.row < 0 || index.column < 0 || index.row >= mGame.getBoard().size || index.column >= mGame.getBoard().size)
                return false; // not a valid row index

            queueEvent(new Runnable() {
                @Override
                public void run() {

                    mRenderer.clearBoardHighlights();
                    mRenderer.highlightBoard(index);

                    requestRender();
                }
            });

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

        mRenderer.setCameraPos(-distanceX + mRenderer.getCameraPos().x(), -distanceY + mRenderer.getCameraPos().y());

        requestRender();

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
    public void onBoardUpdate(BoardIndex origin, int playerId, Collection<BoardIndex> updated) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.onBoardUpdate(origin, playerId, updated);
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
        mListener.onBoardScoreChanged();
    }

    @Override
    public void onAnimationsDone(Board3D board) {
        // enable the ability to control again
        if(mGame.getCurrentPlayer() instanceof NullPlayer)
            mCanDoCommand = true;
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

    public GameRenderer getRenderer() {
        return mRenderer;
    }

    public void setGame(Game game) {
        mGame = game;

        mGame.getBoard().addBoardUpdateListener(this);

        if(mGame.getCurrentPlayer() instanceof NullPlayer)
            mCanDoCommand = true;

        queueEvent(new Runnable() {
            @Override
            public void run() {

                mRenderer.setGame(game);


                if(mRenderer.getBoard() == null)
                    queueEvent(this); // TODO: Improve synchronization? I do not have a solution for this atm.

                mRenderer.getBoard().addListener(GameSurfaceView.this);

                mListener.onBoardScoreChanged();
            }
        });
    }

    public BoardIndex getCurrentSelected() {
        return mSelectedIndex;
    }

    public void disablePlayer() {
        mCanDoCommand = false;
    }

    public int getPlayerScore(int playerId) {
        if(mRenderer.getBoard() != null)
            return mRenderer.getBoard().getScore(playerId);

        return 0;
    }

    public interface GameSurfaceViewListener {
        void onBoardSelected(BoardIndex index);
        void onBoardScoreChanged();
    }
}
