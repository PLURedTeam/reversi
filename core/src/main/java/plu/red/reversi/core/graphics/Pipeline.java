package plu.red.reversi.core.graphics;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.HashMap;

public class Pipeline extends Handle {
    private HashMap<Shader.ShaderType, Shader> shaders;

    private HashMap<String, VertexBufferObject> extras;
    private HashMap<String, Handle> extraHandles;
    private HashMap<String, Object> uniforms;

    public Pipeline(PipelineDefinition def, VertexShader vs, PixelShader fs) {

        if(vs.def != def || fs.def != def)
            throw new InvalidParameterException("Vertex and pixel shader pipeline definitions must match pipeline");

        this.shaders = new HashMap<>();
        this.extras = new HashMap<>();
        this.extraHandles = new HashMap<>();
        this.uniforms = new HashMap<>();

        this.shaders.put(Shader.ShaderType.VERTEX, vs);
        this.shaders.put(Shader.ShaderType.PIXEL, fs);

        for(Shader shader : shaders.values())
            for(String name : shader.getExtras())
                extras.put(name, null);

        for(Shader shader : shaders.values())
            for(String name : shader.getUniforms())
                uniforms.put(name, null);
    }

    public Collection<Shader> getShaders() {
        return shaders.values();
    }

    public HashMap<String, VertexBufferObject> getExtras() {
        return extras;
    }

    public HashMap<String, Object> getUniforms() {
        return uniforms;
    }

    // NOTE: This call should rarely be used because of what it is for
    public void setExtra(String name, VertexBufferObject data) {
        extras.put(name, data);
    }

    public void setExtraHandle(String name, Object data) {
        extraHandles.put(name, new Handle(getGraphics3D(), data));
    }

    public Object getExtraHandle(String name) {
        return extraHandles.get(name).getHandle();
    }

    public void setUniform(String name, Object data) {
        uniforms.put(name, data);
    }
}
