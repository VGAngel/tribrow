package mythruna.phys;

import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityComponent;
import mythruna.es.TransientComponent;
import mythruna.sim.TimeBuffer;

@Serializable
public class BodyState implements EntityComponent, TransientComponent {

    private transient TimeBuffer state = new TimeBuffer(10);
    private transient boolean remote;

    public BodyState() {
    }

    public BodyState(boolean remote) {
        this.remote = remote;
    }

    public Class<BodyState> getType() {
        return BodyState.class;
    }

    public boolean isRemote() {
        return this.remote;
    }

    public TimeBuffer getState() {
        return this.state;
    }

    public String toString() {
        return "BodyState[" + this.state + ", remote=" + this.remote + "]";
    }
}