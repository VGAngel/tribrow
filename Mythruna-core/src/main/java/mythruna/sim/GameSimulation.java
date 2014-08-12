package mythruna.sim;

import mythruna.GameSystems;
import mythruna.db.WorldDatabase;

import java.util.ArrayList;
import java.util.List;

public class GameSimulation implements Runnable {

    private GameSystems systems;
    private WorldDatabase worldDb;
    private MobManager entityManager;
    private long lastTime = -1L;
    private static long simTime = -1L;
    private Runnable endLoop;
    private List<SimCommand> commandBuffer = new ArrayList(1000);
    private List<SimCommand> executingBuffer = new ArrayList(1000);

    public GameSimulation(GameSystems systems) {
        this(systems, null);
    }

    public GameSimulation(GameSystems systems, Runnable endLoop) {
        this.systems = systems;
        this.worldDb = systems.getWorld().getWorldDatabase();
        this.endLoop = endLoop;
        this.entityManager = new MobManager(systems.getWorld());

        swap();
    }

    public static long getTime() {
        return simTime;
    }

    public GameSystems getSystems() {
        return this.systems;
    }

    public void setEndLoopProcessor(Runnable r) {
        if (this.endLoop != null)
            throw new RuntimeException("End loop processor already set.");
        this.endLoop = r;
    }

    public WorldDatabase getWorldDb() {
        return this.worldDb;
    }

    public MobManager getEntityManager() {
        return this.entityManager;
    }

    public synchronized void addCommand(SimCommand cmd) {
        if (this.commandBuffer.size() > 15000) {
            this.commandBuffer.clear();
            throw new RuntimeException("Simulation command queue too large, potential overrun recursion detected.");
        }
        this.commandBuffer.add(cmd);
    }

    protected synchronized List<SimCommand> swap() {
        List temp = this.commandBuffer;
        this.commandBuffer = this.executingBuffer;
        this.executingBuffer = temp;

        this.commandBuffer.clear();

        return this.executingBuffer;
    }

    protected void processCommands(long time, long delta) {
        List<SimCommand> commands = swap();
        if (commands.isEmpty()) {
            return;
        }
        for (SimCommand c : commands) {
            c.execute(this, time, delta);
        }
    }

    public void runSim(long time, long delta) {
        processCommands(time, delta);
    }

    public void run() {
        try {
            simTime = System.currentTimeMillis();

            if (this.lastTime == -1L) {
                this.lastTime = simTime;
                return;
            }

            long delta = simTime - this.lastTime;
            this.lastTime = simTime;
            runSim(simTime, delta);

            if (this.endLoop != null)
                this.endLoop.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}