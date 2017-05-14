package plu.red.reversi.core.graphics;

public abstract class VertexShader extends Shader {

    public final PipelineDefinition def;

    public VertexShader(PipelineDefinition d) {
        super(ShaderType.VERTEX);

        def = d;
    }
}
