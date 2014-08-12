package mythruna.event;

import org.progeeks.util.log.Log;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventDispatcher {

    static Log log = Log.getLog();

    private static EventDispatcher instance = new EventDispatcher();

    private Map<EventType, List<EventListener>> listenerMap = new ConcurrentHashMap();

    protected EventDispatcher() {
    }

    public static EventDispatcher getInstance() {
        return instance;
    }

    protected List<EventListener> getListeners(EventType type) {
        List list = (List) this.listenerMap.get(type);
        if (list == null) {
            list = new CopyOnWriteArrayList();
            this.listenerMap.put(type, list);
        }
        return list;
    }

    public <E> void addListener(EventType<E> type, EventListener<E> listener) {
        getListeners(type).add(listener);
    }

    public <E> void removeListener(EventType<E> type, EventListener<E> listener) {
        getListeners(type).remove(listener);
    }

    public <E> void publishEvent(EventType<E> type, E event) {
        for (Iterator i$ = getListeners(type).iterator(); i$.hasNext(); ) {
            EventListener l = (EventListener) i$.next();
            try {
                l.newEvent(type, event);
            } catch (Throwable t) {
                log.error("Error handling event:" + event + " for type:" + type + "  in handler:" + l, t);
            }
        }
        EventListener l;
    }
}