package plu.red.reversi.core.graphics;

public abstract class PixelShader extends Shader {

    public final PipelineDefinition def;

    public PixelShader(PipelineDefinition d) {
        super(ShaderType.PIXEL);

        def = d;
    }
}
