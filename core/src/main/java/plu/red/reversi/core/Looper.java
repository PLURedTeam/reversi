package plu.red.reversi.core;

/**
 * Created by daniel on 3/6/17.
 * Glory to the Red Team.
 */

import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * For inter-thread communication, threads can call methods on other threads by executing calls through this class.
 *
 * Every thread is automatically allocated a looper thread which can be accessed using Looper.getLooper(Thread).
 */
public class Looper {

    private static WeakHashMap<Thread, Looper> loopers = new WeakHashMap<>();

    private ConcurrentLinkedQueue<LooperCall> calls;

    private Looper() {
        calls = new ConcurrentLinkedQueue<>();
    }

    public static Looper getLooper(Thread t) {
        if(!loopers.containsKey(t))
            loopers.put(t, new Looper());

        return loopers.get(t);
    }

    // We need this call for some reason
    public <T> LooperCall<T> getCall(LooperCallback<T> callback) {
        return new LooperCall<>(callback);
    }

    public int run() {
        int count = 0;

        LooperCall call;

        while((call = calls.poll()) != null) {
            count++;
            call.execute();
        }

        return count;
    }

    public class LooperCall<T> {

        LooperCallback<T> callback;
        ConcurrentLinkedQueue<T> results;


        public LooperCall(LooperCallback<T> callback) {
            this.callback = callback;
            results = new ConcurrentLinkedQueue<T>();
        }

        public void call(T result) {
            results.add(result);
            calls.add(this);
        }

        public void execute() {
            T val = results.poll();

            if(val == null)
                throw new IllegalStateException("Not enough results in queue for call");

            callback.onLooperCallback(val);
        }
    }

    public interface LooperCallback<T> {
        void onLooperCallback(T result);
    }
}
