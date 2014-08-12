package mythruna.db.building;

import mythruna.Vector3i;

public class SectionType {

    public static final SectionType CORNER = new SectionType("Corner", new Vector3i(8, 8, 3));

    public static final SectionType CORNER_END = new SectionType("CornerEnd", new Vector3i(8, 8, 3));

    public static final SectionType SIDE = new SectionType("Side", new Vector3i(4, 8, 3));

    public static final SectionType CENTER = new SectionType("Center", new Vector3i(4, 4, 3));
    private String name;
    private Vector3i size;

    public SectionType(String name, Vector3i size) {
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return this.name;
    }

    public Vector3i getSize() {
        return this.size;
    }

    public String toString() {
        return "SectionType[" + this.name + ", " + this.size + "]";
    }
}