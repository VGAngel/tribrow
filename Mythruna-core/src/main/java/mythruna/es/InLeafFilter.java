package mythruna.es;

import mythruna.Coordinates;
import mythruna.Vector3i;

public class InLeafFilter implements ComponentFilter {

    private Vector3i leaf;

    public InLeafFilter(Vector3i leaf) {
        this.leaf = leaf;
    }

    public Class getComponentType() {
        return Position.class;
    }

    public boolean evaluate(EntityComponent c) {
        if (!(c instanceof Position)) {
            return false;
        }
        Vector3i in = Coordinates.worldToLeaf(((Position) c).getLocation());

        return this.leaf.equals(in);
    }

    public String toString() {
        return "InLeafFilter[" + this.leaf + "]";
    }
}