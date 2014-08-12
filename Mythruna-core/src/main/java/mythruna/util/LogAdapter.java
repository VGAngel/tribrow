package mythruna.util;

import org.progeeks.util.log.Log;
import org.progeeks.util.log.LogLevel;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.*;

public class LogAdapter extends Handler {
    private Set<String> seen = new HashSet();

    public LogAdapter() {
    }

    public static void initialize() {
        Logger root = LogManager.getLogManager().getLogger("");

        for (Handler h : root.getHandlers()) {
            root.removeHandler(h);
        }

        LogAdapter adapter = new LogAdapter();
        root.addHandler(adapter);

        Log rootLog = Log.getRootLog();
        LogLevel main = rootLog.getLogLevel();

        if (main != null) {
            System.out.println("Setting root JUL log level to:" + toJUL(main));
            root.setLevel(toJUL(main));
        }

        for (Iterator it = Log.categoryNames(); it.hasNext(); ) {
            String s = (String) it.next();
            Log l = Log.getLog(s);
            LogLevel lvl = l.getLogLevel();
            if (lvl != null) {
                setJulLevel(s, lvl);
            }
        }
    }

    public void close() {
    }

    public void flush() {
    }

    protected static String foldMessage(LogRecord record) {
        String msg = record.getMessage();
        Object[] parms = record.getParameters();
        if ((parms != null) && (parms.length > 0))
            msg = MessageFormat.format(msg, record.getParameters());
        return msg;
    }

    protected static LogLevel convert(Level lvl) {
        if (lvl == Level.CONFIG)
            return LogLevel.INFO;
        if (lvl == Level.FINE)
            return LogLevel.DEBUG;
        if (lvl == Level.FINER)
            return LogLevel.TRACE;
        if (lvl == Level.FINEST)
            return LogLevel.TRACE;
        if (lvl == Level.INFO)
            return LogLevel.INFO;
        if (lvl == Level.OFF)
            return LogLevel.OFF;
        if (lvl == Level.SEVERE)
            return LogLevel.ERROR;
        if (lvl == Level.WARNING)
            return LogLevel.WARN;
        return LogLevel.INFO;
    }

    protected static Level toJUL(LogLevel lvl) {
        if (lvl == LogLevel.TRACE)
            return Level.FINER;
        if (lvl == LogLevel.DEBUG)
            return Level.FINE;
        if (lvl == LogLevel.ERROR)
            return Level.SEVERE;
        if (lvl == LogLevel.FATAL)
            return Level.SEVERE;
        if (lvl == LogLevel.INFO)
            return Level.INFO;
        if (lvl == LogLevel.OFF)
            return Level.OFF;
        if (lvl == LogLevel.WARN)
            return Level.WARNING;
        return Level.INFO;
    }

    protected static void setJulLevel(String logger, LogLevel lvl) {
        Level newLevel = toJUL(lvl);
        System.out.println("Setting JUL Log:" + logger + " level to:" + newLevel);
        Logger l = Logger.getLogger(logger);
        if (l == null)
            return;
        l.setLevel(newLevel);
    }

    public void publish(LogRecord record) {
        String message = foldMessage(record);
        String logger = record.getLoggerName();
        if (logger == null) {
            logger = "";
        }
        Level lvl = record.getLevel();

        Log log = Log.getLog(logger);
        LogLevel msgLevel = convert(lvl);

        if (this.seen.add(logger)) {
            LogLevel current = log.getEffectiveLogLevel();

            if (!msgLevel.isGreaterOrEqual(current)) {
                setJulLevel(logger, current);
            }
        }

        log.log(message, msgLevel, record.getThrown());
    }
}