package mythruna.script;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import mythruna.Vector3i;

@Serializable
public class BlockParameter implements ActionParameter {

    private Vector3i block;
    private Vector3f location;
    private byte side;

    public BlockParameter() {
    }

    public BlockParameter(Vector3f location, Vector3i block, int side) {
        this.location = location;
        this.block = block;
        this.side = (byte) side;
    }

    public Vector3f getLocation() {
        return this.location;
    }

    public Vector3i getBlock() {
        return this.block;
    }

    public int getSide() {
        return this.side;
    }

    public String toString() {
        return "BlockParameter[" + this.block + ", " + this.side + "]";
    }
}