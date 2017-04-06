package plu.red.reversi.core;


import org.junit.Test;
import plu.red.reversi.core.util.ConcurrentCall;

import java.util.HashSet;

import static org.junit.Assert.fail;
import static plu.red.reversi.core.util.ConcurrentCall.*;

import static org.junit.Assert.assertEquals;

public class ConcurrentCallTest {

    public static class CCObject {

        @Result public String result = "Before";
        public volatile boolean done = false;

        public int val1 = 0;
        public boolean val2 = false;
        public String val3 = "NoStr";

        @Body
        public void defaultPriority() {
            val1 = -5;
            val2 = false;
        }

        @Body(5)
        public void higherPriority() {
            val2 = false;
            val3 = "Diff String";
        }

        @Body(-5)
        public void lowerPriority() {
            val2 = true;
            val3 = "Another String";
        }

        @Body(-10)
        public void finalPriority() {
            val1 = 42;
            result = "After";
        }

        @Result
        public void doResult(String r) {
            done = true;
        }

    }

    public static class CCObjectLong {

        @Result public String result = "NoResult";

        private volatile boolean done = false;

        public boolean stage1 = false;
        public int stage2 = 0;
        public boolean stage3 = false;

        @Body(2)
        public void stage1() {
            try { Thread.sleep(500); }
            catch(InterruptedException ex) {}
            stage1 = true;
        }

        @Body(1)
        public void stage2() {
            for(int i = 0; i < 3; i++) {
                try {
                    Thread.sleep(500);
                    stage2++;
                    if(Thread.interrupted()) break;
                } catch (InterruptedException ex) { break; }
            }
        }

        @Body
        public void stage3() {
            try { Thread.sleep(1000); }
            catch(InterruptedException ex) {}
            stage3 = true;
            result = "A String";
        }

        @Result
        public void doResult(String r) {
            result = r + " that has been modified";
            done = true;
        }
    }

    public static class Counter {
        public volatile int count = 0;
        @Result
        public void doResult(String r) {
            count++;
        }
    }

    public static final int MASS_TEST_COUNT = 10;

    @Test
    public void testPriorities() {
        CCObject testObject = new CCObject();
        assertEquals("Before", testObject.result);
        ConcurrentCall.createCall(testObject, testObject);
        long currentTime = System.currentTimeMillis();
        while(!testObject.done) {
            if((System.currentTimeMillis()-currentTime) > 1000*5) {
                fail("Timed Out - 5 seconds");
                break;
            }
        }
        assertEquals("After", testObject.result);
        assertEquals(42, testObject.val1);
        assertEquals(true, testObject.val2);
        assertEquals("Another String", testObject.val3);
        assertEquals(true, testObject.done);
    }

    @Test
    public void testLong() {
        CCObjectLong testObjectLong = new CCObjectLong();
        assertEquals("NoResult", testObjectLong.result);
        ConcurrentCall.createCall(testObjectLong, testObjectLong);
        long currentTime = System.currentTimeMillis();
        while(!testObjectLong.done) {
            if((System.currentTimeMillis()-currentTime) > 1000*5) {
                fail("Timed Out - 5 seconds");
                break;
            }
        }
        assertEquals(true, testObjectLong.stage1);
        assertEquals(3, testObjectLong.stage2);
        assertEquals(true, testObjectLong.stage3);
        assertEquals(true, testObjectLong.done);
        assertEquals("A String that has been modified", testObjectLong.result);
    }

    @Test
    public void testLongMass() {
        HashSet<CCObjectLong> testSet = new HashSet<>();
        for(int i = 0; i < MASS_TEST_COUNT; i++) testSet.add(new CCObjectLong());
        Counter count = new Counter();
        for(CCObjectLong cc : testSet) assertEquals("NoResult", cc.result);
        CallID id = new CallID();
        for(CCObjectLong cc : testSet) ConcurrentCall.createCall(cc, count, id);
        long currentTime = System.currentTimeMillis();
        while(count.count < MASS_TEST_COUNT) {
            int stopCount = 0;
            for(CCObjectLong cc : testSet) if(cc.done) stopCount++;
            if(stopCount >= MASS_TEST_COUNT) break;
            long time = System.currentTimeMillis();
            if((time-currentTime) > 1000*5) {
                fail("Timed Out - 5 seconds");
                break;
            }
        }
        for(CCObjectLong cc : testSet) assertEquals(true, cc.stage1);
        for(CCObjectLong cc : testSet) assertEquals(3, cc.stage2);
        for(CCObjectLong cc : testSet) assertEquals(true, cc.stage3);
        for(CCObjectLong cc : testSet) assertEquals("A String", cc.result);
    }

    @Test
    public void testLongStoppedQuick() {
        HashSet<CCObjectLong> testSet = new HashSet<>();
        for(int i = 0; i < MASS_TEST_COUNT; i++) testSet.add(new CCObjectLong());
        Counter count = new Counter();
        for(CCObjectLong cc : testSet) assertEquals("NoResult", cc.result);
        CallID id = new CallID();
        for(CCObjectLong cc : testSet) ConcurrentCall.createCall(cc, count, id);
        long currentTime = System.currentTimeMillis();
        while(count.count < MASS_TEST_COUNT) {
            int stopCount = 0;
            for(CCObjectLong cc : testSet) if(cc.done) stopCount++;
            if(stopCount >= MASS_TEST_COUNT) break;
            long time = System.currentTimeMillis();
            if((time-currentTime) > 2200) {
                ConcurrentCall.stopCall(id);
                break;
            }
            if((time-currentTime) > 1000*5) {
                fail("Timed Out - 5 seconds");
                break;
            }
        }
        for(CCObjectLong cc : testSet) assertEquals(true, cc.stage1);
        for(CCObjectLong cc : testSet) assertEquals(3, cc.stage2);
        for(CCObjectLong cc : testSet) assertEquals(false, cc.stage3);
        for(CCObjectLong cc : testSet) assertEquals(false, cc.done);
        for(CCObjectLong cc : testSet) assertEquals("NoResult", cc.result);
    }

    @Test
    public void testLongStoppedExact() {
        HashSet<CCObjectLong> testSet = new HashSet<>();
        for(int i = 0; i < MASS_TEST_COUNT; i++) testSet.add(new CCObjectLong());
        Counter count = new Counter();
        for(CCObjectLong cc : testSet) assertEquals("NoResult", cc.result);
        CallID id = new CallID();
        for(CCObjectLong cc : testSet) ConcurrentCall.createCall(cc, count, id);
        long currentTime = System.currentTimeMillis();
        while(count.count < MASS_TEST_COUNT) {
            int stopCount = 0;
            for(CCObjectLong cc : testSet) if(cc.done) stopCount++;
            if(stopCount >= MASS_TEST_COUNT) break;
            long time = System.currentTimeMillis();
            if((time-currentTime) > 1200) {
                ConcurrentCall.stopCall(id);
                break;
            }
            if((time-currentTime) > 1000*5) {
                fail("Timed Out - 5 seconds");
                break;
            }
        }
        for(CCObjectLong cc : testSet) assertEquals(true, cc.stage1);
        for(CCObjectLong cc : testSet) assertEquals(1, cc.stage2);
        for(CCObjectLong cc : testSet) assertEquals(false, cc.stage3);
        for(CCObjectLong cc : testSet) assertEquals(false, cc.done);
        for(CCObjectLong cc : testSet) assertEquals("NoResult", cc.result);
    }
}
