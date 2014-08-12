package mythruna.client.ui;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ButtonControl extends AbstractControl {
    private List<Command> commands = new CopyOnWriteArrayList();

    public ButtonControl() {
    }

    public void addCommand(Command cmd) {
        this.commands.add(cmd);
    }

    public void removeCommand(Command cmd) {
        this.commands.remove(cmd);
    }

    public void click() {
        for (Command c : this.commands)
            c.execute(this, null);
    }

    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    protected void controlUpdate(float tpf) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        return this;
    }
}