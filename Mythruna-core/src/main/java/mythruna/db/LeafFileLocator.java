package mythruna.db;

import java.io.File;

public interface LeafFileLocator {

    public abstract File getNodeDirectory(int i, int j, int k, boolean flag);

    public abstract File getLeafFile(int i, int j, int k, boolean flag);
}
