package mythruna.client;

public class TimeLog {
    private static long start = System.nanoTime();

    public TimeLog() {
    }

    public static void log(String context, String log) {
        long time = System.nanoTime() - start;
        System.out.println("TIME:" + time / 1000000.0D + " ms -- (" + context + ") " + log);
    }
}