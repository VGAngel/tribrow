package mythruna.client.temp;

public class CellMemTest {
    private static Runtime runtime = Runtime.getRuntime();

    public CellMemTest() {
    }

    public static void runGc(int count) {
        System.out.println("Running GC " + count + " times.");
        for (int i = 0; i < count; i++)
            System.gc();
    }

    public static long usedRam() {
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public static void memTest1(int count) {
        runGc(5);
        long before = usedRam();

        CellArray[] list = new CellArray[count];
        for (int i = 0; i < count; i++) {
            list[i] = new Combined();
        }
        runGc(5);
        long after = usedRam();

        System.out.println("Test 1 total memory change:" + (after - before) + " bytes");
    }

    public static void memTest2(int count) {
        runGc(5);
        long before = usedRam();

        CellArray[] list = new CellArray[count];
        for (int i = 0; i < count; i++) {
            list[i] = new Split();
        }
        runGc(5);
        long after = usedRam();

        System.out.println("Test 2 total memory change:" + (after - before) + " bytes");
    }

    public static void memTest3(int count) {
        runGc(5);
        long before = usedRam();

        CellArray[] list = new CellArray[count];
        for (int i = 0; i < count; i++) {
            list[i] = new SingleDimensional();
        }
        runGc(5);
        long after = usedRam();

        System.out.println("Test 3 total memory change:" + (after - before) + " bytes");
    }

    public static void memTest4(int count) {
        runGc(5);
        long before = usedRam();

        CellArray[] list = new CellArray[count];
        for (int i = 0; i < count; i++) {
            list[i] = new SplitSingleDimensional();
        }
        runGc(5);
        long after = usedRam();

        System.out.println("Test 4 total memory change:" + (after - before) + " bytes");
    }

    public static void rawTest1(int count, int[][][] array) {
        long start = System.nanoTime();

        long total = 0L;
        for (int i = 0; i < count; i++) {
            for (int x = 0; x < 32; x++) {
                for (int y = 0; y < 32; y++) {
                    for (int z = 0; z < 32; z++) {
                        total += array[x][y][z];
                    }
                }
            }
        }

        long end = System.nanoTime();
        System.out.println("Raw1 time:" + (end - start / 1000000.0D));
    }

    public static void rawTest2(int count, int[] array) {
        long start = System.nanoTime();

        long total = 0L;
        for (int i = 0; i < count; i++) {
            for (int x = 0; x < 32768; x++) {
                total += array[x];
            }
        }

        long end = System.nanoTime();
        System.out.println("Raw2 time:" + (end - start / 1000000.0D));
    }

    public static void accessTest(int count, CellArray array) {
        long start = System.nanoTime();

        long total = 0L;
        for (int i = 0; i < count; i++) {
            for (int x = 0; x < 32; x++) {
                for (int y = 0; y < 32; y++) {
                    for (int z = 0; z < 32; z++) {
                        total += array.getType(x, y, z);
                    }
                }
            }
        }

        long end = System.nanoTime();
        System.out.println(array.getClass().getSimpleName() + " time:" + (end - start / 1000000.0D));
    }

    public static void main(String[] args) {
        System.out.println("----------- starting access performance tests ---------");
        for (int i = 0; i < 5; i++) {
            runGc(5);
            System.out.println("Pass:" + (i + 1));
            rawTest1(10000, new int[32][32][32]);
            rawTest2(10000, new int[32768]);
            accessTest(10000, new Combined());
            accessTest(10000, new Split());
            accessTest(10000, new SingleDimensional());
            accessTest(10000, new SplitSingleDimensional());
        }
    }

    public static class SplitSingleDimensional
            implements CellMemTest.CellArray {
        private short[] cells;
        private byte[] lights;

        public SplitSingleDimensional() {
            this.cells = new short[32768];
            this.lights = new byte[32768];
        }

        public final int getType(int x, int y, int z) {
            int index = x * 32 * 32 + (y + 32) + z;
            return this.cells[index];
        }

        public final int getLight(int x, int y, int z) {
            int index = x * 32 * 32 + (y + 32) + z;
            return this.lights[index];
        }
    }

    public static class SingleDimensional
            implements CellMemTest.CellArray {
        private int[] cells;

        public SingleDimensional() {
            this.cells = new int[32768];
        }

        public final int getType(int x, int y, int z) {
            int index = x * 32 * 32 + (y + 32) + z;
            return this.cells[index] & 0xFFFF;
        }

        public final int getLight(int x, int y, int z) {
            int index = x * 32 * 32 + (y + 32) + z;
            return this.cells[index] >> 16 & 0xFFFF;
        }
    }

    public static class Split
            implements CellMemTest.CellArray {
        private short[][][] cells;
        private byte[][][] lights;

        public Split() {
            this.cells = new short[32][32][32];
            this.lights = new byte[32][32][32];
        }

        public final int getType(int x, int y, int z) {
            return this.cells[x][y][z];
        }

        public final int getLight(int x, int y, int z) {
            return this.lights[x][y][z];
        }
    }

    public static class Combined
            implements CellMemTest.CellArray {
        private int[][][] cells;

        public Combined() {
            this.cells = new int[32][32][32];
        }

        public final int getType(int x, int y, int z) {
            return this.cells[x][y][z] & 0xFFFF;
        }

        public final int getLight(int x, int y, int z) {
            return this.cells[x][y][z] >> 16 & 0xFFFF;
        }
    }

    public static abstract interface CellArray {
        public abstract int getType(int paramInt1, int paramInt2, int paramInt3);

        public abstract int getLight(int paramInt1, int paramInt2, int paramInt3);
    }
}