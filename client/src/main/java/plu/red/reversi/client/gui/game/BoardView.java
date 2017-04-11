package plu.red.reversi.client.gui.game;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingTargetAdapter;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import plu.red.reversi.client.gui.SwingGraphics3D;
import plu.red.reversi.client.gui.util.Utilities;
import plu.red.reversi.core.command.BoardCommand;
import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.command.MoveCommand;
import plu.red.reversi.core.game.Board;
import plu.red.reversi.core.game.BoardIndex;
import plu.red.reversi.core.game.Game;
import plu.red.reversi.core.game.player.HumanPlayer;
import plu.red.reversi.core.graphics.*;
import plu.red.reversi.core.listener.IBoardUpdateListener;
import plu.red.reversi.core.listener.ICommandListener;
import plu.red.reversi.core.listener.IGameOverListener;
import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.reversi3d.Board3D;
import plu.red.reversi.core.reversi3d.Camera;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * The JPanel containing the board and its edges.
 */
public class BoardView extends GLCanvas implements MouseListener, GLEventListener, IBoardUpdateListener, ICommandListener, IGameOverListener {

    public static final Vector3fc LAST_MOVE_COLOR = new Vector3f(1.0f, 0.2f, 0.2f);
    public static final Vector3fc POSSIBLE_MOVES_COLOR = new Vector3f(0.0f, 1.0f, 0.0f);

    private Game game;

    private SwingGraphics3D g3d;
    private Pipeline pipeline;

    private Board3D board;
    private Camera camera;

    private HighlightMode highlightMode;

    private boolean canPlay;

    private int tick;

    /**
     * Constructs a new BoardView.
     *
     * @param game Game object to pull data from
     */
    public BoardView(Game game) {
        super(new GLCapabilities(GLProfile.getDefault()));

        this.game = game;

        game.addListener(this);

        this.setPreferredSize(new Dimension(500,500) );

        this.addMouseListener(this);
    }

    public void setHighlightMode(HighlightMode highlightMode) {
        this.highlightMode = highlightMode;

        updateHighlights();
    }

    private void updateHighlights() {
        board.clearHighlights();

        if(canPlay) {
            if(highlightMode == HighlightMode.HIGHLIGHT_POSSIBLE_MOVES) {
                // we can use the game board because GUI will be caught up animation wise
                for(BoardIndex index : game.getBoard().getPossibleMoves(game.getCurrentPlayer().getID())) {
                    board.highlightAt(index, POSSIBLE_MOVES_COLOR);
                }
            }
            else if(highlightMode == HighlightMode.HIGHLIGHT_BEST_MOVE) {
                // TODO
            }

            BoardCommand lastMove = game.getHistory().getBoardCommand(game.getHistory().getNumBoardCommands() - 1);

            if(lastMove instanceof MoveCommand) {
                board.highlightAt(lastMove.position, LAST_MOVE_COLOR);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        BoardIndex index = board.getIndexAtCoord(
                camera.pixelToPosition(new Vector2f(mouseEvent.getX(), mouseEvent.getY()))
        );

        game.getCurrentPlayer().boardClicked(index);
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
        if(game.getCurrentPlayer() instanceof HumanPlayer)
            canPlay = true;

        updateHighlights();
    }

    @Override
    public void onGameOver(Player player, int score) {
        // TODO
    }

    @Override
    public void onBoardUpdate(BoardIndex origin, int playerId, Collection<BoardIndex> updated) {
        board.animBoardUpdate(origin, playerId, updated);
    }

    @Override
    public void onBoardRefresh() {
        board.setBoard(game);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        g3d = new SwingGraphics3D(drawable.getGL().getGL3());

        PipelineDefinition def = new PipelineDefinition();

        def.directionalLightCount = 4;

        pipeline = new Pipeline(def, new SimpleGLVertexShader(def), new SimpleGLFragmentShader(def));

        board = new Board3D(g3d, pipeline, game);

        camera = new Camera();
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        // TODO: Implement disposal in the Model3D stuff because I did not think about that until now.
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        // TODO: This method is automatically called by JOGL at the refresh rate of the screen. In most cases, this is 60fps, but it could be different. Examine.
        tick++;

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

        camera.setZoom(Math.min(width, height) / board.getBoardRadius());
    }

    public enum HighlightMode {
        HIGHLIGHT_NONE,
        HIGHLIGHT_POSSIBLE_MOVES,
        HIGHLIGHT_BEST_MOVE;
    }
}
