package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonFieldCollector {

    public List<JsonField> collect(JsonNode node) {

        List<JsonField> fields = new ArrayList<>();

        collect(node, "", "", fields);

        return fields;
    }

    private void collect(
            JsonNode node,
            String path,
            String context,
            List<JsonField> fields
    ) {

        if (node.isObject()) {

            String localContext = buildContext(node);

            String objectContext;

            if (localContext.isEmpty()) {
                objectContext = context;
            } else {
                objectContext = localContext;
            }

            for (Map.Entry<String, JsonNode> field : node.properties()) {

                String fieldName = field.getKey();
                JsonNode childNode = field.getValue();

                String childPath;

                if (path.isEmpty()) {
                    childPath = fieldName;
                } else {
                    childPath = path + "." + fieldName;
                }

                collect(
                        childNode,
                        childPath,
                        objectContext,
                        fields
                );
            }

        } else if (node.isArray()) {

            for (int i = 0; i < node.size(); i++) {

                JsonNode childNode = node.get(i);

                String childPath =
                        path + "[" + i + "]";

                collect(
                        childNode,
                        childPath,
                        "",
                        fields
                );
            }

        } else {

            String fieldName =
                    getFieldNameFromPath(path);

            JsonFieldType dataType =
                    determineDataType(node);

            JsonField field =
                    new JsonField(
                            fieldName,
                            path,
                            node,
                            dataType,
                            context
                    );

            fields.add(field);
        }
    }

    private String buildContext(JsonNode node) {

        StringBuilder context =
                new StringBuilder();

        for (Map.Entry<String, JsonNode> field
                : node.properties()) {

            String fieldName =
                    field.getKey();

            if (isContextField(fieldName)) {

                appendTextValue(
                        context,
                        fieldName,
                        field.getValue()
                );
            }
        }

        return context.toString().trim();
    }

    private boolean isContextField(
            String fieldName
    ) {

        if (fieldName == null) {
            return false;
        }

        String normalized =
                fieldName.toLowerCase();

        return normalized.equals("type")
                || normalized.equals("name")
                || normalized.equals("description")
                || normalized.equals("label")
                || normalized.endsWith("type")
                || normalized.endsWith("name")
                || normalized.endsWith("description")
                || normalized.endsWith("label");
    }

    private void appendTextValue(
            StringBuilder context,
            String fieldName,
            JsonNode value
    ) {

        if (value == null
                || !value.isTextual()) {
            return;
        }

        String text =
                value.asText().trim();

        if (text.isEmpty()) {
            return;
        }

        if (!context.isEmpty()) {
            context.append(" ");
        }

        context.append(fieldName)
                .append(" ")
                .append(text);
    }

    private String getFieldNameFromPath(
            String path
    ) {

        int lastDot =
                path.lastIndexOf(".");

        String fieldName;

        if (lastDot == -1) {
            fieldName = path;
        } else {
            fieldName =
                    path.substring(lastDot + 1);
        }

        int arrayStart =
                fieldName.indexOf("[");

        if (arrayStart != -1) {
            fieldName =
                    fieldName.substring(
                            0,
                            arrayStart
                    );
        }

        return fieldName;
    }

    private JsonFieldType determineDataType(
            JsonNode node
    ) {

        if (node.isTextual()) {
            return JsonFieldType.TEXT;
        }

        if (node.isNumber()) {
            return JsonFieldType.NUMBER;
        }

        if (node.isBoolean()) {
            return JsonFieldType.BOOLEAN;
        }

        throw new IllegalArgumentException(
                "Unsupported JSON value type"
        );
    }
}