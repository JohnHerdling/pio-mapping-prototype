package de.johnherdling.piomapping;

import java.util.List;

public class MappingConfig {

    private List<MappingRule> mappings;

    public MappingConfig() {
    }

    public List<MappingRule> getMappings() {
        return mappings;
    }

    public void setMappings(List<MappingRule> mappings) {
        this.mappings = mappings;
    }
}