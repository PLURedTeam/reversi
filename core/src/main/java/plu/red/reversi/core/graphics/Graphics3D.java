package plu.red.reversi.core.graphics;

import org.joml.Vector3fc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;

/**
 * Created by daniel on 3/19/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public abstract class Graphics3D {

    /**
     * Specifies to the rendering backend the viewport drawing dimensions
     * @param x x value from the bottom left corner
     * @param y y value from the bottom left corner
     * @param w the width of the viewport
     * @param h the height of the viewport
     */
    public abstract void setViewport(int x, int y, int w, int h);

    /**
     * Specifies whether or not alpha blending should be enabled or not. This is useful for transparency.
     * @param blend whether or not to have alpha blending
     */
    public abstract void setAlphaBlending(boolean blend);

    /**
     * Using the specified shader, compile the code returned by getCode() and associate it with the shader using setHandle()
     * @param shader the shader to compile
     * @throws ShaderCompileException if the shader fails to compile
     * @throws UnsupportedOperationException if the shader type is not available or for another related error
     */
    public abstract void compileShader(Shader shader);

    /**
     * Generates a rendering pipeline from the given shaders
     *
     * If a shader is found to not be compiled, it should be compiled automatically using compileShader()
     * @param pipeline the pipeline to create
     * @return the generated pipeline
     * @throws UnsupportedOperationException if the pipeline fails to build with the given shaders for whatever reason.
     */
    public abstract void createPipeline(Pipeline pipeline);

    /**
     * Sets the currently active pipeline to the one given for future draw commands
     * @param pipeline the pipeline to activate
     * @throws UnsupportedOperationException if the pipeline has not been compiled or is not able to be active for any reason
     */
    public abstract void setPipeline(Pipeline pipeline);

    /**
     * Uploads the specified data as a generic buffer to the graphics card.
     * @param obj the buffer to upload
     */
    public abstract void uploadVBO(VertexBufferObject obj) throws IOException;

    /**
     * Apply shader attribute to the specified VBO.
     *
     * @param name the attribute name within the shader
     * @param p the pipeline to associate
     * @param obj the VBO to associate with the given name and pipeline
     */
    public abstract void bindPipelineVBO(String name, Pipeline p, VertexBufferObject obj);


    /**
     * Apply shader uniform to pipeline based on the properties provided for future render commands.
     *
     * Implementations are expected to read the type of value and to supply the correct options based
     * on that
     *
     * @param name the variable within the shader to set for this uniform
     * @param p the pipeline to bind with. Should be currently enabled
     * @param data the value. Should be a primitive type that a normal graphics engine can recognize (for example, float).
     */
    public abstract void bindPipelineUniform(String name, Pipeline p, Object data);

    /**
     * Tell the pipeline which VBO should be treated as the vertex position
     * @param name the name of the attribute to be the vertex position
     * @param p the pipeline to associate
     */
    public abstract void enablePipelineVerticesVBO(String name, Pipeline p);


    /**
     * Unset the results of a previous call to enablePipelineVerticesVBO()
     * @param name the name of the attribute to be the vertex position
     * @param p the pipeline to associate
     */
    public abstract void disablePipelineVerticesVBO(String name, Pipeline p);

    /**
     * Sets the color to clear the render buffer to when clearBuffers() is called.
     * @param color the RGB color value to set
     */
    public abstract void setClearColor(Vector3fc color);

    /**
     * Clears the color and depth buffers
     */
    public abstract void clearBuffers();

    /**
     * Draw the specified verticies to the screen using the currently active pipeline. Currently only draws triangles.
     * @param first the point at which the renderer should start reading coordinates
     * @param count the point at which the renderer should stop reading coordinates. Use 0 to read all the way to the end
     */
    public abstract void drawVertices(int first, int count);

    public void drawVertices() {
        drawVertices(0, 0);
    }

    /**
     * Similar to draw, but draws indices of the VBO rather than from start to end
     * @param start the point at which the renderer should start reading coordinates
     * @param end the point at which the renderer should stop reading coordinates. Use 0 to read all the way to the end
     */
    public abstract void drawIndices(int start, int end);

    public void drawIndices() {
        drawIndices(0, 0);
    }
}
