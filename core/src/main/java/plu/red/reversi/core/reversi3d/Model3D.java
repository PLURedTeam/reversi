package plu.red.reversi.core.reversi3d;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import plu.red.reversi.core.graphics.Graphics3D;
import plu.red.reversi.core.graphics.Pipeline;
import plu.red.reversi.core.graphics.VertexBufferObject;

import java.security.InvalidParameterException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class Model3D {

    // IDs are only required here because of the fact that sortedset requires completely unique objects.
    // this makes sure of that.
    private static int nextID = 0;

    private int id; /// the id for the current object instance, which is ONLY used to distinguish between priorities in the TreeMap.

    private SortedSet<Model3D> children; /// the set of children for this object, sorted by priority

    private Model3D parent; /// the parent model of this object, or null if the object does not  have a parent assigned.

    private Vector3f pos; /// the current position of this object (relative to parent coordinate space)
    private Vector3f scale; /// the current scale of this object (relative to the parent coordinate space)
    private Quaternionf rot; /// the current rotation of this object (relative to the parent coordinate space)

    private Vector3f worldPos; /// the actual position of the model in world coordinates
    private Vector3f worldScale; /// the actual scale of the model in world coordinates
    private Quaternionf worldRot; /// the actual rotation of the model in world coordinates

    protected boolean alphaBlended; /// whether or not this object should support transparency. NOTE: this may soon be deprecated because it is silly since alpha is already supported.

    private int lastTick; /// keeps track of the last value for 'tick' which was supplied to update()

    private final Graphics3D g3d;    /// the graphics renderer used by this model (should be the same for a whole tree)
    private final Pipeline pipeline; /// the rendering pipeline used by this model

    private float priority = 1.0f; /// the priority of this child to be rendered in front of other children. 1 means defualt priority.

    public Model3D(Graphics3D g3d, Pipeline pipeline) {

        id = nextID++;

        alphaBlended = false;

        children = new TreeSet<>(new Model3DPriorityComparator());

        this.g3d = g3d;
        this.pipeline = pipeline;

        pos = new Vector3f();
        rot = new Quaternionf();
        scale = new Vector3f(1);

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
    // THIS SHOULD CALL SUPER
    public Model3D clone() {
        Model3D n = newInstance();

        return n;
    }

    abstract Model3D newInstance();

    /**
     * Should be called 60 times per second to allow object to perform changes to itself/animations.
     * @param tick the current "call count" in a sense. Starts from 0, increments 60 times per second, once per call.
     * @return whether or not something about the object or its children has been modified that requires a rerender.
     */
    // THIS SHOULD CALL SUPER
    public boolean update(int tick) {

        lastTick = tick;

        boolean updated = false;

        for(Model3D child : children)
            updated = child.update(tick) || updated;

        return updated;
    }

    /**
     * Draws this model to the screen.
     *
     * Calls delegated methods 'getExtra()' and 'getUniform()' to specify vertex rendering data. So put your data there
     * and make override to specify rendering data.
     *
     * This method is deliberately not supposed to be overridden because it uses delegate methods instead.
     */
    public final void draw() {

        //g3d.setPipeline(pipeline);
        g3d.setAlphaBlending(alphaBlended);
        //g3d.enablePipelineVerticesVBO("position", getPipeline());

        int size = 0;

        HashMap<String, VertexBufferObject> vbos = pipeline.getExtras();
        for(String vbo : vbos.keySet()) {
            if(vbos.get(vbo) == null) {
                VertexBufferObject extra = getExtra(vbo);
                try {
                    g3d.bindPipelineVBO(vbo, pipeline, extra);
                    size = Math.max(size, extra.size());
                } catch(InvalidParameterException e) {
                    // TODO: for right now ignore
                    System.err.println("WARNING: Buffer was not uploaded to graphics card before draw");
                }
            }
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
        g3d.drawVertices(0, size);

        //g3d.disablePipelineVerticesVBO("position", getPipeline());

        for(Model3D child : children)
            child.draw();
    }

    /**
     * Sets the specified model to be a child of this model.
     *
     * If the specified model is already a child of another model, it will be removed from that model.
     *
     * @param child the child to add to this model.
     */
    public void addChild(Model3D child) {

        if(child.parent != null) {
            child.parent.removeChild(child);
        }

        if(!children.add(child)) {
            //System.out.println("Added duplicate object as child--use clone()!");
            return;
        }

        child.parent = this;

        child.posChanged();
        child.scaleChanged();
        child.rotChanged();
    }

    public void removeChild(Model3D child) {
        children.remove(child);
        child.parent = null;

        child.posChanged();
        child.scaleChanged();
        child.rotChanged();
    }

    /**
     * Gets the current parent of this model.
     * @return the parent model
     */
    public Model3D getParent() {
        return parent;
    }

    /**
     * Returns whether or not the provided model is in fact a direct child of this object.
     * @param model the model to check
     * @return true if the model is a direct child of this model; false otherwise.
     */
    public boolean isChild(Model3D model) {
        return children.contains(model);
    }

    /**
     * Returns the current world position of the model
     * @return the position in world coordinates
     */
    public Vector3f getWorldPosition() {
        return worldPos;
    }

    /**
     * Returns the current world rotation of the model
     *
     * @return the rotation in world coordinates
     */
    public Quaternionf getWorldRotation() {
        return worldRot;
    }

    /**
     * Returns the current world scale of the model
     *
     * @return the scale in world coordinates
     */
    public Vector3f getWorldScale() {
        return worldScale;
    }

    /**
     * Returns the current object position relative to its parent
     * @return the position relative to the parent.
     */
    public Vector3f getPos() {
        return pos;
    }

    /**
     * Sets the current object position relative to the parent
     * @param pos the position of the object relative to parent
     */
    public void setPos(Vector3f pos) {
        this.pos = new Vector3f(pos);

        posChanged();
    }

    private void posChanged() {
        if(parent != null)
            parent.worldPos.add(pos, worldPos);
        else
            worldPos = new Vector3f(pos);

        for(Model3D child : children)
            child.posChanged();
    }

    /**
     * Returns the current object scale relative to its parent
     * @return the scale relative to the parent.
     */
    public Vector3f getScale() {
        return scale;
    }

    /**
     * Sets the current object scale relative to the parent
     * @param scale the scale of the object relative to parent
     */
    public void setScale(Vector3f scale) {
        this.scale = new Vector3f(scale);

        scaleChanged();
    }

    private void scaleChanged() {
        if(parent != null)
            parent.worldScale.mul(scale, worldScale);
        else
            worldScale = new Vector3f(scale);

        for(Model3D child : children)
            child.scaleChanged();
    }

    /**
     * Returns the current object rotation relative to its parent
     * @return the rotation relative to the parent.
     */
    public Quaternionf getRot() {
        return rot;
    }

    /**
     * Sets the current object rotation relative to the parent
     * @param rot the rotation of the object relative to parent
     */
    public void setRot(Quaternionf rot) {
        this.rot = new Quaternionf(rot);

        rotChanged();
    }

    private void rotChanged() {
        // NOTE: Order matters here!
        // TODO: Fix, does not work as documented technically
        //rot.mul(offsetRot, worldRot);

        for(Model3D child : children)
            child.rotChanged();
    }

    /**
     * Called by #draw() to get a uniform variable requested by the shader pipeline.
     * @param name the name of the uniform attribute which has been requested
     * @return some convertable object representing the uniform. A vertex or matrix or primitive, for example.
     */
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

    /**
     * Called by #draw() to get a additional vertex attribute which should be used.
     * @param name the name of the vertex attribute which has been requested
     * @return a VBO for the shader to use. Should match the type requested in the shader, although not enforced.
     */
    public abstract VertexBufferObject getExtra(String name);

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
