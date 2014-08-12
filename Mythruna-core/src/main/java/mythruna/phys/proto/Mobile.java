package mythruna.phys.proto;

import mythruna.es.EntityComponent;
import mythruna.es.PersistentComponent;

public class Mobile implements EntityComponent, PersistentComponent {

    private boolean canSleep;

    public Mobile() {
    }

    public Mobile(boolean canSleep) {
        this.canSleep = canSleep;
    }

    public Class<Mobile> getType() {
        return Mobile.class;
    }

    public boolean canSleep() {
        return this.canSleep;
    }

    public String toString() {
        return new StringBuilder().append("Mobile[").append(this.canSleep ? "Can Sleep" : "Cannot Sleep").append("]").toString();
    }
}