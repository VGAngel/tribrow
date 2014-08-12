package mythruna.client.tabs.property;

import com.jme3.app.Application;
import com.jme3.asset.TextureKey;
import com.jme3.font.BitmapFont;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import mythruna.client.GameClient;
import mythruna.client.ui.*;
import mythruna.es.Entity;
import mythruna.script.ActionParameter;
import mythruna.script.ObjectParameter;

public class TableView extends Node {
    private static final ColorRGBA headerColor = new ColorRGBA(0.3568628F, 0.07450981F, 0.07450981F, 1.0F);
    private static final ColorRGBA selectColor = new ColorRGBA(0.1215686F, 0.3921569F, 0.2078431F, 1.0F);
    private static final ColorRGBA cellColor = new ColorRGBA(0.2431373F, 0.2431373F, 0.4156863F, 1.0F);

    private static final ColorRGBA shadowColor = new ColorRGBA(0.0F, 0.0F, 0.0F, 0.0F);
    private Application app;
    private GameClient gameClient;
    private BitmapFont writingFont;
    private long lastVersion = -1L;
    private Table table;
    private int visibleRows;
    private int page = 0;
    private int maxPage = 0;
    private float[] columnSizes;
    private Label[] header;
    private Label[][] cells;
    private Geometry[] rowQuads;
    private Material selectedMaterial;
    private Material unselectedMaterial;
    private String clickAction = "Row Click";

    public TableView(GameClient gameClient, Application app, int visibleRows) {
        this.gameClient = gameClient;
        this.app = app;
        this.visibleRows = visibleRows;

        this.writingFont = app.getAssetManager().loadFont("Interface/templar32.fnt");

        this.rowQuads = new Geometry[visibleRows];

        this.selectedMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        this.selectedMaterial.setColor("Color", new ColorRGBA(0.7843137F, 0.7843137F, 0.003921569F, 0.15F));
        this.selectedMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        this.unselectedMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        this.unselectedMaterial.setColor("Color", new ColorRGBA(0.0F, 0.0F, 0.0F, 0.0F));
        this.unselectedMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
    }

    public void setTable(Table table, float[] columnSizes) {
        if (this.table == table) {
            return;
        }
        this.gameClient.execute("Close Context Menu", null, null);

        detachAllChildren();

        this.table = table;
        this.columnSizes = columnSizes;
        this.page = 0;
        this.maxPage = 0;
        buildViews();
    }

    public Table getTable() {
        return this.table;
    }

    protected void clampPage() {
        if (this.table == null) {
            return;
        }
        this.page = Math.min(this.maxPage, this.page);
        this.page = Math.max(0, this.page);
    }

    public int getPageCount() {
        return this.maxPage;
    }

    public void setPage(int page) {
        if (this.page == page) {
            return;
        }
        this.page = page;
        clampPage();
        this.lastVersion -= 1L;
    }

    public int getPage() {
        return this.page;
    }

    public void updateLogicalState(float tpf) {
        super.updateLogicalState(tpf);

        if ((this.table != null) && (this.table.getChangeVersion() != this.lastVersion)) {
            updateCells();
        }
    }

    protected String cell(int row, int col) {
        if (this.table == null) {
            return "";
        }
        if (row >= this.table.getSize()) {
            return "";
        }
        return this.table.getValue(row, col);
    }

    protected void buildViews() {
        if (this.table == null) {
            return;
        }
        Material horzMat = new Material(this.app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key = new TextureKey("Textures/sand.jpg");
        key.setGenerateMips(false);
        Texture t = this.app.getAssetManager().loadTexture(key);
        t.setWrap(Texture.WrapMode.Repeat);
        horzMat.setTexture("ColorMap", t);
        horzMat.setColor("Color", new ColorRGBA(1.0F, 0.6862745F, 0.4705882F, 0.35F));
        horzMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        Material vertMat = new Material(this.app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        vertMat.setTexture("ColorMap", t);
        vertMat.setColor("Color", new ColorRGBA(0.7843137F, 0.003921569F, 0.7843137F, 0.15F));
        vertMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        float height = this.visibleRows * 24 + 30;
        Quad vertical = new Quad(4.0F, height);
        float colSpacer = 6.0F;
        float tx = 0.015625F;
        float ty = height / 256.0F;
        vertical.setBuffer(VertexBuffer.Type.TexCoord, 2, new float[]{0.0F, ty, tx, ty, tx, 0.0F, 0.0F, 0.0F});

        int cols = this.table.getColumnCount();
        this.header = new Label[cols];
        int y = 0;
        int x = 0;
        for (int i = 0; i < cols; i++) {
            this.header[i] = new Label(this.writingFont);
            this.header[i].setText(this.table.getHeading(i));
            this.header[i].setColor(headerColor);
            this.header[i].setShadowColor(shadowColor);
            this.header[i].setVAlignment(VAlignment.TOP);
            this.header[i].setLocalTranslation(x, y, 0.0F);

            attachChild(this.header[i]);

            x = (int) (x + this.columnSizes[i]);

            if (i < cols - 1) {
                Geometry div = new Geometry("vert divider", vertical);
                div.setMaterial(vertMat);
                div.setLocalTranslation(x, 0.0F - height - 5.0F, 0.0F);
                attachChild(div);
                x = (int) (x + colSpacer);
            }
        }

        float width = x - colSpacer;
        y -= 24;
        float textScale = 0.6666667F;

        x += 18;
        Quad horizontal = new Quad(x, 6.0F);
        tx = x / 256.0F;
        ty = 0.023438F;
        y -= 12;
        horizontal.setBuffer(VertexBuffer.Type.TexCoord, 2, new float[]{0.0F, ty, tx, ty, tx, 0.0F, 0.0F, 0.0F});

        Geometry geom = new Geometry("horz divider", horizontal);
        geom.setMaterial(horzMat);
        geom.setLocalTranslation(0.0F, y, 0.0F);
        attachChild(geom);

        Quad quad = new Quad(width, 20.0F);

        this.cells = new Label[this.visibleRows][cols];
        for (int i = 0; i < this.visibleRows; i++) {
            x = 0;
            for (int j = 0; j < cols; j++) {
                Label l = new Label(this.writingFont);
                l.setText("Test");
                l.setLocalScale(textScale);
                l.setColor(cellColor);
                l.setShadowColor(shadowColor);
                l.setVAlignment(VAlignment.TOP);
                l.setLocalTranslation(x, y, 0.0F);
                this.cells[i][j] = l;
                attachChild(l);

                x = (int) (x + (this.columnSizes[j] + colSpacer));
            }

            y -= 24;

            if (this.rowQuads[i] == null) {
                this.rowQuads[i] = new Geometry("row[" + i + "]", quad);
                this.rowQuads[i].setMaterial(horzMat);
                this.rowQuads[i].addControl(new MouseEventControl(new MouseListener[]{new RowSelector(i, this.rowQuads[i])}));
            }

            if (this.rowQuads[i].getParent() == null) {
                attachChild(this.rowQuads[i]);
            }
            this.rowQuads[i].setLocalTranslation(0.0F, y + 3, 0.5F);
        }

        updateCells();
    }

    protected void updateCells() {
        this.lastVersion = this.table.getChangeVersion();

        this.maxPage = (this.table.getSize() / this.visibleRows);
        if (this.table.getSize() % this.visibleRows == 0)
            this.maxPage -= 1;
        clampPage();

        int cols = this.table.getColumnCount();
        int base = this.page * this.visibleRows;
        int size = this.table.getSize();
        float textScale = 0.6666667F;

        for (int i = 0; i < this.visibleRows; i++) {
            int row = base + i;

            for (int j = 0; j < cols; j++) {
                String s = cell(row, j);
                float w = this.writingFont.getLineWidth(s) * textScale;

                while (w > this.columnSizes[j]) {
                    s = s.substring(0, s.length() - 1);
                    w = this.writingFont.getLineWidth(s) * textScale;
                }

                this.cells[i][j].setText(s);
            }

            if ((row < size) && (this.rowQuads[i].getParent() == null)) {
                attachChild(this.rowQuads[i]);
            } else if ((row >= size) && (this.rowQuads[i].getParent() != null)) {
                detachChild(this.rowQuads[i]);
            }
        }
    }

    private class RowSelector extends MouseAdapter {
        private int row;
        private Spatial quad;

        public RowSelector(int i, Spatial quad) {
            this.row = i;
            this.quad = quad;
            quad.setMaterial(TableView.this.unselectedMaterial);
        }

        public void click(MouseButtonEvent event) {
            int realRow = TableView.this.page * TableView.this.visibleRows + this.row;
            Object clicked = TableView.this.table.getRow(realRow);
            System.out.println("Click[" + realRow + "]:" + clicked);
            Entity e = (Entity) clicked;

            Vector3f p = new Vector3f(event.getX(), event.getY(), 0.0F);
            ActionParameter parm = new ObjectParameter(e.getId(), p);
            TableView.this.gameClient.execute(TableView.this.clickAction, e.getId(), parm);
        }

        public void mouseEntered(MouseMotionEvent event, Spatial capture) {
            this.quad.setMaterial(TableView.this.selectedMaterial);
        }

        public void mouseExited(MouseMotionEvent event, Spatial capture) {
            this.quad.setMaterial(TableView.this.unselectedMaterial);
        }
    }
}