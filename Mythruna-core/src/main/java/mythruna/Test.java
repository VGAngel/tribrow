package mythruna;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Test {
    public static List<Double> testRaw = new ArrayList();
    public static List<Double> testConc = new CopyOnWriteArrayList();

    public Test() {
    }

    public static double testPlain() {
        int num = testRaw.size();
        double val = 0.0D;
        for (int i = 0; i < num; i++) {
            val += ((Double) testRaw.get(i)).doubleValue();
        }
        return val;
    }

    public static double testForEach() {
        double val = 0.0D;
        for (Double d : testRaw)
            val += d.doubleValue();
        return val;
    }

    public static double testSync() {
        int num = testRaw.size();
        double val = 0.0D;
        for (int i = 0; i < num; i++) {
            synchronized (testRaw) {
                val += ((Double) testRaw.get(i)).doubleValue();
            }
        }
        return val;
    }

    public static double testConcurrent() {
        double val = 0.0D;
        for (Double d : testConc)
            val += d.doubleValue();
        return val;
    }

    public static void accessTimingTests() {
        long plain = 0L;
        long forEach = 0L;
        long sync = 0L;
        long conc = 0L;

        int total = 4000;
        for (int i = 0; i < total; i++) {
            System.out.println("Iteration: " + (i + 1) + " / " + total);
            long start = System.nanoTime();

            testPlain();
            long end = System.nanoTime();
            plain += end - start;
            System.out.println("     indexed:" + (end - start / 1000000.0D) + " ms");

            start = System.nanoTime();
            testForEach();
            end = System.nanoTime();
            forEach += end - start;
            System.out.println("     forEach:" + (end - start / 1000000.0D) + " ms");

            start = System.nanoTime();
            testSync();
            end = System.nanoTime();
            sync += end - start;
            System.out.println("        sync:" + (end - start / 1000000.0D) + " ms");

            start = System.nanoTime();
            testConcurrent();
            end = System.nanoTime();
            conc += end - start;
            System.out.println("        conc:" + (end - start / 1000000.0D) + " ms");
        }

        System.out.println("--------results-----------------------");
        System.out.println("  plain average:" + plain / total / 1000000.0D + " ms");
        System.out.println("forEach average:" + forEach / total / 1000000.0D + " ms");
        System.out.println("   sync average:" + sync / total / 1000000.0D + " ms");
        System.out.println("   conc average:" + conc / total / 1000000.0D + " ms");
    }

    public static void main(String[] args)
            throws Exception {
    }

    static {
        for (int i = 0; i < 100000; i++) {
            testRaw.add(Double.valueOf(Math.random()));
        }
        testConc.addAll(testRaw);
    }
}