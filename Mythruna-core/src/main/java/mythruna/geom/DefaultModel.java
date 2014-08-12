package mythruna.geom;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class DefaultModel implements Model {

    private long id;
    private GeomPartBuffer parts;
    private Vector3f offset;
    private Node node;
    private float scale;

    public DefaultModel(long id, GeomPartBuffer parts, float scale, Vector3f offset) {
        this.id = id;
        this.parts = parts;
        this.scale = scale;
        this.offset = offset;
    }

    public Class<Model> getType() {
        return Model.class;
    }

    public Node getNode() {
        if (this.node != null)
            return this.node;
        this.node = this.parts.createNode("entity:" + this.id, this.offset.x, this.offset.y, this.offset.z);
        this.node.setUserData("id", Long.valueOf(this.id));
        this.node.setLocalScale(this.scale);
        return this.node;
    }
}