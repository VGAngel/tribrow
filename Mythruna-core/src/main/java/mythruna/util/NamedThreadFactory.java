package mythruna.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class NamedThreadFactory implements ThreadFactory {

    public NamedThreadFactory(String baseName) {
        this(baseName, true, Executors.defaultThreadFactory());
    }

    public NamedThreadFactory(String baseName, boolean daemon) {
        this(baseName, daemon, Executors.defaultThreadFactory());
    }

    public NamedThreadFactory(String baseName, boolean daemon, ThreadFactory delegate) {
        this.baseName = baseName;
        this.daemon = daemon;
        _flddelegate = delegate;
    }

    public Thread newThread(Runnable r) {
        Thread t = _flddelegate.newThread(r);
        t.setName((new StringBuilder()).append(baseName).append(t.getName()).toString());
        t.setDaemon(daemon);
        return t;
    }

    private ThreadFactory _flddelegate;
    private String baseName;
    private boolean daemon;
}
