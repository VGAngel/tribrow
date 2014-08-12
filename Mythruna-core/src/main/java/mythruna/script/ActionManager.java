package mythruna.script;

import mythruna.PlayerContext;
import mythruna.es.EntityId;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import java.util.*;

public class ActionManager {

    public static final String ENTITY_ACTIONS_GROUP = "entityActions";
    private ScriptEngine engine;
    private Bindings bindings;
    private Map<String, Map<String, ScriptedAction>> actions = new LinkedHashMap();
    private int nextId = 0;
    private Map<ScriptedAction, ActionReference> refs = new HashMap();
    private Map<Integer, ScriptedAction> actionLookup = new HashMap();

    public ActionManager() {
    }

    public ActionReference addAction(ScriptedAction a) {
        ScriptedAction original = (ScriptedAction) getGroup(a.getGroup(), true).put(a.getName(), a);

        a.setOriginal(original);

        int id = this.nextId++;
        ActionReference ref = new ActionReference(id, a.getGroup(), a.getName(), a.getType(), -1L);
        this.refs.put(a, ref);
        this.actionLookup.put(Integer.valueOf(id), a);

        return ref;
    }

    public void execute(String group, String name, PlayerContext env, ActionParameter arg) {
        ScriptedAction a = getAction(group, name);
        if (a == null)
            throw new RuntimeException("Action not found for " + group + ":" + name);
        a.runAction(env, null, arg);
    }

    public void execute(int id, PlayerContext env, ActionParameter arg) {
        ScriptedAction a = getAction(id);
        if (a == null)
            throw new RuntimeException("Action not found for " + id);
        a.runAction(env, null, arg);
    }

    public void execute(String entityAction, PlayerContext env, EntityId source, ActionParameter target) {
        ScriptedAction a = getAction("entityActions", entityAction);
        if (a == null)
            throw new RuntimeException("Entity action not found for " + entityAction);
        a.runAction(env, source, target);
    }

    public void execute(int id, PlayerContext env, EntityId target, ActionParameter parm) {
        ScriptedAction a = getAction(id);
        if (a == null)
            throw new RuntimeException("Entity action not found for " + id);
        a.runAction(env, target, parm);
    }

    public ScriptedAction getAction(int id) {
        return (ScriptedAction) this.actionLookup.get(Integer.valueOf(id));
    }

    public ScriptedAction getAction(String group, String name) {
        Map g = getGroup(group, false);
        if (g == null)
            return null;
        return (ScriptedAction) g.get(name);
    }

    public ActionReference getRef(ScriptedAction a) {
        if (a == null)
            return null;
        return (ActionReference) this.refs.get(a);
    }

    public ActionReference getRef(String group, String name) {
        ScriptedAction a = getAction(group, name);
        if (a == null)
            return null;
        return (ActionReference) this.refs.get(a);
    }

    public Collection<ActionReference> getRefs(String group) {
        List results = new ArrayList();
        Map<String, ScriptedAction> g = getGroup(group, false);
        if (g == null) {
            return results;
        }
        for (ScriptedAction a : g.values()) {
            results.add(this.refs.get(a));
        }

        return results;
    }

    protected Map<String, ScriptedAction> getGroup(String group, boolean create) {
        Map result = (Map) this.actions.get(group);
        if ((result == null) && (create)) {
            result = new LinkedHashMap();
            this.actions.put(group, result);
        }
        return result;
    }

    public static void main(String[] args) {
    }
}