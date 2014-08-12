package mythruna.script;

import groovy.lang.Closure;
import mythruna.GameSystems;
import mythruna.PlayerContext;
import mythruna.PlayerData;
import mythruna.PlayerPermissions;
import mythruna.es.EntityId;
import mythruna.shell.Console;
import org.progeeks.tool.console.Shell;
import org.progeeks.util.Inspector;
import org.progeeks.util.log.Log;

import java.util.HashMap;
import java.util.Map;

public class ScriptedAction {

    static Log log = Log.getLog();
    private String group;
    private String name;
    private Closure closure;
    private Closure condition;
    private ActionType type = ActionType.Block;
    private String access;
    private ScriptedAction original;

    public ScriptedAction(String group, String name, Closure closure) {
        if (group == null)
            throw new IllegalArgumentException("Group cannot be null.");
        if (name == null)
            throw new IllegalArgumentException("Name cannot be null.");
        if (closure == null) {
            throw new IllegalArgumentException("Closure cannot be null.");
        }

        this.group = group;
        this.name = name;
        this.closure = closure;
    }

    public void setCondition(Closure condition) {
        this.condition = condition;
    }

    public void setOriginal(ScriptedAction original) {
        this.original = original;
    }

    public String getGroup() {
        return this.group;
    }

    public String getName() {
        return this.name;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public ActionType getType() {
        return this.type;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public String getAccess() {
        return this.access;
    }

    public boolean isEnabled(PlayerContext env, EntityId source, ActionParameter target) {
        if (this.condition == null) {
            return true;
        }
        LocalContext context = new LocalContext(this, env);

        Object previous = ExecutionContext.getEnvironment();
        ExecutionContext.setEnvironment(context);
        try {
            System.out.println("isEnabled(" + this.name + ", " + context + ") ");
            this.closure.setDelegate(context);
            for (ScriptedAction o = this.original; o != null; o = o.original) {
                o.closure.setDelegate(new LocalContext(o, env));
            }

            Object result = null;
            if (this.condition.getMaximumNumberOfParameters() == 2)
                result = this.condition.call(new Object[]{source, target});
            else if ((source != null) && (target != null) && (this.condition.getMaximumNumberOfParameters() > 1))
                result = this.condition.call(new Object[]{source, target});
            else if (source != null)
                result = this.condition.call(source);
            else {
                result = this.condition.call(target);
            }
            System.out.println("result:" + result + "  class:" + result.getClass());
            return Boolean.TRUE.equals(result);
        } catch (RuntimeException e) {
            log.error("Error executing action condition:" + this.name, e);
        } finally {
            ExecutionContext.setEnvironment(previous);
        }
        return false;
    }

    public void runAction(PlayerContext env, EntityId source, ActionParameter target) {
        LocalContext context = new LocalContext(this, env);

        Object previous = ExecutionContext.getEnvironment();
        ExecutionContext.setEnvironment(context);
        try {
            System.out.println("runAction(" + this.name + ", " + context + ") ");
            this.closure.setDelegate(context);
            for (ScriptedAction o = this.original; o != null; o = o.original) {
                o.closure.setDelegate(new LocalContext(o, env));
            }

            if (this.closure.getMaximumNumberOfParameters() == 2)
                this.closure.call(new Object[]{source, target});
            else if ((source != null) && (target != null) && (this.closure.getMaximumNumberOfParameters() > 1))
                this.closure.call(new Object[]{source, target});
            else if (source != null)
                this.closure.call(source);
            else {
                this.closure.call(target);
            }
        } catch (RuntimeException e) {
            log.error("Error executing action:" + this.name, e);
        } finally {
            ExecutionContext.setEnvironment(previous);
        }
    }

    public String toString() {
        return "ScriptedAction[" + this.group + ", " + this.name + ", " + this.type + " -> " + this.original + "]";
    }

    protected static class LocalContext extends HashMap implements PlayerContext {
        private ScriptedAction action;
        private PlayerContext delegate;
        private Inspector ins;

        public LocalContext(ScriptedAction action, PlayerContext delegate) {
            this.action = action;
            this.delegate = delegate;
            this.ins = new Inspector(delegate);
        }

        public GameSystems getSystems() {
            return this.delegate.getSystems();
        }

        public EntityId getPlayer() {
            return this.delegate.getPlayer();
        }

        public PlayerData getPlayerData() {
            return this.delegate.getPlayerData();
        }

        public Console getConsole() {
            return this.delegate.getConsole();
        }

        public Shell getShell() {
            return this.delegate.getShell();
        }

        public PlayerPermissions getPerms() {
            return this.delegate.getPerms();
        }

        public Map<String, Object> getSessionData() {
            return this.delegate.getSessionData();
        }

        public void echo(String msg) {
            this.delegate.echo(msg);
        }

        public void runEntityAction(String action) {
            this.delegate.runEntityAction(action);
        }

        public void runEntityAction(String action, ActionParameter target) {
            this.delegate.runEntityAction(action, target);
        }

        public Closure getOriginal() {
            return action.original.closure;
        }

        public Object original(Object arg) {
            if (getOriginal() == null) {
                return null;
            }
            return getOriginal().call(arg);
        }

        public Object get(Object key) {
            String s = String.valueOf(key);
            if ("original".equals(s)) {
                return getOriginal();
            }

            if (!this.ins.hasAccessor(s))
                throw new RuntimeException("No getter for property:" + key + " for environment:" + this.delegate);
            return this.ins.get(s);
        }

        public Object put(Object key, Object value) {
            String s = String.valueOf(key);
            if (!this.ins.hasMutator(s))
                throw new RuntimeException("No setter for property:" + key + " for environment:" + this.delegate);
            this.ins.set(s, value);
            return null;
        }

        public String toString() {
            return "LocalContext[" + this.delegate + "]";
        }
    }
}