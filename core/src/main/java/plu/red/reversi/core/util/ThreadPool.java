package plu.red.reversi.core.util;

/**
 * Created by daniel on 3/12/17.
 * Glory to the Red Team.
 */

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Maintaines a set of on-demand threads which can be relied upon to execute a wide variety of tasks.
 *
 * Utilizes Looper to return the results of these tasks.
 */
public class ThreadPool {

    private Thread[] threads;

    private LinkedBlockingQueue<Job> jobs;

    /**
     * Initialize the thread pool with the specified number of threads
     * @param threadCount
     */
    public ThreadPool(int threadCount) {
        threads = new Thread[0];

        jobs = new LinkedBlockingQueue<>();

        changeThreadCount(threadCount);
    }

    /**
     * Hot update the thread count for this pool
     *
     * If the thread pool is to be shrunk, excess threads will be terminated.
     * If the thread pool is to be expanded, additional threads will be allocated.
     *
     * Changes take effect immediately.
     *
     * @param threadCount
     */
    public void changeThreadCount(int threadCount) {
        Thread[] newThreads = new Thread[threadCount];

        // finish existing threads
        for(int i = threads.length - 1;i >= threadCount;i--) {
            threads[i].interrupt();
        }

        // copy over current threads
        System.arraycopy(threads, 0, newThreads, 0, Math.min(threadCount, threads.length));

        // add new threads
        for(int i = threads.length;i < threadCount;i++) {
            newThreads[i] = new Thread(new JobRunner());
            newThreads[i].start();
        }

        threads = newThreads;
    }

    /**
     * Add the specified job to the job queue
     * @param j the job to add
     * @return always true
     */
    public boolean add(Job j) {
        return jobs.add(j);
    }

    /**
     * Add all the jobs specified in the provided collection to the queue
     * @param j the collection of jobs to add
     * @return always true
     */
    public boolean addAll(Collection<? extends Job> j) {
        return jobs.addAll(j);
    }

    /**
     * Returns the number of jobs waiting in the job queue for execution.
     */
    public int size() {
        return jobs.size();
    }

    /**
     * Get a list of currently active threads being used by this pool
     *
     * NOTE: This should only be used for testing purposes only!
     *
     * @return the list of threads
     */
    public Thread[] getThreads() {
        return threads;
    }

    /**
     * Extend this class to specify a new job to be run by a thread in the pool.
     *
     * Job functionality should be specified within the abstract run() function (implemented from Runnable)
     *
     * @param <T> the type of result which should eventually be returned by the job.
     */
    public abstract static class Job<T> implements Runnable {

        Looper.LooperCall<T> callback;

        public Job(Looper.LooperCall<T> callback) {
            this.callback = callback;
        }

        public abstract T getResult();

        public void runCallback() {
            T result = getResult();

            callback.call(result);
        }
    }

    /**
     * Internal class to represent a thread which receives and executes jobs
     */
    private class JobRunner implements Runnable {

        @Override
        public void run() {
            while(true) {
                try {
                    Job j = jobs.take();
                    j.run();

                } catch(InterruptedException ignored) {
                    // interrupt means terminate
                    break;
                }
            }
        }
    }
}
