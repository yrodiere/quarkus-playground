package org.acme.caller;

import org.acme.callee.Callee;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Caller {

    private final Method method;

    public Caller(String methodName) {
        System.out.println("Using reflection to get method " + methodName);
        try {
            method = Callee.class.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    public String call() {
        try {
            return (String) method.invoke(new Callee());
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

}
