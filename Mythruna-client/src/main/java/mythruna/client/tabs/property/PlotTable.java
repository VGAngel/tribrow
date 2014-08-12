package mythruna.client.tabs.property;

import mythruna.es.*;

public class PlotTable implements Table<Entity> {

    private String[] columnNames = {"Plot Name", "Owner", "Lock"};
    private EntityId player;
    private EntityData ed;
    private EntitySet plots;
    private Entity[] rows;
    private long version;

    public PlotTable(EntityId player, EntityData ed, EntitySet plots) {
        this.ed = ed;
        this.player = player;
        this.plots = plots;
        update();
    }

    public Entity getRow(int row) {
        if (this.rows == null)
            return null;
        return this.rows[row];
    }

    protected boolean needsUpdate() {
        if (this.rows == null)
            return true;
        if (!this.plots.getAddedEntities().isEmpty())
            return true;
        if (!this.plots.getRemovedEntities().isEmpty())
            return true;
        return false;
    }

    public long getChangeVersion() {
        return this.version;
    }

    public void update() {
        if (needsUpdate()) {
            updateRows();
            this.version += 1L;
        } else if (!this.plots.getChangedEntities().isEmpty()) {
            this.version += 1L;
        }
    }

    protected void updateRows() {
        this.rows = new Entity[this.plots.size()];
        this.rows = ((Entity[]) this.plots.toArray(this.rows));
    }

    public int getColumnCount() {
        return this.columnNames.length;
    }

    public int getSize() {
        return this.rows.length;
    }

    public String getHeading(int col) {
        return this.columnNames[col];
    }

    protected String safeName(Name name) {
        return name == null ? "Unknown" : name.getName();
    }

    public String getValue(int row, int col) {
        if (this.rows == null)
            updateRows();
        Entity e = this.rows[row];

        switch (col) {
            case 0:
                return safeName((Name) e.get(Name.class));
            case 1:
                OwnedBy owner = (OwnedBy) e.get(OwnedBy.class);
                if (owner.getOwnerId().equals(this.player))
                    return "You";
                return safeName((Name) this.ed.getComponent(owner.getOwnerId(), Name.class));
            case 2:
        }
        return "";
    }
}