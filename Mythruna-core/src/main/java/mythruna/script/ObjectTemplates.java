package mythruna.script;

import mythruna.es.EntityId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectTemplates {

    private Map<String, ObjectTemplate> templates = new ConcurrentHashMap();
    private Map<EntityId, ObjectTemplate> entities = new ConcurrentHashMap();

    public ObjectTemplates() {
    }

    public ObjectTemplate getTemplate(String name) {
        return (ObjectTemplate) this.templates.get(name);
    }

    public ObjectTemplate getTemplate(EntityId id) {
        return (ObjectTemplate) this.entities.get(id);
    }

    public void addTemplate(ObjectTemplate template) {
        this.templates.put(template.getName(), template);
        this.entities.put(template.getClassEntity(), template);
    }

    public void compile() {
        for (ObjectTemplate t : this.templates.values())
            t.compile();
    }

    public static abstract interface ObjectTemplate {
        public abstract String getName();

        public abstract EntityId getClassEntity();

        public abstract void compile();
    }
}