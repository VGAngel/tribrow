package mythruna.phys;

import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityComponent;
import mythruna.es.PersistentComponent;

@Serializable
public class Volume implements EntityComponent, PersistentComponent {

    private double volume;
    private double size;

    public Volume() {
    }

    public Volume(double volume, double size) {
        this.volume = volume;
        this.size = size;
    }

    public double getVolume() {
        return this.volume;
    }

    public double getSize() {
        return this.size;
    }

    public Class<Volume> getType() {
        return Volume.class;
    }

    public String toString() {
        return "Volume[" + getVolume() + "/" + getSize() + "]";
    }
}