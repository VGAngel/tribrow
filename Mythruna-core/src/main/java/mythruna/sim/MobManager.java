package mythruna.sim;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import mythruna.World;
import mythruna.db.WorldDatabase;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MobManager {

    private World world;
    private WorldDatabase worldDb;
    private Map<MobType, Map<Long, Mob>> classMaps = new HashMap();

    private List<MobChangeListener> listeners = new CopyOnWriteArrayList();

    public MobManager(World world) {
        this.world = world;
        this.worldDb = world.getWorldDatabase();

        this.classMaps.put(MobType.PLAYER, new ConcurrentHashMap());
        this.classMaps.put(MobType.MOB, new ConcurrentHashMap());
        this.classMaps.put(MobType.PLACEABLE, new ConcurrentHashMap());
    }

    protected Map<Long, Mob> getMobs(MobType type) {
        return (Map) this.classMaps.get(type);
    }

    public Collection<Mob> mobs(MobClass ec) {
        return ((Map) this.classMaps.get(ec.getType())).values();
    }

    public void remove(Mob e) {
        getMobs(e.getType().getType()).remove(Long.valueOf(e.getId()));
        e.setAlive(false);
    }

    public Mob getMob(MobClass ec, long id) {
        Mob result = (Mob) getMobs(ec.getType()).get(Long.valueOf(id));
        if (result != null) {
            return result;
        }

        if (ec.getType() != MobType.PLAYER) {
            throw new UnsupportedOperationException("Don't support the type yet:" + ec.getType());
        }
        System.out.println("Creating new mob for:" + ec + "  id:" + id);

        result = new Mob(MobClass.PLAYER, id, 10);

        result.initializeTransform(GameSimulation.getTime(), this.world.getDefaultSpawnLocation(), this.world.getDefaultSpawnDirection());

        getMobs(ec.getType()).put(Long.valueOf(id), result);

        fireChangeEvent(GameSimulation.getTime(), result, null, null);

        return result;
    }

    public void changeMob(long time, Mob e, Quaternion rotation, Vector3f position) {
        if (e.updateTransform(time, position, rotation))
            fireChangeEvent(time, e, position, rotation);
    }

    protected void fireChangeEvent(long time, Mob e, Vector3f newPos, Quaternion newRot) {
        for (MobChangeListener l : this.listeners)
            l.mobChanged(e, newPos, newRot);
    }

    public void addMobChangeListener(MobChangeListener l) {
        this.listeners.add(l);
    }

    public void removeMobChangeListener(MobChangeListener l) {
        this.listeners.remove(l);
    }
}