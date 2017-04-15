package plu.red.reversi.core.util;

import javax.swing.*;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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
 *   be ran by the ConcurrentCall internal system. They must have no parameters. There may be any number of callMethods
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

    private static final HashMap<CallID, HashSet<ConcurrentCall>> calls = new HashMap<>();
    private static final HashSet<ResultMethodRunnable> syncBuffer = new HashSet<>();

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

        // Scan for BodyMethods
        Method[] methods = callObject.getClass().getMethods();
        TreeSet<BodyMethodStruct> methodSet = new TreeSet<>();
        for(Method method : methods) {
            if(method.isAnnotationPresent(BodyMethod.class)) {

                int priority = method.getDeclaredAnnotation(BodyMethod.class).value();

                // Check to make sure there are no parameters
                if(method.getParameterCount() > 0)
                    throw new IllegalArgumentException("Body Method in a ConcurrentCall cannot have parameters");

                // Add our BodyMethod
                methodSet.add(new BodyMethodStruct(priority, method));
            }
        }

        if(methodSet.isEmpty())
            throw new IllegalArgumentException("No BodyMethods found in class of type "+callObject.getClass().getName());

        // Scan for ResultFields
        Field[] fields = callObject.getClass().getFields();
        HashMap<Integer, Field> resultFields = new HashMap<>();
        for(Field field : fields) {
            if(field.isAnnotationPresent(ResultField.class)) {

                int resultID = field.getDeclaredAnnotation(ResultField.class).value();

                // Check for ID conflicts
                if(resultFields.containsKey(resultID)) {
                    Field oldField = resultFields.get(resultID);
                    throw new IllegalArgumentException("ID Conflict in annotated ResultFields for class " + callObject.getClass().getName() +
                            ": fields '" + oldField.getName() + "' and '" + field.getName() + "' both have ID " + resultID + ".");
                }

                // Add our ResultField
                resultFields.put(resultID, field);
            }
        }

        // Make sure we found at least one ResultField
        if(resultFields.isEmpty())
            throw new IllegalArgumentException("No ResultFields found in class of type "+callObject.getClass().getName());

        // Scan for ResultMethods
        methods = resultObject.getClass().getMethods();
        HashSet<ResultMethodStruct> resultMethods = new HashSet<>();
        for(Method method : methods) {
            if(method.isAnnotationPresent(ResultMethod.class)) {

                InvokeType sync = method.getDeclaredAnnotation(ResultMethod.class).value();

                // Get ResultParameter IDs
                Parameter[] params = method.getParameters();
                int[] resultIDs = new int[params.length];
                for(int i = 0; i < params.length; i++) {
                    if(params[i].isAnnotationPresent(ResultParameter.class))
                        resultIDs[i] = params[i].getAnnotation(ResultParameter.class).value();
                    else
                        resultIDs[i] = 0;
                }

                // Check to make Class types match with already scanned ResultFields
                for(int i = 0; i < resultIDs.length; i++) {
                    if(resultFields.containsKey(resultIDs[i])) {
                        Field f = resultFields.get(resultIDs[i]);
                        if(!method.getParameterTypes()[i].isAssignableFrom(f.getType()))
                            throw new IllegalArgumentException("ResultMethod parameter " + i + " of type " + method.getParameterTypes()[i].getName() +
                                    " cannot be assigned from ResultField of type " + f.getType().getName() + ".");
                    } else throw new IllegalArgumentException("No ResultField with ID " + resultIDs[i] + " found for ResultMethod '" + method.getName() +
                            "' of class " + resultObject.getClass().getName() + ".");
                }

                // Add our ResultMethod
                resultMethods.add(new ResultMethodStruct(resultIDs, sync, method));
            }
        }

        // Create our call and register it
        ConcurrentCall call = new ConcurrentCall(id, callObject, methodSet, resultFields, resultObject, resultMethods);
        synchronized(calls) {
            if (!calls.containsKey(id)) calls.put(id, new HashSet<>());
            calls.get(id).add(call);
        }
        return id;
    }

    /**
     * Stops one or more ConcurrentCalls. Forcibly stops the ConcurrentCalls that are identified with the given CallID.
     *
     * @param id A CallID to identify calls with
     */
    public static void stopCall(CallID id) {
        synchronized(calls) {
            if (calls.containsKey(id)) {
                HashSet<ConcurrentCall> callSet = calls.get(id);
                for (ConcurrentCall call : callSet)
                    call.stop();
                calls.remove(id);
            }
        }
    }

    /**
     * Synchronized waiting ResultMethods. Iterates through and invokes all ResultMethods that have been told to wait
     * for synchronization. This method should usually be called from the main Thread.
     */
    public static void syncCalls() {
        synchronized(syncBuffer) {
            // Do all the stored ResultMethod calls
            for (ResultMethodRunnable method : syncBuffer) {
                method.run();
            }
            syncBuffer.clear();
        }
    }


    public final CallID id;

    private final Thread thread;
    private final Object callObject;
    private final TreeSet<BodyMethodStruct> callMethods;
    private final HashMap<Integer, Field> resultFields;
    private final Object resultObject;
    private final HashSet<ResultMethodStruct> resultMethods;

    private volatile boolean stopped = false;

    private ConcurrentCall(CallID id, Object callObject, TreeSet<BodyMethodStruct> methods, HashMap<Integer, Field> resultFields, Object resultObject, HashSet<ResultMethodStruct> resultMethods) {
        this.id = id;
        this.callObject = callObject;
        this.callMethods = methods;
        this.resultFields = resultFields;
        this.resultObject = resultObject;
        this.resultMethods = resultMethods;
        thread = new Thread(this);
        thread.start();
    }

    private void cleanup() {
        synchronized(calls) {
            if (calls.containsKey(id)) calls.get(id).remove(this);
        }
    }

    private void stop() {
        stopped = true;
        thread.interrupt();
    }

    @Override
    public void run() {

        try {

            // Run the BodyMethods
            for (BodyMethodStruct method : callMethods) {

                // Try to invoke BodyMethod
                try {
                    method.method.invoke(callObject);
                } catch (IllegalAccessException ex) {
                    throw new IllegalArgumentException("Cannot access BodyMethod " + method.method.getName());
                } catch (InvocationTargetException ex) {
                    throw new RuntimeException(ex.getMessage());
                }

                // Check for Stop flag
                if (stopped) return;
            }

            // Run the ResultMethods
            for (ResultMethodStruct method : resultMethods) {

                // Get our ResultFields
                Object[] values = new Object[method.ids.length];
                for(int i = 0; i < method.ids.length; i++) {
                    Field field = resultFields.get(method.ids[i]);
                    try { // Try to get the ResultField value
                        values[i] = field.get(callObject);
                    } catch(IllegalAccessException ex) {
                        throw new IllegalArgumentException("Cannot access ResultField " + field.getName());
                    }
                }

                ResultMethodRunnable runnable = new ResultMethodRunnable(method.method, resultObject, values);

                switch(method.sync) {
                    default:
                    case IMMEDIATE:
                        // Try to invoke ResultMethod
                        runnable.run();
                        break;
                    case SYNC:
                        // Cache the ResultMethod for calling later
                        synchronized(syncBuffer) {
                            syncBuffer.add(runnable);
                        }
                        break;
                    case SWING:
                        // Tell Swing to invoke later
                        SwingUtilities.invokeLater(runnable);
                        break;
                }

                // Check for Stop flag
                if (stopped) return;
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

    private static class BodyMethodStruct implements Comparable<BodyMethodStruct> {
        final int priority;
        final Method method;
        BodyMethodStruct(int priority, Method method) {
            this.priority = priority; this.method = method;
        }
        @Override public int compareTo(BodyMethodStruct o) {
            return (priority < o.priority) ? 1 : ( (priority > o.priority) ? -1 : 0 );
        }
    }

    private static class ResultMethodStruct {
        final int[] ids;
        final InvokeType sync;
        final Method method;
        ResultMethodStruct(int[] ids, InvokeType sync, Method method) {
            this.ids = ids; this.sync = sync; this.method = method;
        }
    }

    private static class ResultMethodRunnable implements Runnable {
        final Method method;
        final Object resultObject;
        final Object[] resultValues;
        ResultMethodRunnable(Method method, Object resultObject, Object[] resultValues) {
            this.method = method; this.resultObject = resultObject; this.resultValues = resultValues;
        }

        @Override
        public void run() {
            try {
                method.invoke(resultObject, resultValues);
            } catch (IllegalAccessException ex) {
                throw new IllegalArgumentException("Cannot access ResultMethod " + method.getName());
            } catch (InvocationTargetException ex) {
                throw new RuntimeException(ex.getMessage());
            }
        }

    }

    /**
     * Body Method Annotation. This annotation is used when determining what callMethods to run in a calling object. More
     * than one method in a class can be annotated with this annotation, but at least one method must be annotated.
     * Methods annotated with this annotation must have no parameters. The Body annotation also has an optional
     * parameter of <code>priority</code> which defaults to <code>0</code>. Higher numbers means that the annotated
     * method will be invoked earlier.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    public @interface BodyMethod {
        int value() default 0;
    }

    /**
     * ResultField Annotation. This annotation is used to mark a field in the calling object as being a container
     * for the end result of the ConcurrentCall. The annotated field will be paired with a method in the result
     * object, and passed as a parameter to said method. Optionally, for complex systems of multiple result fields
     * and result methods, an <code>ID</code> may be specified for the ResultField annotation in order to numerically
     * pair with a ResultMethod annotation.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ResultField {
        int value() default 0;
    }

    /**
     * InvokeType Enumeration. Used to determine when to invoke a ResultMethod. <code>IMMEDIATE</code> will invoke the
     * ResultMethod as soon as the Call is finished, <code>SYNC</code> will wait to invoke the ResultMethod until
     * <code>syncCalls()</code> is called, and <code>SWING</code> will schedule the ResultMethod with Swing's
     * <code>invokeLater()</code>.
     */
    public enum InvokeType {
        IMMEDIATE,
        SYNC,
        SWING
    }

    /**
     * ResultMethod Annotation. This annotation is used to mark a method in the result object as the end target for
     * a result variable/s. By default, a ResultMethod will execute immediately after the ConcurrentCall has run, on
     * the same thread as the ConcurrentCall. Instead, the <code>value</code> parameter - which defaults to
     * <code>InvokeType.IMMEDIATE</code> - can be used to specify that the ConcurrentCall will not immediately call the
     * marked method, but instead wait until a later point in time.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ResultMethod {
        InvokeType value() default InvokeType.IMMEDIATE;
    }

    /**
     * ResultParameter Annotation. This annotation is used to specify that certain parameters in a ResultMethod have
     * different IDs to map to a ResultField, rather than the default of <code>0</code>. If this annotation is omitted
     * from a parameter, then that parameter will be assumed to map to a ResultField with an ID of <code>0</code>.
     */
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ResultParameter {
        int value();
    }


}
