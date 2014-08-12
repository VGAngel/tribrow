package mythruna.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ReportSystem {
    public static final String REPORT_CACHE = "cache";
    private static Map<String, List<Reporter>> reporters = new ConcurrentHashMap();

    public ReportSystem() {
    }

    private static List<Reporter> getList(String reportType) {
        List list = (List) reporters.get(reportType);
        if (list == null) {
            list = new CopyOnWriteArrayList();
            reporters.put(reportType, list);
        }
        return list;
    }

    public static void registerCacheReporter(Reporter r) {
        registerReporter("cache", r);
    }

    public static void registerReporter(String type, Reporter r) {
        getList(type).add(r);
    }

    public static void printReport(String type, PrintWriter out) {
        for (Reporter r : getList(type))
            r.printReport(type, out);
    }

    public static String getReport(String type) {
        StringWriter sOut = new StringWriter();
        PrintWriter out = new PrintWriter(sOut);
        printReport(type, out);
        out.close();
        return sOut.toString();
    }
}