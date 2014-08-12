package mythruna.es.sql;

import mythruna.es.*;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SqlEntityData extends AbstractEntityData {

    private String dbPath;
    private ThreadLocal<SqlSession> cachedSession = new ThreadLocal<>();

    private Map<Class, ComponentHandler> handlers = new ConcurrentHashMap<>();
    private EntityIdGenerator idGenerator;
    private SqlStringIndex stringIndex;

    public SqlEntityData(File dbPath, long writeDelay)
            throws SQLException {
        this(dbPath.toURI().toString(), writeDelay);
    }

    public SqlEntityData(String dbPath, long writeDelay) throws SQLException {
        this.dbPath = dbPath;
        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver not found for: org.hsqldb.jdbc.JDBCDriver", e);
        }

        SqlSession session = getSession();

        execute("SET FILES WRITE DELAY " + writeDelay + " MILLIS");
        execute("SET FILES DEFRAG 50");

        this.idGenerator = EntityIdGenerator.create(session);

        this.stringIndex = new SqlStringIndex(this, 100);
    }

    protected void execute(String statement) throws SQLException {
        SqlSession session = getSession();
        try (Statement st = session.getConnection().createStatement()) {
            st.execute(statement);
        }
    }

    protected SqlSession getSession() throws SQLException {
        SqlSession session = (SqlSession) this.cachedSession.get();
        if (session != null) {
            return session;
        }
        Connection conn = DriverManager.getConnection("jdbc:hsqldb:" + this.dbPath + "/entity_db", "SA", "");

        System.out.println("Created connection.  Autocommit:" + conn.getAutoCommit());

        session = new SqlSession(conn);
        this.cachedSession.set(session);
        return session;
    }

    public StringIndex getStrings() {
        return this.stringIndex;
    }

    public void close() {
        super.close();
        try {
            SqlSession session = getSession();
            execute("SHUTDOWN COMPACT");
            session.getConnection().close();
        } catch (SQLException e) {
            throw new RuntimeException("Database was not shutdown cleanly", e);
        }
    }

    public EntityId createEntity() {
        try {
            return new EntityId(this.idGenerator.nextEntityId(getSession()));
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    public void removeEntity(EntityId entityId) {
        for (Class c : this.handlers.keySet())
            removeComponent(entityId, c);
    }

    protected ComponentHandler getHandler(Class type) {
        ComponentHandler result = (ComponentHandler) this.handlers.get(type);
        if (result == null) {
            synchronized (this) {
                result = (ComponentHandler) this.handlers.get(type);
                if (result == null) {
                    if (PersistentComponent.class.isAssignableFrom(type))
                        result = new SqlComponentHandler(this, type);
                    else
                        result = new MapComponentHandler();
                    this.handlers.put(type, result);
                }
            }
        }
        return result;
    }

    public <T extends EntityComponent> T getComponent(EntityId entityId, Class<T> type) {
        ComponentHandler handler = getHandler(type);
        return (T) handler.getComponent(entityId);
    }

    public void setComponent(EntityId entityId, EntityComponent component) {
        ComponentHandler handler = getHandler(component.getType());

        handler.setComponent(entityId, component);

        entityChange(new EntityChange(entityId, component));
    }

    public boolean removeComponent(EntityId entityId, Class type) {
        ComponentHandler handler = getHandler(type);
        boolean result = handler.removeComponent(entityId);

        entityChange(new EntityChange(entityId, type));

        return result;
    }

    protected EntityId findSingleEntity(ComponentFilter filter) {
        return getHandler(filter.getComponentType()).findEntity(filter);
    }

    protected Set<EntityId> getEntityIds(Class type) {
        return getHandler(type).getEntities();
    }

    protected Set<EntityId> getEntityIds(Class type, ComponentFilter filter) {
        return getHandler(type).getEntities(filter);
    }
}