package mythruna.client.tabs.bp;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.shape.Box;
import mythruna.BlockType;
import mythruna.BlockTypeIndex;
import mythruna.client.ui.ButtonControl;
import mythruna.client.ui.Command;
import mythruna.geom.GeomFactory;
import mythruna.geom.GeomPartBuffer;

import java.util.*;

public class BlockIconSelection {
    private AssetManager assets;
    private Box boxMesh;
    private WireBox wireMesh;
    private Material boxMaterial;
    private Material wireMaterial;
    private Map<BlockType, Spatial> icons = new HashMap();
    private List<String> groups = new ArrayList();

    private int groupId = 0;
    private int itemType = 0;
    private BlockType lastType = null;

    public BlockIconSelection(AssetManager assets) {
        this.assets = assets;

        this.boxMesh = new Box(0.51F, 0.51F, 0.51F);
        this.wireMesh = new WireBox(0.51F, 0.51F, 0.51F);
        this.boxMaterial = new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md");
        this.boxMaterial.setColor("Color", new ColorRGBA(1.0F, 1.0F, 1.0F, 0.01F));
        this.boxMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        this.boxMaterial.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Front);

        this.wireMaterial = new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md");
        this.wireMaterial.setColor("Color", new ColorRGBA(1.0F, 1.0F, 1.0F, 0.1F));
        this.wireMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        for (int i = 0; i <= BlockTypeIndex.getMaxId(); i++) {
            BlockType t = BlockTypeIndex.types[i];
            if (t != null) {
                this.icons.put(t, createBlockIcon(t));
            }
        }
        this.groups.addAll(BlockTypeIndex.groupNames());
    }

    public String getLabel() {
        BlockType t = getSelectedType();
        if (t == null)
            return "(none)";
        return "(" + t.getId() + ") " + t.getName();
    }

    public Spatial getIcon() {
        BlockType t = getSelectedType();
        if (t == null)
            return null;
        return (Spatial) this.icons.get(t);
    }

    public BlockType getSelectedType() {
        if (this.lastType != null) {
            return this.lastType;
        }
        List types = getTypes((String) this.groups.get(this.groupId));
        if (types.size() == 0)
            return null;
        this.lastType = ((BlockType) types.get(this.itemType));
        return this.lastType;
    }

    public void setSelectedType(BlockType type) {
        int groupIndex = 0;
        for (BlockTypeIndex.BlockGroup g : BlockTypeIndex.groups()) {
            int typeIndex = 0;
            for (BlockType t : g.getTypes()) {
                if (t == type) {
                    setGroup(groupIndex);
                    setItemType(typeIndex);
                    break;
                }
                typeIndex++;
            }
            groupIndex++;
        }
    }

    public int getTypeId() {
        BlockType t = getSelectedType();
        if (t == null)
            return 0;
        return t.getId();
    }

    public List<BlockType> getTypes(String group) {
        if (BlockTypeIndex.getGroup(group) == null)
            return Collections.EMPTY_LIST;
        return BlockTypeIndex.getGroup(group).getTypes();
    }

    public List<Spatial> getTypeIcons() {
        List results = new ArrayList();
        for (BlockType type : getTypes((String) this.groups.get(this.groupId))) {
            Spatial icon = (Spatial) this.icons.get(type);
            results.add(icon);
        }
        return results;
    }

    public List<Spatial> getGroupIcons() {
        List results = new ArrayList();
        for (String s : this.groups) {
            List types = getTypes(s);
            if (types.size() != 0) {
                Spatial icon = (Spatial) this.icons.get(types.get(0));
                results.add(icon);
            }
        }
        return results;
    }

    protected int getGroupSize(String group) {
        return getTypes(group).size();
    }

    public void setItemType(int type) {
        if (this.itemType == type) {
            return;
        }
        if (type < 0)
            type = 0;
        else if (type >= getGroupSize((String) this.groups.get(this.groupId)))
            type = getGroupSize((String) this.groups.get(this.groupId)) - 1;
        this.itemType = type;
        this.lastType = null;
    }

    public int getItemType() {
        return this.itemType;
    }

    public void setGroup(int group) {
        if (this.groupId == group) {
            return;
        }
        if (group < 0)
            group = 0;
        if (group >= this.groups.size())
            group = this.groups.size() - 1;
        this.groupId = group;
        setItemType(Math.max(0, Math.min(getItemType(), getGroupSize((String) this.groups.get(this.groupId)) - 1)));
        this.lastType = null;
    }

    public int getGroup() {
        return this.groupId;
    }

    public void incrementItemType() {
        int type = getItemType();
        type++;
        if (type >= getGroupSize((String) this.groups.get(this.groupId)))
            type = 0;
        setItemType(type);
    }

    public void decrementItemType() {
        int type = getItemType();
        type--;
        if (type < 0)
            type = Math.max(0, getGroupSize((String) this.groups.get(this.groupId)) - 1);
        setItemType(type);
    }

    public void incrementGroup() {
        int g = getGroup();
        g++;
        if (g >= this.groups.size())
            g = 0;
        setGroup(g);
    }

    public void decrementGroup() {
        int g = getGroup();
        g--;
        if (g < 0)
            g = this.groups.size() - 1;
        setGroup(g);
    }

    protected Spatial createBlockIcon(BlockType type) {
        GeomFactory gf = type.getGeomFactory();

        GeomPartBuffer buffer = new GeomPartBuffer(new BoundingBox(Vector3f.ZERO, 1.0F, 1.0F, 1.0F));

        for (int i = 0; i < 6; i++) {
            gf.createGeometry(buffer, 0, 0, 0, 0, 0, 0, 1.0F, 0.5F, type, i);
        }
        gf.createInternalGeometry(buffer, 0, 0, 0, 0, 0, 0, 1.0F, 0.5F, type);

        Node node = buffer.createNode("Icon:" + type.getName(), -0.5F, -0.5F, -0.5F);

        Geometry box = new Geometry("Icon:" + type.getName() + " Box", this.boxMesh);
        box.setMaterial(this.boxMaterial);
        box.setQueueBucket(RenderQueue.Bucket.Transparent);

        Geometry wireBox = new Geometry("Icon:" + type.getName() + " Wire", this.wireMesh);
        wireBox.setMaterial(this.wireMaterial);
        wireBox.setQueueBucket(RenderQueue.Bucket.Transparent);

        node.addControl(new ButtonControl());
        ((ButtonControl) node.getControl(ButtonControl.class)).addCommand(new SelectCommand(type));
        box.addControl(new ButtonControl());
        ((ButtonControl) box.getControl(ButtonControl.class)).addCommand(new SelectCommand(type));

        node.attachChild(box);
        node.attachChild(wireBox);

        return node;
    }

    public class SelectCommand implements Command {
        private BlockType blockType;

        public SelectCommand(BlockType blockType) {
            this.blockType = blockType;
        }

        public void execute(Object source, Object action) {
            BlockIconSelection.this.setSelectedType(this.blockType);
        }
    }
}