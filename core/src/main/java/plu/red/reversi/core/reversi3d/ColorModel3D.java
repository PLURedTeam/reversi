package plu.red.reversi.core.reversi3d;

import org.joml.Vector4f;
import org.joml.Vector4fc;
import plu.red.reversi.core.graphics.Graphics3D;
import plu.red.reversi.core.graphics.Pipeline;
import plu.red.reversi.core.graphics.VertexBufferObject;

import java.io.IOException;

/**
 * Extends model3d by providing functionality that allows for the uploading of albedos along with face data.
 *
 * Will also select a shader which supports this type of shading automatically.
 */
public abstract class ColorModel3D extends SurfaceModel3D {

    private VertexBufferObject<Vector4fc> albedos;

    public ColorModel3D(Graphics3D g3d, Pipeline pipeline) {
        super(g3d, pipeline);

        albedos = new VertexBufferObject<>(Vector4fc.class);
    }

    public ColorModel3D(Graphics3D g3d, Pipeline pipeline, float priority) {
        super(g3d, pipeline, priority);

        albedos = new VertexBufferObject<>(Vector4fc.class);
    }

    @Override
    public Model3D clone() {

        ColorModel3D n = (ColorModel3D)super.clone();

        n.albedos = albedos;

        return n;
    }

    @Override
    public VertexBufferObject getExtra(String name) {
        if(name.equals("vAlbedo"))
            return albedos;

        return super.getExtra(name);
    }

    abstract Vector4f[] getFaceColor(int sectionIndex, int faceIndex);

    @Override
    public void recalculate(String vbo) {
        if("vAlbedo".equals(vbo)) {

            albedos.clear();

            // recalculate colors from the get face color call
            int sectionCount = getSectionCount();
            for(int sectionId = 0;sectionId < sectionCount;sectionId++) {
                int count = getFaceCount(sectionId);

                for(int i = 0;i < count;i++) {

                    Vector4f[] albs = getFaceColor(sectionId, i);

                    for(int j = 1;j < albs.length - 1;j++) {
                        albedos.add(albs[0]);
                        albedos.add(albs[j]);
                        albedos.add(albs[j + 1]);
                    }
                }
            }

            try {
                getGraphics3D().uploadVBO(albedos);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else super.recalculate(vbo);
    }
}
