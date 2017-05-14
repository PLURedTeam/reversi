package plu.red.reversi.core.reversi3d;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import plu.red.reversi.core.graphics.Graphics3D;
import plu.red.reversi.core.graphics.Pipeline;
import plu.red.reversi.core.graphics.VertexBufferObject;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by daniel on 5/12/17.
 */
public class ObjModel3D extends Model3D {

    private File origFile;
    private VertexBufferObject<Vector4fc> vertices;
    private VertexBufferObject<Vector3fc> normals;

    public ObjModel3D(Graphics3D g3d, Pipeline pipeline, File file) throws IOException {
        this(g3d, pipeline, new FileInputStream(file));
    }

    public ObjModel3D(Graphics3D g3d, Pipeline pipeline, InputStream stream) throws IOException {
        super(g3d, pipeline);

        // go ahead and load the model
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(" ");

            ArrayList<Vector4fc> verts = new ArrayList<>();
            ArrayList<Vector3fc> norms = new ArrayList<>();

            if(parts[0].equals("v")) {
                verts.add(new Vector4f(
                        Float.parseFloat(parts[1]),
                        Float.parseFloat(parts[2]),
                        Float.parseFloat(parts[3]),
                        1
                ));
            }
            else if(parts[0].equals("n")) {
                norms.add(new Vector3f(
                        Float.parseFloat(parts[1]),
                        Float.parseFloat(parts[2]),
                        Float.parseFloat(parts[3])
                ));
            }
            else if(parts[0].equals("f")) {

                String[] root = parts[1].split("/");

                for(int i = 1;i < parts.length - 2;i++) {
                    String[] p1 = parts[i].split("/");
                    String[] p2 = parts[i + 1].split("/");

                    vertices.add(verts.get(Integer.parseInt(root[0])));
                    vertices.add(verts.get(Integer.parseInt(p1[0])));
                    vertices.add(verts.get(Integer.parseInt(p2[0])));
                    normals.add(norms.get(Integer.parseInt(root[2])));
                    normals.add(norms.get(Integer.parseInt(p1[2])));
                    normals.add(norms.get(Integer.parseInt(p2[2])));
                }
            }
        }

        getGraphics3D().uploadVBO(vertices);
        getGraphics3D().uploadVBO(normals);
    }

    private ObjModel3D(Graphics3D g3d, Pipeline pipeline) {
        super(g3d, pipeline);
    }

    @Override
    Model3D newInstance() {
        return null;
    }

    @Override
    public VertexBufferObject getExtra(String name) {
        if(name.equals("vertices"))
            return vertices;
        else if(name.equals("normals"))
            return normals;

        return null;
    }

    @Override
    public Model3D clone() {
        ObjModel3D n = new ObjModel3D(getGraphics3D(), getPipeline());

        n.vertices = vertices;
        n.normals = normals;

        return n;
    }
}
