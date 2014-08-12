package mythruna.item;

import mythruna.es.*;

public class LocalVariables {

    private final EntityData ed;
    private EntityId holder;

    public LocalVariables(EntityData ed, EntityId holder) {
        this.ed = ed;
        this.holder = holder;
    }

    protected <T extends EntityComponent> ComponentFilter<T> filter(Class<T> type, int nameId) {
        ComponentFilter filter1 = FieldFilter.create(type, "holder", this.holder);
        ComponentFilter filter2 = FieldFilter.create(type, "nameId", Integer.valueOf(nameId));
        ComponentFilter filter = AndFilter.create(type, new ComponentFilter[]{filter1, filter2});

        return filter;
    }

    protected void setVariable(Variable newValue) {
        ComponentFilter filter = filter(newValue.getClass(), newValue.getNameId());
        EntityId varId = this.ed.findEntity(filter, new Class[0]);
        if (varId == null) {
            synchronized (this.ed) {
                varId = this.ed.findEntity(filter, new Class[0]);
                if (varId == null) {
                    varId = this.ed.createEntity();
                    this.ed.setComponent(varId, newValue);
                    return;
                }
            }
        }

        this.ed.setComponent(varId, newValue);
    }

    public void setInt(String name, int value) {
        int id = this.ed.getStrings().getStringId(name, true);
        if (id < 0)
            throw new RuntimeException("Error creating ID for:" + name);
        IntVariable newValue = new IntVariable(this.holder, id, value);
        setVariable(newValue);
    }

    public int getInt(String name) {
        int id = this.ed.getStrings().getStringId(name, false);
        if (id < 0) {
            return 0;
        }
        ComponentFilter filter = filter(IntVariable.class, id);
        EntityId varId = this.ed.findEntity(filter, new Class[]{IntVariable.class});
        if (varId == null)
            return 0;
        IntVariable val = (IntVariable) this.ed.getComponent(varId, IntVariable.class);
        if (val == null)
            return 0;
        return val.getValue();
    }
}