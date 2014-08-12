package mythruna;

import groovy.lang.Closure;
import org.progeeks.util.StringUtils;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestGroovy {
    private static List test = new ArrayList();

    public TestGroovy() {
    }

    public static void main(String[] args) throws Exception {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("groovy");

        Bindings bindings = engine.createBindings();
        bindings.put("test", test);
        engine.setBindings(bindings, 100);

        String initScript = StringUtils.readStringResource(TestGroovy.class, "Test2.groovy");

        engine.eval(initScript);

        System.out.println("test:" + test);

        for (Iterator i$ = test.iterator(); i$.hasNext(); ) {
            Object o = i$.next();

            if ((o instanceof Closure)) {
                Closure c = (Closure) o;
                System.out.println("Closure delegate:" + c.getDelegate());
                System.out.println("Closure owner:" + c.getOwner());
                System.out.println("Setting property: args");
                c.setProperty("args", "My dog has fleas.");
                System.out.println("prop args:" + c.getProperty("args"));
                System.out.println("Calling closure:" + c);
                c.call();
            }
        }
    }
}