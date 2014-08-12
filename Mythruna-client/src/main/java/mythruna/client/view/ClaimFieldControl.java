package mythruna.client.view;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import mythruna.Coordinates;
import mythruna.MaterialIndex;
import mythruna.Vector3i;
import mythruna.World;
import mythruna.client.GameClient;
import mythruna.db.BlueprintData;
import mythruna.db.ColumnInfo;
import mythruna.db.DefaultBlueprintDatabase;
import mythruna.es.*;
import mythruna.geom.ForceFieldMesh;
import mythruna.script.ComponentParameter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ClaimFieldControl extends AbstractControl {
    private GameClient gameClient;
    private World world;
    private EntityData ed;
    private Vector3i areaLocation = new Vector3i();
    private int clip;
    private int clipSize;
    private EntitySet entities;
    private Map<EntityId, BlockObject> flags = new HashMap<>();
    private Map<EntityId, ClaimCorners> corners = new HashMap<>();

    private EntityId parent = null;
    private BlueprintData flag1;
    private BlockObject flagObject1;
    private BlueprintData flag2;
    private BlockObject flagObject2;
    private ClaimFieldControl localClaims = null;
    private Entity currentZone = null;

    public ClaimFieldControl(GameClient gameClient) {
        this.gameClient = gameClient;
        this.world = gameClient.getWorld();
        this.ed = this.world.getEntityData();
        super.setEnabled(false);
        try {
            this.flag1 = DefaultBlueprintDatabase.loadResource("flag1.bp");
            this.flagObject1 = createBlockObject(this.flag1);
            this.flag2 = DefaultBlueprintDatabase.loadResource("flag2.bp");
            this.flagObject2 = createBlockObject(this.flag2);
        } catch (IOException e) {
            throw new RuntimeException("Error loading flag tool models.", e);
        }
    }

    protected ClaimFieldControl(GameClient gameClient, EntityId parent) {
        this.gameClient = gameClient;
        this.world = gameClient.getWorld();
        this.ed = this.world.getEntityData();
        this.parent = parent;
        super.setEnabled(false);
        try {
            this.flag1 = DefaultBlueprintDatabase.loadResource("flag1.bp");
            this.flagObject1 = createBlockObject(this.flag1);
            this.flag2 = DefaultBlueprintDatabase.loadResource("flag2.bp");
            this.flagObject2 = createBlockObject(this.flag2);
        } catch (IOException e) {
            throw new RuntimeException("Error loading flag tool models.", e);
        }
    }

    public void setLocation(Vector3i areaLocation, int clipDistance) {
        setWorldLocation(Coordinates.leafToWorld(areaLocation), clipDistance);
    }

    public void setWorldLocation(Vector3i worldLocation, int clipDistance) {
        this.areaLocation.set(worldLocation);

        this.clip = clipDistance;
        this.clipSize = ((clipDistance * 2 + 1) * 32);

        adjustToLocal();

        if (this.localClaims != null)
            this.localClaims.setWorldLocation(this.areaLocation, clipDistance);
    }

    protected boolean isAreaVisible(ClaimArea area) {
        int xMiddle = this.areaLocation.x + 192;
        int yMiddle = this.areaLocation.y + 192;

        int x = xMiddle - this.clip * 32;
        int y = yMiddle - this.clip * 32;

        if (area.getMax().x < x)
            return false;
        if (area.getMax().y < y)
            return false;
        if (area.getMin().x > x + this.clipSize)
            return false;
        if (area.getMin().y > y + this.clipSize) {
            return false;
        }
        return true;
    }

    protected void initialize() {
        this.entities = this.ed.getEntities(new FieldFilter(ClaimType.class, "parent", this.parent), new Class[]{ClaimType.class, ClaimArea.class, Position.class, Name.class, OwnedBy.class});

        addNodes(this.entities);
    }

    protected void terminate() {
        if (this.localClaims != null)
            this.localClaims.terminate();
        this.localClaims = null;
        this.currentZone = null;
        removeNodes(this.entities);
        this.entities.release();
        this.entities = null;
    }

    protected void adjustToLocal() {
        if (this.entities == null) {
            return;
        }
        for (Entity e : this.entities)
            adjustToLocal(e);
    }

    protected Node adjustToLocal(Entity e) {
        BlockObject bo = getBlockObject(e, true);
        Node n = bo.getNode();

        Position pos = (Position) e.get(Position.class);
        Vector3f loc = pos.getLocation();

        float x = loc.x - this.areaLocation.x;
        float y = loc.y - this.areaLocation.y;
        float z = loc.z;

        n.setLocalTranslation(x, z, y);
        n.setLocalRotation(pos.getRotation());

        ClaimCorners c = getClaimCorners(e, true);
        c.setBase(this.areaLocation);
        c.update();

        if ((c.isVisible()) && (n.getCullHint() != Spatial.CullHint.Inherit))
            n.setCullHint(Spatial.CullHint.Inherit);
        else if ((!c.isVisible()) && (n.getCullHint() != Spatial.CullHint.Always)) {
            n.setCullHint(Spatial.CullHint.Always);
        }
        return n;
    }

    protected ClaimCorners getClaimCorners(Entity e, boolean create) {
        ClaimCorners c = (ClaimCorners) this.corners.get(e.getId());
        if ((c == null) && (create)) {
            c = new ClaimCorners(e, this.areaLocation);
            this.corners.put(e.getId(), c);
        }

        return c;
    }

    protected BlockObject createBlockObject(BlueprintData bp) {
        BlockObject bo = new BlockObject(bp);
        bo.build();
        bo.applyUpdates(null);
        return bo;
    }

    protected BlockObject getBlockObject(Entity e, boolean create) {
        BlockObject bo = (BlockObject) this.flags.get(e.getId());
        if ((bo == null) && (create)) {
            bo = this.flagObject1.cloneFully();

            Node n = bo.getNode();
            n.setUserData("id", Long.valueOf(e.getId().getId()));

            n.addControl(new FlagDragControl(e));
        }

        this.flags.put(e.getId(), bo);
        return bo;
    }

    protected void addNodes(Set<Entity> set) {
        for (Entity e : set) {
            Node n = adjustToLocal(e);

            ((Node) this.spatial).attachChild(n);
        }
    }

    protected void updateNodes(Set<Entity> set) {
        for (Entity e : set) {
            Node n = adjustToLocal(e);
        }
    }

    protected void removeNodes(Set<Entity> set) {
        for (Entity e : set) {
            BlockObject bo = (BlockObject) this.flags.remove(e.getId());
            Node n = bo.getNode();
            if (n != null) {
                ((Node) this.spatial).detachChild(n);
            }
            ClaimCorners c = (ClaimCorners) this.corners.remove(e.getId());
            if (c != null)
                c.detach();
        }
    }

    protected Entity findClaim(Vector3i loc) {
        if (this.entities == null) {
            return null;
        }
        for (Entity e : this.entities) {
            ClaimArea area = (ClaimArea) e.get(ClaimArea.class);
            if (area.contains(loc))
                return e;
        }
        return null;
    }

    protected void setZone(Entity e) {
        if (this.currentZone == e) {
            return;
        }
        if (this.currentZone != null) {
            if (this.localClaims != null)
                this.localClaims.terminate();
            this.localClaims = null;
        }

        this.currentZone = e;

        if (this.currentZone != null) {
            ClaimType type = (ClaimType) e.get(ClaimType.class);
            if (type.canBeParent()) {
                this.localClaims = new ClaimFieldControl(this.gameClient, e.getId());
                this.localClaims.setSpatial(this.spatial);
                this.localClaims.setEnabled(true);
                this.localClaims.setWorldLocation(this.areaLocation, this.clip);
            }
        }
    }

    protected void setWorldLocation(Vector3i loc) {
        Entity e = findClaim(loc);
        setZone(e);
    }

    public Control cloneForSpatial(Spatial spatial) {
        return null;
    }

    public void setEnabled(boolean b) {
        if (isEnabled() == b)
            return;
        super.setEnabled(b);

        if (b)
            initialize();
        else
            terminate();
    }

    protected void controlRender(RenderManager rm, ViewPort vp) {
        if (this.localClaims != null)
            this.localClaims.controlRender(rm, vp);
    }

    protected void controlUpdate(float tpf) {
        if (this.entities == null) {
            return;
        }
        this.entities.applyChanges();
        removeNodes(this.entities.getRemovedEntities());
        addNodes(this.entities.getAddedEntities());
        updateNodes(this.entities.getChangedEntities());

        setWorldLocation(Coordinates.worldToCell(this.gameClient.getLocation()));
        if (this.localClaims != null)
            this.localClaims.controlUpdate(tpf);
    }

    protected class ClaimCorners {
        private Entity entity;
        private ClaimArea area;
        private Vector3i base;
        private BlockObject[] flags = new BlockObject[4];
        private Node[] nodes = new Node[4];
        private Vector3i[] currentLocs = new Vector3i[4];
        private Vector3i currentMin;
        private Vector3i currentMax;
        private Vector3i[] tempLocs = new Vector3i[4];
        private ForceFieldMesh fieldMesh;
        private Material fieldMaterial;
        private Geometry fieldGeom;
        private boolean visible = false;

        public ClaimCorners(Entity entity, Vector3i base) {
            this.entity = entity;
            this.base = base;
            setArea((ClaimArea) entity.get(ClaimArea.class));

            for (int i = 0; i < 4; i++) {
                this.flags[i] = ClaimFieldControl.this.flagObject2.cloneFully();
                this.nodes[i] = this.flags[i].getNode();
                this.nodes[i].addControl(new ClaimFieldControl.CornerDragControl(this, i));
                this.nodes[i].setUserData("id", Long.valueOf(entity.getId().getId()));
                this.currentLocs[i] = new Vector3i();
                this.tempLocs[i] = new Vector3i();
            }

            resetCornerLocations(this.currentLocs, this.currentMin, this.currentMax);
        }

        protected void initialize() {
            resetElevations(this.currentLocs, this.currentMin, this.currentMax);

            Vector3i relMin = new Vector3i();
            Vector3i relMax = new Vector3i(this.currentMax);
            relMax.x -= this.currentMin.x;
            relMax.y -= this.currentMin.y;

            float height = Math.max(this.currentMax.z - this.currentMin.z + 5, Math.min(10, this.area.getHeight()));
            float depth = Math.min(10, this.area.getDepth());

            this.fieldMesh = new ForceFieldMesh(relMin, relMax, height, depth);
            this.fieldGeom = new Geometry("Field:" + this.entity.getId(), this.fieldMesh);
            this.fieldMaterial = MaterialIndex.FIELD_MATERIAL.clone();

            ClaimType type = (ClaimType) this.entity.get(ClaimType.class);
            switch (type.getClaimType()) {
                case 0:
                    break;
                case 4:
                case 5:
                    this.fieldGeom.setUserData("sortBias", Integer.valueOf(1));
                case 1:
                    this.fieldMaterial.setColor("Color", getEditColor(this.entity));
                    break;
                case 2:
                case 3:
                    this.fieldMaterial.setColor("Color", ColorRGBA.Magenta);
            }

            this.fieldGeom.setMaterial(this.fieldMaterial);
            this.fieldGeom.setQueueBucket(RenderQueue.Bucket.Transparent);

            resetFieldMesh();

            attach();
        }

        public EntityId getEntityId() {
            return this.entity.getId();
        }

        protected void setVisible(boolean visible) {
            if (this.visible == visible)
                return;
            this.visible = visible;
            Spatial.CullHint hint;
            if (visible)
                hint = Spatial.CullHint.Inherit;
            else
                hint = Spatial.CullHint.Always;
            for (Node n : this.nodes) {
                n.setCullHint(hint);
            }
            if (this.fieldGeom != null) {
                this.fieldGeom.setCullHint(hint);
            } else if (visible) {
                initialize();
            }
        }

        protected boolean isVisible() {
            return this.visible;
        }

        protected ColorRGBA getEditColor(Entity claim) {
            if (ClaimFieldControl.this.gameClient.getPlayer().equals(((OwnedBy) claim.get(OwnedBy.class)).getOwnerId()))
                return ColorRGBA.Cyan;
            if (ClaimFieldControl.this.gameClient.getPerms().getPermissions(claim).canDo(4))
                return ColorRGBA.Green;
            return ColorRGBA.Red;
        }

        protected boolean isEditable(Entity claim) {
            if (ClaimFieldControl.this.gameClient.getPlayer().equals(((OwnedBy) claim.get(OwnedBy.class)).getOwnerId()))
                return true;
            return false;
        }

        protected void setCornerLocations(Vector3i[] locs, Vector3i min, Vector3i max) {
            locs[0].set(min.x, min.y, min.z);
            locs[1].set(max.x, min.y, min.z);
            locs[2].set(max.x, max.y, min.z);
            locs[3].set(min.x, max.y, min.z);
            resetElevations(locs, min, max);
        }

        protected void resetElevations(Vector3i[] locs, Vector3i min, Vector3i max) {
            for (int i = 0; i < locs.length; i++) {
                ColumnInfo col = ClaimFieldControl.this.world.getWorldDatabase().getColumnInfo(locs[i].x, locs[i].y, true);

                locs[i].z = (col.getElevation(locs[i].x, locs[i].y) + 1);
            }

            int minZ = 160;
            int maxZ = 0;
            for (Vector3i l : locs) {
                if (l.z < minZ)
                    minZ = l.z;
                if (l.z > maxZ)
                    maxZ = l.z;
            }
            min.z = minZ;
            max.z = maxZ;
        }

        protected void resetCornerLocations(Vector3i[] locs, Vector3i min, Vector3i max) {
            locs[0].set(min.x, min.y, locs[0].z);
            locs[1].set(max.x, min.y, locs[1].z);
            locs[2].set(max.x, max.y, locs[2].z);
            locs[3].set(min.x, max.y, locs[3].z);
        }

        public Vector3f getCornerLocation(int corner) {
            return new Vector3f(this.currentLocs[corner].x + 0.5F, this.currentLocs[corner].y + 0.5F, this.currentLocs[corner].z);
        }

        public Vector3i getCornerLocationInt(int corner) {
            return this.currentLocs[corner].clone();
        }

        public ClaimArea updateCornerLocation(int corner, Vector3i loc) {
            Vector3i min = new Vector3i(this.currentMin);
            Vector3i max = new Vector3i(this.currentMax);
            for (int i = 0; i < this.tempLocs.length; i++) {
                this.tempLocs[i].set(this.currentLocs[i]);
            }

            setCornerLocation(corner, loc, this.tempLocs, min, max);

            return new ClaimArea(min, max, this.area);
        }

        public void setCornerLocation(int corner, Vector3i loc, Vector3i[] locs, Vector3i min, Vector3i max) {
            locs[corner] = loc;

            switch (corner) {
                case 0:
                    min.x = loc.x;
                    min.y = loc.y;
                    break;
                case 1:
                    max.x = loc.x;
                    min.y = loc.y;
                    break;
                case 2:
                    max.x = loc.x;
                    max.y = loc.y;
                    break;
                case 3:
                    min.x = loc.x;
                    max.y = loc.y;
            }

            resetCornerLocations(locs, min, max);
            resetElevations(locs, min, max);
        }

        protected void adjustLocal() {
            for (int i = 0; i < 4; i++) {
                this.nodes[i].setLocalTranslation(this.currentLocs[i].x - this.base.x + 0.5F, this.currentLocs[i].z, this.currentLocs[i].y - this.base.y + 0.5F);
            }

            Vector3i aMin = this.area.getMin();
            this.fieldGeom.setLocalTranslation(aMin.x - this.base.x + 0.5F, aMin.z, aMin.y - this.base.y + 0.5F);
        }

        public void setBase(Vector3i base) {
            this.base = base.clone();
        }

        protected void attach() {
            Node parent = (Node) ClaimFieldControl.this.spatial;
            for (Node n : this.nodes)
                parent.attachChild(n);
            parent.attachChild(this.fieldGeom);
        }

        public void detach() {
            Node parent = (Node) ClaimFieldControl.this.spatial;
            for (Node n : this.nodes)
                parent.detachChild(n);
            if (this.fieldGeom != null)
                parent.detachChild(this.fieldGeom);
        }

        public void update() {
            setVisible(ClaimFieldControl.this.isAreaVisible(this.area));
            if (!this.visible) {
                return;
            }
            setArea((ClaimArea) this.entity.get(ClaimArea.class));
            adjustLocal();
        }

        protected void setArea(ClaimArea area) {
            if (this.area == area)
                return;
            this.area = area;

            this.currentMin = area.getMin().clone();
            this.currentMax = area.getMax().clone();

            if ((this.fieldMesh != null) && (area != null)) {
                resetCornerLocations(this.currentLocs, this.currentMin, this.currentMax);
                resetElevations(this.currentLocs, this.currentMin, this.currentMax);

                resetFieldMesh();

                adjustLocal();
            }
        }

        protected void resetFieldMesh() {
            Vector3i relMin = new Vector3i();
            Vector3i relMax = new Vector3i(this.currentMax);
            relMax.x -= this.currentMin.x;
            relMax.y -= this.currentMin.y;

            float height = this.currentMax.z - this.currentMin.z * 1.5F;

            height = Math.max(height, Math.min(10, this.area.getHeight()));
            float depth = Math.min(10, this.area.getDepth());

            this.fieldMesh.setCorners(relMin, relMax, height, depth);
        }
    }

    protected class CornerDragControl extends DragControl {
        private ClaimFieldControl.ClaimCorners corners;
        private int corner;
        private ClaimArea lastArea;

        public CornerDragControl(ClaimFieldControl.ClaimCorners corners, int corner) {
            this.corners = corners;
            this.corner = corner;
        }

        public void setPosition(Position pos) {
            Vector3f loc = pos.getLocation();
            loc.x -= 0.5F;
            loc.y -= 0.5F;
            Vector3i world = Coordinates.worldToCell(loc);

            ClaimArea newArea = this.corners.updateCornerLocation(this.corner, world);

            if ((this.lastArea == null) || (!this.lastArea.equals(newArea))) {
                this.lastArea = newArea;

                ClaimFieldControl.this.gameClient.execute("Update Claim Area", this.corners.getEntityId(), new ComponentParameter(newArea));
            }
        }

        public Position getPosition() {
            Vector3f v = this.corners.getCornerLocation(this.corner);

            return new Position(v);
        }
    }

    protected class FlagDragControl extends DragControl {
        private Entity entity;
        private Position lastPos;

        public FlagDragControl(Entity entity) {
            this.entity = entity;
        }

        public void setPosition(Position pos) {
            if ((this.lastPos == null) || (!this.lastPos.equals(pos))) {
                ClaimFieldControl.this.gameClient.execute("Move Claim Marker", this.entity.getId(), new ComponentParameter(pos));

                this.lastPos = pos;
            }
        }

        public Position getPosition() {
            Position entityPos = (Position) this.entity.get(Position.class);
            return entityPos;
        }
    }
}