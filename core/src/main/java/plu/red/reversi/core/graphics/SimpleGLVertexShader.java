package plu.red.reversi.core.graphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 3/19/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public class SimpleGLVertexShader extends VertexShader {

    public SimpleGLVertexShader(PipelineDefinition d) {
        super(d);
    }

    @Override
    public String getSource() {
        String s = "";

        if(def.isES)
            s += "#version 300 es\n";
        else
            s += "#version 330\n";

        s +=
                "precision mediump float;\n" +
                "uniform mat4 modelMatrix;\n" +
                "uniform mat4 viewMatrix;\n" +
                "uniform mat4 projectionMatrix;\n" +
                "\n" +
                "in vec4 vPosition;\n" +
                "in vec3 vNormal;\n" +
                "in vec4 vAlbedo;\n" +
                "\n" +
                "out vec3 fNormal;\n" +
                "out vec3 fPosition;\n" +
                "out vec4 fAlbedo;\n" +
                "\n" +
                "void main()\n" +
                "{\n" +
                "  fNormal = normalize((transpose(inverse(modelMatrix)) * vec4(vNormal, 0)).xyz);\n" +
                "  vec4 pos = modelMatrix * vPosition;\n" +
                "  fPosition = pos.xyz;\n" +
                "  fAlbedo = vAlbedo;\n" +
                "  gl_Position = projectionMatrix * viewMatrix * modelMatrix * vPosition;\n" +
                "}";

        return s;
    }

    @Override
    public List<String> getExtras() {
        List<String> extras = new ArrayList<>();

        extras.add("vPosition");
        extras.add("vNormal");
        extras.add("vAlbedo");

        return extras;
    }

    @Override
    public List<String> getUniforms() {
        List<String> uniforms = new ArrayList<>();

        uniforms.add("modelMatrix");
        uniforms.add("viewMatrix");
        uniforms.add("projectionMatrix");

        return uniforms;
    }
}
