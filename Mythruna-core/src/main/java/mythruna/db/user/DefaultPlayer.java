package mythruna.db.user;

import com.jme3.math.Vector3f;
import mythruna.PlayerData;
import org.progeeks.util.PropertyAccess;
import org.progeeks.util.SimpleExpressionLanguage;
import org.progeeks.util.TemplateExpressionProcessor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DefaultPlayer implements PlayerData {

    private DefaultUserDatabase parent;
    private Map values;
    private SimpleExpressionLanguage el = TemplateExpressionProcessor.getDefaultProcessor().getExpressionLanguage();

    public DefaultPlayer(DefaultUserDatabase parent, Map values) {
        this.parent = parent;
        this.values = values;
    }

    public synchronized void set(String property, Object value) {
        List exps = this.el.parseExpression(property);
        Object o = this.values;

        PropertyAccess access = this.el.getPropertyAccess();

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
        return (T) this.el.getProperty(this.values, property);
    }

    public synchronized int increment(String property) {
        Integer i = (Integer) get(property);
        if (i == null) {
            i = Integer.valueOf(0);
        }
        set(property, i = Integer.valueOf(i.intValue() + 1));
        return i.intValue();
    }

    public synchronized void addValue(String property, long value) {
        Long old = getLong(property);
        if (old == null)
            set(property, Long.valueOf(value));
        else
            set(property, Long.valueOf(old.longValue() + value));
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

    public synchronized void save() {
        this.parent.saveUser(this.values);
    }

    public String toString() {
        return "DefaultPlayer[" + this.values + "]";
    }
}