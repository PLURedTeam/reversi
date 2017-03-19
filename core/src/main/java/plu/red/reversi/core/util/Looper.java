package plu.red.reversi.core.util;

/**
 * Created by daniel on 3/6/17.
 * Glory to the Red Team.
 */

import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Primarily for the purpose of simplifying async message passing in a synchronous programming environment, this interface
 * is provided.
 *
 * To use:
 * 1. Place a call to Looper.run() in the main loop of your class whenever you would like events to be handled
 * 2. When you want to prepare an asynchronous callback, pass an instance of LooperCall<>() to the asynchronous function
 * 3. When the asynchronous function returns, you will receive a callback with the parameter specified in LooperCall<>()
 */
public class Looper {

    private static WeakHashMap<Thread, Looper> loopers = new WeakHashMap<>();

    private ConcurrentLinkedQueue<LooperCall> calls;

    private Looper() {
        calls = new ConcurrentLinkedQueue<>();
    }

    /**
     * Gets (and possibly constructs) the looper for the current thread. Each thread is mapped to a looper.
     * @param t the thread to get the looper for
     * @return a looper which is designed to contain the Looper.run() callback
     */
    public static Looper getLooper(Thread t) {
        if(!loopers.containsKey(t))
            loopers.put(t, new Looper());

        return loopers.get(t);
    }

    /**
     * Construct a new looper call
     * @param <T> the type of result which should be expected in the LooperCallback
     * @param callback the callback which should be executed after the asynchronous operation completes.
     * @return a looper call connected to this thread which may be passed to the asynchronous operation
     */
    public <T> LooperCall<T> getCall(LooperCallback<T> callback) {
        return new LooperCall<>(callback);
    }

    /**
     * Allow looper to execute asynchronous callbacks.
     *
     * This method should be called somewhere in the thread's main loop.
     * @return the number of callback jobs which were executed in this call to run
     */
    public int run() {
        int count = 0;

        LooperCall call;

        while((call = calls.poll()) != null) {
            count++;
            call.execute();
        }

        return count;
    }

    /**
     * A class which encloses a capability to a callback. It maintains a queue of arguments so that the callbacks may
     * all be executed.
     *
     * A single looper call may be reused as much as necessary or desired
     * @param <T> the type of return value from an asynchronous function.
     */
    public class LooperCall<T> {

        LooperCallback<T> callback;
        ConcurrentLinkedQueue<T> results;

        public LooperCall(LooperCallback<T> callback) {
            this.callback = callback;
            results = new ConcurrentLinkedQueue<T>();
        }

        /**
         * Call this method to enqueue a callback at the end of your asynchronous operation. This function may be called
         * as many times as necessary.
         * @param result the result of whatever asynchronous operation occured.
         */
        public void call(T result) {
            results.add(result);
            calls.add(this);
        }

        /**
         * Called by Looper to run the callback function. Should not be called outside of looper.
         */
        public void execute() {
            T val = results.poll();

            if(val == null)
                throw new IllegalStateException("Not enough results in queue for call");

            callback.onLooperCallback(val);
        }
    }

    /**
     * Implement this interface to specify a callback function to be used for LooperCall.
     * @param <T> the type of return value which should be expected for this callback.
     */
    public interface LooperCallback<T> {
        void onLooperCallback(T result);
    }
}
