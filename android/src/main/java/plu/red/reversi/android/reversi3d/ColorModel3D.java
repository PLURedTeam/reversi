package plu.red.reversi.android.reversi3d;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.io.IOException;
import java.util.List;

import plu.red.reversi.android.graphics.Graphics3D;
import plu.red.reversi.android.graphics.Pipeline;
import plu.red.reversi.android.graphics.VertexBufferObject;

/**
 * Created by daniel on 3/20/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public abstract class ColorModel3D extends Model3D {

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
    public void recalculate(int sectionId) {
        if(sectionId != -1) {
            // recalculate colors from the get face color call
            int count = getFaceCount(sectionId);

            for(int i = 0;i < count;i++) {

                Vector4f[] albs = getFaceColor(sectionId, i);

                for(int j = 1;j < albs.length - 1;j++) {
                    albedos.add(albs[0]);
                    albedos.add(albs[j]);
                    albedos.add(albs[j + 1]);
                }
            }

            try {
                getGraphics3D().uploadVBO(albedos);
            } catch(IOException e) {
                // TODO: better error handling.... this is not very appropriate at all
                System.err.println("Failed to upload vertex data for object: ");
                e.printStackTrace();

            }
        }
        else albedos.clear();

        super.recalculate(sectionId);

        if(this instanceof Piece3D) {
            System.out.println("Color size: " + albedos.size());
        }
    }

    @Override
    public void uploadBuffers() throws IOException {
        super.uploadBuffers();

        getGraphics3D().uploadVBO(albedos);
    }
}
