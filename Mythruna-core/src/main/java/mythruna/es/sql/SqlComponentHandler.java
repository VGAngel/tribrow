package mythruna.es.sql;

import mythruna.es.ComponentFilter;
import mythruna.es.EntityComponent;
import mythruna.es.EntityId;

import java.sql.SQLException;
import java.util.Set;

public class SqlComponentHandler<T extends EntityComponent> implements ComponentHandler<T> {

    private SqlEntityData parent;
    private Class<T> type;
    private ComponentTable<T> table;

    public SqlComponentHandler(SqlEntityData parent, Class<T> type) {
        this.parent = parent;
        this.type = type;
        try {
            this.table = ComponentTable.create(parent.getSession(), type);
        } catch (SQLException e) {
            throw new RuntimeException("Error creating table for component type:" + type, e);
        }
    }

    protected SqlSession getSession() throws SQLException {
        return this.parent.getSession();
    }

    public void setComponent(EntityId entityId, T component) {
        try {
            this.table.setComponent(getSession(), entityId, component);
        } catch (SQLException e) {
            throw new RuntimeException("Error setting component:" + component + " on entity:" + entityId, e);
        }
    }

    public boolean removeComponent(EntityId entityId) {
        try {
            return this.table.removeComponent(getSession(), entityId);
        } catch (SQLException e) {
        }
        throw new RuntimeException("Error removing component type:" + this.type + " from entity:" + entityId);
    }

    public T getComponent(EntityId entityId) {
        try {
            return (T) this.table.getComponent(getSession(), entityId);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving component type:" + this.type + " for entity:" + entityId, e);
        }
    }

    public Set<EntityId> getEntities() {
        try {
            return this.table.getEntityIds(getSession());
        } catch (SQLException e) {
        }
        throw new RuntimeException("Error retrieving component entities for type:" + this.type);
    }

    public Set<EntityId> getEntities(ComponentFilter filter) {
        if (filter == null)
            return getEntities();
        try {
            return this.table.getEntityIds(getSession(), filter);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving component entities for type:" + this.type, e);
        }
    }

    public EntityId findEntity(ComponentFilter filter) {
        if (filter == null)
            return null;
        try {
            return this.table.getEntityId(getSession(), filter);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving entity for filter:" + filter, e);
        }
    }
}