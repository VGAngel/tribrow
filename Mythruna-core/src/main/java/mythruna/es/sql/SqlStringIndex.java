package mythruna.es.sql;

import mythruna.es.StringIndex;
import mythruna.util.LruCache;

import java.sql.SQLException;

public class SqlStringIndex implements StringIndex {

    private SqlEntityData parent;
    private StringTable stringTable;
    private int cacheSize;
    private LruCache<Integer, String> idToString;
    private LruCache<String, Integer> stringToId;

    public SqlStringIndex(SqlEntityData parent, int cacheSize) {
        this.parent = parent;
        this.cacheSize = cacheSize;
        this.idToString = new LruCache("idToString", 100);
        this.stringToId = new LruCache("stringToId", 100);
        try {
            this.stringTable = StringTable.create(parent.getSession());

            int test = getStringId("testing", true);
            System.out.println("Test string id:" + test);

            System.out.println("Reciprocal test lookup:" + getString(test));
        } catch (SQLException e) {
            throw new RuntimeException("Error creating string table", e);
        }
    }

    protected SqlSession getSession() throws SQLException {
        return this.parent.getSession();
    }

    protected int lookupId(String s) {
        try {
            return this.stringTable.getStringId(getSession(), s, false);
        } catch (SQLException e) {
            throw new RuntimeException("Error getting string ID for:" + s, e);
        }
    }

    public int getStringId(String s, boolean add) {
        Integer result = (Integer) this.stringToId.get(s);
        if (result != null) {
            return result.intValue();
        }

        int i = lookupId(s);
        if ((i < 0) && (add)) {
            synchronized (this) {
                result = (Integer) this.stringToId.get(s);
                if (result != null) {
                    return result.intValue();
                }
                try {
                    i = this.stringTable.getStringId(getSession(), s, add);
                    if (i < 0) {
                        return -1;
                    }

                    this.stringToId.put(s, Integer.valueOf(i));
                    this.idToString.put(Integer.valueOf(i), s);
                    return i;
                } catch (SQLException e) {
                    throw new RuntimeException("Error getting string ID for:" + s, e);
                }
            }

        }

        if (i < 0) {
            return -1;
        }
        this.stringToId.put(s, Integer.valueOf(i));
        this.idToString.put(Integer.valueOf(i), s);

        return i;
    }

    public String getString(int id) {
        String result = (String) this.idToString.get(Integer.valueOf(id));
        if (result != null) {
            return result;
        }
        try {
            result = this.stringTable.getString(getSession(), id);
            if (result != null) {
                this.idToString.put(Integer.valueOf(id), result);
                this.stringToId.put(result, Integer.valueOf(id));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Error getting string for ID:" + id, e);
        }
    }
}