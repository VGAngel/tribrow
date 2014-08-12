package mythruna.geom;

import com.jme3.scene.Node;
import mythruna.es.EntityComponent;

public abstract interface Model extends EntityComponent {

    public abstract Node getNode();
}