package mythruna.es;

import com.jme3.math.Vector3f;

public class Test {

    private EntityData ed;
    private EntityId entity1;
    private EntityId entity2;
    private EntitySet entities1;
    private EntitySet entities2;
    private EntitySet entities3;
    private EntitySet entities4;

    public Test() {
    }

    public void setupEntitySet1() {
        this.entities1 = this.ed.getEntities(new Class[]{TestComponent.class});
    }

    public void setupEntitySet2() {
    }

    public void setupEntitySet3() {
        this.entities3 = this.ed.getEntities(new Class[]{TestComponent.class});
    }

    public void setupEntitySet4() {
    }

    public void createSamples1() {
        this.entity1 = this.ed.createEntity();
        this.ed.setComponent(this.entity1, new TestComponent("Test1"));

        this.entity2 = this.ed.createEntity();
        this.ed.setComponent(this.entity2, new TestComponent("Test2"));
    }

    public void testPosition() {
        this.ed.setComponent(this.entity1, new Position(new Vector3f(513.0F, 513.0F, 70.0F)));
    }

    public void testMove1() {
        this.ed.setComponent(this.entity1, new Position(new Vector3f(523.0F, 513.0F, 70.0F)));
    }

    public void testMove2() {
        this.ed.setComponent(this.entity1, new Position(new Vector3f(485.0F, 513.0F, 70.0F)));
    }

    public void dumpEntities12() {
        System.out.println("entities1:" + this.entities1);
        System.out.println("     adds:" + this.entities1.getAddedEntities());
        System.out.println("  changes:" + this.entities1.getChangedEntities());
        System.out.println("  removes:" + this.entities1.getRemovedEntities());
        System.out.println("entities2:" + this.entities2);
        System.out.println("     adds:" + this.entities2.getAddedEntities());
        System.out.println("  changes:" + this.entities2.getChangedEntities());
        System.out.println("  removes:" + this.entities2.getRemovedEntities());
    }

    public void dumpEntities3() {
        System.out.println("entities3:" + this.entities3);
        System.out.println("     adds:" + this.entities3.getAddedEntities());
        System.out.println("  changes:" + this.entities3.getChangedEntities());
        System.out.println("  removes:" + this.entities3.getRemovedEntities());
    }

    public void dumpEntities4() {
        System.out.println("entities4:" + this.entities4);
        System.out.println("     adds:" + this.entities4.getAddedEntities());
        System.out.println("  changes:" + this.entities4.getChangedEntities());
        System.out.println("  removes:" + this.entities4.getRemovedEntities());
    }

    public static void main(String[] args) throws Exception {
        Test test = new Test();

        test.setupEntitySet1();
        test.setupEntitySet2();

        test.createSamples1();

        System.out.println("Before applyChanges:");
        test.dumpEntities12();

        test.entities1.applyChanges();
        test.entities2.applyChanges();

        System.out.println("After applyChanges:");
        test.dumpEntities12();
        test.entities1.clearChangeSets();
        test.entities2.clearChangeSets();

        System.out.println("After adding a position:");
        test.testPosition();
        test.dumpEntities12();

        System.out.println("After applyChanges:");
        test.entities1.applyChanges();
        test.entities2.applyChanges();
        test.dumpEntities12();
        test.entities1.clearChangeSets();
        test.entities2.clearChangeSets();

        System.out.println("Accessing as a new set:");
        test.setupEntitySet3();
        test.setupEntitySet4();
        test.dumpEntities3();
        test.dumpEntities4();

        System.out.println("Moving position:");
        test.testMove1();
        test.dumpEntities12();
        System.out.println("After applyChanges:");
        test.entities1.applyChanges();
        test.entities2.applyChanges();
        test.dumpEntities12();
        test.entities1.clearChangeSets();
        test.entities2.clearChangeSets();

        System.out.println("Moving position out of zone:");
        test.testMove2();
        test.dumpEntities12();
        System.out.println("After applyChanges:");
        test.entities1.applyChanges();
        test.entities2.applyChanges();
        test.dumpEntities12();
        test.entities1.clearChangeSets();
        test.entities2.clearChangeSets();

        System.out.println("Accessing as a new set:");
        test.entities3.release();
        test.entities4.release();
        test.setupEntitySet3();
        test.setupEntitySet4();
        test.dumpEntities3();
        test.dumpEntities4();

        System.out.println("Moving position:");
        test.testMove1();
        test.dumpEntities12();
        System.out.println("After applyChanges:");
        test.entities1.applyChanges();
        test.entities2.applyChanges();
        test.dumpEntities12();
        test.entities1.clearChangeSets();
        test.entities2.clearChangeSets();

        System.out.println("Moving position out of zone twice:");
        test.testMove2();
        test.testMove2();
        test.dumpEntities12();
        System.out.println("After applyChanges:");
        test.entities1.applyChanges();
        test.entities2.applyChanges();
        test.dumpEntities12();
        test.entities1.clearChangeSets();
        test.entities2.clearChangeSets();
    }

    public static class TestComponent implements EntityComponent {
        private String value;

        public TestComponent(String value) {
            this.value = value;
        }

        public Class<TestComponent> getType() {
            return TestComponent.class;
        }

        public String toString() {
            return this.value;
        }
    }
}