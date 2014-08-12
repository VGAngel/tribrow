package mythruna.client.bm;

import com.jme3.collision.CollisionResult;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import mythruna.Vector3i;
import mythruna.client.WorldIntersector;
import mythruna.es.EntityId;
import mythruna.script.*;
import org.progeeks.util.log.Log;

public class ActionTool
        implements Tool {
    static Log log = Log.getLog();
    private ObjectSelector selector;
    private ActionReference ref;
    private Vector3f lastPos = null;
    private Vector3f lastDir = null;

    public ActionTool(ActionReference ref) {
        this.ref = ref;
    }

    public void initialize(ObjectSelector selector) {
        this.selector = selector;
    }

    public void update() {
    }

    public Spatial getIcon() {
        return null;
    }

    public String getName() {
        return this.ref.getName();
    }

    protected void selectObject(Vector3f pos, Vector3f dir, Quaternion rotation, boolean value) {
        CollisionResult collision = this.selector.intersectObjects(pos, dir);
        if (collision == null) {
            return;
        }
        Node parent = collision.getGeometry().getParent();
        Long id = (Long) parent.getUserData("id");
        log.info("Selected id:" + id);

        Vector3f p = collision.getContactPoint().clone();
        float t = p.y;
        p.y = p.z;
        p.z = t;

        ActionParameter parm = new ObjectParameter(new EntityId(id.longValue()), p);

        this.selector.getGameClient().executeRef(this.ref, parm);
    }

    protected void selectBlock(Vector3f pos, Vector3f dir, Quaternion rotation, boolean value) {
        WorldIntersector.Intersection hit = this.selector.intersectWorld(pos, dir, new Integer[0]);
        if (hit == null) {
            return;
        }

        Vector3i block = hit.getBlock();
        int side = hit.getSide();
        Vector3f point = hit.getPoint();

        ActionParameter parm = new BlockParameter(point, block, side);

        System.out.println("block:" + block);

        this.selector.getGameClient().executeRef(this.ref, parm);
    }

    public void select(Vector3f pos, Vector3f dir, Quaternion rotation, boolean value) {
        System.out.println("select(" + value + ") :" + this.ref);

        if (value) {
            return;
        }
        if (this.ref.getType() == ActionType.Object) {
            selectObject(pos, dir, rotation, value);
        } else if (this.ref.getType() == ActionType.Block) {
            selectBlock(pos, dir, rotation, value);
        } else if (this.ref.getType() != ActionType.BlockOrObject) {
            selectBlock(pos, dir, rotation, value);
        }
    }

    public void place(Vector3f pos, Vector3f dir, Quaternion rotation, boolean value) {
        System.out.println("place(" + value + ") :" + this.ref);
    }

    public boolean showBlockSelection() {
        return (this.ref.getType() == ActionType.Block) || (this.ref.getType() == ActionType.BlockOrObject);
    }

    public boolean isCapturingView() {
        return false;
    }

    public void viewMoved(Vector3f pos, Vector3f dir, Quaternion rotation) {
        if (!isCapturingView()) {
            return;
        }
        System.out.println("viewMoved(" + pos + ", " + dir + ", " + rotation + ")");
    }
}
