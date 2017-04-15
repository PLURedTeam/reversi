package plu.red.reversi.core.graphics;

/**
 * Created by daniel on 3/19/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public abstract class VertexShader extends Shader {

    public final PipelineDefinition def;

    public VertexShader(PipelineDefinition d) {
        super(ShaderType.VERTEX);

        def = d;
    }
}
