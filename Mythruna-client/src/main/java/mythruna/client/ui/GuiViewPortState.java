package mythruna.client.ui;

import com.jme3.app.Application;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class GuiViewPortState extends ObservableState {
    private Camera cam;
    private ViewPort view;
    private Node root;

    public GuiViewPortState(String name) {
        super(name, false);
    }

    public Node getRoot() {
        return this.root;
    }

    public Camera getCamera() {
        return this.cam;
    }

    protected void initialize(Application app) {
        super.initialize(app);

        this.root = new Node(getName() + " Root");
        this.root.setCullHint(Spatial.CullHint.Never);
        this.root.setQueueBucket(RenderQueue.Bucket.Gui);

        this.cam = app.getCamera();
        this.view = app.getRenderManager().createPostView(getName() + " ViewPort", this.cam);
        this.view.setEnabled(isEnabled());
        this.view.setClearFlags(false, false, false);
        this.view.attachScene(this.root);

        this.root.updateLogicalState(1.0F);
        this.root.updateGeometricState();

        DirectionalLight light = new DirectionalLight();
        light.setDirection(new Vector3f(0.5F, -1.0F, -1.0F).normalizeLocal());
        this.root.addLight(light);
    }

    protected void enable() {
        super.enable();
        if (this.view != null)
            this.view.setEnabled(true);
    }

    protected void disable() {
        super.disable();
        if (this.view != null)
            this.view.setEnabled(false);
    }

    public void render(RenderManager rm) {
        this.root.updateGeometricState();
        super.render(rm);
    }

    public void update(float tpf) {
        this.root.updateLogicalState(tpf);
    }
}