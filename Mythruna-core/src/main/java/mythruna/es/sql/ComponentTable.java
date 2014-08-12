package mythruna.es.sql;

import mythruna.es.*;
import org.progeeks.util.StringUtils;
import org.progeeks.util.log.Log;

import java.sql.*;
import java.util.*;

public class ComponentTable<T> {

    static Log log = Log.getLog();

    private boolean cached = true;
    private Class<T> type;
    private FieldType[] fields;
    private String tableName;
    private String[] dbFieldNames;
    private String insertSql;
    private String updateSql;

    protected ComponentTable(Class<T> type, FieldType[] fields) {
        this.type = type;
        this.fields = fields;
        this.tableName = type.getSimpleName().toUpperCase();

        List names = new ArrayList();
        for (FieldType t : fields) {
            t.addFields("", names);
        }
        this.dbFieldNames = new String[names.size()];
        this.dbFieldNames = ((String[]) names.toArray(this.dbFieldNames));

        this.insertSql = createInsertSql();
        this.updateSql = createUpdateSql();
    }

    public static <T extends EntityComponent> ComponentTable<T> create(SqlSession session, Class<T> type)
            throws SQLException {
        List types = FieldTypes.getFieldTypes(type);
        FieldType[] array = new FieldType[types.size()];
        array = (FieldType[]) types.toArray(array);

        ComponentTable result = new ComponentTable(type, array);
        result.initialize(session);

        return result;
    }

    protected String createUpdateSql() {
        StringBuilder sql = new StringBuilder(new StringBuilder().append("UPDATE ").append(this.tableName).toString());
        sql.append(" SET (");

        sql.append(StringUtils.join(Arrays.asList(this.dbFieldNames), ", "));
        sql.append(")");

        sql.append(" = ");

        sql.append("(");
        for (int i = 0; i < this.dbFieldNames.length; i++)
            sql.append(new StringBuilder().append(i > 0 ? ", " : "").append("?").toString());
        sql.append(")");

        sql.append(" WHERE entityId = ?");
        return sql.toString();
    }

    protected String createInsertSql() {
        StringBuilder sql = new StringBuilder(new StringBuilder().append("INSERT INTO ").append(this.tableName).toString());
        sql.append(" (");

        sql.append(StringUtils.join(Arrays.asList(this.dbFieldNames), ", "));
        sql.append(", entityId");
        sql.append(")");
        sql.append(" VALUES ");
        sql.append("(");
        for (int i = 0; i < this.dbFieldNames.length; i++)
            sql.append(new StringBuilder().append(i > 0 ? ", " : "").append("?").toString());
        sql.append(", ?");
        sql.append(")");

        return sql.toString();
    }

    protected void initialize(SqlSession session)
            throws SQLException {
        DatabaseMetaData md = session.getConnection().getMetaData();
        System.out.println("Checking for table:" + this.tableName);
        ResultSet rs = md.getColumns(null, "PUBLIC", this.tableName, null);
        Map<String, Integer> dbFields = new HashMap();
        try {
            while (rs.next()) {
                dbFields.put(rs.getString("COLUMN_NAME"), Integer.valueOf(rs.getInt("DATA_TYPE")));
            }

            dbFields.remove("ENTITYID");
        } finally {
            rs.close();
        }

        final Map<String, FieldType> defs = new LinkedHashMap();
        for (FieldType t : this.fields) {
            t.addFieldDefinitions("", defs);
        }

        if (!dbFields.isEmpty()) {
            checkStructure(defs, dbFields);
            return;
        }

        StringBuilder sb = new StringBuilder("CREATE");
        if (this.cached) {
            sb.append(" CACHED");
        }
        sb.append(" TABLE");
        sb.append(" " + this.tableName + "\n");
        sb.append("(\n");
        sb.append("  entityId BIGINT PRIMARY KEY");
        for (Map.Entry<String, FieldType> e : defs.entrySet()) {
            sb.append(",\n  " + (String) e.getKey() + " " + ((FieldType) e.getValue()).getDbType());
        }
        sb.append("\n)");

        System.out.println("Create statement:\n" + sb);

        Statement st = session.getConnection().createStatement();
        int i = st.executeUpdate(sb.toString());
        st.close();

        System.out.println("Result:" + i);
    }

    protected void checkStructure(Map<String, FieldType> defs, Map<String, Integer> dbFields) throws SQLException {

        System.out.println(new StringBuilder().append("Table fields:").append(dbFields).toString());
        System.out.println(new StringBuilder().append("Object fields:").append(defs).toString());

        Set newFields = new HashSet();
        Set removedFields = new HashSet();

        for (String s : dbFields.keySet()) {
            if (!defs.containsKey(s)) {
                removedFields.add(s);
            }
        }
        for (String s : defs.keySet()) {
            if (!dbFields.containsKey(s)) {
                newFields.add(s);
            }
        }

        for (Map.Entry e : dbFields.entrySet()) {
            FieldType ft = (FieldType) defs.get(e.getKey());
            if (ft == null) ;
        }

        System.out.println(new StringBuilder().append("New fields:").append(newFields).toString());
        System.out.println(new StringBuilder().append("Removed fields:").append(removedFields).toString());

        if ((newFields.isEmpty()) && (removedFields.isEmpty())) {
            return;
        }

        if ((!newFields.isEmpty()) || (!removedFields.isEmpty())) {
            throw new RuntimeException(new StringBuilder().append("Schema mismatch, table fields:").append(dbFields).append(" object fields:").append(defs.keySet()).toString());
        }
    }

    protected FieldType getFieldType(String field) {
        for (FieldType t : this.fields) {
            if (t.getFieldName().equals(field))
                return t;
        }
        return null;
    }

    public void setComponent(SqlSession session, EntityId entityId, T component)
            throws SQLException {
        PreparedStatement st = session.prepareStatement(this.updateSql);
        int index = 1;
        for (FieldType t : this.fields) {
            index = t.store(component, st, index);
        }
        st.setObject(index++, Long.valueOf(entityId.getId()));

        int result = st.executeUpdate();

        if (result > 0) {
            return;
        }

        st = session.prepareStatement(this.insertSql);

        index = 1;
        for (FieldType t : this.fields) {
            index = t.store(component, st, index);
        }
        st.setObject(index++, Long.valueOf(entityId.getId()));

        result = st.executeUpdate();
    }

    public boolean removeComponent(SqlSession session, EntityId entityId)
            throws SQLException {
        String sql = new StringBuilder().append("DELETE FROM ").append(this.tableName).append(" WHERE entityId=").append(entityId.getId()).toString();

        PreparedStatement st = session.prepareStatement(sql.toString());
        int result = st.executeUpdate();

        return result > 0;
    }

    public T getComponent(SqlSession session, EntityId entityId) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(StringUtils.join(Arrays.asList(this.dbFieldNames), ", "));
        sql.append(new StringBuilder().append(" FROM ").append(this.tableName).toString());
        sql.append(" WHERE entityId=?");

        PreparedStatement st = session.prepareStatement(sql.toString());
        st.setObject(1, Long.valueOf(entityId.getId()));
        ResultSet rs = st.executeQuery();
        try {
            int index;
            if (rs.next()) {
                index = 1;
                Object target = this.type.newInstance();
                for (FieldType t : this.fields) {
                    index = t.load(target, rs, index);
                }

                return (T) target;
            }
            return null;
        } catch (InstantiationException e) {
            throw new RuntimeException("Error in table mapping", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error in table mapping", e);
        } finally {
            rs.close();
        }
    }

    public Set<EntityId> getEntityIds(SqlSession session) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(" entityId");
        sql.append(new StringBuilder().append(" FROM ").append(this.tableName).toString());

        Set results = new HashSet();

        PreparedStatement st = session.prepareStatement(sql.toString());
        ResultSet rs = st.executeQuery();
        try {
            while (rs.next()) {
                Long entityId = Long.valueOf(rs.getLong(1));
                results.add(new EntityId(entityId.longValue()));
            }
        } finally {
            rs.close();
        }

        return results;
    }

    protected int appendFilter(FieldFilter f, StringBuilder where, List<Object> parms) {
        FieldType ft = getFieldType(f.getFieldName());

        if (where.length() > 0) {
            where.append(" AND ");
        }
        Object dbValue = ft.toDbValue(f.getValue());
        if (dbValue == null) {
            where.append(new StringBuilder().append(f.getFieldName()).append(" IS NULL").toString());
        } else {
            where.append(new StringBuilder().append(f.getFieldName()).append(" = ?").toString());
            parms.add(dbValue);
        }
        return 1;
    }

    protected int appendFilter(OrFilter f, StringBuilder where, List<Object> parms) {
        if (where.length() > 0) {
            where.append(" AND ");
        }
        int count = 0;

        StringBuilder sub = new StringBuilder();
        for (ComponentFilter op : f.getOperands()) {
            if (count > 0) {
                where.append(" OR ");
            }
            int nested = appendFilter(op, sub, parms);
            if (nested > 1)
                where.append(new StringBuilder().append("(").append(sub).append(")").toString());
            else {
                where.append(sub);
            }
            sub.setLength(0);
            count += nested;
        }
        return count;
    }

    protected int appendFilter(AndFilter f, StringBuilder where, List<Object> parms) {
        if (where.length() > 0) {
            where.append(" AND ");
        }
        int count = 0;

        StringBuilder sub = new StringBuilder();
        for (ComponentFilter op : f.getOperands()) {
            if (count > 0) {
                where.append(" AND ");
            }
            int nested = appendFilter(op, sub, parms);
            if (nested > 1)
                where.append(new StringBuilder().append("(").append(sub).append(")").toString());
            else {
                where.append(sub);
            }
            sub.setLength(0);
            count += nested;
        }
        return count;
    }

    protected int appendFilter(ComponentFilter f, StringBuilder where, List<Object> parms) {
        if ((f instanceof FieldFilter)) {
            return appendFilter((FieldFilter) f, where, parms);
        }
        if ((f instanceof OrFilter)) {
            return appendFilter((OrFilter) f, where, parms);
        }
        if ((f instanceof AndFilter)) {
            return appendFilter((AndFilter) f, where, parms);
        }

        throw new IllegalArgumentException(new StringBuilder().append("Cannot handle filter:").append(f).toString());
    }

    public Set<EntityId> getEntityIds(SqlSession session, ComponentFilter filter) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(" entityId");
        sql.append(new StringBuilder().append(" FROM ").append(this.tableName).toString());

        List parms = new ArrayList();

        StringBuilder where = new StringBuilder();
        appendFilter(filter, where, parms);

        if (where.length() > 0) {
            sql.append(new StringBuilder().append(" WHERE ").append(where).toString());
        }
        try {
            PreparedStatement st = session.prepareStatement(sql.toString());
            int index = 1;
            for (Iterator i$ = parms.iterator(); i$.hasNext(); ) {
                Object o = i$.next();
                st.setObject(index++, o);
            }
            Set results = new HashSet();

            ResultSet rs = st.executeQuery();
            try {
                while (rs.next()) {
                    Long entityId = Long.valueOf(rs.getLong(1));
                    results.add(new EntityId(entityId.longValue()));
                }
            } finally {
                rs.close();
            }

            return results;
        } catch (SQLException e) {
            throw new RuntimeException(new StringBuilder().append("Error executing sql:").append(sql).toString(), e);
        }
    }

    public EntityId getEntityId(SqlSession session, ComponentFilter filter)
            throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(" entityId");
        sql.append(new StringBuilder().append(" FROM ").append(this.tableName).toString());

        List parms = new ArrayList();

        StringBuilder where = new StringBuilder();
        appendFilter(filter, where, parms);

        if (where.length() > 0) {
            sql.append(new StringBuilder().append(" WHERE ").append(where).toString());
        }
        PreparedStatement st = session.prepareStatement(sql.toString());
        int index = 1;
        for (Iterator i$ = parms.iterator(); i$.hasNext(); ) {
            Object o = i$.next();
            st.setObject(index++, o);
        }
        ResultSet rs = st.executeQuery();
        try {
            if (rs.next()) {
                Long entityId = Long.valueOf(rs.getLong(1));
                return new EntityId(entityId.longValue());
            }
        } finally {
            rs.close();
        }

        return null;
    }

    public Iterator<Map.Entry<EntityId, T>> components(final SqlSession session) throws SQLException {
        final List<Map.Entry<EntityId, T>> results = new ArrayList();
        final StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(StringUtils.join(Arrays.asList(this.dbFieldNames), ", "));
        sql.append(", entityId");
        sql.append(" FROM " + this.tableName);
        final PreparedStatement st = session.prepareStatement(sql.toString());
        final ResultSet rs = st.executeQuery();
        try {
            while (rs.next()) {
                int index = 1;
                final T target = this.type.newInstance();
                for (FieldType t : this.fields) {
                    index = t.load(target, rs, index);
                }
                final Long entityId = rs.getLong(index);
                results.add(new ComponentReference(new EntityId(entityId.longValue()), target));
            }
        } catch (InstantiationException e) {
            throw new RuntimeException("Error in table mapping", e);
        } catch (IllegalAccessException e2) {
            throw new RuntimeException("Error in table mapping", e2);
        } finally {
            rs.close();
        }
        rs.close();
        return results.iterator();
    }

    private class ComponentReference<T> implements Map.Entry<EntityId, T> {
        private EntityId entityId;
        private T component;

        public ComponentReference(final EntityId entityId, final T component) {
            this.entityId = entityId;
            this.component = component;
        }

        public EntityId getKey() {
            return this.entityId;
        }

        public T getValue() {
            return this.component;
        }

        public T setValue(final T value) {
            throw new UnsupportedOperationException("Cannot set the component on a reference.");
        }
    }
}