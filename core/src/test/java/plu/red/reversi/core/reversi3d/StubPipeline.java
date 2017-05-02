package plu.red.reversi.core.reversi3d;

import plu.red.reversi.core.graphics.Pipeline;
import plu.red.reversi.core.graphics.PipelineDefinition;
import plu.red.reversi.core.graphics.PixelShader;
import plu.red.reversi.core.graphics.VertexShader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 4/21/17.
 */
public class StubPipeline extends Pipeline {

    private final static PipelineDefinition definition = new PipelineDefinition();

    public StubPipeline() {
        super(definition, new VertexShader(definition) {
            @Override
            public String getSource() {
                return "";
            }

            @Override
            public List<String> getExtras() {
                return new ArrayList<>();
            }

            @Override
            public List<String> getUniforms() {
                return new ArrayList<>();
            }
        }, new PixelShader(definition) {
            @Override
            public String getSource() {
                return "";
            }

            @Override
            public List<String> getExtras() {
                List<String> extras = new ArrayList<>();

                // add normal properties which should be expected
                extras.add("vPosition");
                extras.add("vNormal");
                //extras.add("vAlbedo");

                return extras;
            }

            @Override
            public List<String> getUniforms() {

                List<String> uniforms = new ArrayList<>();

                // add normal properties which should be expected
                uniforms.add("modelMatrix");
                uniforms.add("viewMatrix");
                uniforms.add("projectionMatrix");

                return uniforms;
            }
        });

    }
}
