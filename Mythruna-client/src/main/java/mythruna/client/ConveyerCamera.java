package mythruna.client;

import com.jme3.app.Application;
import com.jme3.input.InputManager;
import com.jme3.input.Joystick;
import com.jme3.input.controls.*;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import mythruna.Coordinates;
import mythruna.MovementState;
import mythruna.client.ui.ObservableState;
import mythruna.client.view.LocalArea;

public class ConveyerCamera extends ObservableState
        implements AnalogListener, ActionListener {
    public static final long MIN_INTERSECT_TIME = 250L;
    private static final String CAM_LEFT = "CAM_PanLeft";
    private static final String CAM_RIGHT = "CAM_PanRight";
    private static final String CAM_UP = "CAM_PanUp";
    private static final String CAM_DOWN = "CAM_PanDown";
    private static final String CAM_FORWARD = "CAM_Forward";
    private static final String CAM_BACK = "CAM_Back";
    private static final String CAM_STRAFE_LEFT = "CAM_StrafeLeft";
    private static final String CAM_STRAFE_RIGHT = "CAM_StrafeRight";
    private static final String CAM_RAISE = "CAM_Raise";
    private static final String CAM_LOWER = "CAM_Lower";
    private static final String CAM_JUMP = "CAM_Jump";
    private static final String CAM_RUN = "CAM_Run";
    private static final String CAM_TEST = "CAM_Test";
    private static final String[] CAM_MAPPINGS = {"CAM_PanLeft", "CAM_PanRight", "CAM_PanUp", "CAM_PanDown", "CAM_Forward", "CAM_Back", "CAM_StrafeLeft", "CAM_StrafeRight", "CAM_Raise", "CAM_Lower", "CAM_Jump", "CAM_Run", "CAM_Test"};
    private Camera camera;
    private float movementSpeed;
    private Vector3f initialUpVec = new Vector3f(0.0F, 1.0F, 0.0F);
    private InputManager inputManager;
    private MovementState movement = new MovementState();
    private GameClient client;
    private LocalArea localArea;
    private long lastIntersectTime = -1L;
    private WorldIntersector.Intersection hit;
    private boolean hitTest = true;

    private boolean walkOnWater = false;

    public ConveyerCamera(Camera camera, GameClient client, LocalArea localArea) {
        super("CameraState", true);
        this.camera = camera;
        this.movementSpeed = 5.0F;

        this.localArea = localArea;
        this.client = client;
    }

    public Camera getCamera() {
        return this.camera;
    }

    public void setHitTest(boolean f) {
        this.hitTest = f;
    }

    public void setSpeed(float f) {
        this.movementSpeed = f;
    }

    public float getSpeed() {
        return this.movementSpeed;
    }

    public float getRotationSpeed() {
        return ClientOptions.getInstance().getRotationSpeed();
    }

    public boolean invertMouse() {
        return ClientOptions.getInstance().getInvertMouse();
    }

    public void setWorldLocation(float x, float y) {
        float z = this.localArea.setLocation(x, y);

        this.client.setLocation(x, y, z);
    }

    protected void initialize(Application app) {
        super.initialize(app);
        registerWithInput(app.getInputManager());
    }

    public void registerWithInput(InputManager inputManager) {
        this.inputManager = inputManager;

        inputManager.addMapping("CAM_PanLeft", new Trigger[]{new MouseAxisTrigger(0, true), new KeyTrigger(203)});

        inputManager.addMapping("CAM_PanRight", new Trigger[]{new MouseAxisTrigger(0, false), new KeyTrigger(205)});

        inputManager.addMapping("CAM_PanUp", new Trigger[]{new MouseAxisTrigger(1, false), new KeyTrigger(200)});

        inputManager.addMapping("CAM_PanDown", new Trigger[]{new MouseAxisTrigger(1, true), new KeyTrigger(208)});

        inputManager.addMapping("CAM_StrafeLeft", new Trigger[]{new KeyTrigger(30)});
        inputManager.addMapping("CAM_StrafeRight", new Trigger[]{new KeyTrigger(32)});
        inputManager.addMapping("CAM_Forward", new Trigger[]{new KeyTrigger(17)});
        inputManager.addMapping("CAM_Back", new Trigger[]{new KeyTrigger(31)});
        inputManager.addMapping("CAM_Raise", new Trigger[]{new KeyTrigger(16)});
        inputManager.addMapping("CAM_Lower", new Trigger[]{new KeyTrigger(44)});
        inputManager.addMapping("CAM_Jump", new Trigger[]{new KeyTrigger(57)});
        inputManager.addMapping("CAM_Run", new Trigger[]{new KeyTrigger(42)});

        inputManager.addMapping("CAM_Test", new Trigger[]{new KeyTrigger(45)});

        inputManager.addListener(this, CAM_MAPPINGS);

        Joystick[] sticks = inputManager.getJoysticks();
        if ((sticks != null) && (sticks.length > 0)) {
            sticks[0].assignAxis("CAM_StrafeRight", "CAM_StrafeLeft", 254);
            sticks[0].assignAxis("CAM_Forward", "CAM_Back", 255);
            sticks[0].assignAxis("CAM_PanRight", "CAM_PanLeft", sticks[0].getXAxisIndex());
            sticks[0].assignAxis("CAM_PanDown", "CAM_PanUp", sticks[0].getYAxisIndex());
        }

        this.client.setFacing(this.camera.getRotation());
    }

    protected void enable() {
        this.inputManager.setCursorVisible(false);
    }

    protected void disable() {
        this.inputManager.setCursorVisible(true);
    }

    private void testIntersect() {
        long start = System.nanoTime();

        long end = System.nanoTime();
        System.out.println("Intersect done in:" + (end - start / 1000000.0D) + " ms");
    }

    private void rotate(float value, Vector3f axis) {
        Matrix3f mat = new Matrix3f();
        mat.fromAngleNormalAxis(getRotationSpeed() * value, axis);

        Vector3f up = this.camera.getUp();
        Vector3f left = this.camera.getLeft();
        Vector3f dir = this.camera.getDirection();

        mat.mult(up, up);
        mat.mult(left, left);
        mat.mult(dir, dir);

        Quaternion q = new Quaternion();
        q.fromAxes(left, up, dir);
        q.normalize();

        this.camera.setAxes(q);

        this.client.setFacing(this.camera.getRotation());
    }

    private void rotatePitch(float value, Vector3f axis) {
        Matrix3f mat = new Matrix3f();

        if (invertMouse())
            value *= -1.0F;
        mat.fromAngleNormalAxis(getRotationSpeed() * value, axis);

        Vector3f up = this.camera.getUp();
        Vector3f left = this.camera.getLeft();
        Vector3f dir = this.camera.getDirection();

        mat.mult(up, up);
        mat.mult(left, left);
        mat.mult(dir, dir);

        Quaternion q = new Quaternion();
        q.fromAxes(left, up, dir);
        q.normalize();

        this.camera.setAxes(q);

        this.client.setFacing(this.camera.getRotation());
    }

    protected Quaternion toWorld(Quaternion q) {
        return Coordinates.flipAxes(q);
    }

    public void onAnalog(String name, float value, float tpf) {
        long start = System.nanoTime();
        try {
            if (!isEnabled()) {
                long end;
                long delta;
                return;
            }
            if ("CAM_PanLeft".equals(name))
                rotate(value, this.initialUpVec);
            else if ("CAM_PanRight".equals(name))
                rotate(-value, this.initialUpVec);
            else if ("CAM_PanUp".equals(name))
                rotatePitch(-value, this.camera.getLeft());
            else if ("CAM_PanDown".equals(name))
                rotatePitch(value, this.camera.getLeft());
        } finally {
            long end = System.nanoTime();
            long delta = end - start;
            if (delta > 1000000L)
                System.out.println("onAnalog() took:" + delta / 1000000.0D + " ms.");
        }
    }

    public boolean isHeadInWater() {
        return this.client.isHeadInWater();
    }

    public WorldIntersector.Intersection getHit() {
        return this.hit;
    }

    public void update() {
        Vector3f pos = this.client.getLocation();

        this.localArea.setLocation(pos.x, pos.y, pos.z);

        long time = System.currentTimeMillis();

        if ((time - this.lastIntersectTime > 250L) && (this.hitTest)) {
            long start = System.nanoTime();

            Vector3f dir = new Vector3f();
            this.camera.getDirection(dir);
            pos = pos.clone();
            dir.set(dir.x, dir.z, dir.y);

            Ray ray = new Ray(pos, dir);
            ray.setLimit(4.0F);

            WorldIntersector wi = new WorldIntersector(this.localArea, ray, new Integer[]{Integer.valueOf(7), Integer.valueOf(8)});
            this.hit = null;
            while (wi.hasNext()) {
                WorldIntersector.Intersection isect = wi.next();
                if ((this.walkOnWater) || (isect.getType() != 7)) {
                    this.hit = isect;
                }
            }

            long end = System.nanoTime();

            this.lastIntersectTime = time;
        }
    }

    private int minIndex(float[] vals) {
        int minIndex = 0;
        float min = (1.0F / 1.0F);
        for (int i = 0; i < vals.length; i++) {
            if (vals[i] != (0.0F / 0.0F)) {
                if (vals[i] < min) {
                    min = vals[i];
                    minIndex = i;
                }
            }
        }
        return minIndex;
    }

    private int maxIndex(float[] vals) {
        int maxIndex = 0;
        float max = (1.0F / -1.0F);
        for (int i = 0; i < vals.length; i++) {
            if (vals[i] != (0.0F / 0.0F)) {
                if (vals[i] > max) {
                    max = vals[i];
                    maxIndex = i;
                }
            }
        }
        return maxIndex;
    }

    public void onAction(String name, boolean value, float tpf) {
        long start = System.nanoTime();
        try {
            if (!isEnabled()) {
                long end;
                long delta;
                return;
            }
            if ("CAM_Test".equals(name)) {
                if (!value) {
                    testIntersect();
                }

                System.gc();
            } else {
                if ("CAM_Forward".equals(name))
                    this.movement.set((byte) 1, value);
                else if ("CAM_Back".equals(name))
                    this.movement.set((byte) 2, value);
                else if ("CAM_StrafeLeft".equals(name))
                    this.movement.set((byte) 4, value);
                else if ("CAM_StrafeRight".equals(name))
                    this.movement.set((byte) 8, value);
                else if ("CAM_Raise".equals(name))
                    this.movement.set((byte) 16, value);
                else if ("CAM_Lower".equals(name))
                    this.movement.set((byte) 32, value);
                else if ("CAM_Jump".equals(name))
                    this.movement.set((byte) 64, value);
                else if ("CAM_Run".equals(name)) {
                    this.movement.set((byte) -128, value);
                }
                this.client.setMoveState(this.movement.getMovementFlags());
            }
        } finally {
            long end = System.nanoTime();
            long delta = end - start;
            if (delta > 1000000L)
                System.out.println("onAction() took:" + delta / 1000000.0D + " ms.");
        }
    }
}