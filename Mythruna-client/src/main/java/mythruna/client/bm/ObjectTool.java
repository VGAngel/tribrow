package mythruna.client.bm;

import com.jme3.collision.CollisionResult;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import mythruna.BlockType;
import mythruna.client.WorldIntersector;
import mythruna.client.view.BlockObject;
import mythruna.db.BlueprintData;
import mythruna.es.*;
import mythruna.es.action.CreateLinkAction;
import mythruna.es.action.CreateObjectAction;
import mythruna.es.action.MoveObjectAction;
import mythruna.es.action.RemoveLinkAction;
import mythruna.script.ActionParameter;
import mythruna.script.ObjectParameter;
import org.progeeks.util.log.Log;

public class ObjectTool implements Tool {

    static Log log = Log.getLog();
    private ObjectSelector selector;
    private Entity blueprint;
    private String name;
    private BlockObject icon;
    private BlueprintReference bpRef;
    private EntityId heldEntity = null;
    private float heldDistance = 0.0F;
    private Vector3f heldOffset = null;
    private float angleOffset = 0.0F;
    private Quaternion rotDelta;
    private boolean oldWay = true;

    private Vector3f lastPos = null;
    private Vector3f lastDir = null;
    private long lastTime = -1L;

    public ObjectTool(Entity blueprint) {
        this.blueprint = blueprint;

        this.bpRef = ((BlueprintReference) blueprint.get(BlueprintReference.class));
        if (this.bpRef == null)
            throw new RuntimeException("No BlueprintReference for entity:" + blueprint);
    }

    public void initialize(ObjectSelector selector) {
        this.selector = selector;
        this.icon = createBlueprintIcon(this.bpRef);
    }

    public void update() {
        BlueprintReference ref = (BlueprintReference) this.blueprint.get(BlueprintReference.class);
        if (ref.getBlueprintId() == this.bpRef.getBlueprintId())
            return;
        this.bpRef = ref;
        this.icon = createBlueprintIcon(this.bpRef);
    }

    protected BlockObject createBlueprintIcon(BlueprintReference ref) {
        BlueprintData bp = this.selector.getWorld().getBlueprint(ref.getBlueprintId());
        if (bp == null)
            throw new RuntimeException("No blueprint found for id:" + ref.getBlueprintId());
        BlockObject bo = new BlockObject(bp);
        bo.build();
        bo.applyUpdates(null);
        return bo;
    }

    public Spatial getIcon() {
        if (this.icon != null) {
            Node n = this.icon.getNode();
            n.setLocalScale(this.icon.getScale() * 2.0F);
            return n;
        }
        return null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void select(Vector3f pos, Vector3f dir, Quaternion rotation, boolean value) {
        System.out.println("selectObject(" + value + ")");
        if (!value) {
            log.info("Dropping:" + this.heldEntity);
            System.out.println("Dropping:" + this.heldEntity);

            if (!this.oldWay) {
                EntityAction action = new RemoveLinkAction();
                this.selector.getGameClient().executeAction(action, this.selector.getGameClient().getPlayer());
            }

            this.heldEntity = null;
            return;
        }

        CollisionResult collision = this.selector.intersectObjects(pos, dir);
        if (collision == null) {
            return;
        }
        Node parent = collision.getGeometry().getParent();
        Long id = (Long) parent.getUserData("id");
        if (id != null) {
            log.info("Selected id:" + id);
            EntityData ed = this.selector.getWorld().getEntityData();

            Position entityPos = (Position) ed.getComponent(new EntityId(id.longValue()), Position.class);
            if (entityPos == null) {
                System.out.println("Clicked entity has no position?:" + id);
                return;
            }

            if (!this.oldWay) {
                EntityAction action = new MoveObjectAction(new Position(pos.clone(), rotation.clone()));
                this.selector.getGameClient().executeAction(action, this.selector.getGameClient().getPlayer());
            }

            this.heldEntity = new EntityId(id.longValue());
            this.heldDistance = collision.getDistance();

            Vector3f relativePos = entityPos.getLocation().subtract(pos);
            Vector3f contact = collision.getContactPoint();
            this.heldOffset = new Vector3f(relativePos.x - contact.x, relativePos.y - contact.z, relativePos.z - contact.y);

            Quaternion rot = entityPos.getRotation();
            float[] objAngles = rot.toAngles(new float[3]);
            float[] us = rotation.toAngles(new float[3]);

            log.debug("angles:" + objAngles[0] + ", " + objAngles[1] + ", " + objAngles[2]);
            log.debug("us:" + us[0] + ", " + us[1] + ", " + us[2]);

            this.angleOffset = (objAngles[1] - us[1]);

            if (!this.oldWay) {
                System.out.println("Contact:" + contact);
                System.out.println("Pos:" + pos + "  rotation:" + rotation);

                Vector3f srcOffset = rotation.inverse().mult(contact);

                Vector3f targetOffset = contact.subtract(parent.getWorldTranslation());
                targetOffset = parent.getWorldRotation().inverse().mult(targetOffset);

                System.out.println("Source offset:" + srcOffset + "  target offset:" + targetOffset);

                EntityAction action = new CreateLinkAction(srcOffset, targetOffset);
                this.selector.getGameClient().executeAction(action, new EntityId(id.longValue()));
            }
        }
    }

    public void place(Vector3f pos, Vector3f dir, Quaternion rotation, boolean value) {
        if (value) {
            return;
        }

        CollisionResult collision = this.selector.intersectObjects(pos, dir);
        if (collision != null) {
            Node parent = collision.getGeometry().getParent();
            Long id = (Long) parent.getUserData("id");
            log.info("Selected id:" + id);
            if (id == null) {
                return;
            }

            if ("Avatar".equals(parent.getUserData("type"))) {
                return;
            }

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
        if (this.icon != null) {
            log.info("Blueprint id:" + this.icon.getBlueprintId());

            Vector3f point = hit.getPoint();

            ModelInfo modelInfo = new ModelInfo(this.icon.getBlueprintId());
            EntityAction action = new CreateObjectAction(modelInfo, new Position(point.clone()));
            this.selector.getGameClient().executeAction(action, null);
        }
    }

    public boolean showBlockSelection() {
        return false;
    }

    public boolean isCapturingView() {
        return this.heldEntity != null;
    }

    public void viewMoved(Vector3f pos, Vector3f dir, Quaternion rotation) {
        if (this.heldEntity == null) {
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

        if (log.isTraceEnabled()) {
            log.trace("Moving:" + this.heldEntity);
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

        if (this.heldEntity != null) {
            EntityId entityId = this.heldEntity;

            if (this.oldWay) {
                EntityAction action = new MoveObjectAction(new Position(projected.clone(), targetRotation.clone()));

                this.selector.getGameClient().executeAction(action, entityId);
            } else {
                EntityAction action = new MoveObjectAction(new Position(pos.clone(), rotation.clone()));
                this.selector.getGameClient().executeAction(action, this.selector.getGameClient().getPlayer());
            }
        }
    }
}
