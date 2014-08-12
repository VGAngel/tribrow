package mythruna.db.building;

public class LevelTemplate {

    private String name;
    private LevelType type;
    private int width;
    private int height;
    private SectionRef[][] array;
    private int levelingType = 0;
    private int elevationOffset = 0;

    public LevelTemplate(String name, LevelType type, int width, int height) {
        this.name = name;
        this.type = type;
        this.array = new SectionRef[width][height];
        this.width = width;
        this.height = height;
    }

    public String getName() {
        return this.name;
    }

    public LevelType getType() {
        return this.type;
    }

    public void set(int x, int y, SectionType type, SectionOrientation orientation) {
        this.array[x][y] = new SectionRef(type, orientation);
    }

    public SectionType getType(int x, int y) {
        SectionRef ref = this.array[x][y];
        if (ref == null)
            return null;
        return ref.type;
    }

    public SectionOrientation getOrientation(int x, int y) {
        SectionRef ref = this.array[x][y];
        if (ref == null)
            return null;
        return ref.orientation;
    }

    protected class SectionRef {
        SectionType type;
        SectionOrientation orientation;

        public SectionRef(SectionType type, SectionOrientation orientation) {
            this.type = type;
            this.orientation = orientation;
        }
    }
}