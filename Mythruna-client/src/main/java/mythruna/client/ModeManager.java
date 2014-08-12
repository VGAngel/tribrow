package mythruna.client;

import com.jme3.input.InputManager;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;

import java.util.HashMap;
import java.util.Map;

public class ModeManager implements AnalogListener {

    public static final String MOUSE_WHEEL_UP = "WheelUp";
    public static final String MOUSE_WHEEL_DOWN = "WheelDown";
    public static final String MOUSE_WHEEL_PRESSED = "WheelPressed";
    public static ModeManager instance = new ModeManager();
    private Mode defaultMode;
    private Map<String, Mode> modes;
    private Mode current;

    public ModeManager() {
        this.modes = new HashMap();
    }

    public void attachInput(InputManager inputManager) {
        inputManager.addMapping("WheelUp", new Trigger[]{new MouseAxisTrigger(2, false)});
        inputManager.addMapping("WheelDown", new Trigger[]{new MouseAxisTrigger(2, true)});
        inputManager.addMapping("WheelDown", new Trigger[]{new MouseAxisTrigger(2, true)});
        inputManager.addMapping("WheelPressed", new Trigger[]{new MouseButtonTrigger(2)});

        inputManager.addListener(this, new String[]{"WheelUp", "WheelDown", "WheelPressed"});
    }

    public void addMode(String name, String upName, String downName, AnalogListener l) {
        this.modes.put(name, new Mode(name, upName, downName, null, l));
    }

    public void addMode(String name, String upName, String downName, String pressedName, AnalogListener l) {
        this.modes.put(name, new Mode(name, upName, downName, pressedName, l));
    }

    public void removeMode(String name) {
        this.modes.remove(name);
    }

    public void setMode(String name, boolean on) {
        if (on)
            this.current = ((Mode) this.modes.get(name));
        else if ((this.current != null) && (this.current.name != null) && (this.current.name.equals(name)))
            this.current = this.defaultMode;
    }

    public String getMode() {
        Mode m = this.current;
        if (m == null)
            m = this.defaultMode;
        if (m == null) {
            return null;
        }
        return m.name;
    }

    public boolean isActive(String name) {
        return this.current == this.modes.get(name);
    }

    public void setDefaultMode(String upName, String downName, AnalogListener l) {
        this.defaultMode = new Mode(null, upName, downName, null, l);
        if (this.current == null)
            this.current = this.defaultMode;
    }

    public void clearDefaultMode(String upName, String downName) {
        if ((this.defaultMode.upKey.equals(upName)) && (this.defaultMode.downKey.equals(downName))) {
            if (this.current == this.defaultMode)
                this.current = null;
            this.defaultMode = null;
        }
    }

    public void onAnalog(String name, float value, float tpf) {
        if (this.current == null) {
            return;
        }
        if ("WheelUp".equals(name)) {
            this.current.listener.onAnalog(this.current.upKey, value, tpf);
        } else if ("WheelDown".equals(name)) {
            this.current.listener.onAnalog(this.current.downKey, value, tpf);
        } else if (("WheelPressed".equals(name)) && (this.current.pressedKey != null)) {
            this.current.listener.onAnalog(this.current.pressedKey, value, tpf);
        }
    }

    protected class Mode {
        protected String name;
        protected String upKey;
        protected String downKey;
        protected String pressedKey;
        protected AnalogListener listener;

        public Mode(String name, String up, String down, String pressed, AnalogListener l) {
            this.name = name;
            this.upKey = up;
            this.downKey = down;
            this.pressedKey = pressed;
            this.listener = l;
        }

        public String toString() {
            return this.name;
        }
    }
}