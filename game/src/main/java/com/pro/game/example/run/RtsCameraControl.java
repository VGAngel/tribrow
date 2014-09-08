package com.pro.game.example.run;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.*;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;

import java.io.IOException;

public class RtsCameraControl implements Control, ActionListener, AnalogListener {

    public enum Degree {
        SIDE, FWD, ROTATE, TILT, DISTANCE,

        MouseAxisX, MouseAxisY, MouseWheel, MouseButtonLeft, MouseButtonMiddle, MouseButtonRight
    }

    private InputManager inputManager;
    private final Camera cam;

    private int[] direction = new int[5];
    private float[] accelPeriod = new float[5];

    private float[] maxSpeed = new float[5];
    private float[] maxAccelPeriod = new float[5];
    private float[] minValue = new float[5];
    private float[] maxValue = new float[5];

    private Vector3f position = new Vector3f();
    private Vector3f focus = new Vector3f();
    private Vector3f vector = new Vector3f(0, 0, 5);

    protected boolean moved = false;
    protected boolean movedR = false;

    protected boolean buttonDownL = false;
    protected boolean buttonDownR = false;
    protected boolean buttonDownM = false;

    private Vector3f center = new Vector3f();
    private float tilt = (float) (Math.PI / 4);
    private float rot = 0;
    private float distance = 15;

    protected Quaternion quaternion = new Quaternion();    
    
    private static final int SIDE = Degree.SIDE.ordinal();
    private static final int FWD = Degree.FWD.ordinal();
    private static final int ROTATE = Degree.ROTATE.ordinal();
    private static final int TILT = Degree.TILT.ordinal();
    private static final int DISTANCE = Degree.DISTANCE.ordinal();

    private EventContext context = new EventContext();

    public RtsCameraControl(Camera cam, Spatial target) {
        this.cam = cam;

        setMinMaxValues(Degree.SIDE, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
        setMinMaxValues(Degree.FWD, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
        setMinMaxValues(Degree.ROTATE, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
        setMinMaxValues(Degree.TILT, 0.2f, (float) (Math.PI / 2) - 0.001f);
        setMinMaxValues(Degree.DISTANCE, 2, Float.POSITIVE_INFINITY);

        setMaxSpeed(Degree.SIDE, 10f, 0.4f);
        setMaxSpeed(Degree.FWD, 10f, 0.4f);
        setMaxSpeed(Degree.ROTATE, 2f, 0.4f);
        setMaxSpeed(Degree.TILT, 1f, 0.4f);
        setMaxSpeed(Degree.DISTANCE, 15f, 0.4f);
        target.addControl(this);
    }

    public void setMaxSpeed(Degree deg, float maxSpd, float accelTime) {
        maxSpeed[deg.ordinal()] = maxSpd / accelTime;
        maxAccelPeriod[deg.ordinal()] = accelTime;
    }

    public void registerWithInput(InputManager inputManager) {
        this.inputManager = inputManager;

        String[] mappingsForKey = new String[]{"+SIDE", "+FWD", "+ROTATE", "+TILT", "+DISTANCE",
                "-SIDE", "-FWD", "-ROTATE", "-TILT", "-DISTANCE",};

        String[] mappingsForMouse = new String[]{"-MouseAxisX", "+MouseAxisX", "-MouseAxisY", "+MouseAxisY",
                "-MouseWheel", "+MouseWheel", "MouseButtonLeft", "MouseButtonMiddle", "MouseButtonRight"};

        inputManager.addMapping("-SIDE", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("+SIDE", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("+FWD", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("-FWD", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("+ROTATE", new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping("-ROTATE", new KeyTrigger(KeyInput.KEY_E));
        inputManager.addMapping("+TILT", new KeyTrigger(KeyInput.KEY_R));
        inputManager.addMapping("-TILT", new KeyTrigger(KeyInput.KEY_F));
        inputManager.addMapping("-DISTANCE", new KeyTrigger(KeyInput.KEY_Z));
        inputManager.addMapping("+DISTANCE", new KeyTrigger(KeyInput.KEY_X));

        inputManager.addMapping("-MouseAxisX", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping("+MouseAxisX", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping("-MouseAxisY", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping("+MouseAxisY", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping("-MouseWheel", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        inputManager.addMapping("+MouseWheel", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping("MouseButtonLeft", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("MouseButtonMiddle", new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));
        inputManager.addMapping("MouseButtonRight", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));

        inputManager.addListener(this, mappingsForKey);
        inputManager.addListener(this, mappingsForMouse);
        inputManager.setCursorVisible(true);
    }

    public void write(JmeExporter ex) throws IOException {

    }

    public void read(JmeImporter im) throws IOException {

    }

    public Control cloneForSpatial(Spatial spatial) {
        RtsCameraControl other = new RtsCameraControl(cam, spatial);
        other.registerWithInput(inputManager);
        return other;
    }

    public void setSpatial(Spatial spatial) {

    }

    public void setEnabled(boolean enabled) {

    }

    public boolean isEnabled() {
        return true;
    }

    boolean isPress = true;

    public void update(final float tpf) {

        for (int i = 0; i < direction.length; i++) {
            int dir = direction[i];
            switch (dir) {
                case -1:
                    accelPeriod[i] = clamp(-maxAccelPeriod[i], accelPeriod[i] - tpf, accelPeriod[i]);
                    break;
                case 0:
                    if (accelPeriod[i] != 0) {
                        double oldSpeed = accelPeriod[i];
                        if (accelPeriod[i] > 0) {
                            accelPeriod[i] -= tpf;
                        } else {
                            accelPeriod[i] += tpf;
                        }
                        if (oldSpeed * accelPeriod[i] < 0) {
                            accelPeriod[i] = 0;
                        }
                    }
                    break;
                case 1:
                    accelPeriod[i] = clamp(accelPeriod[i], accelPeriod[i] + tpf, maxAccelPeriod[i]);
                    break;
            }

        }

        distance += maxSpeed[DISTANCE] * accelPeriod[DISTANCE] * tpf;
        tilt += maxSpeed[TILT] * accelPeriod[TILT] * tpf;
        rot += maxSpeed[ROTATE] * accelPeriod[ROTATE] * tpf;

        distance = clamp(minValue[DISTANCE], distance, maxValue[DISTANCE]);
        rot = clamp(minValue[ROTATE], rot, maxValue[ROTATE]);
        tilt = clamp(minValue[TILT], tilt, maxValue[TILT]);

        double offX = maxSpeed[SIDE] * accelPeriod[SIDE] * tpf;
        double offZ = maxSpeed[FWD] * accelPeriod[FWD] * tpf;

        center.x += offX * Math.cos(-rot) + offZ * Math.sin(rot);
        center.z += offX * Math.sin(-rot) + offZ * Math.cos(rot);

        position.x = center.x + (float) (distance * Math.cos(tilt) * Math.sin(rot));
        position.y = center.y + (float) (distance * Math.sin(tilt));
        position.z = center.z + (float) (distance * Math.cos(tilt) * Math.cos(rot));

        cam.setLocation(position);
        if (isPress) {
            cam.lookAt(center, new Vector3f(0, 1, 0));
            isPress = false;
        }


        float zoomStep = 1.0f;
        float zoomMin = 2f;
        float zoomMax = 100f;
        boolean zoomRelativeToDistance = true;
        if (context.MouseWheel) {
            Vector3f zoomVec = Vector3f.UNIT_Z.clone().multLocal(zoomStep);
            if (zoomRelativeToDistance) {
                zoomVec.multLocal(0.5f + (0.05f * cam.getLocation().getZ()));
            }

            switch (context.MouseWheelAction) {
                case ZoomIn: cam.setLocation(cam.getLocation().add(zoomVec.negateLocal())); break;
                case ZoomOut: cam.setLocation(cam.getLocation().add(zoomVec)); break;
            }

            if (cam.getLocation().z > zoomMax) {
                cam.setLocation(new Vector3f(0, 0, zoomMax));
            } else if (cam.getLocation().z < zoomMin) {
                cam.setLocation(new Vector3f(0, 0, zoomMin));
            }

            context.MouseWheel = false;
        }

    }

    private static float clamp(float min, float value, float max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        } else {
            return value;
        }
    }

    public float getMaxSpeed(Degree dg) {
        return maxSpeed[dg.ordinal()];
    }

    public float getMinValue(Degree dg) {
        return minValue[dg.ordinal()];
    }

    public float getMaxValue(Degree dg) {
        return maxValue[dg.ordinal()];
    }

    // SIDE and FWD min/max values are ignored
    public void setMinMaxValues(Degree dg, float min, float max) {
        minValue[dg.ordinal()] = min;
        maxValue[dg.ordinal()] = max;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setCenter(Vector3f center) {
        this.center.set(center);
    }

    public void render(RenderManager rm, ViewPort vp) {

    }

    public void onAction(String name, boolean isPressed, float tpf) {
        System.out.println("-------------------------------------------------------------");
        System.out.println("onAction name=" + name + " isPressed=" + isPressed + " tpf=" + tpf);
        int press = isPressed ? 1 : 0;

        char sign = name.charAt(0);
        if (sign == '-') {
            press = -press;
        } else if (sign != '+') {

        }

        Boolean way = (sign == '-' || sign == '+') ? sign == '+' : null;

        Degree deg = Degree.valueOf(name.substring(1));

        switch (deg) {
            case MouseButtonLeft: buttonDownL = isPressed; break;
            case MouseButtonMiddle: buttonDownM = isPressed; break;
            case MouseButtonRight: buttonDownR = isPressed; break;
            case MouseAxisX: break;
            case MouseAxisY: break;
            case MouseWheel: {
                context.MouseWheel = isPressed;
                context.MouseWheelAction = way ? EventContext.MouseWheelActionEnum.ZoomIn : EventContext.MouseWheelActionEnum.ZoomOut;
            } break;
            default: direction[deg.ordinal()] = press;
        }
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        System.out.println("-------------------------------------------------------------");
        System.out.println("onAnalog name=" + name + " value=" + value + " tpf=" + tpf);
        System.out.println("onAnalog buttonDownL=" + buttonDownL + " buttonDownM=" + buttonDownM + " buttonDownR=" + buttonDownR);

//        if ("MouseAxisX".equals(name)) {
//            moved = true;
//            movedR = true;
//
//            if ((buttonDownL) || (buttonDownM)) {
//                doRotateCamera(Vector3f.UNIT_Y, -value * 2.5f);
//            }
//            if ((buttonDownR) || (buttonDownM)) {
//                doPanCamera(value * 2.5f, 0);
//            }
//
//        } else if ("MouseAxisY".equals(name)) {
//            moved = true;
//            movedR = true;
//
//            if ((buttonDownL) || (buttonDownM)) {
//                doRotateCamera(cam.getLeft(), -value * 2.5f);
//            }
//            if ((buttonDownR) || (buttonDownM)) {
//                doPanCamera(0, -value * 2.5f);
//            }
//
//        } else if ("MouseAxisX-".equals(name)) {
//            moved = true;
//            movedR = true;
//
//            if ((buttonDownL) || (buttonDownM)) {
//                doRotateCamera(Vector3f.UNIT_Y, value * 2.5f);
//            }
//            if ((buttonDownR) || (buttonDownM)) {
//                doPanCamera(-value * 2.5f, 0);
//            }
//
//        } else if ("MouseAxisY-".equals(name)) {
//            moved = true;
//            movedR = true;
//
//            if ((buttonDownL) || (buttonDownM)) {
//                doRotateCamera(cam.getLeft(), value * 2.5f);
//            }
//            if ((buttonDownR) || (buttonDownM)) {
//                doPanCamera(0, value * 2.5f);
//            }
//
//        } else if ("-MouseWheel".equals(name)) {
//            //doZoomCamera(1f);
//        } else if ("+MouseWheel".equals(name)) {
//            //doZoomCamera(-1f);
//        }
    }

    protected void doZoomCamera(float amount) {
        System.out.println("doZoomCamera amount=" + amount);
        amount = cam.getLocation().distance(focus) * amount;
        float dist = cam.getLocation().distance(focus);
        amount = dist - Math.max(0f, dist - amount);
        Vector3f loc = cam.getLocation().clone();
        loc.scaleAdd(amount, cam.getDirection(), loc);
        cam.setLocation(loc);

        if (cam.isParallelProjection()) {
            float aspect = (float) cam.getWidth() / cam.getHeight();
            float h = FastMath.tan(45f * FastMath.DEG_TO_RAD * .5f) * dist;
            float w = h * aspect;
            cam.setFrustum(-1000, 1000, -w, w, h, -h);
        }
    }

    protected void doPanCamera(float left, float up) {
        cam.getLeft().mult(left, vector);
        vector.scaleAdd(up, cam.getUp(), vector);
        vector.multLocal(cam.getLocation().distance(focus));
        cam.setLocation(cam.getLocation().add(vector));
        focus.addLocal(vector);
    }

    protected void doRotateCamera(Vector3f axis, float amount) {
        if (axis.equals(cam.getLeft())) {
            float elevation = -FastMath.asin(cam.getDirection().y);
            amount = Math.min(Math.max(elevation + amount,
                    -FastMath.HALF_PI), FastMath.HALF_PI)
                    - elevation;
        }
        quaternion.fromAngleAxis(amount, axis);
        cam.getLocation().subtract(focus, vector);
        quaternion.mult(vector, vector);
        focus.add(vector, cam.getLocation());

        Quaternion curRot = cam.getRotation().clone();
        cam.setRotation(quaternion.mult(curRot));
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                SceneViewerTopComponent svtc = SceneViewerTopComponent.findInstance();
//                if (svtc != null) {
//                    CameraToolbar.getInstance().switchToView(View.User);
//                }
//
//            }
//        });
    }
}