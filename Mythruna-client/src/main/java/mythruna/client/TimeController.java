package mythruna.client;

import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import mythruna.GameTime;

public class TimeController implements AnalogListener, ActionListener {

    private static final String ADJUST_TIME = "Time Adjust";
    private static final String TIME_UP = "Time Up";
    private static final String TIME_DOWN = "Time Down";
    private static final String TIME_ADVANCE = "Time Advance";
    private static final String[] TIME_MAPPINGS = {"Time Adjust", "Time Up", "Time Down", "Time Advance"};
    private GameTime gameTime;
    private boolean activated;
    private InputManager inputManager;

    public TimeController(GameTime gameTime, InputManager inputManager) {
        this.gameTime = gameTime;
        registerWithInput(inputManager);
    }

    public boolean isActivated() {
        return this.activated;
    }

    protected void registerWithInput(InputManager inputManager) {
        this.inputManager = inputManager;

        inputManager.addMapping("Time Adjust", new Trigger[]{new KeyTrigger(62)});

        ModeManager.instance.addMode("Time Adjust", "Time Up", "Time Down", "Time Advance", this);
        inputManager.addListener(this, new String[]{"Time Adjust"});
    }

    public void onAnalog(String name, float value, float tpf) {
        double increment = 300.0D;
        if ("Time Up".equals(name))
            this.gameTime.setTime(this.gameTime.getTime() + increment);
        else if ("Time Down".equals(name))
            this.gameTime.setTime(this.gameTime.getTime() - increment);
        else if ("Time Advance".equals(name))
            this.gameTime.setTime(this.gameTime.getTime() + 100000.0F * tpf);
    }

    public void onAction(String name, boolean value, float tpf) {
        if ("Time Adjust".equals(name)) {
            ModeManager.instance.setMode("Time Adjust", value);
        }
    }
}