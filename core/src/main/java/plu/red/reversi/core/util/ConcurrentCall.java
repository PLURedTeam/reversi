package plu.red.reversi.core.util;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;


public class ConcurrentCall implements Runnable {

    private static HashMap<CallID, HashSet<ConcurrentCall>> calls = new HashMap<>();

    public static CallID createCall(Call callObject, Object resultObject) {
        return createCall(callObject, resultObject, new CallID());
    }

    public static CallID createCall(Call callObject, Object resultObject, CallID id) {

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

        ConcurrentCall call = new ConcurrentCall(callObject, methodSet, callField, resultObject, resultMethod);
        if(!calls.containsKey(id)) calls.put(id, new HashSet<>());
        calls.get(id).add(call);
        return id;
    }

    public static void stopCall(CallID id) {
        if(calls.containsKey(id)) {
            HashSet<ConcurrentCall> callSet = calls.get(id);
            for(ConcurrentCall call : callSet)
                call.stop();
        }
    }



    private final Thread thread;
    private final Call callObject;
    private final TreeSet<MethodBody> methods;
    private final Field callField;
    private final Object resultObject;
    private final Method resultMethod;

    private volatile boolean stopped = false;

    private ConcurrentCall(Call callObject, TreeSet<MethodBody> methods, Field callField, Object resultObject, Method resultMethod) {
        this.callObject = callObject;
        this.methods = methods;
        this.callField = callField;
        this.resultObject = resultObject;
        this.resultMethod = resultMethod;
        thread = new Thread(this);
        thread.start();
    }

    private void stop() {
        stopped = true;
        callObject.stop();
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
            throw ex;
        }
    }

    public static class CallID implements Comparable<CallID> {
        private static HashSet<Integer> freeIDs = new HashSet<>();
        private static int nextID = 0;

        public final int ID;

        public CallID() {
            if(freeIDs.isEmpty()) ID = nextID++;
            else {
                int id = freeIDs.iterator().next();
                freeIDs.remove(id);
                ID = id;
            }
        }

        public CallID(CallID other) { ID = other.ID; }

        @Override public int compareTo(CallID o) { return (ID < o.ID) ? -1 : ( (ID > o.ID) ? 1 : 0 ); }
        @Override public int hashCode() { return ID; }
        @Override public String toString() { return ""+ID; }
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

    public interface Call {
        void stop();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    public @interface Body {
        int value() default 0;
    }

    @Target({ElementType.METHOD, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    public @interface Result {

    }
}
