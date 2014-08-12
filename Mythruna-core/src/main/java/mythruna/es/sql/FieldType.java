package mythruna.es.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public abstract interface FieldType {

    public abstract String getFieldName();

    public abstract Class getType();

    public abstract String getDbType();

    public abstract void addFieldDefinitions(String paramString, Map<String, FieldType> paramMap);

    public abstract void addFields(String paramString, List<String> paramList);

    public abstract Object toDbValue(Object paramObject);

    public abstract int store(Object paramObject, PreparedStatement paramPreparedStatement, int paramInt)
            throws SQLException;

    public abstract int load(Object paramObject, ResultSet paramResultSet, int paramInt)
            throws SQLException;
}