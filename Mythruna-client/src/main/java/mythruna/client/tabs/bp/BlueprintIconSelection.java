package mythruna.client.tabs.bp;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.shape.Box;
import mythruna.World;
import mythruna.client.GameClient;
import mythruna.client.ui.*;
import mythruna.db.BlueprintData;
import mythruna.es.*;
import org.progeeks.util.log.Log;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class BlueprintIconSelection {
    static Log log = Log.getLog();
    private BlueprintEditorState editor;
    private Application app;
    private AssetManager assets;
    private GameClient gameClient;
    private World world;
    private Box boxMesh;
    private WireBox wireMesh;
    private Material boxMaterial;
    private Material wireMaterial;
    private Map<EntityId, Spatial> icons = new TreeMap();
    private EntitySet blueprints;

    public BlueprintIconSelection(BlueprintEditorState editor, Application app, GameClient gameClient) {
        this.editor = editor;
        this.app = app;
        this.assets = app.getAssetManager();
        this.gameClient = gameClient;
        this.world = gameClient.getWorld();

        this.boxMesh = new Box(0.51F, 0.51F, 0.51F);
        this.wireMesh = new WireBox(0.51F, 0.51F, 0.51F);
        this.boxMaterial = new Material(this.assets, "Common/MatDefs/Misc/Unshaded.j3md");
        this.boxMaterial.setColor("Color", new ColorRGBA(1.0F, 1.0F, 1.0F, 0.01F));
        this.boxMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        this.boxMaterial.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Front);

        this.wireMaterial = new Material(this.assets, "Common/MatDefs/Misc/Unshaded.j3md");
        this.wireMaterial.setColor("Color", new ColorRGBA(1.0F, 1.0F, 1.0F, 0.1F));
        this.wireMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        EntityData ed = this.world.getEntityData();
        this.blueprints = ed.getEntities(new FieldFilter(InContainer.class, "parentId", gameClient.getPlayer()), new Class[]{InContainer.class, BlueprintReference.class});

        this.icons.put(EntityId.NULL_ID, createNewIcon());
        for (Entity e : this.blueprints) {
            BlueprintReference ref = (BlueprintReference) e.get(BlueprintReference.class);
            BlueprintData bp = this.world.getBlueprint(ref.getBlueprintId());
            this.icons.put(e.getId(), createBlueprintIcon(e.getId(), bp));
        }
    }

    public Collection<Spatial> getIcons() {
        return this.icons.values();
    }

    public void update() {
        if (this.blueprints.applyChanges()) {
            log.trace("******* There are blueprint changes...");

            for (Entity e : this.blueprints.getAddedEntities()) {
                log.trace("  added:" + e);
                BlueprintReference ref = (BlueprintReference) e.get(BlueprintReference.class);
                BlueprintData bp = this.world.getBlueprint(ref.getBlueprintId());

                this.icons.put(e.getId(), createBlueprintIcon(e.getId(), bp));
            }
            for (Entity e : this.blueprints.getChangedEntities()) {
                log.trace("  changed:" + e);
                BlueprintReference ref = (BlueprintReference) e.get(BlueprintReference.class);
                BlueprintData bp = this.world.getBlueprint(ref.getBlueprintId());

                this.icons.put(e.getId(), createBlueprintIcon(e.getId(), bp));
            }
            for (Entity e : this.blueprints.getRemovedEntities()) {
                log.trace("  removed:" + e);
                Spatial icon = (Spatial) this.icons.remove(e.getId());
                if (icon == null) ;
            }

            this.editor.refreshBlueprints();
        }
    }

    protected Node createBlueprintIcon(EntityId entityId, BlueprintData bp) {
        BlueprintObject obj = new BlueprintObject(bp);
        obj.pack();
        obj.setOffset(new Vector3f(obj.getSizeX() * 0.5F, obj.getSizeY() * 0.5F, obj.getSizeZ() * 0.5F));

        Node n = obj.getNode();

        int max = Math.max(obj.getSizeX(), obj.getSizeY());
        max = Math.max(max, obj.getSizeZ());

        n.setLocalScale(1.0F / max);

        Geometry box = new Geometry("Icon:" + bp.name + " Box", this.boxMesh);
        box.setMaterial(this.boxMaterial);
        box.setQueueBucket(RenderQueue.Bucket.Transparent);
        box.setLocalScale(max);

        Geometry wireBox = new Geometry("Icon:" + bp.name + " Wire", this.wireMesh);
        wireBox.setMaterial(this.wireMaterial);
        wireBox.setQueueBucket(RenderQueue.Bucket.Transparent);
        wireBox.setLocalScale(max);

        n.addControl(new ButtonControl());
        ((ButtonControl) n.getControl(ButtonControl.class)).addCommand(new SelectBlueprintCommand(entityId, bp));
        box.addControl(new ButtonControl());
        ((ButtonControl) box.getControl(ButtonControl.class)).addCommand(new SelectBlueprintCommand(entityId, bp));

        n.attachChild(box);
        n.attachChild(wireBox);

        n.setUserData("id", Long.valueOf(entityId.getId()));

        return n;
    }

    protected Node createNewIcon() {
        Node n = new Node("New");

        Label l = new Label(this.app);
        l.setHAlignment(HAlignment.CENTER);
        l.setVAlignment(VAlignment.CENTER);
        l.setText("New");
        l.setLocalScale(0.02F);
        l.addControl(new BillboardControl());

        n.attachChild(l);

        Geometry box = new Geometry("Icon: New Box", this.boxMesh);
        box.setMaterial(this.boxMaterial);
        box.setQueueBucket(RenderQueue.Bucket.Transparent);

        Geometry wireBox = new Geometry("Icon: New Wire", this.wireMesh);
        wireBox.setMaterial(this.wireMaterial);
        wireBox.setQueueBucket(RenderQueue.Bucket.Transparent);

        n.addControl(new ButtonControl());
        ((ButtonControl) n.getControl(ButtonControl.class)).addCommand(new SelectBlueprintCommand(null, null));
        box.addControl(new ButtonControl());
        ((ButtonControl) box.getControl(ButtonControl.class)).addCommand(new SelectBlueprintCommand(null, null));

        n.attachChild(box);
        n.attachChild(wireBox);

        return n;
    }

    protected class SelectBlueprintCommand implements Command {
        private EntityId itemId;
        private BlueprintData blueprint;

        public SelectBlueprintCommand(EntityId itemId, BlueprintData blueprint) {
            this.itemId = itemId;
            this.blueprint = blueprint;
        }

        public void execute(Object source, Object action) {
            BlueprintIconSelection.this.editor.setWorkingBlueprint(this.blueprint, this.itemId);
        }
    }
}