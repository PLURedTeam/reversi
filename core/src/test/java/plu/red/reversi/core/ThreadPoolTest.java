package plu.red.reversi.core;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

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
    public void testChangeSize() {
        ThreadPool pool = new ThreadPool(1);

        pool.changeThreadCount(20);


        pool.changeThreadCount(5);
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
