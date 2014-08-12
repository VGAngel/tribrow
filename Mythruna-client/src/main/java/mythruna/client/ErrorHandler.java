package mythruna.client;

import mythruna.GameConstants;
import org.progeeks.util.log.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ErrorHandler {
    static Log log = Log.getLog();

    private static volatile boolean initialized = false;
    private static ExceptionHandler exceptionHandler = new ExceptionHandler();

    private static File errorLog = null;

    public ErrorHandler() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        System.setProperty("sun.awt.exception.handler", ExceptionHandler.class.getName());

        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);

        initialized = true;
    }

    public static void registerExceptionHandler() {
        initialize();

        Thread.UncaughtExceptionHandler original = Thread.currentThread().getUncaughtExceptionHandler();

        Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler(original));
    }

    private static void saveException(Thread t, Throwable e) throws IOException {
        System.out.println("Uncaught exception from thread:" + t);
        e.printStackTrace();

        boolean append = true;
        if (errorLog == null) {
            long time = System.currentTimeMillis();
            errorLog = new File("error-" + time + ".log");
            append = false;
        }

        System.out.println("Writing exception data to file:" + errorLog + "   append=" + append);
        FileWriter fOut = new FileWriter(errorLog, append);
        PrintWriter out = new PrintWriter(fOut);
        try {
            out.println("Uncaught exception from thread:" + t);
            e.printStackTrace(out);

            MainStart app = MainStart.instance;
            if (app == null) {
                out.println("Application was not initialized.");
            } else {
                if (append) {
                    out.println();
                    app.writeMemInfo(out);
                    out.println();
                    app.writeStats(out);
                    out.println();
                } else {
                    out.println();
                    out.println("Build version:" + GameConstants.buildVersion());

                    out.println();
                    app.writeAppInfo(out);
                    out.println();
                }

                MainStart.showError("Unexpected Error", "Error:" + e.getMessage() + "\nError log written to:" + errorLog.getAbsoluteFile());
            }
        } catch (Throwable ex) {
            log.error("Error writing log info", ex);
        } finally {
            out.close();
        }
    }

    public static void handle(Throwable t, boolean chainHandling) {
        Thread thread = Thread.currentThread();
        Thread.UncaughtExceptionHandler ueh = thread.getUncaughtExceptionHandler();
        if (!(ueh instanceof ExceptionHandler)) {
            try {
                saveException(thread, t);
            } catch (Throwable bad) {
                System.err.println("Error saving exception information:" + bad);
                bad.printStackTrace();
                log.error("Error saving exception information:" + bad);
            }
        }
        if (chainHandling)
            ueh.uncaughtException(thread, t);
        else
            log.info("Not chaining.");
    }

    public static void handle(Throwable t) {
        handle(t, true);
    }

    public static class ExceptionHandler implements Thread.UncaughtExceptionHandler {
        private Thread.UncaughtExceptionHandler original;

        public ExceptionHandler() {
        }

        public ExceptionHandler(Thread.UncaughtExceptionHandler original) {
            this.original = original;
        }

        public void uncaughtException(Thread t, Throwable e) {
            try {
                ErrorHandler.saveException(t, e);
            } catch (Throwable bad) {
                System.err.println("Error saving exception information:" + bad);
                bad.printStackTrace();
                ErrorHandler.log.error("Error saving exception information:" + bad);
            }

            try {
                if (this.original != null)
                    this.original.uncaughtException(t, e);
            } catch (Throwable bad) {
                System.err.println("Error delegating uncaught exception:" + bad);
                bad.printStackTrace();
                ErrorHandler.log.error("Error delegating uncaught exception:" + bad);
            }
        }

        public void handle(Throwable t) {
            try {
                ErrorHandler.saveException(Thread.currentThread(), t);
            } catch (Throwable bad) {
                System.err.println("Error saving exception information:" + bad);
                bad.printStackTrace();
                ErrorHandler.log.error("Error saving exception information:" + bad);
            }
        }
    }
}