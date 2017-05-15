package plu.red.reversi.core.reversi3d;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import plu.red.reversi.core.graphics.Graphics3D;
import plu.red.reversi.core.graphics.Pipeline;

/**
 * Highlights a square region of space... extending ridiculously high upward.
 */
public class Highlight3D extends ColorModel3D {

    private float height;

    private Vector3fc color;

    public Highlight3D(Graphics3D g3d, Pipeline pipeline) {
        super(g3d, pipeline, 0.1f);

        alphaBlended = true;
        height = 0.02f;

        this.color = new Vector3f(1.0f, 1.0f, 0.0f);
    }

    public Highlight3D(Highlight3D other) {
        super(other.getGraphics3D(), other.getPipeline(), 0.1f);

        alphaBlended = other.alphaBlended;
        height = other.height;
        color = other.color;
    }

    @Override
    public Model3D clone() {
        Highlight3D m3d = (Highlight3D)super.clone();

        m3d.height = height;

        return m3d;
    }

    @Override
    Vector4f[] getFaceColor(int sectionIndex, int faceIndex) {

        Vector4f c;

        if(sectionIndex < 2) {
            c = new Vector4f(color, 1.0f);
        }
        else
            c = new Vector4f(color, 0.5f);

        return new Vector4f[] {c, c, c, c};
    }

    @Override
    public Model3D newInstance() {
        return new Highlight3D(this);
    }

    @Override
    int getSectionCount() {
        return 6;
    }

    @Override
    int getFaceCount(int sectionIndex) {
        return 1;
    }

    @Override
    Vector3f[] getFace(int sectionIndex, int faceIndex) {
        // return a cube face

        // 8 points of the cube in a more readable format
        Vector3f tdl = new Vector3f(0, 0, 10000f);
        Vector3f tdr = new Vector3f(1, 0, 10000f);
        Vector3f tul = new Vector3f(0, 1, 10000f);
        Vector3f tur = new Vector3f(1, 1, 10000f);
        Vector3f bdl = new Vector3f(0, 0, height);
        Vector3f bdr = new Vector3f(1, 0, height);
        Vector3f bul = new Vector3f(0, 1, height);
        Vector3f bur = new Vector3f(1, 1, height);

        switch(sectionIndex) {
            case 0:
                return new Vector3f[]{tur, tul, tdl, tdr};
            case 1:
                return new Vector3f[]{bur, bul, bdl, bdr};
            case 2:
                return new Vector3f[]{tur, tdr, bdr, bur};
            case 3:
                return new Vector3f[]{tdl, tul, bul, bdl};
            case 4:
                return new Vector3f[]{tdr, tdl, bdl, bdr};
            case 5:
                return new Vector3f[]{tul, tur, bur, bul};
        }

        return null;
    }

    public void highlightOn(Vector2f min, Vector2f max) {
        Vector2f size = new Vector2f(max).sub(min);
        setScale(new Vector3f(size, 1.0f));
        setPos(new Vector3f(min, 0.0f));
    }

    public void setHeight(float height) {

        this.height = height;

        Vector3f pos = getPos();

        pos.z = height;

        setPos(pos);
    }

    public void setColor(Vector3fc color) {
        this.color = color;

        // unfortunately current pipeline expectations require complete buffer refresh
        recalculate("vAlbedo");
    }

    public Vector3fc getColor() {
        return color;
    }
}
