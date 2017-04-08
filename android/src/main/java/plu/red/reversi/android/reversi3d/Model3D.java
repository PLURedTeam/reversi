package plu.red.reversi.android.reversi3d;

import android.support.annotation.CallSuper;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import plu.red.reversi.android.graphics.Shape;
import plu.red.reversi.android.graphics.Graphics3D;
import plu.red.reversi.android.graphics.Pipeline;
import plu.red.reversi.android.graphics.VertexBufferObject;

/**
 * Created by daniel on 3/20/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public abstract class Model3D extends Shape {

    // IDs are only required here because of the fact that sortedset requires completely unique objects.
    // this makes sure of that.
    private static int nextID = 0;

    private int id;

    private SortedSet<Model3D> children;

    private Vector3f pos;
    private Vector3f scale;
    private Quaternionf rot;

    private Vector3f offsetPos;
    private Vector3f offsetScale;
    private Quaternionf offsetRot;

    // temporary vars only!
    private Vector3f worldPos;
    private Vector3f worldScale;
    private Quaternionf worldRot;

    protected boolean alphaBlended;

    private Model3D parent;

    private int lastTick;


    public VertexBufferObject<Vector4fc> vertices;
    public VertexBufferObject<Vector3fc> normals;

    private final Graphics3D g3d;
    private final Pipeline pipeline;

    private float priority = 1.0f;

    public Model3D(Graphics3D g3d, Pipeline pipeline) {

        id = nextID++;

        vertices = new VertexBufferObject<>(Vector4fc.class);
        normals = new VertexBufferObject<>(Vector3fc.class);

        alphaBlended = false;

        children = new TreeSet<>(new Model3DPriorityComparator());

        this.g3d = g3d;
        this.pipeline = pipeline;

        pos = new Vector3f();
        rot = new Quaternionf();
        scale = new Vector3f(1);

        offsetPos = new Vector3f();
        offsetRot = new Quaternionf();
        offsetScale = new Vector3f(1);

        worldPos = new Vector3f();
        worldRot = new Quaternionf();
        worldScale = new Vector3f(1);
    }

    public Model3D(Graphics3D g3d, Pipeline pipeline, float priority) {

        this(g3d, pipeline);

        this.priority = priority;
    }

    /**
     * Make a clone of this object, meaning that VBO data will be shared between all instances.
     * For example, in the reversi game, this method is useful for making many pieces, which all have
     * rigid surfaces. Obviously this is much more efficient than having 64 VBO sets.
     *
     * This method needs to be extended in subclasses
     *
     * @return a new model3d instance with the shared properties
     */
    @CallSuper
    public Model3D clone() {

        Model3D n = newInstance();

        n.vertices = vertices;
        n.normals = normals;

        return n;
    }

    abstract Model3D newInstance();

    public HashMap<String, VertexBufferObject> extras;

    abstract int getSectionCount();
    abstract int getFaceCount(int sectionIndex);

    abstract Vector3f[] getFace(int sectionIndex, int faceIndex);

    @CallSuper
    public void recalculate(int sectionId) {

        if(sectionId == -1) {
            // get all the points for the curve added, and begin preparing a map of points to normals
            int sectionCount = getSectionCount();

            vertices.clear();
            normals.clear();

            for(int i = 0;i < sectionCount;i++)
                recalculate(i);

            try {
                uploadBuffers();

                /*if(this instanceof Piece3D) {
                    System.out.println("Vertices: ");
                    for(Vector4fc pos : vertices)
                        System.out.println(pos);
                    System.out.println("Normals: ");
                    for(Vector3fc norm : normals)
                        System.out.println(norm);

                    System.out.println("Pos size: " + vertices.size());
                    System.out.println("Norm size: " + normals.size());
                }*/

            } catch(IOException e) {
                // TODO: Better error handling here.
                e.printStackTrace();
            }
        }

        else {

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
                face[face.length - 1].sub(face[0], tmp);

                norm.cross(tmp).normalize();

                if(Float.isNaN(norm.length())) {
                    Vector3f tmp2 = new Vector3f();
                    Vector3f tmp3 = new Vector3f();
                    face[1].sub(face[0], tmp2);
                    face[face.length - 1].sub(face[0], tmp3);
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
                    System.out.println("Could not find preexisting vertex for normal!!!");
                }

                if(n.length() == 0) {
                    System.out.println("Have a 0 length vector!");
                }

                if(Float.isNaN(n.length())) {
                    System.out.println("NaN vector!");
                }

                normals.add(new Vector3f(n).normalize());
            }
        }
    }

    @CallSuper
    protected void uploadBuffers() throws IOException {
        g3d.uploadVBO(vertices);
        g3d.uploadVBO(normals);
    }

    @CallSuper
    public boolean update(int tick) {

        lastTick = tick;

        boolean updated = false;

        for(Model3D child : children)
            updated = child.update(tick) || updated;

        return updated;
    }

    public final void draw() {

        //g3d.setPipeline(pipeline);

        g3d.setAlphaBlending(alphaBlended);

        if(vertices.isEmpty() && getSectionCount() > 0)
            // vertex data should be generated
            recalculate(-1);

        //g3d.enablePipelineVerticesVBO("position", getPipeline());

        HashMap<String, VertexBufferObject> vbos = pipeline.getExtras();
        for(String vbo : vbos.keySet()) {
            if(vbos.get(vbo) == null)
                g3d.bindPipelineVBO(vbo, pipeline, getExtra(vbo));
        }

        HashMap<String, Object> uniforms = pipeline.getUniforms();
        for(String uniform : uniforms.keySet()) {
            if(uniforms.get(uniform) == null) {
                Object o = getUniform(uniform);
                if(o != null)
                    g3d.bindPipelineUniform(uniform, pipeline, o);
            }
        }

        // now we have bound all the data for this object. Commence draw!
        g3d.drawVertices(0, vertices.size());

        //g3d.disablePipelineVerticesVBO("position", getPipeline());

        for(Model3D child : children)
            child.draw();
    }

    public void addChild(Model3D child) {
        if(!children.add(child)) {
            //System.out.println("Added duplicate object as child--use clone()!");
            return;
        }

        // these shared references are very helpful here
        child.offsetPos = worldPos;
        child.offsetRot = worldRot;
        child.offsetScale = worldScale;
    }

    public void removeChild(Model3D child) {
        children.remove(child);

        // ensure no associations
        child.offsetPos = new Vector3f();
        child.offsetRot = new Quaternionf();
        child.offsetScale = new Vector3f();
    }

    public Vector3f getWorldPosition() {
        return worldPos;
    }

    public Quaternionf getWorldRotation() {
        return worldRot;
    }

    public Vector3f getWorldScale() {
        return worldScale;
    }

    public Vector3f getPos() {
        return pos;
    }

    public void setPos(Vector3f pos) {
        this.pos = new Vector3f(pos);

        posChanged();
    }

    private void posChanged() {
        offsetPos.add(pos, worldPos);

        for(Model3D child : children)
            child.posChanged();
    }

    public Vector3f getScale() {
        return scale;
    }

    public void setScale(Vector3f scale) {
        this.scale = new Vector3f(scale);

        scaleChanged();
    }

    private void scaleChanged() {
        offsetScale.mul(scale, worldScale);

        for(Model3D child : children)
            child.scaleChanged();
    }

    public Quaternionf getRot() {
        return rot;
    }

    public void setRot(Quaternionf rot) {
        this.rot = new Quaternionf(rot);

        rotChanged();
    }

    private void rotChanged() {
        // NOTE: Order matters here!
        //rot.mul(offsetRot, worldRot);

        for(Model3D child : children)
            child.rotChanged();
    }

    @CallSuper
    public Object getUniform(String name) {
        switch(name) {
            case "modelMatrix":
                Matrix4f matrix = new Matrix4f();

                matrix
                        // We have two rotates here because I do not know quaternion math and someone needs to help me :)
                        //.rotate(offsetRot)
                        .translate(getWorldPosition())
                        .scale(getWorldScale())
                        .rotate(rot)
                        ;

                return matrix;
        }

        return null;
    }

    @CallSuper
    public VertexBufferObject getExtra(String name) {
        switch(name) {
            case "vPosition":
                return vertices;
            case "vNormal":
                return normals;
        }

        return null;
    }

    protected Pipeline getPipeline() {
        return pipeline;
    }

    protected Graphics3D getGraphics3D() {
        return g3d;
    }

    protected int getLastTick() {
        return lastTick;
    }

    @Override
    public boolean equals(Object other) {
        return this == other;
    }

    private static class Model3DPriorityComparator implements Comparator<Model3D> {

        /**
         * Compares its two arguments for order.  Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.<p>
         * <p>
         * In the foregoing description, the notation
         * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
         * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
         * <tt>0</tt>, or <tt>1</tt> according to whether the value of
         * <i>expression</i> is negative, zero or positive.<p>
         * <p>
         * The implementor must ensure that <tt>sgn(compare(x, y)) ==
         * -sgn(compare(y, x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
         * implies that <tt>compare(x, y)</tt> must throw an exception if and only
         * if <tt>compare(y, x)</tt> throws an exception.)<p>
         * <p>
         * The implementor must also ensure that the relation is transitive:
         * <tt>((compare(x, y)&gt;0) &amp;&amp; (compare(y, z)&gt;0))</tt> implies
         * <tt>compare(x, z)&gt;0</tt>.<p>
         * <p>
         * Finally, the implementor must ensure that <tt>compare(x, y)==0</tt>
         * implies that <tt>sgn(compare(x, z))==sgn(compare(y, z))</tt> for all
         * <tt>z</tt>.<p>
         * <p>
         * It is generally the case, but <i>not</i> strictly required that
         * <tt>(compare(x, y)==0) == (x.equals(y))</tt>.  Generally speaking,
         * any comparator that violates this condition should clearly indicate
         * this fact.  The recommended language is "Note: this comparator
         * imposes orderings that are inconsistent with equals."
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive integer as the
         * first argument is less than, equal to, or greater than the
         * second.
         * @throws NullPointerException if an argument is null and this
         *                              comparator does not permit null arguments
         * @throws ClassCastException   if the arguments' types prevent them from
         *                              being compared by this comparator.
         */
        @Override
        public int compare(Model3D o1, Model3D o2) {
            if(o2.priority != o1.priority) {
                return o2.priority > o1.priority ? 1 : -1;
            }
            else {
                return o2.id - o1.id;
            }
        }
    }
}
