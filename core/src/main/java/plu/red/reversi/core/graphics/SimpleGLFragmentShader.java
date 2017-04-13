package plu.red.reversi.core.graphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 3/19/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public class SimpleGLFragmentShader extends PixelShader {

    public SimpleGLFragmentShader(PipelineDefinition d) {
        super(d);
    }

    @Override
    public String getSource() {
        String s = "";

        s +=    "#version 300 es\n" +
                "precision mediump float;\n" +
                "const float screenGamma = 2.2;" +
                "\n" +
                "in vec3 fPosition;\n" +
                "in vec3 fNormal;\n" +
                "in vec4 fAlbedo;\n" +
                "in float fAmbient;\n";

        if(def.pointLightCount > 0)
            s += "uniform vec4 fPointLights[" + def.pointLightCount + "];\n" +
                    "uniform vec4 fPointLightColors[" + def.pointLightCount + "];\n";

        if(def.directionalLightCount > 0)
            s += "uniform vec3 fDirectionalLights[" + def.directionalLightCount + "];\n" +
                    "uniform vec3 fDirectionalLightColors[" + def.directionalLightCount + "];\n";

        s +=
                "\n" +
                "out vec4 oColor;\n" +
                "\n" +
                "void main()\n" +
                "{\n" +
                        "  vec3 normal = normalize(fNormal);\n" +
                        "  vec3 diffuse = vec3(0);\n" +
                        "  vec3 specular = vec3(0);\n";

        if(def.directionalLightCount > 0) {
            s +=
                    "  for(int i = 0;i < " + def.directionalLightCount + ";i++) {\n" +
                            "    if(length(fDirectionalLightColors[i]) == 0.0)\n" +
                            "      diffuse += vec3(max(dot(fDirectionalLights[i], fNormal), 0.0));\n" +
                            "    else\n" +
                            "      diffuse += max(dot(fDirectionalLights[i], fNormal), 0.0) * fDirectionalLightColors[i];\n" +
                            "  }\n";

            // TODO: Add shine for each of the directional lights
        }

        if(def.pointLightCount > 0) {
            s +=
                    "  for(int i = 0;i < " + def.pointLightCount + ";i++) {\n" +
                            "    vec3 lightDir = normalize(fPointLights[i] - fPosition);" +
                            "    if(length(fPointLightColors[i]) == 0.0)\n" +
                            "      specular += max(dot(lightDir, fNormal), 0.0);\n" +
                            "    else\n" +
                            "      specular += max(dot(lightDir, fNormal), 0.0) * fPointLightColors[i];\n" +
                            "  }\n";
            // TODO: Add shine for each of these point lights as well.
        }

        s +=
                "  vec4 gammaCorrected = pow(vec4(diffuse, 1.0) * fAlbedo, vec4(1.0 / screenGamma, 1.0 / screenGamma, 1.0 / screenGamma, 1.0));" +
                        "  gammaCorrected.w = fAlbedo.w;" +
                        "  oColor = gammaCorrected;\n" +
                        "}";

        return s;
    }

    @Override
    public List<String> getExtras() {
        return new ArrayList<>();
    }

    @Override
    public List<String> getUniforms() {
        List<String> uniforms = new ArrayList<>();

        uniforms.add("fAmbient");

        for(int i = 0;i < def.pointLightCount;i++) {
            uniforms.add("fPointLights[" + i + "]");
        }

        for(int i = 0;i < def.directionalLightCount;i++) {
            uniforms.add("fDirectionalLights[" + i + "]");
        }

        return uniforms;
    }
}
