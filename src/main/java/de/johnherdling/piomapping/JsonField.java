package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonField {

    private final String fieldName;
    private final String path;
    private final JsonNode value;
    private final JsonFieldType dataType;
    private final String context;
    private final Long temporalOrder;

    public JsonField(
            String fieldName,
            String path,
            JsonNode value,
            JsonFieldType dataType,
            String context
    ) {
        this(
                fieldName,
                path,
                value,
                dataType,
                context,
                null
        );
    }

    public JsonField(
            String fieldName,
            String path,
            JsonNode value,
            JsonFieldType dataType,
            String context,
            Long temporalOrder
    ) {
        this.fieldName = fieldName;
        this.path = path;
        this.value = value;
        this.dataType = dataType;
        this.context = context;
        this.temporalOrder = temporalOrder;
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

    public String getContext() {
        return context;
    }

    public Long getTemporalOrder() {
        return temporalOrder;
    }
}