package mythruna;

public class MaterialType {

    public static MaterialType[] types = new MaterialType[17];

    public static final MaterialType EMPTY = new MaterialType("Nothing", 0, 0.0F, 0.0F, 0.0F);
    public static final MaterialType DIRT = new MaterialType("Dirt", 1, 1500.0F, 0.25F, 0.2F);
    public static final MaterialType GRASS = new MaterialType("Grass", 2, 1400.0F, 0.15F, 0.1F);
    public static final MaterialType SAND = new MaterialType("Sand", 3, 1600.0F, 0.05F, 0.1F);
    public static final MaterialType STONE = new MaterialType("Stone", 4, 2500.0F, 0.75F, 0.3F);
    public static final MaterialType COBBLE = new MaterialType("Cobble", 5, 2400.0F, 0.75F, 0.25F);
    public static final MaterialType ROCK = new MaterialType("Rock", 6, 2400.0F, 0.75F, 0.5F);
    public static final MaterialType WATER = new MaterialType("Water", 7, 1000.0F, 0.0F, 0.0F);
    public static final MaterialType WOOD = new MaterialType("Wood", 8, 500.0F, 0.5F, 0.25F);
    public static final MaterialType WADDLE = new MaterialType("W&D", 9, 300.0F, 0.3F, 0.35F);
    public static final MaterialType GLASS = new MaterialType("Glass", 10, 2500.0F, 0.75F, 0.05F);
    public static final MaterialType MARBLE = new MaterialType("Marble", 11, 2500.0F, 0.75F, 0.1F);
    public static final MaterialType LEAVES = new MaterialType("Leaves", 12, 250.0F, 0.05F, 0.1F);
    public static final MaterialType FLORA = new MaterialType("Flora", 13, 5.0F, 0.0F, 0.1F);
    public static final MaterialType SHINGLES = new MaterialType("Shingles", 14, 800.0F, 0.5F, 0.25F);
    public static final MaterialType FIRE = new MaterialType("Fire", 15, 0.0F, 0.0F, 0.0F);
    public static final MaterialType MORTARED_ROCK = new MaterialType("Mrtr Rock", 16, 2400.0F, 0.75F, 0.25F);
    private String name;
    private float mass;
    private float hardness;
    private float roughness;
    private int index;

    public MaterialType(String name, int index, float mass, float hardness, float roughness) {
        this.name = name;
        this.index = index;
        this.mass = mass;
        this.hardness = hardness;
        this.roughness = roughness;
        types[index] = this;
    }

    public static MaterialType type(int id) {
        if ((id < 0) || (id >= types.length))
            return null;
        return types[id];
    }

    public int getId() {
        return this.index;
    }

    public float getMass() {
        return this.mass;
    }

    public float getHardness() {
        return this.hardness;
    }

    public float getRoughness() {
        return this.roughness;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return "MaterialType[" + this.name + "]";
    }
}