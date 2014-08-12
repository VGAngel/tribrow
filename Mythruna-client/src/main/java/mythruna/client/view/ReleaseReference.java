package mythruna.client.view;

public class ReleaseReference implements BuilderReference {

    private LeafReference ref;

    public ReleaseReference(LeafReference ref) {
        this.ref = ref;
    }

    public void build() {
        this.ref.release();
    }

    public void applyUpdates(LocalArea parent) {
    }
}