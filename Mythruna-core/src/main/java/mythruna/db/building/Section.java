package mythruna.db.building;

import mythruna.db.BlueprintData;

import java.util.Set;

public class Section {

    private SectionType type;
    private Set<String> features;
    private Set<String> style;
    private BlueprintData data;

    public Section(SectionType type, BlueprintData data) {
        this.type = type;
        this.data = data;
    }

    public String toString() {
        return "Section[]";
    }
}