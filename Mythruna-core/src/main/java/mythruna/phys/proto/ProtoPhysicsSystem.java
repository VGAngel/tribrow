package mythruna.phys.proto;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import mythruna.BlockType;
import mythruna.Coordinates;
import mythruna.Vector3i;
import mythruna.World;
import mythruna.db.BlueprintData;
import mythruna.es.*;
import mythruna.mathd.Matrix3d;
import mythruna.mathd.Quatd;
import mythruna.mathd.Vec3d;
import org.progeeks.util.log.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProtoPhysicsSystem
        implements Runnable {
    static Log log = Log.getLog();

    private static boolean debugOn = true;
    private EntityData ed;
    private long physTime;
    private double secs;
    private ContactResolver resolver;
    private EntitySet mobs;
    private EntitySet links;
    private Map<EntityId, RigidBody> bodies = new HashMap();
    private Map<EntityId, ContactGenerator> generators = new HashMap();

    private List<Contact> contacts = new ArrayList();

    private List<EntityId> debugContacts = new ArrayList();
    private int debugContactsInUse = 0;
    private World world;
    private long currentTime;

    public ProtoPhysicsSystem(long physTime, EntityData ed, World world) {
        this.physTime = physTime;
        this.secs = (physTime / 1000.0D);
        this.ed = ed;
        this.resolver = new ContactResolver();
        this.world = world;
    }

    public void initialize() {
        this.mobs = this.ed.getEntities(new Class[]{Mobile.class, Position.class});

        this.links = this.ed.getEntities(new Class[]{SourceLink.class, TargetLink.class, Mobile.class});
    }

    public void run() {
        try {
            this.currentTime = System.currentTimeMillis();
            long start = System.nanoTime();
            update(this.secs);
            long end = System.nanoTime();

            double ms = end - start / 1000000.0D;
            if (ms > this.secs * 1000.0D) {
                System.out.println("Frame length:" + ms + " ms");
            }

        } catch (RuntimeException e) {
            log.error("Error updating physics", e);

            throw e;
        }
    }

    protected Position getPosition(Entity e) {
        Position p = (Position) e.get(Position.class);
        if (p != null) {
            return p;
        }
        return null;
    }

    protected RigidBody getBody(EntityId id) {
        RigidBody result = (RigidBody) this.bodies.get(id);
        if (result != null) {
            return result;
        }

        return getBody(this.ed.getEntity(id, new Class[]{Position.class, Mobile.class}));
    }

    protected RigidBody getBody(Entity entity) {
        RigidBody result = (RigidBody) this.bodies.get(entity.getId());
        if (result != null) {
            return result;
        }

        result = new RigidBody(entity.getId());

        ModelInfo mi = (ModelInfo) this.ed.getComponent(entity.getId(), ModelInfo.class);
        System.out.println("********** Entity:" + entity.getId() + "   Model:" + mi);

        double mass = 10.0D;
        Vec3d halfExtents = new Vec3d(0.25D, 0.25D, 0.25D);
        if (entity.getId().getId() < 0L) {
            result.setInverseMass(0.0D);
        } else if (mi != null) {
            System.out.println("Building model mass data...");
            BlueprintData data = this.world.getBlueprint(mi.getBlueprintId());
            result.setMesh(data);

            result.setAcceleration(0.0D, -20.0D, 0.0D);

            halfExtents.x = (data.xSize * 0.5D);
            halfExtents.y = (data.zSize * 0.5D);
            halfExtents.z = (data.ySize * 0.5D);

            int[][][] cells = data.cells;
            int xSize = data.xSize;
            int ySize = data.ySize;
            int zSize = data.zSize;

            double massScale = data.scale * data.scale * data.scale;
            System.out.println("************* massScale:" + massScale);
            double totalMass = 0.0D;

            Vec3d center = new Vec3d();
            for (int i = 0; i < xSize; i++) {
                for (int j = 0; j < ySize; j++) {
                    for (int k = 0; k < zSize; k++) {
                        int t = cells[i][j][k];
                        if (t != 0) {
                            BlockType type = mythruna.BlockTypeIndex.types[t];
                            if (type != null) {
                                double m = type.getMaterial().getMass();

                                System.out.println("t:" + t + " @ " + i + ", " + j + ", " + k + "  mass/cu.m.:" + m);
                                System.out.println("  portion:" + type.getGeomFactory().getMassPortion());
                                System.out.println("  min:" + type.getGeomFactory().getMin() + "  max:" + type.getGeomFactory().getMax());

                                m = m * type.getGeomFactory().getMassPortion() * massScale;

                                totalMass += m;

                                center.addLocal(i * m, k * m, j * m);
                            }
                        }
                    }
                }
            }
            System.out.println("Total mass:" + totalMass);
            System.out.println("Center of mass before:" + center);
            center.multLocal(1.0D / totalMass);
            System.out.println("Center of mass:" + center);

            center.x += 0.5D;
            center.y += 0.5D;
            center.z += 0.5D;
            center.multLocal(data.scale);
            result.cog.set(center);
            System.out.println("Center of mass:" + center);

            result.setMass(totalMass);
        } else {
            result.setMass(mass);
            result.setAcceleration(0.0D, -10.0D, 0.0D);
        }

        result.setDamping(0.9D, 0.8D);

        Mobile m = (Mobile) entity.get(Mobile.class);
        Position p = getPosition(entity);

        if (entity.getId().getId() >= 0L)
            result.setCanSleep(true);
        else
            result.setCanSleep(false);
        if (m != null)
            result.setAwake(true);
        else {
            result.setAwake(false);
        }
        Matrix3d tensor = new Matrix3d();
        Vec3d squares = halfExtents.mult(halfExtents);
        tensor.m00 = (0.300000011920929D * mass * (squares.y + squares.z));
        tensor.m11 = (0.300000011920929D * mass * (squares.x + squares.z));
        tensor.m22 = (0.300000011920929D * mass * (squares.x + squares.y));

        result.setInertiaTensor(tensor);

        Vector3f fPos = p.getLocation();
        Quaternion q = p.getRotation();
        result.setPosition(fPos.x, fPos.z, fPos.y);
        result.setOrientation(new Quatd(q.getX(), q.getY(), q.getZ(), q.getW()));

        result.calculateDerivedData();

        this.bodies.put(entity.getId(), result);

        return result;
    }

    protected void updatePosition(RigidBody body) {
        Position p = (Position) this.ed.getComponent(body.getEntityId(), Position.class);

        Vector3f fPos = p.getLocation();
        Quaternion q = p.getRotation();
        body.setPosition(fPos.x, fPos.z, fPos.y);
        body.setOrientation(new Quatd(q.getX(), q.getY(), q.getZ(), q.getW()));
    }

    protected void pushPosition(RigidBody body) {
        Vec3d pos = body.getPosition().clone();
        Quatd q = body.getOrientation();

        BlueprintData bp = body.getMesh();
        Vec3d origin = body.cog.mult(-1.0D);

        Vec3d offset = origin.add(bp.scale * bp.xSize * 0.5D, 0.0D, bp.scale * bp.ySize * 0.5D);

        offset = q.mult(offset);

        pos.addLocal(offset);

        Vector3f fPos = new Vector3f((float) pos.x, (float) pos.z, (float) pos.y);
        Quaternion fRot = new Quaternion((float) q.x, (float) q.y, (float) q.z, (float) q.w);

        this.ed.setComponent(body.getEntityId(), new Position(fPos, fRot));
    }

    protected void updateDebugState(RigidBody body) {
        if (!debugOn) {
            return;
        }
        PhysicsDebug.PhysicsState state = null;
        if (body.isStatic()) {
            state = PhysicsDebug.PhysicsState.STATIC;
        } else if (body.isAwake()) {
            state = PhysicsDebug.PhysicsState.AWAKE;
        } else {
            state = PhysicsDebug.PhysicsState.SLEEPING;
        }

        if (body.debugState != state) {
            body.debugState = state;
            System.out.println("************* setting physics debug:" + state);
            this.ed.setComponent(body.getEntityId(), new PhysicsDebug(state, 1.0D));
        }
    }

    protected ContactGenerator getGenerator(Entity e) {
        ContactGenerator result = (ContactGenerator) this.generators.get(e.getId());
        if (result != null) {
            return result;
        }
        SourceLink source = (SourceLink) e.get(SourceLink.class);
        Vector3f v1 = source.getOffset();

        TargetLink target = (TargetLink) e.get(TargetLink.class);
        Vector3f v2 = target.getOffset();

        RigidBody b1 = getBody(source.getSource());
        RigidBody b2 = getBody(target.getTarget());

        Vec3d sourceOffset = new Vec3d(v1.x, v1.y, v1.z);
        if (b1.getMesh() != null) {
            Vec3d origin = b1.cog.mult(-1.0D);
            BlueprintData bp = b1.getMesh();
            Vec3d offset = origin.add(bp.scale * bp.xSize / 2.0F, 0.0D, bp.scale * bp.ySize / 2.0F);
            sourceOffset.addLocal(offset);
        }

        Vec3d targetOffset = new Vec3d(v2.x, v2.y, v2.z);

        if (b2.getMesh() != null) {
            Vec3d origin = b2.cog.mult(-1.0D);
            BlueprintData bp = b2.getMesh();
            Vec3d offset = origin.add(bp.scale * bp.xSize / 2.0F, 0.0D, bp.scale * bp.ySize / 2.0F);
            targetOffset.addLocal(offset);
        }

        result = ContactGenerators.hardLink(b2, targetOffset, b1, sourceOffset, 0.1D);

        this.generators.put(e.getId(), result);
        return result;
    }

    protected final void generateContacts() {
        this.contacts.clear();

        Vec3d up = new Vec3d(0.0D, 1.0D, 0.0D);
        double offset = 77.0D;
        double radius = 0.25D;

        for (RigidBody b : this.bodies.values()) {
            if ((!b.isStatic()) && (b.isAwake())) {
                generateWorldContacts(b);
            }

        }

        List list = new ArrayList(this.bodies.values());
        for (int i = 0; i < list.size(); i++) {
            RigidBody b1 = (RigidBody) list.get(i);
            for (int j = i + 1; j < list.size(); j++) {
                RigidBody b2 = (RigidBody) list.get(j);
                generateBodyContacts(b1, b2);
            }
        }

        for (ContactGenerator g : this.generators.values()) {
            g.addContacts(this.contacts);
        }
    }

    protected final boolean bodyBroadPhase(RigidBody b1, RigidBody b2) {
        Vec3d sep = b2.getPosition().subtract(b1.getPosition());
        double d = sep.lengthSq();
        if (d < b2.getMaxRadius() + b1.getMaxRadius())
            return true;
        return false;
    }

    protected final void generateBodyContacts(RigidBody b1, RigidBody b2) {
        if ((!bodyBroadPhase(b1, b2)) || (b1.getMesh() == null) || (b2.getMesh() == null)) {
            return;
        }
        System.out.println("Hit.");

        if (b1.getVolume() < b2.getVolume()) {
            RigidBody temp = b2;
            b2 = b1;
            b1 = temp;
        }

        BlueprintData bp1 = b1.getMesh();
        BlueprintData bp2 = b2.getMesh();
        double scale1 = bp1.scale;
        double scale2 = bp2.scale;
        double cellRadius1 = scale1 * 0.5D;
        double cellRadius2 = scale2 * 0.5D;
        int[][][] cells1 = bp1.cells;
        int[][][] cells2 = bp2.cells;

        Vec3d xModel = new Vec3d(scale2, 0.0D, 0.0D);
        Vec3d yModel = new Vec3d(0.0D, scale2, 0.0D);
        Vec3d zModel = new Vec3d(0.0D, 0.0D, scale2);

        xModel = b2.getOrientation().mult(xModel);
        yModel = b2.getOrientation().mult(yModel);
        zModel = b2.getOrientation().mult(zModel);

        Quatd inverse = b1.getOrientation().inverse();
        xModel = inverse.mult(xModel);
        yModel = inverse.mult(yModel);
        zModel = inverse.mult(zModel);

        double invScale1 = 1.0D / scale1;
        xModel.multLocal(invScale1);
        yModel.multLocal(invScale1);
        zModel.multLocal(invScale1);

        double cellRadius2in1 = invScale1 * cellRadius2;

        Vec3d b2Origin = new Vec3d();
        b2Origin.subtractLocal(b2.cog);

        b2Origin.x += cellRadius2;
        b2Origin.y += cellRadius2;
        b2Origin.z += cellRadius2;

        b2Origin = b2.getOrientation().mult(b2Origin);

        b2Origin.addLocal(b2.getPosition());

        Vec3d base = b2Origin.subtract(b1.getPosition());

        base = inverse.mult(base);

        base.addLocal(b1.cog);

        base.multLocal(invScale1);

        Vec3d px = base.clone();
        Vec3d py = new Vec3d();
        Vec3d pz = new Vec3d();

        double min = (1.0D / 0.0D);
        Vec3d dir = null;
        Vec3d contact = new Vec3d();

        Vec3d inCell = new Vec3d();
        Vec3d signs = new Vec3d();

        for (int i = 0; i < bp2.xSize; ) {
            py.set(px);
            for (int j = 0; j < bp2.ySize; ) {
                pz.set(py);
                for (int k = 0; k < bp2.zSize; ) {
                    int val = cells2[i][j][k];

                    if (val != 0) {
                        int x = Coordinates.worldToCell(pz.x);
                        int y = Coordinates.worldToCell(pz.z);
                        int z = Coordinates.worldToCell(pz.y);

                        clip(x, y, z, pz, inCell, signs);

                        boolean xOverlap = inCell.x < cellRadius2in1;
                        boolean yOverlap = inCell.y < cellRadius2in1;
                        boolean zOverlap = inCell.z < cellRadius2in1;

                        boolean main = checkBlueprint(b1, b2, x, y, z, bp1, pz, inCell, signs, cellRadius2in1, true, true, true);

                        int xs = (int) signs.x;
                        int ys = (int) signs.y;
                        int zs = (int) signs.z;

                        boolean found = false;
                        if (xOverlap) {
                            clip(x + xs, y, z, pz, inCell, signs);
                            found |= checkBlueprint(b1, b2, x + xs, y, z, bp1, pz, inCell, signs, cellRadius2in1, true, false, false);
                        }
                        if (yOverlap) {
                            clip(x, y, z + ys, pz, inCell, signs);
                            found |= checkBlueprint(b1, b2, x, y, z + ys, bp1, pz, inCell, signs, cellRadius2in1, false, true, false);
                        }
                        if (zOverlap) {
                            clip(x, y + zs, z, pz, inCell, signs);
                            found |= checkBlueprint(b1, b2, x, y + zs, z, bp1, pz, inCell, signs, cellRadius2in1, false, false, true);
                        }

                        if ((xOverlap) && (yOverlap)) {
                            clip(x + xs, y, z + ys, pz, inCell, signs);
                            checkBlueprint(b1, b2, x + xs, y, z + ys, bp1, pz, inCell, signs, cellRadius2in1, true, true, false);
                        }
                        if ((xOverlap) && (zOverlap)) {
                            clip(x + xs, y + zs, z, pz, inCell, signs);
                            checkBlueprint(b1, b2, x + xs, y + zs, z, bp1, pz, inCell, signs, cellRadius2in1, true, false, true);
                        }
                        if ((yOverlap) && (zOverlap)) {
                            clip(x, y + zs, z + ys, pz, inCell, signs);
                            checkBlueprint(b1, b2, x, y + zs, z + ys, bp1, pz, inCell, signs, cellRadius2in1, false, true, true);
                        }
                        if ((xOverlap) && (yOverlap) && (zOverlap)) {
                            clip(x + xs, y + zs, z + ys, pz, inCell, signs);
                            checkBlueprint(b1, b2, x + xs, y + zs, z + ys, bp1, pz, inCell, signs, cellRadius2in1, true, true, true);
                        }
                    }
                    k++;
                    pz.addLocal(yModel);
                }
                j++;
                py.addLocal(zModel);
            }
            i++;
            px.addLocal(xModel);
        }
    }

    private int getType(BlueprintData bp, int x, int y, int z) {
        if ((x < 0) || (y < 0) || (z < 0))
            return 0;
        if ((x >= bp.xSize) || (y >= bp.ySize) || (z >= bp.zSize))
            return 0;
        return bp.cells[x][y][z];
    }

    private boolean checkBlueprint(RigidBody b1, RigidBody b2, int x, int y, int z, BlueprintData bp, Vec3d pos, Vec3d inCell, Vec3d signs, double cellRadius, boolean xCheck, boolean yCheck, boolean zCheck) {
        int t = getType(bp, x, y, z);
        if (t == 0) {
            return false;
        }

        double xd = inCell.x + cellRadius;
        double yd = inCell.y + cellRadius;
        double zd = inCell.z + cellRadius;
        double xs = signs.x;
        double ys = signs.y;
        double zs = signs.z;

        Vec3d normal = new Vec3d();
        double penetration = (1.0D / 0.0D);
        Vec3d contact = new Vec3d();

        if (xCheck) {
            contact.x = x;
            contact.y = pos.y;
            contact.z = pos.z;
            if (xs > 0.0D)
                contact.x += 1.0D;
            normal.x = xs;
            normal.y = 0.0D;
            normal.z = 0.0D;
            penetration = xd;
        }

        if ((yCheck) && (yd < penetration)) {
            contact.x = pos.x;
            contact.y = z;
            contact.z = pos.z;
            if (ys > 0.0D)
                contact.y += 1.0D;
            normal.x = 0.0D;
            normal.y = ys;
            normal.z = 0.0D;
            penetration = yd;
        }

        if ((zCheck) && (zd < penetration)) {
            contact.x = pos.x;
            contact.y = pos.y;
            contact.z = y;
            if (zs > 0.0D)
                contact.z += 1.0D;
            normal.x = 0.0D;
            normal.y = 0.0D;
            normal.z = zs;
            penetration = zd;
        }

        if (penetration < (1.0D / 0.0D)) {
            Contact c = new Contact();

            c.contactNormal = b1.getWorldDirection(normal);

            double scale = bp.scale;

            c.penetration = (penetration * scale);

            contact.multLocal(scale);

            contact.subtractLocal(b1.cog);

            contact = b1.getOrientation().mult(contact);

            contact.addLocal(b1.getPosition());

            c.contactPoint = contact;
            c.setBodyData(b2, b1, 0.95D, 0.05D);

            this.contacts.add(c);

            return true;
        }

        return false;
    }

    protected final boolean worldBroadPhase(RigidBody b) {
        double maxRadius = b.getMaxRadius();
        Vec3d minPos = b.getPosition().subtract(maxRadius, maxRadius, maxRadius);
        Vec3d maxPos = b.getPosition().add(maxRadius, maxRadius, maxRadius);

        Vector3i minCell = Coordinates.physToCell(minPos);
        Vector3i maxCell = Coordinates.physToCell(maxPos);

        for (int i = minCell.x; i <= maxCell.x; i++) {
            for (int j = minCell.y; j <= maxCell.y; j++) {
                for (int k = minCell.z; k <= maxCell.z; k++) {
                    int t = this.world.getType(i, j, k, null);

                    if (t != 0) {
                        BlockType type = mythruna.BlockTypeIndex.types[t];
                        if (type != null) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void clip(int x, int y, int z, Vec3d pos, Vec3d inCell, Vec3d signs) {
        double xd = pos.x - x;
        double xs = -1.0D;
        if (xd > 0.5D) {
            xd = 1.0D - xd;
            xs = 1.0D;
        }
        double yd = pos.y - z;
        double ys = -1.0D;
        if (yd > 0.5D) {
            yd = 1.0D - yd;
            ys = 1.0D;
        }
        double zd = pos.z - y;
        double zs = -1.0D;
        if (zd > 0.5D) {
            zd = 1.0D - zd;
            zs = 1.0D;
        }

        inCell.set(xd, yd, zd);
        signs.set(xs, ys, zs);
    }

    private boolean checkWorld(RigidBody b, int x, int y, int z, Vec3d pos, Vec3d inCell, Vec3d signs, double cellRadius, boolean xCheck, boolean yCheck, boolean zCheck) {
        int t = this.world.getType(x, y, z, null);
        if (t == 0) {
            return false;
        }

        double xd = inCell.x + cellRadius;
        double yd = inCell.y + cellRadius;
        double zd = inCell.z + cellRadius;
        double xs = signs.x;
        double ys = signs.y;
        double zs = signs.z;

        Vec3d normal = new Vec3d();
        double penetration = (1.0D / 0.0D);
        Vec3d contact = new Vec3d();

        if (xCheck) {
            contact.x = x;
            contact.y = pos.y;
            contact.z = pos.z;
            if (xs > 0.0D)
                contact.x += 1.0D;
            normal.x = xs;
            normal.y = 0.0D;
            normal.z = 0.0D;
            penetration = xd;
        }

        if ((yCheck) && (yd < penetration)) {
            contact.x = pos.x;
            contact.y = z;
            contact.z = pos.z;
            if (ys > 0.0D)
                contact.y += 1.0D;
            normal.x = 0.0D;
            normal.y = ys;
            normal.z = 0.0D;
            penetration = yd;
        }

        if ((zCheck) && (zd < penetration)) {
            contact.x = pos.x;
            contact.y = pos.y;
            contact.z = y;
            if (zs > 0.0D)
                contact.z += 1.0D;
            normal.x = 0.0D;
            normal.y = 0.0D;
            normal.z = zs;
            penetration = zd;
        }

        if (penetration < (1.0D / 0.0D)) {
            Contact c = new Contact();
            c.contactNormal = normal;
            c.penetration = penetration;
            c.contactPoint = contact;
            c.setBodyData(b, null, 0.95D, 0.05D);

            this.contacts.add(c);

            return true;
        }

        return false;
    }

    protected final void generateWorldContacts(RigidBody b) {
        if (!worldBroadPhase(b)) {
            return;
        }

        Vec3d xWorld = new Vec3d(1.0D, 0.0D, 0.0D);
        Vec3d yWorld = new Vec3d(0.0D, 1.0D, 0.0D);
        Vec3d zWorld = new Vec3d(0.0D, 0.0D, 1.0D);

        Vec3d xModel = b.getOrientation().mult(xWorld);
        Vec3d yModel = b.getOrientation().mult(yWorld);
        Vec3d zModel = b.getOrientation().mult(zWorld);

        BlueprintData bp = b.getMesh();
        double scale = bp.scale;
        double cellRadius = scale * 0.5D;
        int[][][] cells = bp.cells;

        Vec3d base = new Vec3d();

        base.subtractLocal(b.cog);
        base.x += cellRadius;
        base.y += cellRadius;
        base.z += cellRadius;

        base = b.getOrientation().mult(base);
        base.addLocal(b.getPosition());

        xModel.multLocal(scale);
        yModel.multLocal(scale);
        zModel.multLocal(scale);

        Vec3d px = base.clone();
        Vec3d py = new Vec3d();
        Vec3d pz = new Vec3d();

        double min = (1.0D / 0.0D);
        Vec3d dir = null;
        Vec3d contact = new Vec3d();

        Vec3d inCell = new Vec3d();
        Vec3d signs = new Vec3d();

        for (int i = 0; i < bp.xSize; ) {
            py.set(px);
            for (int j = 0; j < bp.ySize; ) {
                pz.set(py);
                for (int k = 0; k < bp.zSize; ) {
                    int val = cells[i][j][k];

                    if (val != 0) {
                        int x = Coordinates.worldToCell(pz.x);
                        int y = Coordinates.worldToCell(pz.z);
                        int z = Coordinates.worldToCell(pz.y);

                        clip(x, y, z, pz, inCell, signs);

                        boolean xOverlap = inCell.x < cellRadius;
                        boolean yOverlap = inCell.y < cellRadius;
                        boolean zOverlap = inCell.z < cellRadius;

                        boolean main = checkWorld(b, x, y, z, pz, inCell, signs, cellRadius, true, true, true);

                        int xs = (int) signs.x;
                        int ys = (int) signs.y;
                        int zs = (int) signs.z;

                        if (xOverlap) {
                            clip(x + xs, y, z, pz, inCell, signs);
                            checkWorld(b, x + xs, y, z, pz, inCell, signs, cellRadius, true, false, false);
                        }
                        if (yOverlap) {
                            clip(x, y, z + ys, pz, inCell, signs);
                            checkWorld(b, x, y, z + ys, pz, inCell, signs, cellRadius, false, true, false);
                        }
                        if (zOverlap) {
                            clip(x, y + zs, z, pz, inCell, signs);
                            checkWorld(b, x, y + zs, z, pz, inCell, signs, cellRadius, false, false, true);
                        }
                    }
                    k++;
                    pz.addLocal(yModel);
                }
                j++;
                py.addLocal(zModel);
            }
            i++;
            px.addLocal(xModel);
        }
    }

    protected final void resolveContacts(double time) {
        this.resolver.resolveContacts(this.contacts, time);
    }

    protected final void publishContacts() {
        int i;
        for (i = 0; i < this.contacts.size(); i++) {
            Contact c = (Contact) this.contacts.get(i);
            EntityId e = null;

            if (i < this.debugContacts.size()) {
                e = (EntityId) this.debugContacts.get(i);
            } else {
                e = this.ed.createEntity();
                this.debugContacts.add(e);
            }

            ContactDebug cb = new ContactDebug(c);

            this.ed.setComponent(e, cb);
        }

        int inUse = i;

        for (; i < this.debugContacts.size(); i++) {
            EntityId e = (EntityId) this.debugContacts.get(i);

            this.ed.setComponent(e, new ContactDebug(false));
        }

        this.debugContactsInUse = inUse;
    }

    public void update(double time) {
        if ((!this.mobs.applyChanges()) ||
                (this.links.applyChanges())) {
            for (Entity e : this.links.getAddedEntities()) {
                System.out.println("Added link:" + e);
                ContactGenerator g = getGenerator(e);
                System.out.println("   generator:" + g);
            }

            for (Entity e : this.links.getRemovedEntities()) {
                System.out.println("Removed link:" + e);
                this.generators.remove(e.getId());
            }

            if (!this.links.getChangedEntities().isEmpty()) {
                System.out.println("----------Links Changed:" + this.links.getChangedEntities());
            }

        }

        long start = System.nanoTime();

        for (RigidBody b : this.bodies.values()) {
            if (b.getEntityId().getId() < 0L)
                updatePosition(b);
            else if (debugOn) {
                updateDebugState(b);
            }
            b.integrate(time);
        }

        long lap = System.nanoTime();
        long integrationTime = lap - start;
        start = lap;

        generateContacts();

        lap = System.nanoTime();
        long genContactsTime = lap - start;
        start = lap;

        if (debugOn) {
            publishContacts();
        }
        lap = System.nanoTime();
        long pubContactsTime = lap - start;
        start = lap;

        resolveContacts(time);

        lap = System.nanoTime();
        long resolveContactsTime = lap - start;
        start = lap;

        for (RigidBody b : this.bodies.values()) {
            if (b.getEntityId().getId() >= 0L) {
                if (b.isAwake()) {
                    pushPosition(b);
                }
            }
        }
        lap = System.nanoTime();
        long updatePositionTime = lap - start;
        start = lap;
    }
}