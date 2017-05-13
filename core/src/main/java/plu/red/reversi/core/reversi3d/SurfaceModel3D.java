package plu.red.reversi.core.reversi3d;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import plu.red.reversi.core.graphics.Graphics3D;
import plu.red.reversi.core.graphics.Pipeline;
import plu.red.reversi.core.graphics.VertexBufferObject;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by daniel on 5/12/17.
 */
public abstract class SurfaceModel3D extends Model3D {
    public VertexBufferObject<Vector4fc> vertices; /// the vertex buffer object containing all of the vertices for the current model
    public VertexBufferObject<Vector3fc> normals;  /// the vertex buffer object containing all of the normal vectors for the current model. This VBO is auto-generated

    public SurfaceModel3D(Graphics3D g3d, Pipeline pipeline) {
        super(g3d, pipeline);

        vertices = new VertexBufferObject<>(Vector4fc.class);
        normals = new VertexBufferObject<>(Vector3fc.class);
    }

    public SurfaceModel3D(Graphics3D g3d, Pipeline pipeline, float priority) {
        super(g3d, pipeline, priority);

        vertices = new VertexBufferObject<>(Vector4fc.class);
        normals = new VertexBufferObject<>(Vector3fc.class);
    }

    @Override
    public Model3D clone() {
        SurfaceModel3D m3d = (SurfaceModel3D)super.clone();

        m3d.vertices = vertices;
        m3d.normals = normals;

        return m3d;
    }

    @Override
    public boolean update(int tick) {
        boolean updated = false;
        if(vertices.size() == 0 || normals.size() == 0) {
            recalculate(null);
            updated = true;
        }

        return super.update(tick) || updated;
    }

    /**
     *
     * Using getFace() abstract method, calculates a set of normals and vertices for the current object. Uploads
     * the VBOs upon completion.
     *
     * @param vbo the buffer object to recalculate, as specified by a name in the shader.
     */
    public void recalculate(String vbo) {

        if(vbo == null) {

            for(String v : getPipeline().getExtras().keySet()) {
                recalculate(v);
            }
        }

        else if(vbo.equals("vPosition")) {

            int sectionCount = getSectionCount();

            for (int sectionId = 0; sectionId < sectionCount; sectionId++) {
                calculateVertexSection(sectionId);
            }

            try {
                getGraphics3D().uploadVBO(vertices);
                getGraphics3D().uploadVBO(normals);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        else if(vbo.equals("vNormal")) {} // do nothing since normals are generated earlier

        else {
            System.err.println("Unknown VBO type not handled by object: " + vbo);
        }
    }

    /**
     * Calculates a vertex face (AKA a surface with no sharp edges) by calling delegated methods.
     * @param sectionId the vertex face id to update
     */
    private void calculateVertexSection(int sectionId) {
        Vector3f norm = new Vector3f();
        Vector3f tmp = new Vector3f();

        // now that we have all the points, we are going to have to add the normals by using those mapped triangles and calculating their adjacent normals.
        TreeMap<Vector3fc, Vector3f> vertexFaces = new TreeMap<>(new Comparator<Vector3fc>() {
            @Override
            public int compare(Vector3fc v1, Vector3fc v2) {
                if(v1.distance(v2) < 1e-4) {
                    return 0;
                }

                if(Math.abs(v2.x() - v1.x()) > 1e-4)
                    return v2.x() - v1.x() > 0 ? 1 : -1;

                if(Math.abs(v2.y() - v1.y()) > 1e-4)
                    return v2.y() - v1.y() > 0 ? 1 : -1;

                return v2.z() - v1.z() > 0 ? 1 : -1;
            }
        });
        List<Vector3fc> verts = new LinkedList<>();

        int count = getFaceCount(sectionId);

        for(int i = 0;i < count;i++) {

            Vector3f[] face = getFace(sectionId, i);

            face[1].sub(face[0], norm);
            face[2].sub(face[0], tmp);

            norm.cross(tmp).normalize();

            if(Float.isNaN(norm.length())) {
                Vector3f tmp2 = new Vector3f();
                Vector3f tmp3 = new Vector3f();
                face[1].sub(face[0], tmp2);
                face[2].sub(face[0], tmp3);
                System.out.println("Got NaN from Cross: " + tmp2 + ", " + tmp3);
                for(int j = 0;j < face.length;j++)
                    System.out.println("FV: " + face[j]);
            }

            for(int j = 1;j < face.length - 1;j++) {
                vertices.add(new Vector4f(face[0], 1));
                verts.add(face[0]);
                vertices.add(new Vector4f(face[j], 1));
                verts.add(face[j]);
                vertices.add(new Vector4f(face[j + 1], 1));
                verts.add(face[j + 1]);
            }

            for(Vector3fc v : face) {
                if(!vertexFaces.containsKey(v)) {
                    vertexFaces.put(v, new Vector3f());
                }

                vertexFaces.get(v).add(norm);
            }
        }

        for(Vector3fc v : verts) {
            Vector3f n = vertexFaces.get(v);

            if(n == null) {
                // something went wrong
                System.out.println("Could not getRep preexisting vertex for normal!!!");
            }

            if(n.length() == 0) {
                System.out.println("Have a 0 length vector!");
            }

            if(Float.isNaN(n.length())) {
                System.out.println("NaN vector!");
            }

            //System.out.println("Add normal: " + new Vector3f(n).normalize());
            //System.out.println("For vertex: " + v);

            normals.add(new Vector3f(n).normalize());
        }
    }

    @Override
    public VertexBufferObject getExtra(String name) {
        if(name.equals("vPosition"))
            return vertices;
        if(name.equals("vNormal"))
            return normals;

        return null;
    }

    abstract int getSectionCount();
    abstract int getFaceCount(int sectionIndex);

    abstract Vector3f[] getFace(int sectionIndex, int faceIndex);
}
