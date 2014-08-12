package mythruna.client.ui;

import org.progeeks.util.DefaultObservableList;

public class SelectionModel<T> extends DefaultObservableList<T> {
    public SelectionModel() {
    }

    public T getSelected() {
        if (size() != 1)
            return null;
        return get(0);
    }

    public void setSelected(T o) {
        clear();
        if (o != null)
            add(o);
    }
}