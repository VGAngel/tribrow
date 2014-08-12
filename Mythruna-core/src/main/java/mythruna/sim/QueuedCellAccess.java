package mythruna.sim;

import mythruna.db.CellAccess;
import mythruna.db.WorldDatabase;
import mythruna.es.EntityId;

public class QueuedCellAccess implements CellAccess {

    private EntityId changer;
    private WorldDatabase worldDb;
    private GameSimulation sim;

    public QueuedCellAccess(EntityId changer, GameSimulation sim, WorldDatabase worldDb) {
        this.changer = changer;
        this.sim = sim;
        this.worldDb = worldDb;
    }

    public int getCellType(int x, int y, int z) {
        return this.worldDb.getCellType(x, y, z);
    }

    public int getLight(int lightType, int x, int y, int z) {
        return this.worldDb.getLight(lightType, x, y, z);
    }

    public void setCellType(int x, int y, int z, int type) {
        int original = this.worldDb.getCellType(x, y, z);
        SetBlockCommand cmd = new SetBlockCommand(GameSimulation.getTime(), x, y, z, type, original, this.changer);
        this.sim.addCommand(cmd);
    }
}