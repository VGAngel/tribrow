package mythruna.client;

import com.jme3.app.Application;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import mythruna.Month;
import mythruna.client.ui.Label;
import mythruna.client.ui.ObservableState;
import mythruna.client.view.BuilderReference;
import mythruna.client.view.LocalArea;
import mythruna.db.LeafData;
import mythruna.db.LeafInfo;

import java.util.ArrayList;
import java.util.List;

public class DebugHudState extends ObservableState {
    private Node hud;
    private BitmapFont guiFont;
    private Node main;
    private GameClient gameClient;
    private LocalArea localArea;
    private BitmapText timeLabel;
    private BitmapText dateLabel;
    private BitmapText lightLabel;
    private BitmapText clipLabel;
    private BitmapText leafLabel;
    private BitmapText loadingLabel;
    private BitmapText locationLabel;
    private BitmapText memoryLabel;
    private List<Label> building = new ArrayList();

    private int lastHour = 0;
    private int lastMinute = 0;
    private int lastSunLevel = -1;
    private int lastLightLevel = -1;
    private int lastType = -1;
    private int lastClip = -1;
    private String lastLoc = null;
    private LeafData lastLeaf = null;
    private int lastDay = -1;
    private int pending = -1;

    private int lastMemTotal = -1;
    private Runtime runtime = Runtime.getRuntime();

    private boolean showBuilding = false;

    private float panelHeight = 0.0F;
    private boolean bottom = true;

    public DebugHudState(Node hud, BitmapFont guiFont) {
        super("Debug HUD", true);
        this.hud = hud;
        this.guiFont = guiFont;
    }

    public void setBottom(boolean f) {
        if (this.bottom == f) {
            return;
        }
        Application app = getApplication();
        this.bottom = f;
        if (this.bottom) {
            this.main.setLocalTranslation(app.getCamera().getWidth(), 0.0F, 0.0F);
        } else {
            this.main.setLocalTranslation(app.getCamera().getWidth(), app.getCamera().getHeight() - this.panelHeight, 0.0F);
        }
    }

    protected void initialize(Application app) {
        super.initialize(app);

        this.gameClient = ((GameAppState) getState(GameAppState.class)).getGameClient();
        this.localArea = ((GameAppState) getState(GameAppState.class)).getLocalArea();

        this.main = new Node("Debug HUD");
        this.hud.attachChild(this.main);

        Camera cam = app.getCamera();

        float debugHudScale = app.getCamera().getWidth() / 1280.0F;
        if (debugHudScale > 1.0F)
            debugHudScale = 1.0F;
        this.main.setLocalScale(debugHudScale);
        this.main.setLocalTranslation(app.getCamera().getWidth(), 0.0F, 0.0F);

        float y = 0.0F;
        this.locationLabel = new BitmapText(this.guiFont, false);
        this.locationLabel.setSize(17.0F);
        this.locationLabel.setText("1000.00, 1000.00, 1000.00");

        y = this.locationLabel.getLineHeight();
        this.locationLabel.setLocalTranslation(-this.locationLabel.getLineWidth() - 20.0F, y, 0.0F);

        this.main.attachChild(this.locationLabel);

        this.timeLabel = new BitmapText(this.guiFont, false);
        this.timeLabel.setSize(17.0F);
        this.timeLabel.setText("00:00");
        y += this.timeLabel.getLineHeight();
        this.timeLabel.setLocalTranslation(-this.timeLabel.getLineWidth() - 20.0F, y, 0.0F);

        this.main.attachChild(this.timeLabel);

        this.dateLabel = new BitmapText(this.guiFont, false);
        this.dateLabel.setSize(17.0F);
        this.dateLabel.setText("Month Name 99, 9999");
        this.dateLabel.setLocalTranslation(-this.timeLabel.getLineWidth() - this.dateLabel.getLineWidth() - 40.0F, y, 0.0F);

        this.main.attachChild(this.dateLabel);

        this.clipLabel = new BitmapText(this.guiFont, false);
        this.clipLabel.setSize(15.0F);
        this.clipLabel.setText("Clip: Nearest");
        y += this.clipLabel.getLineHeight();
        this.clipLabel.setLocalTranslation(-this.clipLabel.getLineWidth() - 120.0F, y, 0.0F);

        this.main.attachChild(this.clipLabel);

        this.lightLabel = new BitmapText(this.guiFont, false);
        this.lightLabel.setSize(15.0F);
        this.lightLabel.setText("00/00 @ 00");
        this.lightLabel.setLocalTranslation(-this.lightLabel.getLineWidth() - 20.0F, y, 0.0F);

        this.main.attachChild(this.lightLabel);

        this.leafLabel = new BitmapText(this.guiFont, false);
        this.leafLabel.setSize(15.0F);
        this.leafLabel.setText("Leaf: 255, 255, 255 v:12345");
        y += this.leafLabel.getLineHeight();
        this.leafLabel.setLocalTranslation(-this.leafLabel.getLineWidth() - 20.0F, y, 0.0F);

        this.main.attachChild(this.leafLabel);

        this.memoryLabel = new BitmapText(this.guiFont, false);
        this.memoryLabel.setSize(15.0F);
        this.memoryLabel.setText("Mem: 100%");
        y += this.memoryLabel.getLineHeight();
        float x = -this.memoryLabel.getLineWidth() - 10.0F;
        this.memoryLabel.setLocalTranslation(x, y, 0.0F);

        long maxMemory = this.runtime.maxMemory();
        if (maxMemory < 512000000L) {
            this.memoryLabel.setColor(ColorRGBA.Red);
        }
        this.main.attachChild(this.memoryLabel);

        this.loadingLabel = new BitmapText(this.guiFont, false);
        this.loadingLabel.setSize(15.0F);
        this.loadingLabel.setText("Loading Status");

        x -= this.loadingLabel.getLineWidth();
        x -= 40.0F;
        this.loadingLabel.setLocalTranslation(x, y, 0.0F);

        this.main.attachChild(this.loadingLabel);

        if (this.showBuilding) {
            y += 15.0F;
            for (int i = 0; i < 10; i++) {
                Label l = new Label(this.guiFont);
                l.setLocalTranslation(-this.leafLabel.getLineWidth() - 50.0F, y, 0.0F);
                l.setText("Leaf: 255, 255, 255 v:12345");
                l.setCullHint(Spatial.CullHint.Always);
                this.building.add(l);
                this.main.attachChild(l);
                y += 15.0F;
            }
        } else {
            y += 15.0F;
        }
        this.panelHeight = y;
    }

    protected void enable() {
        if (this.main.getParent() != null)
            return;
        this.hud.attachChild(this.main);
    }

    protected void disable() {
        this.hud.detachChild(this.main);
    }

    public void update(float tpf) {
        float gameTime = GameAppState.environment.getTime();
        int hour = (int) gameTime;
        int minute = (int) ((gameTime - hour) * 60.0F);

        if ((hour != this.lastHour) || (minute != this.lastMinute)) {
            String h = "" + hour;
            String m = "" + minute;
            this.timeLabel.setText(h + ":" + m);
            this.lastHour = hour;
            this.lastMinute = minute;
        }

        int day = this.gameClient.getTimeProvider().getGameDay();
        if (day != this.lastDay) {
            this.lastDay = day;
            Month m = this.gameClient.getTimeProvider().getMonth();
            int d = this.gameClient.getTimeProvider().getMonthDay();
            int y = this.gameClient.getTimeProvider().getYear();

            this.dateLabel.setText(m + " " + (d + 1) + ", " + (100 + y));
        }

        int sunLevel = this.localArea.getCenterSunlightValue();
        int lightLevel = this.localArea.getCenterLocalLightValue();
        int type = this.localArea.getCenterType();
        if ((this.lastSunLevel != sunLevel) || (this.lastLightLevel != lightLevel) || (this.lastType != type)) {
            this.lightLabel.setText(lightLevel + "/" + sunLevel + " @ " + type);
            this.lastSunLevel = sunLevel;
            this.lastLightLevel = lightLevel;
            this.lastType = type;
        }

        if (this.lastLeaf != this.localArea.getCenter()) {
            this.lastLeaf = this.localArea.getCenter();
            if (this.lastLeaf != null) {
                LeafInfo info = this.lastLeaf.getInfo();
                this.leafLabel.setText("Leaf:" + info.x + ", " + info.y + ", " + info.z + " v:" + info.version);
            }
        }

        if (this.pending != this.localArea.getPendingSize()) {
            this.pending = this.localArea.getPendingSize();
            if (this.pending == 0)
                this.loadingLabel.setText("");
            else {
                this.loadingLabel.setText("* Loading *");
            }
        }
        if (this.lastClip != this.localArea.getClipDistance()) {
            this.lastClip = this.localArea.getClipDistance();
            this.clipLabel.setText("Clip:" + this.lastClip * 32 + " m");
        }

        Vector3f ourPos = this.localArea.getLocation();
        String loc = String.format("%.2f, %.2f, %.2f", new Object[]{Float.valueOf(ourPos.x), Float.valueOf(ourPos.y), Float.valueOf(ourPos.z)});
        if (!loc.equals(this.lastLoc)) {
            this.lastLoc = loc;
            this.locationLabel.setText(loc);
            Camera cam = getApplication().getCamera();
            this.locationLabel.setLocalTranslation(-this.locationLabel.getLineWidth() - 20.0F, this.locationLabel.getLineHeight(), 0.0F);
        }

        long usedMemory = this.runtime.totalMemory() - this.runtime.freeMemory();
        long maxMemory = this.runtime.maxMemory();

        int memTotal = (int) (usedMemory * 100L / maxMemory);
        if (this.lastMemTotal != memTotal) {
            this.lastMemTotal = memTotal;
            this.memoryLabel.setText("Mem: " + this.lastMemTotal + "%");
        }

        if (this.showBuilding) {
            int index = 0;

            Label l = (Label) this.building.get(index++);
            l.setText("Pending:" + this.localArea.getLeafBuilder().getPendingSize());
            l.setCullHint(Spatial.CullHint.Never);

            for (BuilderReference ref : this.localArea.getLeafBuilder().getWorking()) {
                l = (Label) this.building.get(index++);
                if (index == 9) {
                    l.setText("More...");
                    l.setCullHint(Spatial.CullHint.Never);
                } else {
                    l.setText(String.valueOf(ref));
                    l.setCullHint(Spatial.CullHint.Never);
                }
            }

            while (index < 10) {
                l = (Label) this.building.get(index++);
                l.setCullHint(Spatial.CullHint.Always);
            }
        }
    }
}