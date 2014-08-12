package mythruna.es.sql;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class EntityIdGenerator {

    private String tableName = "ENTITY_ID";
    private long entityId;

    protected EntityIdGenerator(SqlSession session)
            throws SQLException {
        DatabaseMetaData md = session.getConnection().getMetaData();
        ResultSet rs = md.getColumns(null, "PUBLIC", this.tableName, null);
        try {
            if (rs.next()) {
                loadId(session);
                return;
            }
        } finally {
            rs.close();
        }

        StringBuilder sb = new StringBuilder("CREATE");

        sb.append(" CACHED TABLE");
        sb.append(new StringBuilder().append(" ").append(this.tableName).append("\n").toString());
        sb.append("(\n");
        sb.append("  id TINYINT,\n");
        sb.append("  entityId BIGINT");
        sb.append("\n)");

        Statement st = session.getConnection().createStatement();
        st.executeUpdate(sb.toString());

        String sql = new StringBuilder().append("INSERT INTO ").append(this.tableName).append("(id,entityId) VALUES (0,0)").toString();
        int i = st.executeUpdate(sql);
        if (i != 1)
            throw new SQLException(new StringBuilder().append("Error initializing sequence table:").append(sb).toString());
        st.close();
    }

    public static EntityIdGenerator create(SqlSession session) throws SQLException {
        return new EntityIdGenerator(session);
    }

    protected void loadId(SqlSession session) throws SQLException {
        Statement st = session.getConnection().createStatement();
        try {
            ResultSet rs = st.executeQuery(new StringBuilder().append("SELECT entityId from ").append(this.tableName).append(" where id=0").toString());
            if (rs.next()) {
                this.entityId = rs.getLong(1);
            }
        } finally {
            st.close();
        }
    }

    public synchronized long nextEntityId(SqlSession session) throws SQLException {
        long result = this.entityId++;
        Statement st = session.getConnection().createStatement();
        try {
            String sql = new StringBuilder().append("UPDATE ").append(this.tableName).append(" SET entityId=").append(this.entityId).append(" WHERE id=0").toString();
            int update = st.executeUpdate(sql);
            if (update != 1)
                throw new SQLException("EntityID sequence not updated.");
            return result;
        } finally {
            st.close();
        }
    }
}