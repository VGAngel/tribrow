package mythruna.client;

import mythruna.db.ColumnFactory;
import mythruna.db.ColumnWorldDatabase;
import mythruna.db.LeafData;
import mythruna.db.LeafDatabase;

public class LocalWorldDatabase extends ColumnWorldDatabase {
    public LocalWorldDatabase(LeafDatabase leafDb, ColumnFactory colFactory) {
        super(leafDb, colFactory);
    }

    protected void fireLeafChanged(LeafData leaf) {
        markChanged(leaf);

        super.fireLeafChanged(leaf);
    }

    protected void fireLeafCreated(LeafData leaf) {
        markChanged(leaf);

        super.fireLeafCreated(leaf);
    }
}