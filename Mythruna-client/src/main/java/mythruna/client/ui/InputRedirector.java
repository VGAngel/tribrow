package mythruna.client.ui;

import com.jme3.input.RawInputListener;
import com.jme3.input.event.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InputRedirector implements RawInputListener {

    private static InputRedirector instance = new InputRedirector();

    private List<KeyListener> keyListeners = new CopyOnWriteArrayList();

    private List<RawInputListener> rawListeners = new CopyOnWriteArrayList();

    protected InputRedirector() {
    }

    public static InputRedirector getInstance() {
        return instance;
    }

    public void addRawKeyListener(KeyListener l) {
        this.keyListeners.add(l);
    }

    public void addFirstRawKeyListener(KeyListener l) {
        this.keyListeners.add(0, l);
    }

    public void removeRawKeyListener(KeyListener l) {
        this.keyListeners.remove(l);
    }

    public void addRawInputListener(RawInputListener l) {
        this.rawListeners.add(l);
    }

    public void addFirstRawInputListener(RawInputListener l) {
        this.rawListeners.add(0, l);
    }

    public void removeRawInputListener(RawInputListener l) {
        this.rawListeners.remove(l);
    }

    public void beginInput() {
        for (RawInputListener l : this.rawListeners)
            l.beginInput();
    }

    public void endInput() {
        for (RawInputListener l : this.rawListeners)
            l.endInput();
    }

    public void onJoyAxisEvent(JoyAxisEvent evt) {
        for (RawInputListener l : this.rawListeners) {
            if (evt.isConsumed())
                break;
            l.onJoyAxisEvent(evt);
        }
    }

    public void onJoyButtonEvent(JoyButtonEvent evt) {
        for (RawInputListener l : this.rawListeners) {
            if (evt.isConsumed())
                break;
            l.onJoyButtonEvent(evt);
        }
    }

    public void onKeyEvent(KeyInputEvent evt) {
        for (RawInputListener l : this.rawListeners) {
            if (evt.isConsumed())
                break;
            l.onKeyEvent(evt);
        }

        for (KeyListener l : this.keyListeners) {
            if (evt.isConsumed())
                break;
            l.onKeyEvent(evt);
        }
    }

    public void onMouseButtonEvent(MouseButtonEvent evt) {
        for (RawInputListener l : this.rawListeners) {
            if (evt.isConsumed())
                break;
            l.onMouseButtonEvent(evt);
        }
    }

    public void onMouseMotionEvent(MouseMotionEvent evt) {
        for (RawInputListener l : this.rawListeners) {
            if (evt.isConsumed())
                break;
            l.onMouseMotionEvent(evt);
        }
    }

    public void onTouchEvent(TouchEvent evt) {
        for (RawInputListener l : this.rawListeners) {
            if (evt.isConsumed())
                break;
            l.onTouchEvent(evt);
        }
    }
}