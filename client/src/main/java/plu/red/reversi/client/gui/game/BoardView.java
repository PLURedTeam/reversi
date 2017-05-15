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
import plu.red.reversi.core.SettingsLoader;
import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.game.Board;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.BoardIterator;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.logic.GameLogicCache;
import plu.red.reversi.core.game.player.HumanPlayer;
import plu.red.reversi.core.game.player.NullPlayer;
import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.graphics.Pipeline;
import plu.red.reversi.core.graphics.PipelineDefinition;
import plu.red.reversi.core.graphics.SimpleGLFragmentShader;
import plu.red.reversi.core.graphics.SimpleGLVertexShader;
import plu.red.reversi.core.listener.IBoardUpdateListener;
import plu.red.reversi.core.listener.ICommandListener;
import plu.red.reversi.core.listener.IGameOverListener;
import plu.red.reversi.core.reversi3d.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

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

    private FreeMode freeCameraMode;
    private FlatMode flatCameraMode;

    private int width, height; // the most recent width/height returned by reshape, hack for retina displays

    private ControlMode currentMode;

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

        canPlay = game.getCurrentPlayer() instanceof HumanPlayer ||
                game.getCurrentPlayer() instanceof NullPlayer;

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
                                    boardIterator.cache,
                                    boardIterator.board,
                                    game.getNextPlayerID (
                                        game.getHistory()
                                            .getBoardCommand(boardIterator.getPos()).playerID
                                    )
                                )
                        );

                        for(BoardIndex index : validMoves)
                            board.highlightAt(index, POSSIBLE_MOVES_COLOR);
                    }
                    else if(highlightMode == HighlightMode.HIGHLIGHT_BEST_MOVE) {
                        // TODO
                    }

                    if(boardIterator.getPos() >= 0) {
                        BoardCommand lastMove = game.getHistory().getBoardCommand(boardIterator.getPos());

                        if(lastMove instanceof MoveCommand)
                            board.highlightAt(lastMove.position, LAST_MOVE_COLOR);
                    }
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
                    camera.pixelToPosition(new Vector2f(((float)mouseEvent.getX() / getWidth()) * width, ((float)mouseEvent.getY() / getHeight()) * height))
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
    public void onGameOver(Player player, int score){
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

        if(game.isGameOver()) {
            currentMode = new EndgameMode(game, camera, board, freeCameraMode);
        }
        else {
            if(game.getCurrentPlayer() instanceof HumanPlayer)
                canPlay = true;

            updateHighlights();
        }
    }

    @Override
    public void onAnimationStepDone(Board3D board) {
        // keep our board iterator synced
        boardIterator.next();
        listener.onBoardStateChanged(this);
    }

    public boolean isPresentationMode() {
        return currentMode == freeCameraMode;
    }

    public void setPresentationMode(boolean present) {
        if(present) {
            currentMode = freeCameraMode;
        }
        else {
            currentMode = flatCameraMode;
        }

        currentMode.start();
    }

    public void setAutoFollow(boolean autoFollow) {
        this.autoFollow = autoFollow;

        queueEvent(new Runnable() {
            @Override
            public void run() {
                if(autoFollow) {
                    board.clearAnimations();

                    // queue up anims til the end
                    BoardIterator iter = new BoardIterator(boardIterator);

                    for(BoardCommand cmd : game.getHistory().getMoveCommandsAfter(boardIterator.getPos() + 1)) {
                        if(cmd instanceof MoveCommand) {
                            game.getGameLogic().play(
                                    iter.cache,
                                    iter.board,
                                    (MoveCommand) cmd,
                                    true, false
                            );
                        }
                        else {
                            BoardUpdate update = new BoardUpdate();
                            update.player = cmd.playerID;
                            update.added.add(cmd.position);

                            board.animBoardUpdate(update);

                            iter.next();
                        }
                    }
                }
                else {
                    board.clearAnimations();
                    updateHighlights();
                }
            }
        });
    }

    private class EventHandler implements GLEventListener {


        @Override
        public void init(GLAutoDrawable drawable) {

            //System.out.println("Init claled: " + drawable);

            g3d = new SwingGraphics3D(drawable.getGL().getGL3());
            g3d.setClearColor(new Vector3f(0.2f));

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

            //camera.setPos(new Vector2f(0,0));
            //camera.setDir(new Vector2f(0, 0.5f * (float)Math.PI));
            freeCameraMode = new FreeMode(game, camera, board, 100);
            flatCameraMode = new FlatMode(game, camera, board, 400);

            freeCameraMode.setAutoRotate(true);

            if(SettingsLoader.INSTANCE.getClientSettings().get(SettingsLoader.GLOBAL_USE_3D_VIEW, true))
                currentMode = freeCameraMode;
            else
                currentMode = flatCameraMode;
            currentMode.start();

            // TODO: This is a little funky... but its the best way to deal with making sure the correct move is displayed
            setMoveIndex(game.getHistory().getNumBoardCommands() - 1);
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

            currentMode.update(tick);

            g3d.clearBuffers();

            // regardless of if anything actually changed, we render on the computer because its a computer
            g3d.bindPipelineUniform("viewMatrix", pipeline, camera.getViewMatrix());
            g3d.bindPipelineUniform("projectionMatrix", pipeline, camera.getProjectionMatrix());

            board.draw();

        }

        @Override
        public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

            camera.setViewport(new Vector2f(width, height));

            float baseZoom = Math.min(width, height) / board.getBoardRadius() / 2;

            if(currentMode == flatCameraMode) {
                freeCameraMode.setZoom(baseZoom / 2);
                flatCameraMode.setZoom(baseZoom);
            }
            else {
                flatCameraMode.setZoom(baseZoom);
                freeCameraMode.setZoom(baseZoom / 2);
            }

            BoardView.this.width = width;
            BoardView.this.height = height;
        }
    }

    /**
     * Get the board object representing the current state of the board as it graphically appears to the user (independant of the game state)
     * @return the board object representing the current state graphically
     */
    public Board getCurrentBoard() {
        return boardIterator.board;
    }

    public GameLogicCache getCurrentCache() { return boardIterator.cache; }

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

        queueEvent(new Runnable() {
            @Override
            public void run() {
                board.setBoard(boardIterator.board);
            }
        });

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
