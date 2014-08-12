package mythruna;

public enum Month {
    SPRING("Gwyrdelia", "Darduu"),
    SUMMER("Boetheula", "Haf"),
    FALL("Frowndelia", "Adfel"),
    WINTER("Bladeira", "Aefa");

    private String name;
    private String alternate;

    private Month(String name, String alternate) {
        this.name = name;
        this.alternate = alternate;
    }

    public String toString() {
        return this.name;
    }
}