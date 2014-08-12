package mythruna.client.view;

import com.jme3.math.Vector3f;

public class RopeLink {

    public Vector3f start;
    public Vector3f end;
    RopeMesh parent;
    boolean changed = false;

    public RopeLink() {
    }

    public RopeLink(Vector3f start, Vector3f end) {
        this.start = start;
        this.end = end;
    }

    public void update(Vector3f start, Vector3f end) {
        if ((start.equals(this.start)) && (end.equals(this.end)))
            return;
        this.start.set(start);
        this.end.set(end);
        this.changed = true;
        if (this.parent != null)
            this.parent.updateRope(this);
    }

    public String toString() {
        return "RopeLink[" + this.start + ", " + this.end + "]";
    }
}