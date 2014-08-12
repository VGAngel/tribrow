package mythruna.db;

public class LeafChangeEvent {

    private ChangeType type;
    private LeafData leaf;

    public LeafChangeEvent(ChangeType type, LeafData leaf) {
        this.type = type;
        this.leaf = leaf;
    }

    public LeafChangeEvent(LeafData leaf) {
        this(ChangeType.MODIFIED, leaf);
    }

    public ChangeType getType() {
        return this.type;
    }

    public LeafData getLeaf() {
        return this.leaf;
    }

    public static enum ChangeType {
        CREATED, MODIFIED, RESET
    }
}