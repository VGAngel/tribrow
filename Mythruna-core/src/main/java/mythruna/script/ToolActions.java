package mythruna.script;

import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityComponent;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

@Serializable
public class ToolActions implements EntityComponent {

    private ActionReference[] actions;

    public ToolActions() {
    }

    public ToolActions(Collection<ActionReference> refs) {
        this.actions = ((ActionReference[]) refs.toArray(new ActionReference[refs.size()]));
    }

    public ToolActions(Collection<ActionReference> refs, ToolActions existing) {
        if (existing != null) {
            Collection unique = new LinkedHashSet();
            unique.addAll(Arrays.asList(existing.getActions()));
            unique.addAll(refs);
            refs = unique;
        }
        this.actions = ((ActionReference[]) refs.toArray(new ActionReference[refs.size()]));
    }

    public Class<ToolActions> getType() {
        return ToolActions.class;
    }

    public ActionReference[] getActions() {
        return this.actions;
    }

    public String toString() {
        if (this.actions != null)
            return "ToolActions[" + Arrays.asList(this.actions) + "]";
        return "ToolActions[]";
    }
}