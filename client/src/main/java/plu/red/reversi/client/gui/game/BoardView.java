package plu.red.reversi.client.gui.game;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.Animator;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import plu.red.reversi.client.gui.SwingGraphics3D;
import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.game.Board;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.BoardIterator;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.logic.GameLogic;
import plu.red.reversi.core.game.player.HumanPlayer;
import plu.red.reversi.core.graphics.*;
import plu.red.reversi.core.listener.IBoardUpdateListener;
import plu.red.reversi.core.listener.ICommandListener;
import plu.red.reversi.core.listener.IGameOverListener;
import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.reversi3d.Board3D;
import plu.red.reversi.core.reversi3d.Camera;
import plu.red.reversi.core.reversi3d.HighlightMode;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.security.InvalidParameterException;
import java.util.*;

/**
 * The JPanel containing the board and its edges.
 */
public class BoardView extends GLJPanel implements MouseListener, IBoardUpdateListener, ICommandListener, IGameOverListener, Board3D.Board3DListener {

    public static final Vector3fc LAST_MOVE_COLOR = new Vector3f(1.0f, 0.2f, 0.2f);
    public static final Vector3fc POSSIBLE_MOVES_COLOR = new Vector3f(0.0f, 1.0f, 0.0f);

    private Animator animator;

    private Game game;

    private SwingGraphics3D g3d;
    private Pipeline pipeline;

    private Board3D board;
    private Camera camera;

    private HighlightMode highlightMode;
    private boolean canPlay;
    private long startTime;
    private Queue<Runnable> renderqueue;
    private BoardViewStateListener listener;
    private BoardIterator boardIterator;
    private boolean autoFollow;

    private static GLCapabilities getCapabilities() {

        GLProfile profile = GLProfile.get(GLProfile.GL3);

        GLCapabilities caps = new GLCapabilities(profile);

        caps.setSampleBuffers(true);
        caps.setNumSamples(4);

        return caps;
    }

    /**
     * Constructs a new BoardView.
     *
     * @param game Game object to pull data from
     */
    public BoardView(Game game) {
        super(getCapabilities());
        startTime = System.currentTimeMillis();
        this.game = game;
        boardIterator = new BoardIterator(game.getHistory(), game.getGameLogic(), game.getBoard().size);
        renderqueue = new LinkedList<>();
        game.addListener(this);
        this.setPreferredSize(new Dimension(500,500) );
        this.addMouseListener(this);
        addGLEventListener(new EventHandler());
        setVisible(true);
        autoFollow = true;
        animator = new Animator(this);
        animator.start();
    }

    /**
     * Specifies how highlighting should be done. For example, should we just highlight the previous move, or should the
     * board also be highlighted to show possible moves or the best move?
     *
     * This method will automatically update the highlighting after the mode has been changed.
     *
     * @param highlightMode the highlighting type.
     */
    public void setHighlightMode(HighlightMode highlightMode) {
        this.highlightMode = highlightMode;

        updateHighlights();
    }

    /**
     * Applies the specified highlighting mode to the board highlights. Called every time an update to the highlighting may occur.
     *
     * Note that highlights will generally not be shown unless canPlay is true.
     */
    public HighlightMode getHighlightMode() {
        return highlightMode;
    }

    private void updateHighlights() {

        queueEvent(new Runnable() {
            @Override
            public void run() {
                board.clearHighlights();

                if(canPlay) {
                    if(highlightMode == HighlightMode.HIGHLIGHT_POSSIBLE_MOVES) {
                        // we can use the game board because GUI will be caught up animation wise

                        Set<BoardIndex> validMoves = (
                            game.getGameLogic()
                                .getValidMoves(
                                    game.getNextPlayerID (
                                        game.getHistory()
                                            .getBoardCommand(boardIterator.getPos()).playerID
                                    ),
                                    boardIterator.board
                                )
                        );

                        for(BoardIndex index : validMoves)
                            board.highlightAt(index, POSSIBLE_MOVES_COLOR);
                    }
                    else if(highlightMode == HighlightMode.HIGHLIGHT_BEST_MOVE) {
                        // TODO
                    }

                    BoardCommand lastMove = game.getHistory().getBoardCommand(boardIterator.getPos());

                    if(lastMove instanceof MoveCommand)
                        board.highlightAt(lastMove.position, LAST_MOVE_COLOR);
                }
            }
        });
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        // must be on the latest move to issue command on board
        if(autoFollow) {
            BoardIndex index = board.getIndexAtCoord(
                    camera.pixelToPosition(new Vector2f(mouseEvent.getX(), mouseEvent.getY()))
            );

            game.getCurrentPlayer().boardClicked(index);

            canPlay = false;
        }
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }

    @Override
    public void commandApplied(Command cmd) {

        updateHighlights();
    }

    @Override
    public void onGameOver(Player player, int score) {
        // TODO
    }

    @Override
    public void onBoardUpdate(BoardUpdate update) {
        if(board != null && autoFollow)
            board.animBoardUpdate(update);
        // the board is not initialized yet, meaning it will be set to be the correct anyway.
    }

    @Override
    public void onBoardRefresh() {
        board.setBoard(game.getBoard());
    }

    @Override
    public void onScoreChange(Board3D board) {

    }

    @Override
    public void onAnimationsDone(Board3D board) {
        if(game.getCurrentPlayer() instanceof HumanPlayer)
            canPlay = true;

        updateHighlights();
    }

    @Override
    public void onAnimationStepDone(Board3D board) {
        // keep our board iterator synced
        boardIterator.next();
        listener.onBoardStateChanged(this);
    }

    private class EventHandler implements GLEventListener {


        @Override
        public void init(GLAutoDrawable drawable) {

            //System.out.println("Init claled: " + drawable);

            g3d = new SwingGraphics3D(drawable.getGL().getGL3());

            PipelineDefinition def = new PipelineDefinition();

            def.directionalLightCount = 2;

            pipeline = new Pipeline(def, new SimpleGLVertexShader(def), new SimpleGLFragmentShader(def));

            g3d.createPipeline(pipeline);
            g3d.setPipeline(pipeline);

            g3d.bindPipelineUniform("fDirectionalLights[0]", pipeline, new Vector3f(-0.6f, 0.25f, 1.0f).normalize());
            g3d.bindPipelineUniform("fDirectionalLights[1]", pipeline, new Vector3f(0.6f, -0.25f, 1.0f).normalize());

            board = new Board3D(g3d, pipeline, game);
            boardIterator.goTo(game.getHistory().getNumBoardCommands() - 1);

            board.addListener(BoardView.this);

            camera = new Camera();

            camera.setPos(new Vector2f(0,0));
            camera.setDir(new Vector2f(0, 0.5f * (float)Math.PI));
        }

        @Override
        public void dispose(GLAutoDrawable drawable) {
            // TODO: Implement disposal in the Model3D stuff because I did not think about that until now.
        }

        @Override
        public void display(GLAutoDrawable drawable) {

            Runnable r;

            while((r = renderqueue.poll()) != null)
                r.run();

            g3d.setPipeline(pipeline);

            g3d.clearBuffers();

            // TODO: Rework the tick system
            int tick = (int)(System.currentTimeMillis() - startTime) / 17;

            board.update(tick);
            camera.update(tick);

            // regardless of if anything actually changed, we render on the computer because its a computer
            g3d.bindPipelineUniform("viewMatrix", pipeline, camera.getViewMatrix());
            g3d.bindPipelineUniform("projectionMatrix", pipeline, camera.getProjectionMatrix());

            board.draw();

        }

        @Override
        public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

            camera.setViewport(new Vector2f(width, height));

            camera.setZoom(Math.min(width, height) / board.getBoardRadius() / 2);
        }
    }

    /**
     * Get the board object representing the current state of the board as it graphically appears to the user (independant of the game state)
     * @return the board object representing the current state graphically
     */
    public Board getCurrentBoard() {
        return boardIterator.board;
    }

    /**
     * Gets the move index of the current game representing the current BoardCommand index.
     * @return the index of the currently shown BoardCommand in the sequence.
     */
    public int getCurrentMoveIndex() {
        return boardIterator.getPos();
    }

    /**
     * Sets the board to show a different move index.
     * @param index the BoardCommand index to show the board state of.
     */
    public void setMoveIndex(int index) {

        boardIterator.goTo(index);

        board.setBoard(boardIterator.board);

        autoFollow = index == game.getHistory().getNumBoardCommands() - 1;
        canPlay = true; // always true because animation will no longer be in progress.

        updateHighlights();

        if(listener != null)
            listener.onBoardStateChanged(this);
    }

    /**
     * Adds a new event to be executed on the next update() loop of rendering. Required if you want to do anything
     * that may result in a g3d call.
     * @param r the code to execute in the graphics context.
     */
    public void queueEvent(Runnable r) {
        renderqueue.add(r);
    }

    /**
     * Specifies a delegate object which will listen to events defined in BoardViewStateListener.
     *
     * Will replace any previously existing listener.
     *
     * @param listener the new listener object.
     */
    public void setBoardViewListener(BoardViewStateListener listener) {
        this.listener = listener;
    }

    public interface BoardViewStateListener {
        /**
         * Called when the BoardView object has changed the current move in which the board is displaying, either through
         * an animation or by explicit call to #setMoveIndex
         * @param view the board view which initiated this call.
         */
        void onBoardStateChanged(BoardView view);
    }
}
