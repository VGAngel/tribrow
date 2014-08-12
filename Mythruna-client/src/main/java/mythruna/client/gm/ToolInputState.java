package mythruna.client.gm;

import com.jme3.app.Application;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.*;
import mythruna.client.ui.ObservableState;

public class ToolInputState extends ObservableState {

    private InputObserver inputListener = new InputObserver();
    private ItemToolState toolState;

    public ToolInputState() {
        super("ToolInput", false);
    }

    public void update(float tpf) {
        this.inputListener.update();
    }

    public void initialize(Application app) {
        super.initialize(app);
    }

    public void cleanup() {
    }

    protected void enable() {
        System.out.println("ToolInputState enabled");
        getApplication().getInputManager().addRawInputListener(this.inputListener);
    }

    protected void disable() {
        System.out.println("ToolInputState disabled");
        getApplication().getInputManager().removeRawInputListener(this.inputListener);
    }

    private ItemToolState getToolState() {
        if (this.toolState == null)
            this.toolState = ((ItemToolState) getState(ItemToolState.class));
        return this.toolState;
    }

    protected void mainClick() {
        getToolState().mainClick();
    }

    protected void altClick() {
        getToolState().alternateClick();
    }

    protected void mainButton(boolean down) {
        getToolState().mainButton(down);
    }

    protected void altButton(boolean down) {
        getToolState().alternateButton(down);
    }

    protected boolean mainDrag(int xDelta, int yDelta, int xTotal, int yTotal) {
        return getToolState().mainDrag(xDelta, yDelta, xTotal, yTotal);
    }

    protected boolean altDrag(int xDelta, int yDelta, int xTotal, int yTotal) {
        return getToolState().alternateDrag(xDelta, yDelta, xTotal, yTotal);
    }

    protected void roll(int amount) {
        getToolState().roll(amount);
    }

    protected boolean hover(boolean on) {
        return getToolState().hover(on);
    }

    private class InputObserver
            implements RawInputListener {
        private long clickTime = 200000000L;

        private long hoverTime = 100000000L;
        private long hoverStart = 0L;
        private boolean hovering = false;

        private int clickDistance = 2;
        private int buttonsDown = 0;
        private boolean[] buttons = new boolean[3];
        private long[] times = new long[3];
        private int[][] starts = new int[3][2];

        private ControlSlot slot = ControlSlot.RightHand;

        private InputObserver() {
        }

        public void beginInput() {
        }

        public void endInput() {
        }

        public void onJoyAxisEvent(JoyAxisEvent evt) {
        }

        public void onJoyButtonEvent(JoyButtonEvent evt) {
        }

        public void onKeyEvent(KeyInputEvent evt) {
            ControlSlot original = this.slot;

            if (evt.getKeyCode() == 29) {
                if (evt.isPressed())
                    this.slot = ControlSlot.LeftHand;
                else if (evt.isReleased()) {
                    this.slot = ControlSlot.RightHand;
                }
            }
            if (this.slot != original) {
                resetHover(System.nanoTime());
            }
        }

        public void onMouseButtonEvent(MouseButtonEvent evt) {
            int index = evt.getButtonIndex();
            if (index >= this.buttons.length)
                return;
            boolean pressed = evt.isPressed();
            if (pressed == this.buttons[index])
                return;
            this.buttons[index] = pressed;

            long time = System.nanoTime();
            if (pressed) {
                this.times[index] = System.nanoTime();
                this.starts[index][0] = evt.getX();
                this.starts[index][1] = evt.getY();

                if (index == 0)
                    ToolInputState.this.mainButton(true);
                else if (index == 1)
                    ToolInputState.this.altButton(true);
                this.buttonsDown += 1;
                return;
            }

            if (index == 0)
                ToolInputState.this.mainButton(false);
            else if (index == 1)
                ToolInputState.this.altButton(false);
            this.buttonsDown -= 1;

            long delta = time - this.times[index];
            if (delta < this.clickTime) {
                if (index == 0) {
                    ToolInputState.this.mainClick();
                    evt.setConsumed();
                } else if (index == 1) {
                    ToolInputState.this.altClick();
                    evt.setConsumed();
                }
            }
            this.times[index] = 0L;
            this.starts[index][0] = -1;
            this.starts[index][1] = -1;
        }

        protected void resetHover(long time) {
            if (this.hovering) {
                this.hovering = (!ToolInputState.this.hover(false));

                this.hoverStart = time;
            } else {
                this.hoverStart = time;
                this.hovering = false;
            }
        }

        public void update() {
            if (this.hovering) {
                return;
            }
            long time = System.nanoTime();

            if (time - this.hoverStart > this.hoverTime) {
                this.hoverStart = time;

                this.hovering = ToolInputState.this.hover(true);
            }
        }

        public void onMouseMotionEvent(MouseMotionEvent evt) {
            if (evt.getDeltaWheel() != 0) {
                ToolInputState.this.roll(evt.getDeltaWheel());
                return;
            }

            long time = System.nanoTime();
            resetHover(time);
            for (int index = 0; index < 2; index++) {
                if (!this.buttons[index]) {
                    long delta = time - this.times[index];
                    int xDelta = evt.getX() - this.starts[index][0];
                    int yDelta = evt.getY() - this.starts[index][1];
                    if ((xDelta == 0) && (yDelta == 0)) {
                        return;
                    }

                    if ((delta >= this.clickTime) || (Math.abs(xDelta) > this.clickDistance) || (Math.abs(yDelta) > this.clickDistance)) {
                        boolean consumed = false;
                        if (index == 0)
                            consumed = ToolInputState.this.mainDrag(evt.getDX(), evt.getDY(), xDelta, yDelta);
                        else if (index == 1)
                            consumed = ToolInputState.this.altDrag(evt.getDX(), evt.getDY(), xDelta, yDelta);
                        if (consumed)
                            evt.setConsumed();
                    }
                }
            }
        }

        public void onTouchEvent(TouchEvent evt) {
        }
    }
}
