package mythruna.db.building;

public enum SectionOrientation {

    SouthEast(0), South(0),
    West(1), SouthWest(1),
    North(2), NorthWest(2),
    East(3), NorthEast(3);

    private int rotation;
    private int mirrorAxes;

    private SectionOrientation(int rotation) {
        this.rotation = rotation;
        this.mirrorAxes = this.mirrorAxes;
    }

    public int getBaseRotation() {
        return this.rotation;
    }
}