package mythruna.client;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.Slider;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import org.progeeks.util.Inspector;

public class OptionsScreen implements ScreenController {

    private Nifty nifty;
    private Screen screen;
    private ClientOptions options = ClientOptions.getInstance();

    public OptionsScreen() {
    }

    public void bind(Nifty nifty, Screen screen) {
        System.out.println("---------------------bind:" + screen);
        this.nifty = nifty;
        this.screen = screen;
        if ("options".equals(screen.getScreenId()))
            setupOptions();
    }

    public void onStartScreen() {
        System.out.println("---------------------onStartScreen:" + this.screen.getScreenId());
    }

    public void onEndScreen() {
        System.out.println("---------------------onEndScreen:" + this.screen.getScreenId());
        this.options.save();
    }

    public void go(String screen) {
        System.out.println("go(" + screen + ")");
        this.nifty.gotoScreen(screen);
    }

    public void moved(String s) {
        System.out.println("moved(" + s + ")");
        Slider slider = (Slider) this.screen.findNiftyControl(s, Slider.class);
        Inspector ins = new Inspector(ClientOptions.getInstance());
        ins.set(s, Float.valueOf(slider.getValue() / 100.0F));
    }

    protected void setupOptions() {
        ClientOptions options = ClientOptions.getInstance();
        for (String s : options.options()) {
            Object value = options.getOption(s);
            if ((value instanceof Boolean)) {
                Boolean b = (Boolean) value;
                CheckBox check = (CheckBox) this.screen.findNiftyControl(s, CheckBox.class);
                check.setChecked(b.booleanValue());
            } else if ((value instanceof Float)) {
                Float f = (Float) value;
                Slider slider = (Slider) this.screen.findNiftyControl(s, Slider.class);
                slider.setValue(f.floatValue() * 100.0F);
            }
        }
    }

    public String getOption(String opt) {
        Inspector ins = new Inspector(ClientOptions.getInstance());
        String result = String.valueOf(ins.get(opt));
        return "false";
    }

    public void toggleOption(String opt) {
        Inspector ins = new Inspector(ClientOptions.getInstance());

        CheckBox check = (CheckBox) this.screen.findNiftyControl(opt, CheckBox.class);
        boolean val = !check.isChecked();
        ins.set(opt, Boolean.valueOf(val));
        check.setChecked(val);
    }
}