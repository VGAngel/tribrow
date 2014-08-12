package mythruna.es.sql;

import mythruna.es.EntityId;
import mythruna.es.StringType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class FieldTypes {

    private static Map<String, String> dbTypes = new HashMap();

    public FieldTypes() {
    }

    public static List<FieldType> getFieldTypes(Class type) {
        return getFieldTypes(null, type);
    }

    protected static List<FieldType> getFieldTypes(String prefix, Class type) {
        List results = new ArrayList();
        Field[] fields = type.getDeclaredFields();

        for (Field f : fields) {
            if (!Modifier.isStatic(f.getModifiers())) {
                if (!Modifier.isTransient(f.getModifiers())) {
                    f.setAccessible(true);

                    Class ft = f.getType();
                    if (ft.isPrimitive()) {
                        results.add(new PrimitiveField(prefix, f));
                    } else if (EntityId.class.isAssignableFrom(ft)) {
                        results.add(new EntityIdField(prefix, f));
                    } else if (String.class.equals(ft)) {
                        results.add(new StringField(prefix, f));
                    } else {
                        results.add(new ObjectField(prefix, f));
                    }
                }
            }
        }
        return results;
    }

    static {
        dbTypes.put("int", "INTEGER");
        dbTypes.put("long", "BIGINT");
        dbTypes.put("short", "SMALLINT");
        dbTypes.put("byte", "TINYINT");
        dbTypes.put("float", "FLOAT");
        dbTypes.put("double", "DOUBLE");
    }

    protected static class PrimitiveField
            implements FieldType {
        private String name;
        private String dbFieldName;
        private Field field;

        public PrimitiveField(Field field) {
            this(null, field);
        }

        public PrimitiveField(String prefix, Field field) {
            this.field = field;
            this.name = field.getName();
            if (prefix == null)
                this.dbFieldName = this.name;
            else
                this.dbFieldName = (prefix + this.name);
        }

        public String getFieldName() {
            return this.name;
        }

        public Class getType() {
            return this.field.getType();
        }

        public String getDbType() {
            String s = this.field.getType().getSimpleName();
            String result = (String) FieldTypes.dbTypes.get(s);
            if (result != null)
                return result;
            return s;
        }

        public void addFieldDefinitions(String prefix, Map<String, FieldType> defs) {
            defs.put(prefix + this.dbFieldName.toUpperCase(), this);
        }

        public void addFields(String prefix, List<String> fields) {
            fields.add(prefix + this.dbFieldName);
        }

        public Object toDbValue(Object o) {
            return o;
        }

        public int store(Object object, PreparedStatement ps, int index) throws SQLException {
            try {
                ps.setObject(index++, this.field.get(object));
                return index;
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error in field mapping", e);
            }
        }

        protected Object cast(Number n, Class c) {
            if (c == Float.TYPE)
                return Float.valueOf(n.floatValue());
            if (c == Byte.TYPE)
                return Byte.valueOf(n.byteValue());
            if (c == Short.TYPE)
                return Short.valueOf(n.shortValue());
            if (c == Integer.TYPE)
                return Integer.valueOf(n.intValue());
            return n;
        }

        public int load(Object target, ResultSet rs, int index) throws SQLException {
            try {
                Object value = rs.getObject(index++);

                if ((value instanceof Number)) {
                    value = cast((Number) value, getType());
                }
                this.field.set(target, value);
                return index;
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error in field mapping", e);
            }
        }

        public String toString() {
            if (this.dbFieldName != this.name)
                return this.name + "/" + this.dbFieldName + ":" + getType();
            return getFieldName() + ":" + getType();
        }
    }

    protected static class ObjectField
            implements FieldType {
        private String name;
        private Field field;
        private FieldType[] fields;

        public ObjectField(String prefix, Field field) {
            this.field = field;
            this.name = field.getName();
            List list = FieldTypes.getFieldTypes(prefix, field.getType());
            this.fields = new FieldType[list.size()];
            this.fields = ((FieldType[]) list.toArray(this.fields));
        }

        public String getFieldName() {
            return this.name;
        }

        public Class getType() {
            return this.field.getType();
        }

        public String getDbType() {
            return "Undefined";
        }

        public void addFieldDefinitions(String prefix, Map<String, FieldType> defs) {
            prefix = prefix + this.name + "_";

            for (FieldType t : this.fields)
                t.addFieldDefinitions(prefix.toUpperCase(), defs);
        }

        public void addFields(String prefix, List<String> fields) {
            prefix = prefix + this.name + "_";

            for (FieldType t : this.fields)
                t.addFields(prefix, fields);
        }

        public Object toDbValue(Object o) {
            return o;
        }

        public int store(Object object, PreparedStatement ps, int index) throws SQLException {
            try {
                Object subValue = this.field.get(object);

                for (FieldType t : this.fields) {
                    index = t.store(subValue, ps, index);
                }
                return index;
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error in field mapping", e);
            }
        }

        public int load(Object target, ResultSet rs, int index) throws SQLException {
            try {
                Object subValue = this.field.getType().newInstance();

                for (FieldType t : this.fields) {
                    index = t.load(subValue, rs, index);
                }

                this.field.set(target, subValue);
                return index;
            } catch (InstantiationException e) {
                throw new RuntimeException("Error in field mapping", e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error in field mapping", e);
            }
        }

        public String toString() {
            return getFieldName() + ":" + getType() + "{" + Arrays.asList(this.fields) + "}";
        }
    }

    protected static class StringField
            implements FieldType {
        private String name;
        private String dbFieldName;
        private Field field;
        private int maxLength;

        public StringField(String prefix, Field field) {
            this.field = field;
            this.name = field.getName();
            if (prefix == null)
                this.dbFieldName = this.name;
            else {
                this.dbFieldName = (prefix + this.name);
            }

            StringType meta = (StringType) field.getAnnotation(StringType.class);
            if (meta != null)
                this.maxLength = meta.maxLength();
            else
                this.maxLength = 512;
        }

        public String getFieldName() {
            return this.name;
        }

        public Class getType() {
            return this.field.getType();
        }

        public String getDbType() {
            return "VARCHAR(" + this.maxLength + ")";
        }

        public void addFieldDefinitions(String prefix, Map<String, FieldType> defs) {
            defs.put(prefix + this.dbFieldName.toUpperCase(), this);
        }

        public void addFields(String prefix, List<String> fields) {
            fields.add(prefix + this.dbFieldName);
        }

        public Object toDbValue(Object o) {
            return o;
        }

        public int store(Object object, PreparedStatement ps, int index) throws SQLException {
            try {
                ps.setObject(index++, this.field.get(object));
                return index;
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error in field mapping", e);
            }
        }

        public int load(Object target, ResultSet rs, int index) throws SQLException {
            try {
                this.field.set(target, rs.getObject(index++));
                return index;
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error in field mapping", e);
            }
        }

        public String toString() {
            if (this.dbFieldName != this.name)
                return this.name + "/" + this.dbFieldName + ":" + getType();
            return getFieldName() + ":" + getType();
        }
    }

    protected static class EntityIdField
            implements FieldType {
        private String name;
        private String dbFieldName;
        private Field field;

        public EntityIdField(Field field) {
            this(null, field);
        }

        public EntityIdField(String prefix, Field field) {
            this.field = field;
            this.name = field.getName();
            if (prefix == null)
                this.dbFieldName = this.name;
            else
                this.dbFieldName = (prefix + this.name);
        }

        public String getFieldName() {
            return this.name;
        }

        public Class getType() {
            return this.field.getType();
        }

        public String getDbType() {
            String result = (String) FieldTypes.dbTypes.get("long");
            return result;
        }

        public void addFieldDefinitions(String prefix, Map<String, FieldType> defs) {
            defs.put(prefix + this.dbFieldName.toUpperCase(), this);
        }

        public void addFields(String prefix, List<String> fields) {
            fields.add(prefix + this.dbFieldName);
        }

        public Object toDbValue(Object o) {
            if (o == null)
                return null;
            return Long.valueOf(((EntityId) o).getId());
        }

        public int store(Object object, PreparedStatement ps, int index) throws SQLException {
            try {
                EntityId entityId = (EntityId) this.field.get(object);
                if (entityId != null)
                    ps.setObject(index++, Long.valueOf(entityId.getId()));
                else
                    ps.setObject(index++, null);
                return index;
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error in field mapping", e);
            }
        }

        public int load(Object target, ResultSet rs, int index) throws SQLException {
            try {
                Number value = (Number) rs.getObject(index++);

                if (value != null)
                    this.field.set(target, new EntityId(value.longValue()));
                else
                    this.field.set(target, null);
                return index;
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error in field mapping", e);
            }
        }

        public String toString() {
            if (this.dbFieldName != this.name)
                return this.name + "/" + this.dbFieldName + ":" + getType();
            return getFieldName() + ":" + getType();
        }
    }
}