package mythruna.script;

public class ExecutionContext {

    private static ThreadLocal<Object> environment = new ThreadLocal();

    public ExecutionContext() {
    }

    public static void setEnvironment(Object env) {
        environment.set(env);
    }

    public static Object getEnvironment() {
        return environment.get();
    }
}