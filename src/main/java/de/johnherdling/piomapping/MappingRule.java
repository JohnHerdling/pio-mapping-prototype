package de.johnherdling.piomapping;

import java.util.Map;

public class MappingRule {

    private String id;
    private String concept;
    private JsonFieldType dataType;
    private String target;
    private boolean multiple;
    private Map<String, String> transformations;

    public MappingRule() {
    }

    public String getId() {
        return id;
    }

    public String getConcept() {
        return concept;
    }

    public JsonFieldType getDataType() {
        return dataType;
    }

    public String getTarget() {
        return target;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public Map<String, String> getTransformations() {
        return transformations;
    }

    public void setId(String id) {
    this.id = id;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public void setDataType(JsonFieldType dataType) {
        this.dataType = dataType;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public void setTransformations(Map<String, String> transformations) {
        this.transformations = transformations;
    }
}