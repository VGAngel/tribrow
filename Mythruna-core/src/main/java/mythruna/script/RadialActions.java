package mythruna.script;

import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityComponent;
import mythruna.es.EntityId;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

@Serializable
public class RadialActions implements EntityComponent {

    private EntityId player;
    private EntityId target;
    private String name;
    private String title;
    private ActionReference[] actions;

    public RadialActions() {
    }

    public RadialActions(EntityId player, EntityId target, String name, String title, Collection<ActionReference> refs) {
        this.player = player;
        this.target = target;
        this.name = name;
        this.title = title;
        this.actions = ((ActionReference[]) refs.toArray(new ActionReference[refs.size()]));
    }

    public RadialActions(Collection<ActionReference> refs, RadialActions existing) {
        this.player = existing.player;
        this.target = existing.target;
        this.name = existing.name;
        this.title = existing.title;

        Collection unique = new LinkedHashSet();
        unique.addAll(Arrays.asList(existing.getActions()));
        unique.addAll(refs);
        refs = unique;
        this.actions = ((ActionReference[]) refs.toArray(new ActionReference[refs.size()]));
    }

    public Class<RadialActions> getType() {
        return RadialActions.class;
    }

    public EntityId getPlayer() {
        return this.player;
    }

    public EntityId getTarget() {
        return this.target;
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
            return "RadialActions[" + this.name + ", " + this.title + ", " + Arrays.asList(this.actions) + "]";
        return "RadialActions[" + this.name + ", " + this.title + "]";
    }
}