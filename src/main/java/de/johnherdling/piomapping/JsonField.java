package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonField {

    private final String fieldName;
    private final String path;
    private final JsonNode value;

    public JsonField(String fieldName, String path, JsonNode value) {
    this.fieldName = fieldName;
    this.path = path;
    this.value = value;
}
}