package plu.red.reversi.core;

import org.junit.Assert;
import org.junit.Test;
import plu.red.reversi.core.util.Looper;
import plu.red.reversi.core.util.ThreadPool;

/**
 * Created by daniel on 3/12/17.
 * Glory to the Red Team.
 */
public class ThreadPoolTest {

    @Test
    public void testJobExecution() {

        SampleJob sample = new SampleJob(Looper.getLooper(Thread.currentThread()).getCall(new Looper.LooperCallback<Integer>() {
            @Override
            public void onLooperCallback(Integer result) {
                Assert.assertEquals(result.intValue(), 1);
            }
        }));

        sample.run();
        sample.runCallback();
    }

    @Test
    public void testChangeSize() throws InterruptedException {
        ThreadPool pool = new ThreadPool(1);

        Thread origThread = pool.getThreads()[0];

        Assert.assertFalse(origThread.isInterrupted());

        pool.changeThreadCount(20);

        Assert.assertEquals(origThread, pool.getThreads()[0]);

        for(Thread t : pool.getThreads()) {
            Assert.assertFalse(t.isInterrupted());
        }


        Thread quitThread = pool.getThreads()[19];

        pool.changeThreadCount(5);

        Assert.assertEquals(origThread, pool.getThreads()[0]);
        Thread.sleep(10); // should eliminate race conditions
        Assert.assertTrue(!quitThread.isAlive() || quitThread.isInterrupted());

        for(Thread t : pool.getThreads()) {
            Assert.assertFalse(t.isInterrupted());
        }
    }

    private class SampleJob extends ThreadPool.Job<Integer> {

        int number;

        public SampleJob(Looper.LooperCall<Integer> callback) {
            super(callback);
        }

        @Override
        public Integer getResult() {
            return number;
        }

        @Override
        public void run() {
            number++;
        }
    }
}
