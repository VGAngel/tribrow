package mythruna.script;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityComponent;
import mythruna.es.EntityId;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

@Serializable
public class ContextActions implements EntityComponent {

    private EntityId player;
    private EntityId target;
    private Vector3f pos;
    private String name;
    private String title;
    private ActionReference[] actions;
    private ActionParameter arg;

    public ContextActions() {
    }

    public ContextActions(EntityId player, EntityId target, Vector3f pos, String name, String title, Collection<ActionReference> refs) {
        this(player, target, pos, name, title, refs, null);
    }

    public ContextActions(EntityId player, EntityId target, Vector3f pos, String name, String title, Collection<ActionReference> refs, ActionParameter arg) {
        this.player = player;
        this.target = target;
        this.pos = (pos == null ? null : pos.clone());
        this.name = name;
        this.title = title;
        this.actions = ((ActionReference[]) refs.toArray(new ActionReference[refs.size()]));
        this.arg = arg;
    }

    public ContextActions(Collection<ActionReference> refs, ContextActions existing) {
        this.player = existing.player;
        this.target = existing.target;
        this.pos = existing.pos.clone();
        this.name = existing.name;
        this.title = existing.title;

        Collection unique = new LinkedHashSet();
        unique.addAll(Arrays.asList(existing.getActions()));
        unique.addAll(refs);
        refs = unique;
        this.actions = ((ActionReference[]) refs.toArray(new ActionReference[refs.size()]));
    }

    public Class<ContextActions> getType() {
        return ContextActions.class;
    }

    public EntityId getPlayer() {
        return this.player;
    }

    public EntityId getTarget() {
        return this.target;
    }

    public ActionParameter getParameter() {
        return this.arg;
    }

    public Vector3f getPosition() {
        return this.pos;
    }

    public String getName() {
        return this.name;
    }

    public String getTitle() {
        return this.title;
    }

    public ActionReference[] getActions() {
        return this.actions;
    }

    public String toString() {
        if (this.actions != null)
            return "ContextActions[" + this.name + ", " + this.title + ", " + Arrays.asList(this.actions) + "]";
        return "ContextActions[" + this.name + ", " + this.title + "]";
    }
}