package mythruna.db;

import mythruna.Coordinates;

import java.io.File;

public class DefaultLeafFileLocator implements LeafFileLocator {

    private File baseDir;

    public DefaultLeafFileLocator(File baseDir) {
        this.baseDir = baseDir;
    }

    public File getNodeDirectory(int x, int y, int z, boolean createPaths) {
        int i = Coordinates.worldToNode(x);
        int j = Coordinates.worldToNode(y);
        int k = Coordinates.worldToNode(z);

        File f = new File(this.baseDir, "node-" + i + "x" + j + "x" + k);
        if (!f.exists()) {
            if (createPaths) {
                if (!f.mkdirs()) {
                    throw new RuntimeException("Could not create node directory:" + f);
                }
            } else {
                f = null;
            }
        }
        return f;
    }

    public File getLeafFile(int x, int y, int z, boolean createPaths) {
        int i = Coordinates.worldToLeaf(x);
        int j = Coordinates.worldToLeaf(y);
        int k = Coordinates.worldToLeaf(z);

        File node = getNodeDirectory(x, y, z, createPaths);
        if (node == null) {
            return null;
        }
        return new File(node, i + "x" + j + "x" + k + ".leaf");
    }
}