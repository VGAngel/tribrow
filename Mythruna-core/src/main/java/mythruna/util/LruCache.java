package mythruna.util;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class LruCache<K, V> implements Iterable<V> {

    private AtomicLong accessCounter = new AtomicLong();
    private Map<K, CacheEntry<V>> cache = new ConcurrentHashMap();
    private int depth;
    private String name;

    public LruCache(int depth) {
        this("Unnamed", depth);
    }

    public LruCache(String name, int depth) {
        this.name = name;
        this.depth = depth;

        ReportSystem.registerCacheReporter(new MemReporter());
    }

    public Iterator<V> iterator() {
        return new EntryIterator(this.cache.values().iterator());
    }

    public boolean containsKey(Object key) {
        return this.cache.containsKey(key);
    }

    public V get(Object key) {
        CacheEntry result = (CacheEntry) this.cache.get(key);
        if (result == null)
            return null;
        result.access(this.accessCounter.getAndIncrement());
        return (V) result.getValue();
    }

    public V put(K key, V value) {
        CacheEntry e = new CacheEntry(value);
        e.access(this.accessCounter.getAndIncrement());
        CacheEntry old = (CacheEntry) this.cache.put(key, e);
        expireOldEntries();
        return old != null ? (V) old.getValue() : null;
    }

    public int size() {
        return this.cache.size();
    }

    protected void expired(V value) {
    }

    protected void expireOldEntries() {
        if (this.cache.size() <= this.depth) {
            return;
        }

        long start = System.nanoTime();

        while (this.cache.size() > this.depth) {
            Object oldest = null;
            long min = this.accessCounter.get();
            for (Map.Entry e : this.cache.entrySet()) {
                CacheEntry ce = (CacheEntry) e.getValue();
                long val = ce.lastAccess.get();
                if (val < min) {
                    min = val;
                    oldest = e.getKey();
                }
            }
            if (oldest == null) {
                throw new RuntimeException("For some reason the cache cannot be expired.");
            }

            CacheEntry removed = (CacheEntry) this.cache.remove(oldest);
            if (removed != null) {
                expired((V) removed.value);
            }
        }

        long end = System.nanoTime();
        long delta = end - start;
        if (delta > 1000000L) {
            System.out.println(this.name + " :Expired old cache entries in > 1 ms:" + (end - start / 1000000.0D) + " ms.");
        }
    }

    private class MemReporter
            implements Reporter {
        private MemReporter() {
        }

        public void printReport(String type, PrintWriter out) {
            out.println("LruCache[" + LruCache.this.name + "]:" + LruCache.this.cache.size());
        }
    }

    protected class EntryIterator
            implements Iterator<V> {
        private Iterator<LruCache.CacheEntry<V>> delegate;

        public EntryIterator(Iterator<CacheEntry<V>> iterator) {
            this.delegate = delegate;
        }

        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        public V next() {
            return (V) ((CacheEntry) this.delegate.next()).getValue();
        }

        public void remove() {
            this.delegate.remove();
        }
    }

    protected static class CacheEntry<V> {
        V value;
        AtomicLong lastAccess = new AtomicLong();

        public CacheEntry(V value) {
            this.value = value;
        }

        public void access(long access) {
            this.lastAccess.set(access);
        }

        public V getValue() {
            return this.value;
        }
    }
}