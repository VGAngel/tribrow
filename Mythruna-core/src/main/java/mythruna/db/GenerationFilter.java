package mythruna.db;

public interface GenerationFilter {

    public abstract void setSeed(long l);

    public abstract void setGenerator(GeneratorColumnFactory generatorcolumnfactory);

    public abstract void filter(int i, int j, int ai[][][], int ai1[][], int ai2[][], long l, int k, int i1, int j1);
}
