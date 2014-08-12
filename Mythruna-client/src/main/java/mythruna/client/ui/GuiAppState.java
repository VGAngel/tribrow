package mythruna.client.ui;

import com.jme3.app.Application;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.*;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import mythruna.client.anim.AnimationState;
import mythruna.client.anim.AnimationTask;
import org.progeeks.util.log.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GuiAppState extends ObservableState {
    static Log log = Log.getLog();
    private Node orthoRoot;
    private ViewPortState view;
    private GuiViewPortState overlay;
    private MouseObserver mouseObserver = new MouseObserver();
    private Spatial hitTarget = null;
    private Spatial capture = null;

    private List<ObservableState> dependentStates = new ArrayList();

    public GuiAppState(Node orthoRoot) {
        super("GUI", false);
        this.orthoRoot = orthoRoot;
    }

    public Node getPerspectiveRoot() {
        return this.view.getRoot();
    }

    public Node getOrthoRoot() {
        return this.orthoRoot;
    }

    public Node getOverlayRoot() {
        return this.overlay.getRoot();
    }

    public Camera getCamera() {
        return this.view.getCamera();
    }

    public void setEnabled(boolean f) {
        log.warn("GuiAppState.setEnabled(" + f + ") called directly instead of adding a dependent.", new Throwable("Call Stack"));
        super.setEnabled(f);
    }

    public void addDependent(ObservableState state) {
        System.out.println("GuiAppState.addDependent(" + state + ")");
        this.dependentStates.add(state);
        super.setEnabled(!this.dependentStates.isEmpty());
    }

    public void removeDependent(ObservableState state) {
        System.out.println("GuiAppState.removeDependent(" + state + ")");
        this.dependentStates.remove(state);
        super.setEnabled(!this.dependentStates.isEmpty());
    }

    public List<AnimationTask> removeDependent(ObservableState state, float delay) {
        return ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{new RemoveDependentState(state, delay)});
    }

    protected void initialize(Application app) {
        super.initialize(app);

        this.view = new ViewPortState("3D GUI");
        getStateManager().attach(this.view);

        this.overlay = new GuiViewPortState("Overlay");
        getStateManager().attach(this.overlay);

        DirectionalLight light = new DirectionalLight();
        light.setDirection(new Vector3f(0.5F, -1.0F, -1.0F).normalizeLocal());
        this.orthoRoot.addLight(light);
    }

    protected void enable() {
        super.enable();
        System.out.println("-------------------------3D view is enabled");
        this.view.setEnabled(true);
        this.overlay.setEnabled(true);
        getApplication().getInputManager().addRawInputListener(this.mouseObserver);
    }

    protected void disable() {
        super.disable();
        releaseCapture();
        getApplication().getInputManager().removeRawInputListener(this.mouseObserver);
        this.view.setEnabled(false);
        this.overlay.setEnabled(false);
        System.out.println("-------------------------3D view is disabled");
    }

    public void render(RenderManager rm) {
        super.render(rm);
    }

    public Vector3f screenToWorld(float x, float y, float distance) {
        Ray upperLeft = getScreenRay(new Vector2f(x, y));
        Vector3f pos = upperLeft.getOrigin().add(upperLeft.getDirection().mult(distance));
        return pos;
    }

    protected Ray getScreenRay(Vector2f cursor) {
        Camera cam = this.view.getCamera();

        Vector3f clickFar = cam.getWorldCoordinates(cursor, 1.0F);
        Vector3f clickNear = cam.getWorldCoordinates(cursor, 0.0F);
        Ray mouseRay = new Ray(clickNear, clickFar.subtractLocal(clickNear).normalizeLocal());

        return mouseRay;
    }

    protected Spatial findHitTarget(Spatial s) {
        if (s == null)
            return null;
        MouseEventControl control = (MouseEventControl) s.getControl(MouseEventControl.class);

        if ((control != null) && (control.isEnabled()))
            return s;
        return findHitTarget(s.getParent());
    }

    protected void setCurrentHitTarget(Spatial s, Vector2f cursor) {
        if (this.hitTarget == s) {
            return;
        }
        MouseMotionEvent event = null;

        if (this.hitTarget != null) {
            event = new MouseMotionEvent((int) cursor.x, (int) cursor.y, 0, 0, 0, 0);
            ((MouseEventControl) this.hitTarget.getControl(MouseEventControl.class)).mouseExited(event, this.capture);
        }
        this.hitTarget = s;
        if (this.hitTarget != null) {
            if (event == null) {
                event = new MouseMotionEvent((int) cursor.x, (int) cursor.y, 0, 0, 0, 0);
            }
            ((MouseEventControl) this.hitTarget.getControl(MouseEventControl.class)).mouseEntered(event, this.capture);
        }
    }

    protected void releaseCapture() {
        if (this.capture != null) {
            MouseButtonEvent event = new MouseButtonEvent(0, false, -1000, -1000);
            ((MouseEventControl) this.capture.getControl(MouseEventControl.class)).mouseButtonEvent(event, this.capture);
        }

        if (this.hitTarget != null) {
            MouseMotionEvent event = new MouseMotionEvent(-1000, -1000, 0, 0, 0, 0);
            ((MouseEventControl) this.hitTarget.getControl(MouseEventControl.class)).mouseExited(event, this.capture);
        }

        this.capture = null;
        this.hitTarget = null;
    }

    public void update(float tpf) {
        super.update(tpf);

        if (this.view.getCamera() == null) {
            return;
        }
        Vector2f cursor = getApplication().getInputManager().getCursorPosition();
        Spatial hit = null;

        if (hit == null) {
            Ray mouseRay = new Ray(new Vector3f(cursor.x, cursor.y, 1000.0F), new Vector3f(0.0F, 0.0F, -1.0F));
            CollisionResults results = new CollisionResults();
            int count = getOverlayRoot().collideWith(mouseRay, results);

            if (count > 0) {
                for (CollisionResult cr : results) {
                    Geometry geom = cr.getGeometry();

                    hit = findHitTarget(geom);
                    if (hit != null) {
                        break;
                    }
                }
            }
        }
        if (hit == null) {
            Ray mouseRay = getScreenRay(cursor);
            CollisionResults results = new CollisionResults();
            int count = getPerspectiveRoot().collideWith(mouseRay, results);
            if (count > 0) {
                Iterator i$ = results.iterator();
                if (i$.hasNext()) {
                    CollisionResult cr = (CollisionResult) i$.next();

                    Geometry geom = cr.getGeometry();
                    hit = findHitTarget(geom);
                }
            }
        }

        if (hit == null) {
            Ray mouseRay = new Ray(new Vector3f(cursor.x, cursor.y, 1000.0F), new Vector3f(0.0F, 0.0F, -1.0F));
            CollisionResults results = new CollisionResults();
            int count = this.orthoRoot.collideWith(mouseRay, results);

            if (count > 0) {
                for (CollisionResult cr : results) {
                    Geometry geom = cr.getGeometry();

                    hit = findHitTarget(geom);
                    if (hit != null) {
                        break;
                    }
                }
            }
        }

        setCurrentHitTarget(hit, cursor);

        if (hit != null) {
            MouseMotionEvent event = new MouseMotionEvent((int) cursor.x, (int) cursor.y, 0, 0, 0, 0);
            ((MouseEventControl) hit.getControl(MouseEventControl.class)).mouseMoved(event, this.capture);
        }
    }

    private class RemoveDependentState
            implements AnimationTask {
        private ObservableState state;
        private float totalTime;
        private float time;

        public RemoveDependentState(ObservableState state, float totalTime) {
            this.state = state;
            this.totalTime = totalTime;
        }

        public boolean animate(AnimationState anim, float seconds) {
            if (this.time >= this.totalTime) {
                GuiAppState.this.removeDependent(this.state);
                return false;
            }
            this.time += seconds;
            return true;
        }
    }

    private class MouseObserver
            implements RawInputListener {
        private MouseObserver() {
        }

        public void beginInput() {
        }

        public void endInput() {
        }

        public void onJoyAxisEvent(JoyAxisEvent evt) {
        }

        public void onJoyButtonEvent(JoyButtonEvent evt) {
        }

        public void onMouseMotionEvent(MouseMotionEvent event) {
        }

        public void onMouseButtonEvent(MouseButtonEvent event) {
            if (event.isPressed()) {
                GuiAppState.this.capture = GuiAppState.this.hitTarget;
            } else if (GuiAppState.this.capture != null) {
                ((MouseEventControl) GuiAppState.this.capture.getControl(MouseEventControl.class)).mouseButtonEvent(event, GuiAppState.this.capture);
                GuiAppState.this.capture = null;

                if (event.isConsumed()) {
                    return;
                }
            }
            if (GuiAppState.this.hitTarget == null) {
                return;
            }

            ((MouseEventControl) GuiAppState.this.hitTarget.getControl(MouseEventControl.class)).mouseButtonEvent(event, GuiAppState.this.capture);
        }

        public void onKeyEvent(KeyInputEvent evt) {
        }

        public void onTouchEvent(TouchEvent evt) {
        }
    }
}