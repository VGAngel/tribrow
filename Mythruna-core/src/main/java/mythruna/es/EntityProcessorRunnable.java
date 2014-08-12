package mythruna.es;

import org.progeeks.util.log.Log;

public class EntityProcessorRunnable implements Runnable {

    static Log log = Log.getLog();
    private EntityProcessor proc;
    private EntityData ed;

    public EntityProcessorRunnable(EntityProcessor proc, EntityData ed) {
        this.proc = proc;
        this.ed = ed;
    }

    public void run() {
        try {
            this.proc.run(this.ed);
        } catch (Throwable t) {
            log.error("Error executing:" + this.proc, t);
        }
    }
}