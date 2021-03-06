package plu.red.reversi.core.graphics;

import org.joml.*;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.security.InvalidParameterException;
import java.util.ArrayList;

// represents data which can be applied to vertices in a pipeline
public class VertexBufferObject<T> extends ArrayList<T> {

    private int stride;

    private Handle handle;

    private Class<T> rawType;

    public VertexBufferObject(Class<T> type) {
        rawType = type;

        if(getRawType() == Float.class ||
                getRawType() == Integer.class ||
                getRawType() == Double.class)

            stride = 1;

        else if(getRawType() == Vector2fc.class ||
                getRawType() == Vector2ic.class ||
                getRawType() == Vector2dc.class)
            stride = 2;

        else if(getRawType() == Vector3fc.class ||
                getRawType() == Vector3ic.class ||
                getRawType() == Vector3dc.class)
            stride = 3;

        else if(getRawType() == Vector4fc.class ||
                getRawType() == Vector4ic.class ||
                getRawType() == Vector4dc.class)
            stride = 4;
        else
            throw new InvalidParameterException("VertexBufferObject can only take certain data types. " + getRawType() +  " is not one of them. RTFM.");

        handle = new Handle();
    }

    public Object getHandle() {
        return handle.getHandle();
    }

    public void setHandle(Graphics3D g3d, Object handle) {
        this.handle.setHandle(g3d, handle);
    }

    private Class getRawType() {
        return rawType;
    }

    public Class getPlainType() {
        if(getRawType() == Float.class ||
                getRawType() == Vector2fc.class ||
                getRawType() == Vector3fc.class ||
                getRawType() == Vector4fc.class)
            return Float.class;

        if(getRawType() == Double.class ||
                getRawType() == Vector2dc.class ||
                getRawType() == Vector3dc.class ||
                getRawType() == Vector4dc.class)
            return Double.class;

        return Integer.class;
    }

    public int getStride() {
        return stride;
    }

    public int getDataSize() {
        if(getPlainType() == Float.class)
            return 4;

        if(getPlainType() == Double.class)
            return 8;

        return 4; // for all integer types
    }

    public Buffer getBuffer() throws IOException {

        if(getRawType() == Float.class) {
            FloatBuffer fb = FloatBuffer.allocate(size());

            for(Object f : this) {
                fb.put((Float)f);
            }

            return fb;
        }
        else if(getRawType() == Double.class) {
            DoubleBuffer fb = DoubleBuffer.allocate(size());

            for(Object f : this) {
                fb.put((Double)f);
            }

            return fb;
        }
        else if(getRawType() == Integer.class) {
            IntBuffer fb = IntBuffer.allocate(size());

            for(Object f : this) {
                fb.put((Integer)f);
            }

            return fb;
        }
        else if(getRawType() == Vector2fc.class) {
            FloatBuffer fb = FloatBuffer.allocate(2 * size());

            for(Object f : this) {
                ((Vector2fc)f).get(fb);
                fb.position(fb.position() + 2);
            }

            return fb;
        }
        else if(getRawType() == Vector3fc.class) {
            float[] fb = new float[3 * size()];

            for(int i = 0;i < size();i++) {
                Vector3fc v = ((Vector3fc)get(i));
                fb[3 * i]     = v.x();
                fb[3 * i + 1] = v.y();
                fb[3 * i + 2] = v.z();
            }

            return FloatBuffer.wrap(fb);
        }
        else if(getRawType() == Vector4fc.class) {
            float[] fb = new float[4 * size()];

            for(int i = 0;i < size();i++) {
                Vector4fc v = ((Vector4fc)get(i));
                fb[4 * i]     = v.x();
                fb[4 * i + 1] = v.y();
                fb[4 * i + 2] = v.z();
                fb[4 * i + 3] = v.w();
            }

            return FloatBuffer.wrap(fb);
        }
        else if(getRawType() == Vector2dc.class) {
            DoubleBuffer fb = DoubleBuffer.allocate(2 * size());

            for(Object f : this) {
                ((Vector2dc)f).get(fb);
                fb.position(fb.position() + 2);
            }

            return fb;
        }
        else if(getRawType() == Vector3dc.class) {
            DoubleBuffer fb = DoubleBuffer.allocate(3 * size());

            for(Object f : this) {
                ((Vector3dc)f).get(fb);
                fb.position(fb.position() + 3);
            }

            return fb;
        }
        else if(getRawType() == Vector4dc.class) {
            DoubleBuffer fb = DoubleBuffer.allocate(4 * size());

            for(Object f : this) {
                ((Vector4dc)f).get(fb);
                fb.position(fb.position() + 4);
            }

            return fb;
        }
        else if(getRawType() == Vector2ic.class) {
            IntBuffer fb = IntBuffer.allocate(2 * size());

            for(Object f : this) {
                ((Vector2ic)f).get(fb);
                fb.position(fb.position() + 2);
            }

            return fb;
        }
        else if(getRawType() == Vector3ic.class) {
            IntBuffer fb = IntBuffer.allocate(3 * size());

            for(Object f : this) {
                ((Vector3ic)f).get(fb);
                fb.position(fb.position() + 3);
            }

            return fb;
        }
        else if(getRawType() == Vector4ic.class) {
            IntBuffer fb = IntBuffer.allocate(4 * size());

            for(Object f : this) {
                ((Vector4ic)f).get(fb);
                fb.position(fb.position() + 4);
            }

            return fb;
        }
        else {
            // technically should not happen
            throw new UnsupportedOperationException("Missing VBO buffer write support for " + getRawType());
        }
    }
}
