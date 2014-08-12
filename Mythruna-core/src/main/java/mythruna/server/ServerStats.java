package mythruna.server;

import com.jme3.math.Vector3f;
import org.progeeks.json.JsonParser;
import org.progeeks.json.JsonPrinter;
import org.progeeks.util.PropertyAccess;
import org.progeeks.util.SimpleExpressionLanguage;
import org.progeeks.util.TemplateExpressionProcessor;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerStats {

    public static final int STATS_VERSION = 1;
    public static final String STATS_FILE = "stats.json";
    private static SimpleExpressionLanguage el = TemplateExpressionProcessor.getDefaultProcessor().getExpressionLanguage();
    private File file;
    private Map values;
    private JsonParser jsonParser = new JsonParser();
    private JsonPrinter jsonPrinter = new JsonPrinter();

    public ServerStats(File file) throws IOException {
        this.file = file;

        this.values = ((Map) loadJson());
        if (this.values == null) {
            this.values = new HashMap();
            this.values.put("version", Integer.valueOf(1));
        }
    }

    public synchronized void set(String property, Object value) {
        List exps = el.parseExpression(property);
        Object o = this.values;

        PropertyAccess access = el.getPropertyAccess();

        SimpleExpressionLanguage.ExpressionElement last = null;
        Object lastObject = this.values;
        for (Iterator i = exps.iterator(); i.hasNext(); ) {
            SimpleExpressionLanguage.ExpressionElement e = (SimpleExpressionLanguage.ExpressionElement) i.next();

            if (!i.hasNext()) {
                last = e;
                break;
            }

            o = e.get(access, o);
            if ((o == null) && ((e instanceof SimpleExpressionLanguage.NamedExpressionElement))) {
                SimpleExpressionLanguage.NamedExpressionElement nel = (SimpleExpressionLanguage.NamedExpressionElement) e;

                Map sub = new HashMap();
                ((Map) lastObject).put(nel.getName(), sub);
                o = sub;
            }
            lastObject = o;
        }

        last.set(access, o, value);
    }

    public synchronized <T> T get(String property) {
        return (T) el.getProperty(this.values, property);
    }

    public synchronized List getList(String property) {
        List l = (List) get(property);
        if (l == null) {
            l = new CopyOnWriteArrayList();
            set(property, l);
        }
        return l;
    }

    public synchronized void add(String property, Object value, int maxSize) {
        List l = getList(property);
        l.add(value);
        while ((maxSize > 0) && (l.size() > maxSize))
            l.remove(0);
    }

    public synchronized void add(String property, int index, Object value, int maxSize, boolean fromEnd) {
        List l = getList(property);
        l.add(index, value);
        while ((maxSize > 0) && (l.size() > maxSize)) {
            if (fromEnd)
                l.remove(l.size() - 1);
            else
                l.remove(0);
        }
    }

    public synchronized int increment(String property) {
        Integer i = (Integer) get(property);
        if (i == null) {
            i = Integer.valueOf(0);
        }
        set(property, i = Integer.valueOf(i.intValue() + 1));
        return i.intValue();
    }

    public synchronized void setLocation(String property, Vector3f value) {
        set(property + ".x", Float.valueOf(value.x));
        set(property + ".y", Float.valueOf(value.y));
        set(property + ".z", Float.valueOf(value.z));
    }

    public synchronized Float getFloat(String property) {
        Number n = (Number) get(property);
        if (n == null)
            return null;
        return Float.valueOf(n.floatValue());
    }

    public synchronized Long getLong(String property) {
        Number n = (Number) get(property);
        if (n == null)
            return null;
        return Long.valueOf(n.longValue());
    }

    public synchronized Vector3f getLocation(String property) {
        Float x = getFloat(property + ".x");
        Float y = getFloat(property + ".y");
        Float z = getFloat(property + ".z");
        if ((x == null) || (y == null) || (z == null))
            return null;
        return new Vector3f(x.floatValue(), y.floatValue(), z.floatValue());
    }

    protected Object loadJson() throws IOException {
        if (!this.file.exists())
            return null;
        try (FileReader in = new FileReader(this.file)) {
            return this.jsonParser.parse(in);
        }
    }

    protected void writeJson(Object o) throws IOException {
        try (FileWriter out = new FileWriter(this.file)) {
            this.jsonPrinter.write(o, out);
        }
    }

    public synchronized void save() {
        try {
            writeJson(this.values);
        } catch (IOException e) {
            throw new RuntimeException("Error saving server stats", e);
        }
    }

    public String toString() {
        return "DefaultPlayer[" + this.values + "]";
    }
}