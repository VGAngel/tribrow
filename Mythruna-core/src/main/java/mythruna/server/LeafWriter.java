package mythruna.server;

import mythruna.db.LeafChangeEvent;
import mythruna.db.LeafChangeListener;
import mythruna.db.LeafData;
import mythruna.db.WorldDatabase;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LeafWriter implements Runnable {

    private Object marker = new Object();
    private WorldDatabase worldDb;
    private Map<LeafData, Object> changed = new ConcurrentHashMap();

    public LeafWriter(WorldDatabase worldDb) {
        this.worldDb = worldDb;

        worldDb.addLeafChangeListener(new LeafObserver());
    }

    public void flush() {
        System.out.println("Storing " + this.changed.size() + " leafs.");
        for (Iterator i = this.changed.keySet().iterator(); i.hasNext(); ) {
            LeafData leaf = (LeafData) i.next();
            this.worldDb.markChanged(leaf);
            i.remove();
        }
    }

    public void run() {
        if (this.changed.isEmpty()) {
            return;
        }

        try {
            flush();
        } catch (Exception e) {
            System.out.println("Error writing leaf objects.");
            e.printStackTrace();
        }
    }

    protected class LeafObserver implements LeafChangeListener {
        protected LeafObserver() {
        }

        public void leafChanged(LeafChangeEvent event) {
            LeafWriter.this.changed.put(event.getLeaf(), LeafWriter.this.marker);
        }
    }
}