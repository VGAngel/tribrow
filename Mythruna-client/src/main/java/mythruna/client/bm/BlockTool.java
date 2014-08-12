package mythruna.client.bm;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import mythruna.BlockType;
import mythruna.Vector3i;
import mythruna.client.WorldIntersector;
import mythruna.geom.GeomFactory;
import mythruna.geom.GeomPartBuffer;

public class BlockTool implements Tool {

    private ObjectSelector selector;
    private BlockType type;
    private String name;
    private Spatial icon;

    public BlockTool(BlockType type) {
        this.type = type;
        if (type != null) {
            this.name = ("(" + type.getId() + ") " + type.getName());
            this.icon = createBlockIcon(type);
        } else {
            this.name = "(none)";
        }
    }

    public void initialize(ObjectSelector selector) {
        this.selector = selector;
    }

    public void update() {
    }

    protected Spatial createBlockIcon(BlockType type) {
        GeomFactory gf = type.getGeomFactory();

        GeomPartBuffer buffer = new GeomPartBuffer();

        for (int i = 0; i < 6; i++) {
            gf.createGeometry(buffer, 0, 0, 0, 0, 0, 0, 1.0F, 0.5F, type, i);
        }
        gf.createInternalGeometry(buffer, 0, 0, 0, 0, 0, 0, 1.0F, 0.5F, type);

        Node node = buffer.createNode("Icon:" + type.getName());
        node.setLocalTranslation(-0.5F, -0.5F, -0.5F);

        return node;
    }

    public Spatial getIcon() {
        return this.icon;
    }

    public String getName() {
        return this.name;
    }

    public void select(Vector3f pos, Vector3f dir, Quaternion rotation, boolean value) {
        if (value) {
            return;
        }
        WorldIntersector.Intersection hit = this.selector.intersectWorld(pos, dir, new Integer[]{7, 8});
        if (hit == null) {
            return;
        }

        Vector3i block = hit.getBlock();

        if (!this.selector.getGameClient().getPerms().canChangeBlock(block)) {
            this.selector.getGameClient().getConsole().echo("You do not have permission to remove that block.");
            return;
        }

        this.selector.getWorld().setCellType(block.x, block.y, block.z, 0);
    }

    public void place(Vector3f pos, Vector3f dir, Quaternion rotation, boolean value) {
        if (value) {
            return;
        }
        WorldIntersector.Intersection hit = this.selector.intersectWorld(pos, dir, new Integer[]{7, 8});
        if (hit == null) {
            return;
        }

        Vector3i block = hit.getBlock();

        int side = hit.getSide();
        if (side < 0) {
            return;
        }

        block.x += mythruna.Direction.DIRS[side][0];
        block.y += mythruna.Direction.DIRS[side][1];
        block.z += mythruna.Direction.DIRS[side][2];

        int existing = this.selector.getWorld().getType(block.x, block.y, block.z, null);
        if ((existing == 0) || (existing == 7) || (existing == 8)) {
            if (!this.selector.getGameClient().getPerms().canChangeBlock(block)) {
                this.selector.getGameClient().getConsole().echo("You do not have permission to add a block there.");
                return;
            }

            this.selector.getWorld().setCellType(block.x, block.y, block.z, this.type.getId());
        }
    }

    public boolean isCapturingView() {
        return false;
    }

    public boolean showBlockSelection() {
        return true;
    }

    public void viewMoved(Vector3f pos, Vector3f dir, Quaternion rotation) {
    }
}
