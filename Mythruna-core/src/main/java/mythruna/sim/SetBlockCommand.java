package mythruna.sim;

import mythruna.Vector3i;
import mythruna.es.EntityId;
import mythruna.event.CellEvent;
import mythruna.event.EventListener;
import mythruna.event.WorldEvents;

public class SetBlockCommand implements SimCommand {

    private long time;
    private int x;
    private int y;
    private int z;
    private int type;
    private int oldType;
    private EntityId changer;
    private EventListener<CellEvent> recall;

    public SetBlockCommand(long time, int x, int y, int z, int type, int oldType, EntityId changer) {
        this(time, x, y, z, type, oldType, changer, null);
    }

    public SetBlockCommand(long time, int x, int y, int z, int type, int oldType, EntityId changer, EventListener<CellEvent> recall) {
        this.time = time;
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
        this.oldType = oldType;
        this.changer = changer;
        this.recall = recall;
    }

    public void execute(GameSimulation sim, long time, long delta) {
        Vector3i cell = new Vector3i(this.x, this.y, this.z);
        CellEvent ce = new CellEvent(cell, this.oldType, this.type);

        sim.getSystems().getEventDispatcher().publishEvent(WorldEvents.cellChanged, ce);
        if (!ce.isReverted()) {
            sim.getWorldDb().setCellType(this.x, this.y, this.z, ce.getNewType());
        } else if (this.recall != null) {
            this.recall.newEvent(WorldEvents.cellChanged, ce);
        }
    }

    public String toString() {
        return "SetBlockCommand[t:" + this.time + ", " + this.x + ", " + this.y + ", " + this.z + ", " + this.type + "]";
    }
}