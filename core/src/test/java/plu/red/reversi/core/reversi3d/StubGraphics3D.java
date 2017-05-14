package plu.red.reversi.core.reversi3d;

import org.joml.Vector3fc;
import plu.red.reversi.core.graphics.Graphics3D;
import plu.red.reversi.core.graphics.Pipeline;
import plu.red.reversi.core.graphics.Shader;
import plu.red.reversi.core.graphics.VertexBufferObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StubGraphics3D extends Graphics3D {

    private List<Object[]> callSequence;

    public StubGraphics3D() {
        callSequence = new ArrayList<>();
    }

    @Override
    public void setViewport(int x, int y, int w, int h) {
        callSequence.add(new Object[]{"setViewport", x, y, w, h});
    }

    @Override
    public void setAlphaBlending(boolean blend) {
        callSequence.add(new Object[]{"setAlphaBlending", blend});
    }

    @Override
    public void compileShader(Shader shader) {
        callSequence.add(new Object[]{"compileShader", shader});
    }

    @Override
    public void createPipeline(Pipeline pipeline) {
        callSequence.add(new Object[]{"createPipeline", pipeline});
    }

    @Override
    public void setPipeline(Pipeline pipeline) {
        callSequence.add(new Object[]{"setPipeline", pipeline});
    }

    @Override
    public void uploadVBO(VertexBufferObject obj) throws IOException {
        callSequence.add(new Object[]{"uploadVBO", obj});
    }

    @Override
    public void bindPipelineVBO(String name, Pipeline p, VertexBufferObject obj) {
        callSequence.add(new Object[]{"bindPipelineVBO", name, p, obj});
    }

    @Override
    public void bindPipelineUniform(String name, Pipeline p, Object data) {
        callSequence.add(new Object[]{"bindPipelineUniform", name, p, data});
    }

    @Override
    public void enablePipelineVerticesVBO(String name, Pipeline p) {
        callSequence.add(new Object[]{"enablePipelineVerticesVBO", name, p});
    }

    @Override
    public void disablePipelineVerticesVBO(String name, Pipeline p) {
        callSequence.add(new Object[]{"disablePipelineVerticesVBO", name, p});
    }

    @Override
    public void setClearColor(Vector3fc color) {
        callSequence.add(new Object[]{"setClearColor", color});
    }

    @Override
    public void clearBuffers() {
        callSequence.add(new Object[]{"clearBuffers"});
    }

    @Override
    public void drawVertices(int first, int count) {
        callSequence.add(new Object[]{"drawVertices", first, count});
    }

    @Override
    public void drawIndices(int start, int end) {
        callSequence.add(new Object[]{"drawIndices", start, end});
    }

    public List<Object[]> getCallSequence() {
        return callSequence;
    }

    public void clearCallSequence() {
        callSequence.clear();
    }
}
