package de.johnherdling.piomapping;

import java.util.List;

public class MappingResult {

    private final String mappingId;
    private final String target;
    private final List<String> values;

    public MappingResult(
            String mappingId,
            String target,
            List<String> values
    ) {
        this.mappingId = mappingId;
        this.target = target;
        this.values = values;
    }

    public String getMappingId() {
        return mappingId;
    }

    public String getTarget() {
        return target;
    }

    public List<String> getValues() {
        return values;
    }
}