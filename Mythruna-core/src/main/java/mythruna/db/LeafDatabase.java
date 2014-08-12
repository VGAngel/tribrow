package mythruna.db;

import java.io.IOException;

public interface LeafDatabase {

    public abstract LeafInfo readInfo(int i, int j, int k) throws IOException;

    public abstract LeafData readData(int i, int j, int k) throws IOException;

    public abstract void writeData(LeafData leafdata) throws IOException;

    public abstract boolean exists(int i, int j, int k) throws IOException;
}
