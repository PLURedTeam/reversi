package plu.red.reversi.core.graphics;

/**
 * Created by daniel on 3/20/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public class PipelineDefinition {
    public int pointLightCount;
    public int directionalLightCount;

    public boolean usesSampler;

    public PipelineDefinition() {
        pointLightCount = 0;
        directionalLightCount = 0;

        usesSampler = false;
    }

    public PipelineDefinition(PipelineDefinition other) {
        pointLightCount = other.pointLightCount;
        directionalLightCount = other.directionalLightCount;

        usesSampler = other.usesSampler;
    }
}
