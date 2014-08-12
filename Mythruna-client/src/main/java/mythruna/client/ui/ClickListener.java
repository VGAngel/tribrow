package mythruna.client.ui;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.scene.Spatial;

public class ClickListener implements MouseListener {

    private CommandList<ClickAction> commands = new CommandList();
    private int xClick;
    private int yClick;
    private long lastClickTime = -1L;
    private long doubleClickTime = 500L;

    public ClickListener(Command<ClickAction>[] list) {
        if (list != null)
            this.commands.addCommands(list);
    }

    public void addCommands(Command<ClickAction>[] list) {
        if (list != null)
            this.commands.addCommands(list);
    }

    protected void execute(Object source, ClickAction action) {
        this.commands.execute(source, action);
    }

    public void mouseButtonEvent(MouseButtonEvent event, Spatial capture) {
        event.setConsumed();

        if (event.isPressed()) {
            return;
        }

        if (event.getButtonIndex() == 0) {
            long time = System.currentTimeMillis();

            if (time - this.lastClickTime < this.doubleClickTime) {
                int x = this.xClick - event.getX();
                int y = this.yClick - event.getY();
                System.out.println("Distance:" + (x * x + y * y));
                if (x * x + y * y < 9) {
                    execute(capture, ClickAction.Enter);

                    this.lastClickTime = (time - this.doubleClickTime);
                    return;
                }

            }

            this.xClick = event.getX();
            this.yClick = event.getY();
            execute(capture, ClickAction.Select);
            this.lastClickTime = time;
        } else if (event.getButtonIndex() == 1) {
            execute(capture, ClickAction.OpenMenu);
        }
    }

    public void mouseEntered(MouseMotionEvent event, Spatial capture) {
        event.setConsumed();
    }

    public void mouseExited(MouseMotionEvent event, Spatial capture) {
    }

    public void mouseMoved(MouseMotionEvent event, Spatial capture) {
        event.setConsumed();
    }
}
