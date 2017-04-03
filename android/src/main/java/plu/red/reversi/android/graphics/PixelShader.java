package plu.red.reversi.android.graphics;

/**
 * Created by daniel on 3/19/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public abstract class PixelShader extends Shader {

    public final PipelineDefinition def;

    public PixelShader(PipelineDefinition d) {
        super(ShaderType.PIXEL);

        def = d;
    }
}
