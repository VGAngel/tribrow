package mythruna.client.ui;

import com.jme3.app.Application;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class ViewPortState extends ObservableState {
    private Camera cam;
    private ViewPort view;
    private Node root;

    public ViewPortState(String name) {
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

        Camera originalCam = app.getCamera();
        this.cam = new Camera(originalCam.getWidth(), originalCam.getHeight());
        float aspect = originalCam.getWidth() / originalCam.getHeight();
        this.cam.setFrustumPerspective(45.0F, aspect, 0.1F, originalCam.getFrustumFar());

        this.view = app.getRenderManager().createPostView(getName() + " ViewPort", this.cam);

        this.view.setEnabled(isEnabled());
        this.view.setClearFlags(false, true, false);

        this.view.attachScene(this.root);

        DirectionalLight light = new DirectionalLight();
        light.setDirection(new Vector3f(1.0F, 0.2F, -1.5F).normalizeLocal());
        this.root.addLight(light);

        this.root.updateLogicalState(1.0F);
        this.root.updateGeometricState();
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