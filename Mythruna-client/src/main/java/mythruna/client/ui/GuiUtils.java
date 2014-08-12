package mythruna.client.ui;

import com.jme3.scene.Spatial;

public class GuiUtils {

    public GuiUtils() {
    }

    public static void addCommand(Spatial s, Command<ClickAction> command) {
        MouseEventControl ctl = (MouseEventControl) s.getControl(MouseEventControl.class);
        if (ctl == null) {
            ctl = new MouseEventControl();
            s.addControl(ctl);
        }

        ClickListener l = (ClickListener) ctl.getMouseListener(ClickListener.class);
        if (l == null) {
            l = new ClickListener(new Command[0]);
            ctl.addMouseListener(l);
        }

        l.addCommands(new Command[]{command});
    }
}