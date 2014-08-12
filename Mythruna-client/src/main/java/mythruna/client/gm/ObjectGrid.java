package mythruna.client.gm;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.*;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import mythruna.client.GameClient;
import mythruna.client.ui.*;
import mythruna.client.view.BlockObject;
import mythruna.es.EntitySet;
import mythruna.geom.Trifold;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ObjectGrid extends Node {
    private int rows;
    private int cols;
    private float cellSize;
    private Vector4f margins = new Vector4f(5.0F, 5.0F, 5.0F, 5.0F);
    private float width;
    private float height;
    private Label titleLabel;
    private Quad grid;
    private Geometry gridGeom;
    private ScrollBar scroll;
    private BoundedRangeModel scrollModel;
    private GameClient client;
    private EntitySet inventory;
    private List<BlockObject> list = new ArrayList();
    private BlockObject[][] cells;
    private GuiAppState gui;
    private Node perspectiveRoot;
    private Node selectionRoot;
    private int currentRow = 0;
    private SelectionModel<BlockObject> selection = new SelectionModel();
    private Material cellMaterial;
    private Geometry singleCellGeom;
    private CommandList<ClickAction> commands = new CommandList();

    public ObjectGrid(GuiAppState gui, AssetManager assets, String title, int rows, int cols, float cellSize) {
        super(title);

        this.gui = gui;

        this.rows = rows;
        this.cols = cols;
        this.cellSize = cellSize;

        if (title != null) {
            BitmapFont font = assets.loadFont("Interface/knights24.fnt");
            this.titleLabel = new Label(font);
            this.titleLabel.setText(title);

            this.margins.x = (this.titleLabel.getHeight() + 5.0F);

            this.titleLabel.setColor(new ColorRGBA(1.0F, 0.9333333F, 0.6235294F, 0.75F));

            attachChild(this.titleLabel);
        }

        this.selectionRoot = new Node("Selection");
        attachChild(this.selectionRoot);

        Quad quad = new Quad(cols * cellSize, rows * cellSize);
        quad.scaleTextureCoordinates(new Vector2f(cols, rows));

        this.gridGeom = new Geometry("Grid", quad);

        this.cellMaterial = new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md");
        this.cellMaterial.setColor("Color", new ColorRGBA(1.0F, 1.0F, 1.0F, 0.75F));

        this.cellMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        this.gridGeom.setMaterial(this.cellMaterial);

        Texture tex = assets.loadTexture("Interface/brown-cell-64.png");
        tex.setWrap(Texture.WrapMode.Repeat);
        this.cellMaterial.setTexture("ColorMap", tex);

        this.gridGeom.setLocalTranslation(this.margins.y, this.margins.z, 0.0F);
        this.gridGeom.addControl(new MouseEventControl(new MouseListener[]{new ObjectClickListener()}));

        attachChild(this.gridGeom);

        this.width = (cols * cellSize + this.margins.y + this.margins.w);
        this.height = (rows * cellSize + this.margins.x + this.margins.z);

        this.scroll = new ScrollBar(assets);
        this.scroll.setHeight(rows * cellSize);
        this.scroll.setLocalTranslation(this.width - this.margins.w, this.margins.z, 0.0F);
        this.scrollModel = this.scroll.getModel();
        this.scrollModel.setExtent(rows);
        this.scrollModel.setMaximum(0);

        this.width += this.scroll.getWidth();

        Trifold background = new Trifold(this.width, this.height, 64.0F, 64.0F, 16.0F, 16.0F);
        Geometry geom = new Geometry("bg", background);
        geom.setLocalTranslation(0.0F, 0.0F, -0.5F);

        Material mat = this.cellMaterial.clone();
        mat.setColor("Color", new ColorRGBA(1.0F, 1.0F, 1.0F, 0.25F));

        geom.setMaterial(mat);
        attachChild(geom);

        attachChild(this.scroll);

        if (title != null) {
            this.titleLabel.setHAlignment(HAlignment.CENTER);
            this.titleLabel.setVAlignment(VAlignment.TOP);
            this.titleLabel.setLocalTranslation(this.width * 0.5F, this.height - 2.0F, 1.0F);
        }

        this.cellMaterial = this.cellMaterial.clone();

        tex = assets.loadTexture("Interface/gray-cell-64.png");
        tex.setWrap(Texture.WrapMode.Repeat);
        this.cellMaterial.setTexture("ColorMap", tex);
        this.cellMaterial.setColor("Color", new ColorRGBA(1.0F, 1.0F, 1.0F, 1.0F));

        this.perspectiveRoot = new Node("Inventory Objects");

        this.cells = new BlockObject[cols][rows];
    }

    public SelectionModel<BlockObject> getSelectionModel() {
        return this.selection;
    }

    public void updateLogicalState(float tpf) {
        super.updateLogicalState(tpf);

        if (this.currentRow != this.scrollModel.getValue())
            setCurrentRow(this.scrollModel.getValue());
    }

    protected void setParent(Node parent) {
        super.setParent(parent);
        Node root = this.gui.getPerspectiveRoot();
        if (parent != null) {
            root.attachChild(this.perspectiveRoot);
            refreshView();
        } else {
            root.detachChild(this.perspectiveRoot);
        }
    }

    public void setCurrentRow(int row) {
        if (this.currentRow == row)
            return;
        this.currentRow = row;
        refreshView();
    }

    public int getCurrentRow() {
        return this.currentRow;
    }

    protected int toRow(int index) {
        double f = Math.ceil(index / this.cols);
        return (int) f;
    }

    protected Vector3f cellLocation(int x, int y) {
        float xBase = this.margins.y;
        float yBase = this.margins.z;

        Vector3f gloc = new Vector3f(xBase + x * this.cellSize, yBase + this.rows - y - 1 * this.cellSize, 0.0F);

        return gloc;
    }

    protected Vector3f getLocation(BlockObject o) {
        for (int x = 0; x < this.cols; x++) {
            for (int y = 0; y < this.rows; y++) {
                if (this.cells[x][y] == o) {
                    return cellLocation(x, y);
                }
            }
        }
        return null;
    }

    protected void refreshSelection() {
        this.selectionRoot.detachAllChildren();

        if (this.selection.isEmpty()) {
            return;
        }
        Material mat = this.cellMaterial.clone();

        if (this.singleCellGeom == null) {
            Quad quad = new Quad(this.cellSize, this.cellSize);
            this.singleCellGeom = new Geometry("Cell", quad);
            this.singleCellGeom.setMaterial(mat);
        }

        for (BlockObject o : this.selection) {
            Vector3f cellLocation = getLocation(o);
            if (cellLocation != null) {
                Geometry geom = this.singleCellGeom.clone();
                cellLocation.z += 0.1F;
                geom.setLocalTranslation(cellLocation);
                this.selectionRoot.attachChild(geom);
            }
        }
    }

    public int getRowCount() {
        double f = Math.ceil(this.list.size() / this.cols);
        return (int) f;
    }

    public void setTransformRefresh() {
        super.setTransformRefresh();
        refreshView();
    }

    protected void updateScrollModel() {
        this.scrollModel.setMaximum(getRowCount());

        this.scrollModel.setExtent(this.rows);
    }

    public void addObject(BlockObject o) {
        this.list.add(o);

        updateScrollModel();
    }

    public void removeObject(BlockObject o) {
        this.perspectiveRoot.detachChild(o.getNode());
        this.list.remove(o);
        updateScrollModel();
    }

    public void clear() {
        for (BlockObject o : this.list) {
            this.perspectiveRoot.detachChild(o.getNode());
        }
        this.list.clear();
        updateScrollModel();
    }

    public void refreshView() {
        int x = 0;
        int y = 0;

        float xBase = this.margins.y;
        float yBase = this.margins.z;

        int start = getCurrentRow() * this.cols;
        int end = Math.min(start + this.rows, getRowCount()) * this.cols;

        this.perspectiveRoot.detachAllChildren();

        for (int r = 0; r < this.rows; r++) {
            for (int c = 0; c < this.cols; c++) {
                this.cells[c][r] = null;
            }
        }

        for (int i = start; i < end; i++) {
            if (i >= this.list.size())
                break;
            if (y >= this.rows) {
                break;
            }
            BlockObject bo = (BlockObject) this.list.get(i);
            this.perspectiveRoot.attachChild(bo.getNode());

            Vector3f gloc = new Vector3f(xBase + x * this.cellSize + this.cellSize * 0.5F, yBase + this.rows - y - 1 * this.cellSize + this.cellSize * 0.33F, 0.0F);

            Vector3f local = localToWorld(gloc, null);

            Vector3f v = this.gui.screenToWorld(local.x, local.y, 20.0F);

            bo.getNode().setLocalTranslation(v);

            Quaternion quat0 = new Quaternion().fromAngles(0.0F, 3.926991F, 0.0F);
            Quaternion quat1 = new Quaternion().fromAngles(0.7853982F, 0.0F, 0.0F);
            Vector3f back = bo.getNode().getWorldTranslation().mult(-1.0F).normalizeLocal();
            Quaternion quat2 = new Quaternion();
            quat2.lookAt(back, new Vector3f(0.0F, 1.0F, 0.0F));

            bo.getNode().setLocalRotation(quat2.mult(quat1).mult(quat0));

            this.cells[x][y] = bo;

            x++;
            if (x >= 4) {
                y++;
                x = 0;
            }
        }

        refreshSelection();
    }

    public float getWidth() {
        Vector3f scale = getLocalScale();
        return this.width * scale.x;
    }

    public float getHeight() {
        Vector3f scale = getLocalScale();
        return this.height * scale.y;
    }

    public BlockObject getGridCell(int xScreen, int yScreen) {
        Vector3f local = worldToLocal(new Vector3f(xScreen, yScreen, 0.0F), null);

        int row = (int) (local.y / this.cellSize);
        row = this.rows - row - 1;
        int col = (int) (local.x / this.cellSize);

        if ((col >= this.cols) || (row >= this.rows)) {
            return null;
        }
        return this.cells[col][row];
    }

    public void addCommand(Command<ClickAction> c) {
        this.commands.addCommand(c);
    }

    public void removeCommand(Command<ClickAction> c) {
        this.commands.removeCommand(c);
    }

    private class ObjectClickListener extends ClickListener {
        private ObjectClickListener() {
            super(new Command[0]);
        }

        protected void execute(Object source, ClickAction action) {
            ObjectGrid.this.commands.execute(source, action);
        }

        public void mouseButtonEvent(MouseButtonEvent event, Spatial capture) {
            System.out.println("Mouse button on:" + ObjectGrid.this + "  pressed:" + event.isPressed());
            event.setConsumed();

            if (event.isPressed()) {
                BlockObject hit = ObjectGrid.this.getGridCell(event.getX(), event.getY());

                ObjectGrid.this.selection.setSelected(hit);
                ObjectGrid.this.refreshSelection();
                return;
            }

            super.mouseButtonEvent(event, capture);
        }
    }

    private class EventConsumer implements MouseListener {

        private EventConsumer() {
        }

        public void mouseButtonEvent(MouseButtonEvent event, Spatial capture) {
            event.setConsumed();
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
}