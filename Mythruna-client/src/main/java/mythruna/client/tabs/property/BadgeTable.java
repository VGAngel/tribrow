package mythruna.client.tabs.property;

import mythruna.es.*;

public class BadgeTable implements Table<Entity> {

    private String[] columnNames = {"Badge Holder"};
    private EntityId player;
    private EntityData ed;
    private EntitySet badges;
    private Entity[] rows;
    private long version;

    public BadgeTable(EntityId player, EntityData ed, EntitySet badges) {
        this.ed = ed;
        this.player = player;
        this.badges = badges;
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
        if (!this.badges.getAddedEntities().isEmpty())
            return true;
        if (!this.badges.getRemovedEntities().isEmpty())
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
        } else if (!this.badges.getChangedEntities().isEmpty()) {
            this.version += 1L;
        }
    }

    protected void updateRows() {
        this.rows = new Entity[this.badges.size()];
        this.rows = ((Entity[]) this.badges.toArray(this.rows));
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
                InContainer in = (InContainer) e.get(InContainer.class);
                if (in.getParentId().equals(this.player))
                    return "You";
                return safeName((Name) this.ed.getComponent(in.getParentId(), Name.class));
        }
        return "";
    }
}