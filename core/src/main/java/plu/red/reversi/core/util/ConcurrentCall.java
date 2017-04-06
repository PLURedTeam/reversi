package plu.red.reversi.core.util;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * ConcurrentCall is a class designed to run actions in a parallel thread, and then report results back into a
 * designated method. Calls are created with the static method <code>createCall()</code>, which returns a CallID in
 * order to identify the call. Multiple calls can share a CallID. In order to create a call, a class that has special
 * annotations must be passed to the <code>createCall()</code> method. The annotations are specified below:
 *
 * \@Result - A single variable field in the class must be annotated with this annotation. The contents of the annotated
 *   variable will be copied into a result method. Only one variable may be annotated.
 * \@Body - At least on method in the class must be annotated with this annotation. The method/s annotated this way will
 *   be ran by the ConcurrentCall internal system. They must have no parameters. There may be any number of methods
 *   annotated this way. In addition, an optional numeric priority is supported; higher priorities will be ran first.
 *
 * In addition to the calling object, another object must be passed to <code>createCall()</code> whose class has a
 * single method annotated with \@Result. The annotated method must have a single parameter of the same type (or
 * assignable type) of the variable in the calling object that is also annotated with \@Result. The value of the
 * annotated variable will be passed into the annotated method. This secondary object may be the same as the calling
 * object.
 *
 * As a side note, when stopping a ConcurrentCall, a stop flag is set and <code>Thread.interrupt()</code> is called.
 * Afterwards, if the ConcurrentCall passes between Body Methods, it will first check the stop flag. If it is set, then
 * the ConcurrentCall will terminate. In order for a ConcurrentCall to correctly stop during a long (or possibly looping)
 * Body Method, the calling object will need to periodically check for <code>Thread.interrupted()</code> and terminate
 * on its own.
 *
 * Anyone who wishes can feel free to re-use this class in current and/or future projects, as long as proper credit is
 * given.
 *
 * @author James De Broeck
 */
public class ConcurrentCall implements Runnable {

    private static HashMap<CallID, HashSet<ConcurrentCall>> calls = new HashMap<>();

    /**
     * Creates and runs a new ConcurrentCall.
     *
     * @param callObject Object to run concurrently
     * @param resultObject Object that will have the result method
     * @return A CallID to identify the created call with
     */
    public static CallID createCall(Object callObject, Object resultObject) {
        return createCall(callObject, resultObject, new CallID());
    }

    /**
     * Creates and runs a new ConcurrentCall. Assigns the created ConcurrentCall the CallID that is given.
     *
     * @param callObject Object to run concurrently
     * @param resultObject Object that will have the result method
     * @param id A CallID to assign to the created call
     * @return A CallID to identify the created call with
     */
    public static CallID createCall(Object callObject, Object resultObject, CallID id) {

        Method[] methods = callObject.getClass().getMethods();
        TreeSet<MethodBody> methodSet = new TreeSet<>();
        for(Method method : methods) {
            if(method.isAnnotationPresent(Body.class)) {
                if(method.getParameterCount() > 0)
                    throw new IllegalArgumentException("Body Method in a ConcurrentCall cannot have parameters");
                int priority = method.getDeclaredAnnotation(Body.class).value();
                methodSet.add(new MethodBody(priority, method));
            }
        }

        if(methodSet.isEmpty())
            throw new IllegalArgumentException("No Body Methods found in Call object of type "+callObject.getClass().getName());

        Field callField = null;
        Field[] fields = callObject.getClass().getFields();
        for(Field field : fields) {
            if(field.isAnnotationPresent(Result.class)) {
                if(callField != null)
                    throw new IllegalArgumentException("Only one Result Field can exist in a Call object (type: "+callObject.getClass().getName()+")");
                callField = field;
            }
        }

        if(callField == null)
            throw new IllegalArgumentException("No Result Field found in Call object of type "+callObject.getClass().getName());

        Method resultMethod = null;
        Method[] resultMethods = resultObject.getClass().getMethods();
        for(Method method : resultMethods) {
            if(method.isAnnotationPresent(Result.class)) {
                if(resultMethod != null)
                    throw new IllegalArgumentException("Only one Result Method can exist in a Result object (type: "+resultObject.getClass().getName());
                if(method.getParameterCount() != 1 || method.getParameterTypes()[0].isAssignableFrom(callField.getDeclaringClass()))
                    throw new IllegalArgumentException("Result Method must have one parameter of type "+callField.getDeclaringClass().getName()+" for Result object of type "+resultObject.getClass().getName());
                resultMethod = method;
            }
        }

        ConcurrentCall call = new ConcurrentCall(id, callObject, methodSet, callField, resultObject, resultMethod);
        if(!calls.containsKey(id)) calls.put(id, new HashSet<>());
        calls.get(id).add(call);
        return id;
    }

    /**
     * Stops one or more ConcurrentCalls. Forcibly stops the ConcurrentCalls that are identified with the given CallID.
     *
     * @param id A CallID to identify calls with
     */
    public static void stopCall(CallID id) {
        if(calls.containsKey(id)) {
            HashSet<ConcurrentCall> callSet = calls.get(id);
            for(ConcurrentCall call : callSet) {
                call.stop();
            }
            calls.remove(id);
        }
    }


    public final CallID id;

    private final Thread thread;
    private final Object callObject;
    private final TreeSet<MethodBody> methods;
    private final Field callField;
    private final Object resultObject;
    private final Method resultMethod;

    private volatile boolean stopped = false;

    private ConcurrentCall(CallID id, Object callObject, TreeSet<MethodBody> methods, Field callField, Object resultObject, Method resultMethod) {
        this.id = id;
        this.callObject = callObject;
        this.methods = methods;
        this.callField = callField;
        this.resultObject = resultObject;
        this.resultMethod = resultMethod;
        thread = new Thread(this);
        thread.start();
    }

    private void cleanup() {
        if(calls.containsKey(id)) calls.get(id).remove(this);
    }

    private void stop() {
        stopped = true;
        thread.interrupt();
    }

    @Override
    public void run() {

        try {

            // Run the Body Methods
            for (MethodBody method : methods) {
                try {
                    method.method.invoke(callObject);
                } catch (IllegalAccessException ex) {
                    throw new IllegalArgumentException("Cannot access Body Method " + method.method.getName());
                } catch (InvocationTargetException ex) {
                    throw new RuntimeException(ex.getMessage());
                }
                if (stopped) return;
            }

            // Get the result
            Object result;
            try {
                result = callField.get(callObject);
            } catch (IllegalAccessException ex) {
                throw new IllegalArgumentException("Cannot access Result Field " + callField.getName());
            }
            if (stopped) return;

            // Give the result to the result method
            try {
                resultMethod.invoke(resultObject, result);
            } catch (IllegalAccessException ex) {
                throw new IllegalArgumentException("Cannot access Result Method " + resultMethod.getName());
            } catch (InvocationTargetException ex) {
                throw new RuntimeException(ex.getMessage());
            }

        } catch(Exception ex) {
            stopped = true;
            cleanup();
            throw ex;
        }

        cleanup();
    }

    /**
     * CallID is an internal class used to represent a ConcurrentCall's ID. It is not a unique identification method,
     * but by default new CallIDs will be given a unique ID number. Multiple ConcurrentCalls may have the same CallID,
     * however.
     */
    public static class CallID implements Comparable<CallID> {
        private static HashSet<Integer> freeIDs = new HashSet<>();
        private static int nextID = 0;

        /**
         * Integer identification variable. Unique per CallID object.
         */
        public final int ID;

        /**
         * CallID Constructor. Creates a new CallID and gives it a unique identification number. ID numbers may be
         * re-used from garbage-collected CallID objects.
         */
        public CallID() {
            if(freeIDs.isEmpty()) ID = nextID++;
            else {
                int id = freeIDs.iterator().next();
                freeIDs.remove(id);
                ID = id;
            }
        }

        @Override public int compareTo(CallID o) { return (ID < o.ID) ? -1 : ( (ID > o.ID) ? 1 : 0 ); }
        @Override public int hashCode() { return ID; }
        @Override public String toString() { return ""+ID; }

        @Override protected void finalize() throws Throwable {
            freeIDs.add(ID);
            super.finalize();
        }
    }

    private static class MethodBody implements Comparable<MethodBody> {
        public final int priority;
        public final Method method;
        public MethodBody(int priority, Method method) {
            this.priority = priority; this.method = method;
        }
        @Override public int compareTo(MethodBody o) {
            return (priority < o.priority) ? 1 : ( (priority > o.priority) ? -1 : 0 );
        }
    }

    /**
     * Body Method Annotation. This annotation is used when determining what methods to run in a calling object. More
     * than one method in a class can be annotated with this annotation, but at least one method must be annotated.
     * Methods annotated with this annotation must have no parameters.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    public @interface Body {
        int value() default 0;
    }

    /**
     * Result Annotation. This annotation is used to pair a resulting variable in the calling object to a
     * result-handling method in another object (possibly the same object as the calling object, however). One and only
     * one variable in the calling object must be annotated with this annotation, and one and only one method that
     * accepts a single parameter of the same type as the annotated variable must be annotated in the result-handling
     * object.
     */
    @Target({ElementType.METHOD, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    public @interface Result {

    }
}
