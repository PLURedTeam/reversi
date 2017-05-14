package plu.red.reversi.core.graphics;

import java.util.List;

public abstract class Shader extends Handle {

    private ShaderType type;

    public Shader(ShaderType type) {
        this.type = type;
    }

    public ShaderType getType() {
        return type;
    }

    public abstract String getSource();

    public abstract List<String> getExtras();
    public abstract List<String> getUniforms();

    public enum ShaderType {
        VERTEX,
        PIXEL,
        GEOMETRY,
        COMPUTE;
    }

    public class ShaderProperties {
    }
}
