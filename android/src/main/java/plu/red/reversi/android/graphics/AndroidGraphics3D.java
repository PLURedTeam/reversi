package plu.red.reversi.android.graphics;

import android.opengl.GLES30;
import android.util.Log;

import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.joml.Vector4fc;
import org.joml.Vector4ic;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.security.InvalidParameterException;
import java.util.Collection;

/**
 * Created by daniel on 3/19/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public class AndroidGraphics3D extends Graphics3D {

    private static final String TAG = AndroidGraphics3D.class.getSimpleName();

    private void checkGLError() {
        int err = GLES30.glGetError();

        if(err == GLES30.GL_NO_ERROR) {
            return;
        }
        else if(err == GLES30.GL_INVALID_ENUM) {
            throw new RuntimeException("GL Error: invalid enum");
        }
        else if(err == GLES30.GL_INVALID_VALUE) {
            throw new RuntimeException("GL Error: invalid value");
        }
        else if(err == GLES30.GL_INVALID_OPERATION) {
            throw new RuntimeException("GL Error: invalid operation");
        }
        else if(err == GLES30.GL_OUT_OF_MEMORY) {
            throw new RuntimeException("GL Error: ran out of memory");
        }
        else {
            throw new RuntimeException("GL Error: unknown code: " + err);
        }
    }


    public AndroidGraphics3D() {
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glEnable(GLES30.GL_CULL_FACE);
        //GLES30.glEnable(GLES30.GL_BLEND);
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Specifies to the rendering backend the viewport drawing dimensions
     *
     * @param x x value from the bottom left corner
     * @param y y value from the bottom left corner
     * @param w the width of the viewport
     * @param h the height of the viewport
     */
    @Override
    public void setViewport(int x, int y, int w, int h) {
        GLES30.glViewport(x, y, w, h);
    }

    /**
     * Specifies whether or not alpha blending should be enabled or not. This is useful for transparency.
     *
     * @param blend whether or not to have alpha blending
     */
    @Override
    public void setAlphaBlending(boolean blend) {
        if(blend)
            GLES30.glEnable(GLES30.GL_BLEND);
        else
            GLES30.glDisable(GLES30.GL_BLEND);
    }

    /**
     * Using the specified shader, compile the code returned by getCode() and associate it with the shader using setHandle()
     *
     * @param shader the shader to compile
     * @throws ShaderCompileException if the shader fails to compile
     * @throws UnsupportedOperationException if the shader type is not available or for another related error
     */
    @Override
    public void compileShader(Shader shader) {
        Integer handle = -1;

        switch (shader.getType()) {
            case VERTEX:
                handle = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER);
                break;
            case PIXEL:
                handle = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER);
                break;
            default:
                throw new UnsupportedOperationException("Shader type is not available in GLES");
        }

        GLES30.glShaderSource(handle, shader.getSource());
        GLES30.glCompileShader(handle);

        int[] buf = new int[1];

        GLES30.glGetShaderiv(handle, GLES30.GL_COMPILE_STATUS, buf, 0);

        if(buf[0] == GLES30.GL_FALSE) {
            // shader failed to compile, get more info
            String message = GLES30.glGetShaderInfoLog(handle);

            throw new ShaderCompileException(message);
        }

        shader.setHandle(this, handle);

        checkGLError();

        //Log.v(TAG, "Uploaded shader to id " + handle);
    }

    /**
     * Generates a rendering pipeline from the given shaders
     * <p>
     * If a shader is found to not be compiled, it should be compiled automatically using compileShader()
     *
     * @param pipeline the pipeline to create
     * @throws ShaderCompileException if opengl fails to link the shaders together
     * @throws UnsupportedOperationException if the pipeline fails to build with the given shaders for whatever reason.
     */
    @Override
    public void createPipeline(Pipeline pipeline) {
        Integer handle = GLES30.glCreateProgram();

        for(Shader shader : pipeline.getShaders()) {

            if(shader.getHandle() == null)
                compileShader(shader);

            GLES30.glAttachShader(handle, (Integer)shader.getHandle());
        }

        // before we link program, make sure opengl knows about the attributes in this shader
        /*int i = 0;
        for(String attr : pipeline.getExtras().keySet()) {
            pipeline.setExtraHandle(attr, i);
            GLES30.glBindAttribLocation(handle, i++, attr);
        }

        checkGLError();*/

        GLES30.glLinkProgram(handle);

        int[] buf = new int[10];

        GLES30.glGetProgramiv(handle, GLES30.GL_LINK_STATUS, buf, 0);

        if(buf[0] == GLES30.GL_FALSE) {
            // program failed to link, get more info
            String message = GLES30.glGetProgramInfoLog(handle);

            throw new ShaderCompileException(message);
        }

        pipeline.setHandle(this, handle);

        GLES30.glGetProgramiv(handle, GLES30.GL_ACTIVE_ATTRIBUTES, buf, 0);
        GLES30.glGetProgramiv(handle, GLES30.GL_ACTIVE_UNIFORMS, buf, 1);
        GLES30.glGetProgramiv(handle, GLES30.GL_ATTACHED_SHADERS, buf, 2);

        checkGLError();

        //Log.v(TAG, "Linked program to id " + handle + ", " + buf[0] + " active attributes detected, " + buf[1] + " active uniforms, " + buf[2] + " attached shaders.");
    }

    /**
     * Sets the currently active pipeline to the one given for future draw commands
     *
     * @param pipeline the pipeline to activate
     * @throws UnsupportedOperationException if the pipeline has not been compiled or is not able to be active for any reason
     */
    @Override
    public void setPipeline(Pipeline pipeline) {
        GLES30.glUseProgram((Integer) pipeline.getHandle());

        checkGLError();

        //Log.v(TAG, "Set current program to " + pipeline.getHandle());
    }

    /**
     * Uploads the specified data as a generic buffer to the graphics card.
     *
     * @param obj the buffer to upload
     */
    @Override
    public void uploadVBO(VertexBufferObject obj) throws IOException {

        Integer handle = (Integer)obj.getHandle();

        if(obj.getHandle() == null) {
            // there is array associated with this object
            int[] buf = new int[1];

            GLES30.glGenBuffers(1, buf, 0);

            handle = buf[0];
        }

        Buffer buf = obj.getBuffer();

        buf.rewind();

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, handle);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, obj.getDataSize() * buf.capacity(), buf, GLES30.GL_STATIC_DRAW);

        obj.setHandle(this, handle);

        checkGLError();

        //Log.v(TAG, "Uploaded VBO data to " + handle + ", " + obj.size());
    }

    /**
     * Apply shader uniform to pipeline based on the properties provided for future render commands.
     * <p>
     * Implementations are expected to read the type of value and to supply the correct options based
     * on that
     *
     * @param name the variable within the shader to set for this uniform
     * @param obj  the value. Should be a primitive type that a normal graphics engine can recognize (for example, float).
     */
    @Override
    public void bindPipelineVBO(String name, Pipeline p, VertexBufferObject obj) {
        Integer handle = (Integer)obj.getHandle();

        if(handle == null)
            throw new InvalidParameterException("Buffer for " + name + " should be initialized before binding");
        if(p.getHandle() == null)
            throw new InvalidParameterException("Pipeline for " + name + " must be initialized before binding");

        Integer attrHandle = GLES30.glGetAttribLocation((int)p.getHandle(), name);

        if(attrHandle == -1)
            return;
            // it is possible for opengl to decide to drop attributes if they are found to not be used
                // so this is a noop
            //throw new InvalidParameterException("Attribute name '" + name + "' does not seem to be valid for the current pipeline.");

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, handle);

        if(obj.getPlainType() == Float.class) {
            GLES30.glVertexAttribPointer(attrHandle, obj.getStride(), GLES30.GL_FLOAT, false, 0, 0);
        }
        else if(obj.getPlainType() == Double.class) {
            // gles does not support double buffer
            throw new UnsupportedOperationException("OpenGL ES does not support double VBOs!");
        }
        else if(obj.getPlainType() == Integer.class) {
            GLES30.glVertexAttribPointer(attrHandle, obj.getStride(), GLES30.GL_INT, false, 0, 0);
        }
        else {
            throw new UnsupportedOperationException("Simple type of VBO must not be " + obj.getPlainType());
        }

        GLES30.glEnableVertexAttribArray(attrHandle);

        checkGLError();

        //Log.v(TAG, "Bound VBO to Pipeline: " + name + " <= " + obj.getHandle() + ". Stride was " + obj.getStride());
    }

    @Override
    public void bindPipelineUniform(String name, Pipeline p, Object data) {
        if(p.getHandle() == null)
            throw new InvalidParameterException("Pipeline must be initialized before binding");

        int uniformHandle = GLES30.glGetUniformLocation((Integer) p.getHandle(), name);

        if(uniformHandle == -1)
            // noop
            return;

        if(data instanceof Float)
            GLES30.glUniform1fv(uniformHandle, 1, new float[]{(Float)data}, 0);
        else if(data instanceof Vector2fc) {
            FloatBuffer buf = FloatBuffer.allocate(2);
            ((Vector2fc) data).get(buf);
            buf.position(0);
            GLES30.glUniform2fv(uniformHandle, 1, buf);
        }
        else if(data instanceof Vector3fc) {
            FloatBuffer buf = FloatBuffer.allocate(3);
            ((Vector3fc) data).get(buf);
            buf.position(0);
            GLES30.glUniform3fv(uniformHandle, 1, buf);
        }
        else if(data instanceof Vector4fc) {
            FloatBuffer buf = FloatBuffer.allocate(4);
            ((Vector4fc) data).get(buf);
            buf.position(0);
            GLES30.glUniform4fv(uniformHandle, 1, buf);
        }
        else if(data instanceof Matrix3fc) {
            FloatBuffer buf = FloatBuffer.allocate(9);
            ((Matrix3fc) data).get(buf);
            buf.position(0);
            GLES30.glUniformMatrix3fv(uniformHandle, 1, false, buf);
        }
        else if(data instanceof Matrix4fc) {
            FloatBuffer buf = FloatBuffer.allocate(16);
            ((Matrix4fc) data).get(buf);
            buf.position(0);
            GLES30.glUniformMatrix4fv(uniformHandle, 1, false, buf);
        }
        else if(data instanceof Integer)
            GLES30.glUniform1iv(uniformHandle, 1, new int[]{(Integer)data}, 0);
        else if(data instanceof Vector2ic) {
            IntBuffer buf = IntBuffer.allocate(2);
            ((Vector2ic) data).get(buf);
            buf.position(0);
            GLES30.glUniform2iv(uniformHandle, 1, buf);
        }
        else if(data instanceof Vector3ic) {
            IntBuffer buf = IntBuffer.allocate(3);
            ((Vector3ic) data).get(buf);
            buf.position(0);
            GLES30.glUniform3iv(uniformHandle, 1, buf);
        }
        else if(data instanceof Vector4ic) {
            IntBuffer buf = IntBuffer.allocate(4);
            ((Vector4ic) data).get(buf);
            buf.position(0);
            GLES30.glUniform4iv(uniformHandle, 1, buf);
        }
        else {
            throw new UnsupportedOperationException("Type is not compatible with uniform in OpenGL ES 3.0");
        }

        checkGLError();

        //Log.v(TAG, "Bound uniform to pipeline: " + name + " <= " + data);
    }

    @Override
    public void enablePipelineVerticesVBO(String name, Pipeline p) {
        if(p.getHandle() == null)
            throw new InvalidParameterException("Pipeline must be initialized before binding");

        int attrHandle = GLES30.glGetAttribLocation((Integer) p.getHandle(), name);

        GLES30.glEnableVertexAttribArray(attrHandle);

        checkGLError();

        //Log.v(TAG, "Set pipeline vertices VBO to " + name);
    }

    @Override
    public void disablePipelineVerticesVBO(String name, Pipeline p) {
        if(p.getHandle() == null)
            throw new InvalidParameterException("Pipeline must be initialized before binding");

        int attrHandle = GLES30.glGetAttribLocation((Integer) p.getHandle(), name);

        GLES30.glDisableVertexAttribArray(attrHandle);

        checkGLError();

        //Log.v(TAG, "Unset pipeline vertices VBO from " + name);
    }

    /**
     * Sets the color to clear the render buffer to when clearBuffers() is called.
     *
     * @param color the RGB color value to set
     */
    @Override
    public void setClearColor(Vector3fc color) {
        GLES30.glClearColor(color.x(), color.y(), color.z(), 1.0f);

        checkGLError();

        //Log.v(TAG, "Set clear color to " + color);
    }

    /**
     * Clears the color and depth buffers
     */
    @Override
    public void clearBuffers() {
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT | GLES30.GL_COLOR_BUFFER_BIT);

        //Log.v(TAG, "Cleared GL buffers");
    }

    /**
     * Draw the specified verticies to the screen using the currently active pipeline. Currently only draws triangles.
     *
     * @param first the pointat which the renderer should start reading coordinates
     * @param count the point at which the renderer should stop reading coordinates. Use 0 to read all the way to the end
     */
    @Override
    public void drawVertices(int first, int count) {

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, first, count);

        checkGLError();

        //Log.v(TAG, "Draw " + count + " vertices, offset " + first);
    }

    /**
     * Similar to draw, but draws indices of the VBO rather than from start to end
     *
     * @param start the point at which the renderer should start reading coordinates
     * @param end   the point at which the renderer should stop reading coordinates. Use 0 to read all the way to the end
     */
    @Override
    public void drawIndices(int start, int end) {
        Log.v(TAG, "Draw indices (not implemented!)");
    }
}
