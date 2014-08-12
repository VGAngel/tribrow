package mythruna.script;

import mythruna.es.EntityData;
import mythruna.es.EntityId;
import mythruna.es.FieldFilter;
import org.progeeks.util.log.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SymbolGroups {

    static Log log = Log.getLog();
    private EntityData ed;
    private boolean readOnly;
    private Map<String, List<String>> tables = new ConcurrentHashMap();

    public SymbolGroups(EntityData ed, boolean readOnly) {
        this.ed = ed;
        this.readOnly = readOnly;
    }

    protected void storeGroup(String group, List<String> list) {
        System.out.println("StringTable.storeGroup(" + group + ", " + list + ")");

        String[] array = (String[]) list.toArray(new String[list.size()]);
        SymbolGroup g = new SymbolGroup(group, array);

        EntityId id = this.ed.createEntity();
        this.ed.setComponent(id, g);
    }

    protected List<String> loadTable(String group) {
        List result = (List) this.tables.get(group);
        if ((result == null) && (this.readOnly)) {
            FieldFilter filter = new FieldFilter(SymbolGroup.class, "name", group);
            EntityId id = this.ed.findEntity(filter, new Class[0]);
            SymbolGroup g = (SymbolGroup) this.ed.getComponent(id, SymbolGroup.class);

            result = new ArrayList(Arrays.asList(g.getStrings()));
            this.tables.put(group, result);
        }

        return result;
    }

    protected List<String> getTable(String group, boolean create) {
        List result = loadTable(group);

        if ((result == null) && (create)) {
            result = new ArrayList();
            this.tables.put(group, result);
        } else if (result == null) {
            log.warn("String table miss for group:" + group);
        }

        return result;
    }

    public void compile() {
        if (this.readOnly) {
            throw new RuntimeException("String table is read-only and cannot be compiled.");
        }
        for (Map.Entry e : this.tables.entrySet()) {
            storeGroup((String) e.getKey(), (List) e.getValue());
        }
    }

    public int addString(String symbolGroup, String value) {
        List table = getTable(symbolGroup, true);
        int index = table.size();
        table.add(value);
        System.out.println("Added string:" + symbolGroup + " -> " + value + " = " + index);
        return index;
    }

    public int getId(String symbolGroup, String value) {
        List table = getTable(symbolGroup, false);
        return table.indexOf(value);
    }

    public String getString(String symbolGroup, int id) {
        List table = getTable(symbolGroup, false);
        if ((id < 0) || (id >= table.size()))
            throw new IndexOutOfBoundsException("Group:" + symbolGroup + "  ID:" + id);
        return (String) table.get(id);
    }
}