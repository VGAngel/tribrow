package com.pro.game.example.test.jme3test.app;

import com.jme3.math.Vector3f;
import com.jme3.util.TempVars;

public class TestTempVars {

    private static final int ITERATIONS = 10000000;
    private static final int NANOS_TO_MS = 1000000;

    private static final Vector3f sumCompute = new Vector3f();

    public static void main(String[] args) {
        long milliseconds, nanos;

        for (int i = 0; i < 4; i++) {
            System.gc();
        }

//        sumCompute.set(0, 0, 0);
//        long nanos = System.nanoTime();
//        for (int i = 0; i < ITERATIONS; i++) {
//            recursiveMethod(0);
//        }
//        long milliseconds = (System.nanoTime() - nanos) / NANOS_TO_MS;
//        System.out.println("100 million TempVars calls with 5 recursions: " + milliseconds + " ms");
//        System.out.println(sumCompute);

        sumCompute.set(0, 0, 0);
        nanos = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            methodThatUsesTempVars();
        }
        milliseconds = (System.nanoTime() - nanos) / NANOS_TO_MS;
        System.out.println("100 million TempVars calls: " + milliseconds + " ms");
        System.out.println(sumCompute);

        sumCompute.set(0, 0, 0);
        nanos = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            methodThatUsesAllocation();
        }
        milliseconds = (System.nanoTime() - nanos) / NANOS_TO_MS;
        System.out.println("100 million allocation calls: " + milliseconds + " ms");
        System.out.println(sumCompute);

        nanos = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            System.gc();
        }
        milliseconds = (System.nanoTime() - nanos) / NANOS_TO_MS;
        System.out.println("cleanup time after allocation calls: " + milliseconds + " ms");
    }

    public static void methodThatUsesAllocation() {
        Vector3f vector = new Vector3f();
        vector.set(0.1f, 0.2f, 0.3f);
        sumCompute.addLocal(vector);
    }

    public static void recursiveMethod(int recurse) {
        TempVars vars = TempVars.get();
        {
            vars.vect1.set(0.1f, 0.2f, 0.3f);

            if (recurse < 4) {
                recursiveMethod(recurse + 1);
            }

            sumCompute.addLocal(vars.vect1);
        }
        vars.release();
    }

    public static void methodThatUsesTempVars() {
        TempVars vars = TempVars.get();
        {
            vars.vect1.set(0.1f, 0.2f, 0.3f);
            sumCompute.addLocal(vars.vect1);
        }
        vars.release();
    }
}
