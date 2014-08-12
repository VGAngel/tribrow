package mythruna.client.bm;

import com.jme3.collision.CollisionResult;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import mythruna.BlockType;
import mythruna.Vector3i;
import mythruna.client.WorldIntersector;
import mythruna.client.view.BlockObject;
import mythruna.client.view.ClaimFieldControl;
import mythruna.client.view.DragControl;
import mythruna.db.BlueprintData;
import mythruna.db.DefaultBlueprintDatabase;
import mythruna.es.*;
import mythruna.script.ActionParameter;
import mythruna.script.BlockParameter;
import mythruna.script.ObjectParameter;
import org.progeeks.util.log.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ClaimsGroup extends ToolGroup {
    static Log log = Log.getLog();
    private EntitySet claims;
    private EntitySet placedClaims;
    private List<Tool> originals = new ArrayList();
    private Map<EntityId, PlaceClaimTool> placeClaimsMap = new TreeMap();
    private Map<EntityId, CreatePlotTool> createPlotMap = new TreeMap();
    private Map<EntityId, GivePlotTool> givePlotMap = new TreeMap();
    private Map<EntityId, GiveBadgeTool> giveBadgeMap = new TreeMap();
    private ClaimTool defaultClaimTool;
    private BlueprintData stronghold;
    private BlueprintData town;
    private BlueprintData city;
    private BlueprintData flag1;
    private BlockObject groupIcon;

    public ClaimsGroup() {
        super("Claims");
    }

    protected BlockObject createIcon(BlueprintData data) {
        BlockObject bo = new BlockObject(data);

        bo.setScale(0.25F);
        bo.setLighting(0.8F, 0.25F);

        bo.build();
        bo.applyUpdates(null);
        return bo;
    }

    public void initialize(ObjectSelector selector) {
        try {
            this.stronghold = DefaultBlueprintDatabase.loadResource("stronghold.bp");
            this.town = DefaultBlueprintDatabase.loadResource("town.bp");
            this.city = DefaultBlueprintDatabase.loadResource("city.bp");
            this.flag1 = DefaultBlueprintDatabase.loadResource("flag1.bp");
        } catch (IOException e) {
            throw new RuntimeException("Error loading claim tool models.", e);
        }

        this.groupIcon = createIcon(this.flag1);

        this.defaultClaimTool = new ClaimTool("Property");
        getTools().add(0, this.defaultClaimTool);

        this.originals.addAll(getTools());

        EntityData ed = selector.getWorld().getEntityData();
        this.claims = ed.getEntities(new FieldFilter(InContainer.class, "parentId", selector.getGameClient().getPlayer()), new Class[]{InContainer.class, ClaimType.class, Name.class});

        for (Entity e : this.claims) {
            this.placeClaimsMap.put(e.getId(), new PlaceClaimTool(e));
        }

        this.placedClaims = ed.getEntities(new FieldFilter(OwnedBy.class, "ownerId", selector.getGameClient().getPlayer()), new Class[]{OwnedBy.class, Position.class, ClaimArea.class, ClaimType.class, Name.class});

        for (Entity e : this.placedClaims) {
            ClaimType type = (ClaimType) e.get(ClaimType.class);
            if (type.canBeParent()) {
                this.createPlotMap.put(e.getId(), new CreatePlotTool(e));
                this.givePlotMap.put(e.getId(), new GivePlotTool(e));
            }

            this.giveBadgeMap.put(e.getId(), new GiveBadgeTool(e));
        }

        getTools().addAll(this.placeClaimsMap.values());
        getTools().addAll(this.createPlotMap.values());
        getTools().addAll(this.givePlotMap.values());
        getTools().addAll(this.giveBadgeMap.values());

        super.initialize(selector);
    }

    public void setVisible(boolean v) {
        if (isVisible() == v)
            return;
        super.setVisible(v);

        ((ClaimFieldControl) getSelector().getLocalArea().getControl(ClaimFieldControl.class)).setEnabled(v);
    }

    public Spatial getIcon() {
        Spatial result = super.getIcon();
        if (result == null) {
            result = this.groupIcon.getNode();
        }
        return result;
    }

    public boolean update() {
        if (!isVisible()) {
            return false;
        }
        boolean resetTools = false;

        if (this.claims.applyChanges()) {
            if (log.isTraceEnabled())
                log.trace("******* There are claim changes...");
            if (log.isTraceEnabled()) {
                log.trace("All:" + this.claims);
            }
            resetTools = true;

            for (Entity e : this.claims.getAddedEntities()) {
                if (log.isTraceEnabled())
                    log.trace("  added:" + e);
                PlaceClaimTool tool = new PlaceClaimTool(e);
                this.placeClaimsMap.put(e.getId(), tool);
                tool.initialize(getSelector());
            }
            for (Entity e : this.claims.getChangedEntities()) {
                if (log.isTraceEnabled())
                    log.trace("  changed:" + e);
                PlaceClaimTool tool = (PlaceClaimTool) this.placeClaimsMap.get(e.getId());
                tool.update();
            }
            for (Entity e : this.claims.getRemovedEntities()) {
                if (log.isTraceEnabled()) {
                    log.trace("  removed:" + e);
                }
                PlaceClaimTool tool = (PlaceClaimTool) this.placeClaimsMap.remove(e.getId());
                if (tool == null) ;
            }

        }

        if (this.placedClaims.applyChanges()) {
            if (log.isTraceEnabled())
                log.trace("******* There are placed town changes...");
            if (log.isTraceEnabled()) {
                log.trace("All:" + this.claims);
            }
            resetTools = true;

            for (Entity e : this.placedClaims.getAddedEntities()) {
                if (log.isTraceEnabled())
                    log.trace("  added:" + e);
                ClaimType type = (ClaimType) e.get(ClaimType.class);
                if (type.canBeParent()) {
                    CreatePlotTool tool1 = new CreatePlotTool(e);
                    this.createPlotMap.put(e.getId(), tool1);
                    tool1.initialize(getSelector());

                    GivePlotTool tool2 = new GivePlotTool(e);
                    this.givePlotMap.put(e.getId(), tool2);
                    tool2.initialize(getSelector());
                }

                GiveBadgeTool tool3 = new GiveBadgeTool(e);
                this.giveBadgeMap.put(e.getId(), tool3);
                tool3.initialize(getSelector());
            }
            for (Entity e : this.placedClaims.getChangedEntities()) {
                if (log.isTraceEnabled())
                    log.trace("  changed:" + e);
                CreatePlotTool tool1 = (CreatePlotTool) this.createPlotMap.get(e.getId());
                if (tool1 != null) {
                    tool1.update();
                }
                GivePlotTool tool2 = (GivePlotTool) this.givePlotMap.get(e.getId());
                if (tool2 != null) {
                    tool2.update();
                }
                GiveBadgeTool tool3 = (GiveBadgeTool) this.giveBadgeMap.get(e.getId());
                if (tool3 != null)
                    tool3.update();
            }
            for (Entity e : this.placedClaims.getRemovedEntities()) {
                if (log.isTraceEnabled()) {
                    log.trace("  removed:" + e);
                }

                CreatePlotTool tool1 = (CreatePlotTool) this.createPlotMap.remove(e.getId());
                if (tool1 != null) ;
                GivePlotTool tool2 = (GivePlotTool) this.givePlotMap.remove(e.getId());
                if (tool2 != null) ;
                GiveBadgeTool tool3 = (GiveBadgeTool) this.giveBadgeMap.remove(e.getId());
                if (tool3 == null) ;
            }

        }

        if (resetTools) {
            getTools().clear();
            getTools().addAll(this.originals);
            getTools().addAll(this.placeClaimsMap.values());
            getTools().addAll(this.createPlotMap.values());
            getTools().addAll(this.givePlotMap.values());
            getTools().addAll(this.giveBadgeMap.values());

            return true;
        }
        return false;
    }

    public String toString() {
        return "ClaimsGroup[" + getName() + "]";
    }

    protected class GiveBadgeTool
            implements Tool {
        private ObjectSelector selector;
        private Entity claim;
        private String name;
        private BlockObject icon;
        private ClaimType type;

        public GiveBadgeTool(Entity claim) {
            this.claim = claim;
            this.name = ((Name) claim.get(Name.class)).getName();
            this.type = ((ClaimType) claim.get(ClaimType.class));
        }

        public void initialize(ObjectSelector selector) {
            this.selector = selector;

            switch (this.type.getClaimType()) {
                case 1:
                    this.icon = ClaimsGroup.this.createIcon(ClaimsGroup.this.stronghold);
                    break;
                case 3:
                case 5:
                    this.icon = ClaimsGroup.this.createIcon(ClaimsGroup.this.city);
                    break;
                case 2:
                case 4:
                    this.icon = ClaimsGroup.this.createIcon(ClaimsGroup.this.town);
            }
        }

        public void update() {
            this.name = ((Name) this.claim.get(Name.class)).getName();
        }

        public Spatial getIcon() {
            return this.icon != null ? this.icon.getNode() : null;
        }

        public String getName() {
            return "Give Badge for:" + this.name;
        }

        public void select(Vector3f pos, Vector3f dir, Quaternion rotation, boolean value) {
            place(pos, dir, rotation, value);
        }

        public void place(Vector3f pos, Vector3f dir, Quaternion rotation, boolean value) {
            System.out.println("GiveBadgeTool.place(" + value + ")");

            if (value) {
                return;
            }

            CollisionResult collision = this.selector.intersectObjects(pos, dir);

            if (collision == null) {
                return;
            }
            Node parent = this.selector.findEntityParent(collision.getGeometry());

            Long id = (Long) parent.getUserData("id");

            ClaimsGroup.log.info("Selected id:" + id);

            Vector3f p = collision.getContactPoint().clone();
            float t = p.y;
            p.y = p.z;
            p.z = t;

            ActionParameter parm = new ObjectParameter(new EntityId(id.longValue()), p);

            if (!"Avatar".equals(parent.getUserData("type"))) {
                this.selector.getGameClient().execute("Alternate Action", new EntityId(id.longValue()), parm);
                return;
            }
            if ("Avatar".equals(parent.getUserData("type"))) {
                this.selector.getGameClient().execute("Give Badge", this.claim.getId(), parm);
            }
        }

        public boolean isCapturingView() {
            return ClaimsGroup.this.defaultClaimTool.isCapturingView();
        }

        public boolean showBlockSelection() {
            return false;
        }

        public void viewMoved(Vector3f pos, Vector3f dir, Quaternion rotation) {
            ClaimsGroup.this.defaultClaimTool.viewMoved(pos, dir, rotation);
        }
    }

    protected class GivePlotTool
            implements Tool {
        private ObjectSelector selector;
        private Entity claim;
        private String name;
        private BlockObject icon;
        private ClaimType type;

        public GivePlotTool(Entity claim) {
            this.claim = claim;
            this.name = ((Name) claim.get(Name.class)).getName();
            this.type = ((ClaimType) claim.get(ClaimType.class));
        }

        public void initialize(ObjectSelector selector) {
            this.selector = selector;

            switch (this.type.getClaimType()) {
                case 1:
                    this.icon = ClaimsGroup.this.createIcon(ClaimsGroup.this.stronghold);
                    break;
                case 3:
                    this.icon = ClaimsGroup.this.createIcon(ClaimsGroup.this.city);
                    break;
                case 2:
                    this.icon = ClaimsGroup.this.createIcon(ClaimsGroup.this.town);
            }
        }

        public void update() {
            this.name = ((Name) this.claim.get(Name.class)).getName();
        }

        public Spatial getIcon() {
            return this.icon != null ? this.icon.getNode() : null;
        }

        public String getName() {
            return "Give Plot for:" + this.name;
        }

        public void select(Vector3f pos, Vector3f dir, Quaternion rotation, boolean value) {
            ClaimsGroup.this.defaultClaimTool.select(pos, dir, rotation, value);
        }

        public void place(Vector3f pos, Vector3f dir, Quaternion rotation, boolean value) {
            if (value) {
                return;
            }

            CollisionResult collision = this.selector.intersectObjects(pos, dir);
            if (collision == null) {
                return;
            }
            Node parent = this.selector.findEntityParent(collision.getGeometry());
            Long id = (Long) parent.getUserData("id");
            ClaimsGroup.log.info("Selected id:" + id);

            Vector3f p = collision.getContactPoint().clone();
            float t = p.y;
            p.y = p.z;
            p.z = t;

            ActionParameter parm = new ObjectParameter(new EntityId(id.longValue()), p);

            if (!"Avatar".equals(parent.getUserData("type"))) {
                this.selector.getGameClient().execute("Alternate Action", new EntityId(id.longValue()), parm);
                return;
            }

            if ("Avatar".equals(parent.getUserData("type"))) {
                this.selector.getGameClient().execute("Give Plot", this.claim.getId(), parm);
            }
        }

        public boolean isCapturingView() {
            return ClaimsGroup.this.defaultClaimTool.isCapturingView();
        }

        public boolean showBlockSelection() {
            return false;
        }

        public void viewMoved(Vector3f pos, Vector3f dir, Quaternion rotation) {
            ClaimsGroup.this.defaultClaimTool.viewMoved(pos, dir, rotation);
        }
    }

    protected class CreatePlotTool
            implements Tool {
        private ObjectSelector selector;
        private Entity claim;
        private String name;
        private BlockObject icon;
        private ClaimType type;

        public CreatePlotTool(Entity claim) {
            this.claim = claim;
            this.name = ((Name) claim.get(Name.class)).getName();
            this.type = ((ClaimType) claim.get(ClaimType.class));
        }

        public void initialize(ObjectSelector selector) {
            this.selector = selector;

            switch (this.type.getClaimType()) {
                case 1:
                    this.icon = ClaimsGroup.this.createIcon(ClaimsGroup.this.stronghold);
                    break;
                case 3:
                    this.icon = ClaimsGroup.this.createIcon(ClaimsGroup.this.city);
                    break;
                case 2:
                    this.icon = ClaimsGroup.this.createIcon(ClaimsGroup.this.town);
            }
        }

        public void update() {
            this.name = ((Name) this.claim.get(Name.class)).getName();
        }

        public Spatial getIcon() {
            return this.icon != null ? this.icon.getNode() : null;
        }

        public String getName() {
            return "Create Plot for:" + this.name;
        }

        public void select(Vector3f pos, Vector3f dir, Quaternion rotation, boolean value) {
            ClaimsGroup.this.defaultClaimTool.select(pos, dir, rotation, value);
        }

        public void place(Vector3f pos, Vector3f dir, Quaternion rotation, boolean value) {
            if (value) {
                return;
            }

            CollisionResult collision = this.selector.intersectObjects(pos, dir);
            if (collision != null) {
                Node parent = this.selector.findEntityParent(collision.getGeometry());
                Long id = (Long) parent.getUserData("id");
                ClaimsGroup.log.info("Selected id:" + id);

                Vector3f p = collision.getContactPoint().clone();
                float t = p.y;
                p.y = p.z;
                p.z = t;

                ActionParameter parm = new ObjectParameter(new EntityId(id), p);
                this.selector.getGameClient().execute("Alternate Action", new EntityId(id), parm);
                return;
            }

            WorldIntersector.Intersection hit = this.selector.intersectWorld(pos, dir, new Integer[0]);
            if (hit == null) {
                return;
            }
            Vector3i block = hit.getBlock();
            int side = hit.getSide();
            Vector3f point = hit.getPoint();
            System.out.println("place:" + point);

            ActionParameter parm = new BlockParameter(point, block, side);

            this.selector.getGameClient().execute("Create Plot", this.claim.getId(), parm);
        }

        public boolean isCapturingView() {
            return ClaimsGroup.this.defaultClaimTool.isCapturingView();
        }

        public boolean showBlockSelection() {
            return false;
        }

        public void viewMoved(Vector3f pos, Vector3f dir, Quaternion rotation) {
            ClaimsGroup.this.defaultClaimTool.viewMoved(pos, dir, rotation);
        }
    }

    protected class PlaceClaimTool
            implements Tool {
        private ObjectSelector selector;
        private Entity claim;
        private String name;
        private BlockObject icon;
        private ClaimType type;

        public PlaceClaimTool(Entity claim) {
            this.claim = claim;
            this.name = ((Name) claim.get(Name.class)).getName();
            this.type = ((ClaimType) claim.get(ClaimType.class));
        }

        public void initialize(ObjectSelector selector) {
            this.selector = selector;

            switch (this.type.getClaimType()) {
                case 1:
                    this.icon = ClaimsGroup.this.createIcon(ClaimsGroup.this.stronghold);
                    break;
                case 3:
                    this.icon = ClaimsGroup.this.createIcon(ClaimsGroup.this.city);
                    break;
                case 2:
                    this.icon = ClaimsGroup.this.createIcon(ClaimsGroup.this.town);
            }
        }

        public void update() {
            this.name = ((Name) this.claim.get(Name.class)).getName();
        }

        public Spatial getIcon() {
            return this.icon != null ? this.icon.getNode() : null;
        }

        public String getName() {
            return "Place Property:" + this.name;
        }

        public void select(Vector3f pos, Vector3f dir, Quaternion rotation, boolean value) {
            ClaimsGroup.this.defaultClaimTool.select(pos, dir, rotation, value);
        }

        public void place(Vector3f pos, Vector3f dir, Quaternion rotation, boolean value) {
            if (value) {
                return;
            }

            CollisionResult collision = this.selector.intersectObjects(pos, dir);
            if (collision != null) {
                Node parent = this.selector.findEntityParent(collision.getGeometry());
                Long id = (Long) parent.getUserData("id");
                ClaimsGroup.log.info("Selected id:" + id);

                Vector3f p = collision.getContactPoint().clone();
                float t = p.y;
                p.y = p.z;
                p.z = t;

                ActionParameter parm = new ObjectParameter(new EntityId(id.longValue()), p);
                this.selector.getGameClient().execute("Alternate Action", new EntityId(id.longValue()), parm);
                return;
            }

            WorldIntersector.Intersection hit = this.selector.intersectWorld(pos, dir, new Integer[0]);
            if (hit == null) {
                return;
            }
            Vector3i block = hit.getBlock();
            int side = hit.getSide();
            Vector3f point = hit.getPoint();
            System.out.println("place:" + point);

            ActionParameter parm = new BlockParameter(point, block, side);

            this.selector.getGameClient().execute("Place Claim", this.claim.getId(), parm);
        }

        public boolean isCapturingView() {
            return ClaimsGroup.this.defaultClaimTool.isCapturingView();
        }

        public boolean showBlockSelection() {
            return false;
        }

        public void viewMoved(Vector3f pos, Vector3f dir, Quaternion rotation) {
            ClaimsGroup.this.defaultClaimTool.viewMoved(pos, dir, rotation);
        }
    }

    protected class ClaimTool
            implements Tool {
        private ObjectSelector selector;
        private String name;
        private Node heldNode = null;
        private float heldDistance = 0.0F;
        private Vector3f heldOffset = null;
        private float angleOffset = 0.0F;
        private Quaternion rotDelta;
        private Vector3f lastPos = null;
        private Vector3f lastDir = null;
        private long lastTime = -1L;

        public ClaimTool(String name) {
            this.name = name;
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
            return this.name;
        }

        public void select(Vector3f pos, Vector3f dir, Quaternion rotation, boolean value) {
            System.out.println("ClaimTool.selectObject(" + value + ")");
            if (!value) {
                ClaimsGroup.log.info("Dropping:" + this.heldNode);
                System.out.println("Dropping:" + this.heldNode);

                this.heldNode = null;
                return;
            }

            System.out.println("intersect:" + pos + ", " + dir);
            CollisionResult collision = this.selector.intersectObjects(pos, dir);
            System.out.println("collisions:" + collision);
            if (collision == null) {
                return;
            }
            Node parent = this.selector.findEntityParent(collision.getGeometry());

            Long id = (Long) parent.getUserData("id");

            DragControl dragger = (DragControl) parent.getControl(DragControl.class);
            System.out.println("Dragger:" + dragger);
            if (dragger != null) {
                Position entityPos = dragger.getPosition();
                if (entityPos == null) {
                    System.out.println("Clicked entity has no position?:" + parent);
                    return;
                }
                System.out.println("--------------- dragging:" + parent + " at position:" + entityPos + "   camera at:" + pos);

                this.heldNode = parent;
                this.heldDistance = collision.getDistance();

                Vector3f relativePos = entityPos.getLocation().subtract(pos);
                Vector3f contact = collision.getContactPoint();
                this.heldOffset = new Vector3f(relativePos.x - contact.x, relativePos.y - contact.z, relativePos.z - contact.y);

                Quaternion rot = entityPos.getRotation();
                float[] objAngles = rot.toAngles(new float[3]);
                float[] us = rotation.toAngles(new float[3]);

                ClaimsGroup.log.debug("angles:" + objAngles[0] + ", " + objAngles[1] + ", " + objAngles[2]);
                ClaimsGroup.log.debug("us:" + us[0] + ", " + us[1] + ", " + us[2]);

                this.angleOffset = (objAngles[1] - us[1]);
            }
        }

        public void place(Vector3f pos, Vector3f dir, Quaternion rotation, boolean value) {
            if (value) {
                return;
            }

            CollisionResult collision = this.selector.intersectObjects(pos, dir);
            if (collision != null) {
                Node parent = this.selector.findEntityParent(collision.getGeometry());
                Long id = (Long) parent.getUserData("id");
                ClaimsGroup.log.info("Selected id:" + id);

                Vector3f p = collision.getContactPoint().clone();
                float t = p.y;
                p.y = p.z;
                p.z = t;

                ActionParameter parm = new ObjectParameter(new EntityId(id.longValue()), p);
                this.selector.getGameClient().execute("Alternate Action", new EntityId(id.longValue()), parm);
                return;
            }
        }

        public boolean showBlockSelection() {
            return false;
        }

        public boolean isCapturingView() {
            return this.heldNode != null;
        }

        public void viewMoved(Vector3f pos, Vector3f dir, Quaternion rotation) {
            if (this.heldNode == null) {
                return;
            }
            long time = System.currentTimeMillis();
            if (time < this.lastTime + 50L)
                return;
            this.lastTime = time;

            if ((pos.equals(this.lastPos)) && (dir.equals(this.lastDir)))
                return;
            this.lastPos = pos.clone();
            this.lastDir = dir.clone();

            if (ClaimsGroup.log.isTraceEnabled()) {
                ClaimsGroup.log.trace("Moving:" + this.heldNode);
            }

            Vector3f projected = dir.mult(this.heldDistance);

            projected.addLocal(this.selector.getLocation());

            projected.addLocal(this.heldOffset);

            int type = this.selector.getWorld().getType(projected.x, projected.y, projected.z, null);

            while (type != 0) {
                BlockType bt = mythruna.BlockTypeIndex.types[type];
                if (!bt.isSolid(4)) {
                    break;
                }
                projected.z = (float) Math.floor(projected.z + 1.0D);
                type = this.selector.getWorld().getType(projected.x, projected.y, projected.z, null);
            }

            float[] angles = rotation.toAngles(null);
            float angle = angles[1] + this.angleOffset;

            Quaternion targetRotation = new Quaternion().fromAngleAxis(angle, new Vector3f(0.0F, 1.0F, 0.0F));

            if (this.heldNode != null) {
                ((DragControl) this.heldNode.getControl(DragControl.class)).setPosition(new Position(projected.clone(), targetRotation.clone()));
            }
        }
    }
}
