package mythruna.item;

public enum Scale {
    Block(1.0F), Placeable(0.25F), Item(0.05F);

    private float scale;

    private Scale(float scale) {
        this.scale = scale;
    }

    public float getScale() {
        return this.scale;
    }
}