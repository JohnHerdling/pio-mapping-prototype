package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonField {

    private final String fieldName;
    private final String path;
    private final JsonNode value;
    private final JsonFieldType dataType;

    public JsonField(String fieldName, String path, JsonNode value, JsonFieldType dataType) {
    this.fieldName = fieldName;
    this.path = path;
    this.value = value;
    this.dataType = dataType;

    }
    public String getFieldName() {
        return fieldName;
    }

    public String getPath() {
        return path;
    }

    public JsonNode getValue() {
        return value;
    }

    public JsonFieldType getDataType() {
        return dataType;
    }
}