package plu.red.reversi.core.graphics;

public class PipelineDefinition {

    public boolean isES;

    public int pointLightCount;
    public int directionalLightCount;

    public boolean usesSampler;

    public PipelineDefinition() {

        isES = false;

        pointLightCount = 0;
        directionalLightCount = 0;

        usesSampler = false;
    }

    public PipelineDefinition(PipelineDefinition other) {

        isES = other.isES;

        pointLightCount = other.pointLightCount;
        directionalLightCount = other.directionalLightCount;

        usesSampler = other.usesSampler;
    }
}
