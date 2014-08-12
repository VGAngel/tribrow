package mythruna.sim;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public class ChangeMobCommand implements SimCommand {
    private long sendTime;
    private Mob entity;
    private Vector3f pos;
    private Quaternion rot;

    public ChangeMobCommand(long sendTime, Mob entity, Vector3f pos, Quaternion rot) {
        this.sendTime = sendTime;
        this.entity = entity;
        this.pos = pos;
        this.rot = rot;
    }

    public void execute(GameSimulation sim, long time, long delta) {
        sim.getEntityManager().changeMob(time, this.entity, this.rot, this.pos);
    }

    public String toString() {
        return "ChangeEntityCommand[t:" + this.sendTime + ", " + this.entity + ", " + this.pos + ", " + this.rot + "]";
    }
}