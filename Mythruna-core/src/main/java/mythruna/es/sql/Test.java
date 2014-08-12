package mythruna.es.sql;

public class Test {

    public Test() {
    }

    public static void main(String[] args)
            throws Exception {
        SqlEntityData ed = new SqlEntityData("mythruna.db/entities", 10L);

        ed.close();
    }
}