package mythruna.client.bm;

import com.jme3.scene.Spatial;

import java.util.ArrayList;
import java.util.List;

public class ToolGroup {
    private String name;
    private Spatial icon;
    private List<Tool> tools = new ArrayList();
    private ObjectSelector selector;
    private boolean visible;

    public ToolGroup(String name) {
        this.name = name;
    }

    public void initialize(ObjectSelector selector) {
        this.selector = selector;
        for (Tool t : this.tools)
            t.initialize(selector);
    }

    protected ObjectSelector getSelector() {
        return this.selector;
    }

    public String getName() {
        return this.name;
    }

    public void setVisible(boolean v) {
        this.visible = v;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setIcon(Spatial icon) {
        this.icon = icon;
    }

    public Spatial getIcon() {
        if (this.icon != null) {
            return this.icon;
        }
        if (this.tools.size() > 0) {
            this.icon = ((Tool) this.tools.get(0)).getIcon();
        }
        return this.icon;
    }

    public List<Tool> getTools() {
        return this.tools;
    }

    public String toString() {
        return "ToolGroup[" + this.name + "]";
    }
}