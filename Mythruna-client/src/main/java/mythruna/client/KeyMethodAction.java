package mythruna.client;

import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;

import java.lang.reflect.Method;

public class KeyMethodAction implements ActionListener {

    private Object target;
    private String name;
    private Method method;
    private KeyTrigger trigger;
    private boolean takesArgument;

    public KeyMethodAction(Object target, String method, int keyCode) {
        this(target, method, new KeyTrigger(keyCode), false);
    }

    public KeyMethodAction(Object target, String method, int keyCode, boolean takesArgument) {
        this(target, method, new KeyTrigger(keyCode), takesArgument);
    }

    public KeyMethodAction(Object target, String method, KeyTrigger trigger, boolean takesArgument) {
        this.target = target;
        this.name = method;
        this.trigger = trigger;
        this.takesArgument = takesArgument;
        resolveMethod();
    }

    public void attach(InputManager inputManager) {
        inputManager.addMapping(this.name, new Trigger[]{this.trigger});
        inputManager.addListener(this, new String[]{this.name});
    }

    public void detach(InputManager inputManager) {
        if (inputManager.hasMapping(this.name))
            inputManager.deleteMapping(this.name);
        inputManager.removeListener(this);
    }

    public String getName() {
        return this.name;
    }

    protected void resolveMethod() {
        try {
            Class c = this.target.getClass();
            if (this.takesArgument)
                this.method = c.getMethod(this.name, new Class[]{Boolean.TYPE});
            else
                this.method = c.getMethod(this.name, new Class[0]);
        } catch (Exception e) {
            throw new RuntimeException("Error resolving action method:" + this.name, e);
        }
    }

    protected void callMethod(boolean pressed) {
        try {
            if (this.takesArgument)
                this.method.invoke(this.target, new Object[]{Boolean.valueOf(pressed)});
            else
                this.method.invoke(this.target, new Object[0]);
        } catch (Exception e) {
            throw new RuntimeException("Error calling method:" + this.name, e);
        }
    }

    public void onAction(String name, boolean keyPressed, float tpf) {
        if ((keyPressed) && (!this.takesArgument)) {
            return;
        }
        callMethod(keyPressed);
    }
}