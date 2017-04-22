package plu.red.reversi.core.reversi3d;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import plu.red.reversi.core.graphics.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by daniel on 4/21/17.
 */
public class Model3DTest {

    private StubGraphics3D g3d = new StubGraphics3D();
    private PipelineDefinition pipelineDefinition = new PipelineDefinition();
    private Pipeline pipeline = new StubPipeline();

    @After
    public void resetG3DStub() {
        g3d.clearCallSequence();
    }

    @Test
    public void testRecalculate() {
        Model3D m3d = new StubModel3D(g3d, pipeline);

        m3d.recalculate(-1);

        // should have all the vertices sent by the caller
        Assert.assertTrue(m3d.vertices.contains(new Vector4f(0,0,0,1)));
        Assert.assertTrue(m3d.vertices.contains(new Vector4f(0,2,0,1)));
        Assert.assertTrue(m3d.vertices.contains(new Vector4f(0,0,2,1)));

        Assert.assertTrue(m3d.normals.contains(new Vector3f(0,0,1)));
        Assert.assertTrue(m3d.normals.contains(new Vector3f(0,1,0)));
        Assert.assertTrue(m3d.normals.contains(new Vector3f(1,0,0)));
        // should not have any issues roundoff since this would effectively be the exact same calculation
        Assert.assertTrue(m3d.normals.contains(new Vector3f(1,1,0).normalize()));

        Assert.assertFalse(m3d.normals.contains(new Vector3f(1,0,1).normalize()));

        Assert.assertEquals(m3d.vertices.size(), m3d.normals.size());

        // some new buffers should have been uploaded
        Assert.assertArrayEquals(g3d.getCallSequence().get(0), new Object[]{"uploadVBO", m3d.vertices});
    }

    @Test
    public void testTransformations() {
        Model3D root = new StubModel3D(g3d, pipeline);
        Model3D l1 = root.clone();
        Model3D l2 = l1.clone();

        // test transforms getting updated after adding children
        root.setPos(new Vector3f(0,0,2));
        root.setScale(new Vector3f(1,2,3));
        Assert.assertEquals(l2.getWorldPosition(), new Vector3f());

        root.addChild(l1);
        l1.addChild(l2);

        Assert.assertEquals(l2.getWorldPosition(), new Vector3f(0,0,2));
        Assert.assertEquals(l2.getWorldScale(), new Vector3f(1,2,3));

        // test transforms getting updated with rotation
        root.setPos(new Vector3f(2,0,0));
        root.setScale(new Vector3f(2,4,4));

        Assert.assertEquals(l2.getWorldPosition(), new Vector3f(2,0,0));
        Assert.assertEquals(l2.getWorldScale(), new Vector3f(2,4,4));

        root.removeChild(l1);

        Assert.assertEquals(l2.getWorldPosition(), new Vector3f(0,0,0));
    }

    @Test
    public void testModelTree() {
        Model3D root = new StubModel3D(g3d, pipeline);
        root.recalculate(-1);
        Model3D l1 = root.clone();
        Model3D l2 = l1.clone();

        root.addChild(l1);
        root.addChild(l2);

        Assert.assertTrue(root.isChild(l1));
        Assert.assertTrue(root.isChild(l2));

        l1.addChild(l2);

        Assert.assertTrue(root.isChild(l1));
        Assert.assertTrue(l1.isChild(l2));
        Assert.assertFalse(root.isChild(l2));

        g3d.clearCallSequence();

        root.draw();

        Assert.assertEquals(g3d.getCallSequence().size(), 6);
    }

    public class StubModel3D extends Model3D {

        public StubModel3D(Graphics3D g3d, Pipeline pipeline) {
            super(g3d, pipeline);
        }

        @Override
        Model3D newInstance() {
            return new StubModel3D(getGraphics3D(), getPipeline());
        }

        @Override
        int getSectionCount() {
            return 2;
        }

        @Override
        int getFaceCount(int sectionIndex) {
            return sectionIndex == 1 ? 2 : 1;
        }

        @Override
        Vector3f[] getFace(int sectionIndex, int faceIndex) {
            if (sectionIndex == 0) {
                return new Vector3f[]{
                        new Vector3f(0,0,0),
                        new Vector3f(2, 0, 0),
                        new Vector3f(0, 2, 0),
                        new Vector3f(2, 2, 0)
                };
            }
            else {
                if(faceIndex == 0)
                    return new Vector3f[]{
                            new Vector3f(0,0,0),
                            new Vector3f(0,2,0),
                            new Vector3f(0,0,2)
                    };
                else
                    return new Vector3f[]{
                            new Vector3f(0,0,0),
                            new Vector3f(0,0,2),
                            new Vector3f(2,0,0)
                    };
            }
        }
    }
}
