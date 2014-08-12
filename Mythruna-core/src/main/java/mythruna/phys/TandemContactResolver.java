package mythruna.phys;

import mythruna.mathd.Vec3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TandemContactResolver implements ContactResolver {

    private static Accumulator nullAccumulator = new Accumulator();

    public TandemContactResolver() {
    }

    public void resolveContacts(List<Contact> contacts, double t) {
        Map accumulators = new HashMap();

        resolvePenetration(contacts, t, accumulators);
        resolveVelocity(contacts, t, accumulators);
    }

    protected void resolvePenetration(List<Contact> contacts, double t, Map<RigidBody, Accumulator> accumulators) {
        Vec3d linear1 = new Vec3d();
        Vec3d angular1 = new Vec3d();
        Vec3d linear2 = new Vec3d();
        Vec3d angular2 = new Vec3d();

        for (Contact c : contacts) {
            c.matchAwakeState();

            double localTemperature = c.localTemperature;

            if (!RigidBody.isCold(localTemperature)) {
                if (c.body1 != null)
                    c.body1.incrementContactCount();
                if (c.body2 != null) {
                    c.body2.incrementContactCount();
                }
                c.calculateInternals(t * localTemperature);

                c.calculatePositionChange(linear1, linear2, angular1, angular2, c.penetration);

                getAccumulator(c.body1, accumulators).accumulate(linear1, angular1);
                getAccumulator(c.body2, accumulators).accumulate(linear2, angular2);
            }

        }

        Vec3d lin = new Vec3d();
        Vec3d ang = new Vec3d();
        for (Map.Entry e : accumulators.entrySet()) {
            RigidBody b = (RigidBody) e.getKey();
            Accumulator acc = (Accumulator) e.getValue();

            Vec3d l = acc.getLinear(lin);
            Vec3d a = acc.getAngular(ang);
            acc.clear();

            b.getCollisionMesh().position.addLocal(l);
            b.getCollisionMesh().orientation.addScaledVectorLocal(a, 1.0D);
            b.calculateDerivedData();
        }
    }

    protected void resolveVelocity(List<Contact> contacts, double t, Map<RigidBody, Accumulator> accumulators) {
        Vec3d vDelta1 = new Vec3d();
        Vec3d rotDelta1 = new Vec3d();
        Vec3d vDelta2 = new Vec3d();
        Vec3d rotDelta2 = new Vec3d();

        for (Contact c : contacts) {
            if (!RigidBody.isCold(c.localTemperature)) {
                c.calculateVelocityChange(vDelta1, vDelta2, rotDelta1, rotDelta2);

                getAccumulator(c.body1, accumulators).accumulate(vDelta1, rotDelta1);
                getAccumulator(c.body2, accumulators).accumulate(vDelta2, rotDelta2);
            }

        }

        Vec3d lin = new Vec3d();
        Vec3d ang = new Vec3d();
        for (Map.Entry e : accumulators.entrySet()) {
            RigidBody b = (RigidBody) e.getKey();
            Accumulator acc = (Accumulator) e.getValue();

            Vec3d l = acc.getLinear(lin);
            Vec3d a = acc.getAngular(ang);
            acc.clear();

            b.addVelocity(l);
            b.addRotation(a);
        }
    }

    private final Accumulator getAccumulator(RigidBody body, Map<RigidBody, Accumulator> accumulators) {
        if (body == null) {
            return nullAccumulator;
        }
        Accumulator result = (Accumulator) accumulators.get(body);
        if (result == null) {
            result = new Accumulator();
            accumulators.put(body, result);
        }
        return result;
    }

    static class Accumulator {
        Vec3d linMin = new Vec3d();
        Vec3d linMax = new Vec3d();
        Vec3d angMin = new Vec3d();
        Vec3d angMax = new Vec3d();

        Accumulator() {
        }

        public void accumulate(Vec3d lin, Vec3d ang) {
            this.linMin.minLocal(lin);
            this.linMax.maxLocal(lin);
            this.angMin.minLocal(ang);
            this.angMax.maxLocal(ang);
        }

        public final Vec3d getLinear(Vec3d store) {
            store.x = (this.linMin.x + this.linMax.x);
            store.y = (this.linMin.y + this.linMax.y);
            store.z = (this.linMin.z + this.linMax.z);
            return store;
        }

        public final Vec3d getAngular(Vec3d store) {
            store.x = (this.angMin.x + this.angMax.x);
            store.y = (this.angMin.y + this.angMax.y);
            store.z = (this.angMin.z + this.angMax.z);
            return store;
        }

        public final void clear() {
            this.linMin.set(0.0D, 0.0D, 0.0D);
            this.linMax.set(0.0D, 0.0D, 0.0D);
            this.angMin.set(0.0D, 0.0D, 0.0D);
            this.angMax.set(0.0D, 0.0D, 0.0D);
        }
    }
}