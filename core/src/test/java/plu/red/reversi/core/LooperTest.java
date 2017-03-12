package plu.red.reversi.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by daniel on 3/6/17.
 * Glory to the Red Team.
 */
public class LooperTest implements Looper.LooperCallback<Integer> {

    private ArrayList<Integer> looperResults = new ArrayList<>();

    @Override
    public void onLooperCallback(Integer result) {
        looperResults.add(result);
    }

    @Before
    public void resetResults() {
        looperResults.clear();
    }

    @Test
    public void testGetLooper() {
        Looper l1 = Looper.getLooper(Thread.currentThread());
        Looper l2 = Looper.getLooper(Thread.currentThread());

        Assert.assertTrue(l1 == l2); // we want to test that the actual references are the same
    }

    @Test
    public void testLooperCallSingleThread() {

        Looper looper = Looper.getLooper(Thread.currentThread());

        Looper.LooperCall<Integer> call = looper.getCall(new Looper.LooperCallback<Integer>() {

            boolean called = false;

            @Override
            public void onLooperCallback(Integer result) {
                if(called)
                    Assert.fail("Looper called too many times");

                Assert.assertEquals(12, result.intValue());
                called = true;
            }
        });

        call.call(12);

        Assert.assertEquals(1, looper.run());

        call = looper.getCall(new Looper.LooperCallback<Integer>() {

            int callCount = 1;

            @Override
            public void onLooperCallback(Integer result) {
                Assert.assertEquals(callCount, result.intValue());
                callCount++;
            }
        });

        call.call(1);
        call.call(2);
        call.call(3);
        call.call(4);
        call.call(5);

        Assert.assertEquals(5, looper.run());
    }
}
