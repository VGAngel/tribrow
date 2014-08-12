package mythruna.client.tabs.property;

import com.jme3.app.Application;
import com.jme3.asset.TextureKey;
import com.jme3.font.BitmapFont;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import mythruna.MaterialIndex;
import mythruna.client.GameClient;
import mythruna.client.anim.*;
import mythruna.client.ui.*;
import mythruna.es.*;
import mythruna.geom.Trifold;
import org.progeeks.util.log.Log;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PropertyViewPanel extends Node {
    static Log log = Log.getLog();

    private static final ColorRGBA titleColor = new ColorRGBA(0.3568628F, 0.07450981F, 0.07450981F, 1.0F);
    private static final ColorRGBA selectColor = new ColorRGBA(0.1215686F, 0.3921569F, 0.2078431F, 1.0F);
    private static final ColorRGBA infoColor = new ColorRGBA(0.2431373F, 0.2431373F, 0.4156863F, 1.0F);

    private static final ColorRGBA shadowColor = new ColorRGBA(0.0F, 0.0F, 0.0F, 0.0F);
    private GameClient gameClient;
    private Application app;
    private BitmapFont writingFont;
    private Entity property;
    private float pageWidth = 426.0F;
    private float pageHeight = 600.0F;
    private float rollerHeight = 80.0F;
    private Node bottomRoller;
    private Label title;
    private Node buttonPanel;
    private Button[] actions;
    private Label tableSwitch;
    private Node info;
    private Map<String, Object> infoValues = new LinkedHashMap();
    private PropertyMapView map;
    private EntitySet plots = null;
    private EntitySet badges = null;
    private TableView table;
    private Table plotTable;
    private Table badgeTable;
    private Node scrollBar;
    private int scrollSize = -1;
    private ContainerAnimation pageOpener;
    private Entity lastProperty = null;

    public PropertyViewPanel(GameClient gameClient, Application app) {
        this.gameClient = gameClient;
        this.app = app;

        this.writingFont = app.getAssetManager().loadFont("Interface/templar32.fnt");

        createScroll();
        createInfoView();
        createMapView();
        createTableView();
    }

    protected void enable() {
        this.pageOpener = new ContainerAnimation();
        ((AnimationState) this.app.getStateManager().getState(AnimationState.class)).add(new AnimationTask[]{this.pageOpener});
    }

    protected void disable() {
        if (this.pageOpener != null)
            this.pageOpener.stop();
    }

    public void updateLogicalState(float tpf) {
        super.updateLogicalState(tpf);

        if ((this.plots != null) && (this.plots.applyChanges())) {
            resetInfo();
            this.plotTable.update();
        }

        if ((this.badges != null) && (this.badges.applyChanges())) {
            resetInfo();
            this.badgeTable.update();
        }

        if (this.table.getPageCount() != this.scrollSize) {
            this.scrollSize = this.table.getPageCount();
            if (this.scrollSize <= 0) {
                this.scrollBar.setCullHint(Spatial.CullHint.Always);
            } else {
                this.scrollBar.setCullHint(Spatial.CullHint.Inherit);
            }
        }
    }

    public void setProperty(Entity property) {
        if (this.lastProperty == property)
            return;
        this.lastProperty = property;
        this.pageOpener.setContainer(new PropertyContainer(property));
    }

    protected void resetInfo() {
        ClaimType type = (ClaimType) this.property.get(ClaimType.class);
        switch (type.getClaimType()) {
            case 1:
                showStrongholdInfo(this.property);
                break;
            case 2:
                showTownInfo("Town", this.property);
                break;
            case 3:
                showTownInfo("City", this.property);
                break;
            case 4:
                showPlotInfo("Town", this.property);
                break;
            case 5:
                showPlotInfo("City", this.property);
        }
    }

    protected void showPlotTable() {
        if (this.table.getTable() == this.plotTable)
            return;
        this.table.setTable(this.plotTable, new float[]{180.0F, 140.0F, 60.0F});
        this.tableSwitch.setText("<Show Badges>");
    }

    protected void showBadgeTable() {
        if (this.table.getTable() == this.badgeTable)
            return;
        this.table.setTable(this.badgeTable, new float[]{180.0F});
        this.tableSwitch.setText("<Show Plots>");
    }

    protected void toggleTable() {
        System.out.println("toggleTable()");

        if (this.table.getTable() == this.badgeTable) {
            showPlotTable();
        } else {
            showBadgeTable();
        }
    }

    protected void setupPlots(Entity e) {
        EntityData ed = this.gameClient.getEntityData();

        if (this.plots != null) {
            this.plots.release();
        }
        this.plots = ed.getEntities(new FieldFilter(ClaimType.class, "parent", e.getId()), new Class[]{ClaimType.class, Name.class, OwnedBy.class});

        this.plotTable = new PlotTable(this.gameClient.getPlayer(), ed, this.plots);
    }

    protected void setupBadges(Entity e) {
        EntityData ed = this.gameClient.getEntityData();

        if (this.badges != null) {
            this.badges.release();
        }
        this.badges = ed.getEntities(new FieldFilter(ClaimPermissions.class, "claimId", e.getId()), new Class[]{ClaimPermissions.class, InContainer.class});

        this.badgeTable = new BadgeTable(this.gameClient.getPlayer(), ed, this.badges);
    }

    protected void createMapView() {
        this.map = new PropertyMapView(this.gameClient, this.app);
        this.map.setLocalTranslation(this.pageWidth - 218.0F, this.pageHeight - 268.0F, 0.0F);
        attachChild(this.map);
    }

    protected void createTableView() {
        this.table = new TableView(this.gameClient, this.app, 10);
        this.table.setLocalTranslation(10.0F, 400.0F, 0.0F);
        attachChild(this.table);

        this.scrollBar = new Node("ScrollBar");
        attachChild(this.scrollBar);

        float scrollHeight = 240.0F;
        Quad page = new Quad(24.0F, scrollHeight);
        Geometry geom = new Geometry("ScrollBar", page);
        Material m = new Material(this.app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        Texture t = this.app.getAssetManager().loadTexture("Interface/scrollbar.jpg");
        m.setTexture("ColorMap", t);
        geom.setMaterial(m);
        this.scrollBar.setLocalTranslation(this.pageWidth - 30.0F, 400.0F - scrollHeight - 30.0F - 6.0F, 0.0F);
        this.scrollBar.attachChild(geom);

        Quad button = new Quad(24.0F, 24.0F);
        geom = new Geometry("ScrollUp", button);
        m = m.clone();
        m.setColor("Color", shadowColor);
        m.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geom.setMaterial(m);
        geom.setLocalTranslation(0.0F, scrollHeight - 24.0F, 0.1F);
        geom.addControl(new MouseEventControl());
        ((MouseEventControl) geom.getControl(MouseEventControl.class)).addMouseListener(new ScrollButtonListener(-1));
        this.scrollBar.attachChild(geom);

        geom = new Geometry("ScrollDown", button);
        geom.setMaterial(m);
        geom.setLocalTranslation(0.0F, 0.0F, 0.1F);
        geom.addControl(new MouseEventControl());
        ((MouseEventControl) geom.getControl(MouseEventControl.class)).addMouseListener(new ScrollButtonListener(1));
        this.scrollBar.attachChild(geom);
    }

    protected void createInfoView() {
        this.title = new Label(this.writingFont);

        this.title.setText("");
        this.title.setColor(titleColor);
        this.title.setShadowColor(shadowColor);
        this.title.setHAlignment(HAlignment.CENTER);
        this.title.setLocalTranslation(this.pageWidth * 0.5F, this.pageHeight + 10.0F, 0.0F);
        this.title.setLocalScale(1.25F);

        attachChild(this.title);

        this.info = new Node("Info Panel");
        this.info.setLocalTranslation(0.0F, this.pageHeight, 0.0F);
        attachChild(this.info);

        this.buttonPanel = new Node("Property Buttons");
        attachChild(this.buttonPanel);

        List list = new ArrayList();

        float buttonWidth = this.pageWidth * 0.3F;
        float buttonHeight = 25.0F;
        float buttonTextScale = 0.75F;

        this.tableSwitch = new Label(this.writingFont);
        this.tableSwitch.setText("<Show Badges>");
        this.tableSwitch.setColor(titleColor);
        this.tableSwitch.setShadowColor(shadowColor);
        this.tableSwitch.setHAlignment(HAlignment.LEFT);
        this.tableSwitch.setLocalTranslation(50.0F, this.pageHeight - 200.0F, 0.0F);
        this.tableSwitch.setLocalScale(0.7F);
        this.tableSwitch.addControl(new MouseEventControl());
        ((MouseEventControl) this.tableSwitch.getControl(MouseEventControl.class)).addMouseListener(new LabelSelector(this.tableSwitch, titleColor, shadowColor, selectColor) {
            public void click(MouseButtonEvent event) {
                PropertyViewPanel.this.toggleTable();
            }
        });
    }

    protected void createScroll() {
        Quad page = new Quad(this.pageWidth, this.pageHeight);
        float x = this.pageWidth / 256.0F;
        float y = this.pageHeight / 256.0F;
        page.setBuffer(VertexBuffer.Type.TexCoord, 2, new float[]{0.0F, y, x, y, x, 0.0F, 0.0F, 0.0F});

        Geometry geom = new Geometry("Page", page);
        Material m = new Material(this.app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        Texture t = this.app.getAssetManager().loadTexture("Textures/sand.jpg");
        t.setWrap(Texture.WrapMode.Repeat);
        m.setTexture("ColorMap", t);
        geom.setMaterial(m);
        geom.setLocalTranslation(0.0F, 70.0F, -1.0F);
        attachChild(geom);

        this.bottomRoller = new Node("Bottom Roller");
        attachChild(this.bottomRoller);

        float rollerWidth = this.pageWidth + 56.0F;
        Trifold roller = new Trifold(rollerWidth, this.rollerHeight);
        roller.setFoldTextureCoordinates(new Vector2f(0.25F, 0.25F), new Vector2f(0.75F, 0.75F));
        roller.setFoldCoordinates(new Vector2f(32.0F, this.rollerHeight * 0.25F), new Vector2f(rollerWidth - 32.0F, this.rollerHeight * 0.75F));

        roller.updateGeometry();

        Geometry roller1 = new Geometry("Roller1", roller);
        m = new Material(this.app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        m.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        TextureKey key = new TextureKey("Interface/roller.png");
        key.setGenerateMips(false);
        t = this.app.getAssetManager().loadTexture(key);
        m.setTexture("ColorMap", t);
        roller1.setMaterial(m);
        roller1.setLocalTranslation(-27.0F, 70.0F - this.rollerHeight * 0.5F, 1.0F);
        this.bottomRoller.attachChild(roller1);

        Quad test2 = new Quad(426.0F, this.pageHeight);
        Geometry geom2 = new Geometry("Overlay1", test2);

        geom2.setMaterial(MaterialIndex.TRANSPARENT_MATERIAL);
        geom2.setLocalTranslation(0.0F, 70.0F - this.pageHeight, 1.0F);
        geom2.setUserData("guiLayer", Integer.valueOf(-1));
        this.bottomRoller.attachChild(geom2);

        Geometry roller2 = new Geometry("Roller2", roller);
        roller2.setMaterial(m);
        roller2.setLocalTranslation(-27.0F, this.pageHeight + 70.0F - this.rollerHeight * 0.5F, 1.1F);

        attachChild(roller2);

        this.bottomRoller.setLocalTranslation(0.0F, this.pageHeight - this.rollerHeight, 0.0F);
    }

    protected void showStrongholdInfo(Entity e) {
        this.infoValues.clear();
        this.infoValues.put("Age", "0 yrs 0 mos 00 days");
        ClaimArea area = (ClaimArea) e.get(ClaimArea.class);
        this.infoValues.put("Area", area.getAreaSize() + " sq. m");
        this.infoValues.put("Size", area.getDeltaX() + " x " + area.getDeltaY());
        this.infoValues.put("Badges", Integer.valueOf(this.badges.size()));
        fillInfoView(this.infoValues);
    }

    protected void showTownInfo(String type, Entity e) {
        this.infoValues.clear();
        this.infoValues.put("Age", "0 yrs 0 mos 00 days");
        ClaimArea area = (ClaimArea) e.get(ClaimArea.class);
        this.infoValues.put("Area", area.getAreaSize() + " sq. m");
        this.infoValues.put("Size", area.getDeltaX() + " x " + area.getDeltaY());
        this.infoValues.put("Plots", Integer.valueOf(this.plots.size()));
        this.infoValues.put("Player Owners", "00");
        this.infoValues.put("NPC Owners", "00");
        this.infoValues.put(type + " Badges", Integer.valueOf(this.badges.size()));
        this.infoValues.put("Last Income", "0000 gp");
        fillInfoView(this.infoValues);
    }

    protected void showPlotInfo(String type, Entity e) {
        this.infoValues.clear();
        ClaimType ct = (ClaimType) e.get(ClaimType.class);
        ClaimArea area = (ClaimArea) e.get(ClaimArea.class);
        Name parentName = (Name) this.gameClient.getEntityData().getComponent(ct.getParent(), Name.class);
        this.infoValues.put("In", parentName.getName());
        this.infoValues.put("Age", "0 yrs 0 mos 00 days");
        this.infoValues.put("Area", area.getAreaSize() + " sq. m");
        this.infoValues.put("Size", area.getDeltaX() + " x " + area.getDeltaY());
        this.infoValues.put("Badges", Integer.valueOf(this.badges.size()));
        fillInfoView(this.infoValues);
    }

    protected void fillInfoView(Map<String, Object> values) {
        System.out.println("Info:" + values);
        clearInfoView();

        float y = 0.0F;
        float x = 10.0F;

        float scale = 0.6666667F;

        for (Map.Entry e : values.entrySet()) {
            Label l = new Label(this.writingFont);
            l.setText((String) e.getKey() + ":");
            l.setVAlignment(VAlignment.TOP);
            l.setColor(titleColor);
            l.setShadowColor(shadowColor);
            l.setLocalTranslation(x, y, 0.0F);
            l.setLocalScale(scale);
            this.info.attachChild(l);

            float width = l.getWidth();

            l = new Label(this.writingFont);
            l.setText(String.valueOf(e.getValue()));
            l.setVAlignment(VAlignment.TOP);
            l.setColor(infoColor);
            l.setShadowColor(shadowColor);
            l.setLocalTranslation(x + width + 10.0F, y, 0.0F);
            l.setLocalScale(scale);
            this.info.attachChild(l);

            y -= l.getHeight() - 4.0F;
        }
    }

    protected void clearInfoView() {
        this.info.detachAllChildren();
    }

    private class ScrollButtonListener
            implements MouseListener {
        private int direction;
        private int xDown;
        private int yDown;

        public ScrollButtonListener(int direction) {
            this.direction = direction;
        }

        public void mouseButtonEvent(MouseButtonEvent event, Spatial capture) {
            event.setConsumed();

            if (event.isPressed()) {
                this.xDown = event.getX();
                this.yDown = event.getY();
            } else {
                int x = event.getX();
                int y = event.getY();
                if ((Math.abs(x - this.xDown) < 3) && (Math.abs(y - this.yDown) < 3)) {
                    PropertyViewPanel.this.table.setPage(PropertyViewPanel.this.table.getPage() + this.direction);
                }
            }
        }

        public void mouseEntered(MouseMotionEvent event, Spatial capture) {
        }

        public void mouseExited(MouseMotionEvent event, Spatial capture) {
        }

        public void mouseMoved(MouseMotionEvent event, Spatial capture) {
        }
    }

    private class PropertyContainer
            implements AnimatedContainer {
        private Entity property;

        public PropertyContainer(Entity property) {
            this.property = property;
        }

        public void detach() {
            System.out.println("-----------------------detach property:" + this.property);
            if (PropertyViewPanel.this.plots != null)
                PropertyViewPanel.this.plots.release();
            PropertyViewPanel.this.plots = null;
            if (PropertyViewPanel.this.badges != null)
                PropertyViewPanel.this.badges.release();
            PropertyViewPanel.this.badges = null;
            PropertyViewPanel.this.table.setTable(null, null);
        }

        public void attach() {
            System.out.println("-----------------------attach property:" + this.property);
            PropertyViewPanel.this.property = this.property;
            PropertyViewPanel.this.map.setProperty(this.property);

            if (this.property != null) {
                Name name = (Name) this.property.get(Name.class);
                PropertyViewPanel.this.title.setText("~ " + name.getName() + " ~");

                ClaimType type = (ClaimType) this.property.get(ClaimType.class);

                PropertyViewPanel.this.setupBadges(this.property);

                if (type.canBeParent()) {
                    PropertyViewPanel.this.setupPlots(this.property);
                    PropertyViewPanel.this.showPlotTable();

                    PropertyViewPanel.this.attachChild(PropertyViewPanel.this.tableSwitch);
                } else {
                    PropertyViewPanel.this.showBadgeTable();

                    PropertyViewPanel.this.detachChild(PropertyViewPanel.this.tableSwitch);
                }

                PropertyViewPanel.this.resetInfo();
            } else {
                PropertyViewPanel.this.title.setText("");
                PropertyViewPanel.this.clearInfoView();
            }
        }

        public AnimationTask[] animateOpen() {
            if (this.property == null) {
                return new AnimationTask[0];
            }
            Vector3f start = PropertyViewPanel.this.bottomRoller.getLocalTranslation().clone();
            Vector3f end = new Vector3f(0.0F, 0.0F, 0.0F);

            return new AnimationTask[]{Animation.move(PropertyViewPanel.this.bottomRoller, start, end, 0.25F)};
        }

        public AnimationTask[] animateClose() {
            if (this.property == null) {
                return new AnimationTask[0];
            }
            Vector3f start = PropertyViewPanel.this.bottomRoller.getLocalTranslation().clone();
            Vector3f end = new Vector3f(0.0F, PropertyViewPanel.this.pageHeight - PropertyViewPanel.this.rollerHeight, 0.0F);

            return new AnimationTask[]{Animation.move(PropertyViewPanel.this.bottomRoller, start, end, 0.25F)};
        }
    }
}