package plu.red.reversi.core;


import org.junit.Test;
import plu.red.reversi.core.util.ConcurrentCall;
import static plu.red.reversi.core.util.ConcurrentCall.*;

import static org.junit.Assert.assertEquals;

public class ConcurrentCallTest {

    public static class CCObject implements ConcurrentCall.Call {

        @Result public String result = "Before";
        public volatile boolean done = false;

        @Override public void stop() {

        }

        @Body
        public void defaultPriority() {
            System.out.println("Default Priority");
        }

        @Body(5)
        public void higherPriority() {
            System.out.println("Higher Priority");
        }

        @Body(-5)
        public void lowerPriority() {
            System.out.println("Lower Priority");
        }

        @Body(-10)
        public void finalPriority() {
            System.out.println("Final Priority");
            result = "After";
        }

        @Result
        public void doResult(String r) {
            System.out.println(r);
            done = true;
        }

    }

    @Test
    public void test() {
        CCObject testObject = new CCObject();
        assertEquals("Before", testObject.result);
        ConcurrentCall.createCall(testObject, testObject);
        while(!testObject.done) {}
        assertEquals("After", testObject.result);
    }
}
