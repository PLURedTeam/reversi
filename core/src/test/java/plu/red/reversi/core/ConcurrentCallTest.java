package plu.red.reversi.core;


import org.junit.Test;
import plu.red.reversi.core.game.player.Player;
import plu.red.reversi.core.util.ConcurrentCall;

import java.util.HashSet;

import static org.junit.Assert.fail;
import static plu.red.reversi.core.util.ConcurrentCall.*;

import static org.junit.Assert.assertEquals;

public class ConcurrentCallTest {

    public static class CCObject {

        @ResultField public String result = "Before";
        public volatile boolean done = false;

        public int val1 = 0;
        public boolean val2 = false;
        public String val3 = "NoStr";

        @BodyMethod
        public void defaultPriority() {
            val1 = -5;
            val2 = false;
        }

        @BodyMethod(5)
        public void higherPriority() {
            val2 = false;
            val3 = "Diff String";
        }

        @BodyMethod(-5)
        public void lowerPriority() {
            val2 = true;
            val3 = "Another String";
        }

        @BodyMethod(-10)
        public void finalPriority() {
            val1 = 42;
            result = "After";
        }

        @ResultMethod
        public void doResult(String r) {
            done = true;
        }

    }

    public static class CCObjectLong {

        @ResultField(0) public String result = "NoResult";

        private volatile boolean done = false;

        @ResultField(1) public boolean stage1 = false;
        @ResultField(2) public int stage2 = 0;
        @ResultField(3) public boolean stage3 = false;

        @BodyMethod(2)
        public void stage1() {
            try { Thread.sleep(500); }
            catch(InterruptedException ex) { return; }
            stage1 = true;
        }

        @BodyMethod(1)
        public void stage2() {
            for(int i = 0; i < 3; i++) {
                try {
                    Thread.sleep(500);
                    stage2++;
                    if(Thread.interrupted()) break;
                } catch (InterruptedException ex) { break; }
            }
        }

        @BodyMethod
        public void stage3() {
            try { Thread.sleep(1000); }
            catch(InterruptedException ex) { return; }
            stage3 = true;
            result = "Results";
        }

        @ResultMethod
        public void doResult(@ResultParameter(0) String r, @ResultParameter(1) boolean s1, @ResultParameter(2) int s2, @ResultParameter(3) boolean s3) {
            result = r + ": " + s1 + ", " + s2 + ", " + s3;
            done = true;
        }
    }

    public static class CCSyncA {
        private volatile boolean done = false;

        @ResultField(42) public boolean val1 = false;
        @ResultField(96) public int val2 = 0;
        @ResultField(101) public String val3 = "NoResult";

        @BodyMethod
        public void call() {
            val1 = true;
            val2 = 42;
            val3 = "Supercalifragilisticexpialodocious";
            this.done = true;
        }
    }

    public static class CCSyncB {

        public boolean val1 = false;
        public int val2 = 0;
        public String val3 = "NoResult";
        public int val4 = 0;

        @ResultMethod(InvokeType.SYNC)
        public void resultA(@ResultParameter(42) boolean val1, @ResultParameter(96) int val2) {
            this.val1 = val1;
            this.val2 = val2;
        }

        @ResultMethod(InvokeType.SYNC)
        public void resultB(@ResultParameter(101) String val3, @ResultParameter(96) int val4) {
            this.val3 = val3;
            this.val4 = val4;
        }
    }

    private static final int MASS_TEST_COUNT = 10;

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
        assertEquals("Results: true, 3, true", testObjectLong.result);
    }

    @Test
    public void testLongMass() {
        HashSet<CCObjectLong> testSet = new HashSet<>();
        for(int i = 0; i < MASS_TEST_COUNT; i++) testSet.add(new CCObjectLong());
        for(CCObjectLong cc : testSet) assertEquals("NoResult", cc.result);
        CallID id = new CallID();
        for(CCObjectLong cc : testSet) ConcurrentCall.createCall(cc, cc, id);
        long currentTime = System.currentTimeMillis();
        while(true) {
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
        for(CCObjectLong cc : testSet) assertEquals("Results: true, 3, true", cc.result);
    }

    @Test
    public void testLongStoppedQuick() {
        HashSet<CCObjectLong> testSet = new HashSet<>();
        for(int i = 0; i < MASS_TEST_COUNT; i++) testSet.add(new CCObjectLong());
        for(CCObjectLong cc : testSet) assertEquals("NoResult", cc.result);
        CallID id = new CallID();
        for(CCObjectLong cc : testSet) ConcurrentCall.createCall(cc, cc, id);
        long currentTime = System.currentTimeMillis();
        while(true) {
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
        for(CCObjectLong cc : testSet) assertEquals("NoResult", cc.result);
        CallID id = new CallID();
        for(CCObjectLong cc : testSet) ConcurrentCall.createCall(cc, cc, id);
        long currentTime = System.currentTimeMillis();
        while(true) {
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

    @Test
    public void testSyncMass() {
        HashSet<CCSyncA> testSetA = new HashSet<>();
        HashSet<CCSyncB> testSetB = new HashSet<>();
        CallID id = new CallID();
        for(int i = 0; i < MASS_TEST_COUNT; i++) {
            CCSyncA a = new CCSyncA();
            CCSyncB b = new CCSyncB();
            ConcurrentCall.createCall(a, b, id);
            testSetA.add(a);
            testSetB.add(b);
        }
        try { Thread.sleep(500); }
        catch(Exception ex) {}
        ConcurrentCall.syncCalls();
        for(CCSyncA a : testSetA) {
            assertEquals(true, a.done);
            assertEquals(true, a.val1);
            assertEquals(42, a.val2);
            assertEquals("Supercalifragilisticexpialodocious", a.val3);
        }
        for(CCSyncB b : testSetB) {
            assertEquals(true, b.val1);
            assertEquals(42, b.val2);
            assertEquals("Supercalifragilisticexpialodocious", b.val3);
            assertEquals(42, b.val4);
        }
    }
}
