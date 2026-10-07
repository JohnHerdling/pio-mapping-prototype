package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonFieldCollector {

    public List<JsonField> collect(JsonNode node) {

        List<JsonField> fields = new ArrayList<>();

        collect(
                node,
                "",
                "",
                null,
                false,
                fields
        );

        return fields;
    }

    private void collect(
            JsonNode node,
            String path,
            String context,
            Long temporalOrder,
            boolean arrayElement,
            List<JsonField> fields
    ) {

        if (node.isObject()) {

            String localContext =
                    buildContext(node);

            String objectContext =
                    localContext.isEmpty()
                            ? context
                            : localContext;

            /*
             * Zeitliche Ordnungsinformationen werden nur
             * innerhalb einzelner Elemente einer Liste
             * berücksichtigt.
             *
             * Dadurch erhalten beispielsweise wiederholte
             * Beobachtungen einen Offset, während allgemeine
             * Objekte wie "patient" nicht anhand irgendeines
             * enthaltenen Offset-Feldes zeitlich eingeordnet
             * werden.
             */
            Long objectTemporalOrder =
                    temporalOrder;

            if (arrayElement) {

                Long localTemporalOrder =
                        findTemporalOrder(node);

                if (localTemporalOrder != null) {
                    objectTemporalOrder =
                            localTemporalOrder;
                }
            }

            for (Map.Entry<String, JsonNode> field
                    : node.properties()) {

                String fieldName =
                        field.getKey();

                JsonNode childNode =
                        field.getValue();

                String childPath =
                        path.isEmpty()
                                ? fieldName
                                : path + "." + fieldName;

                collect(
                        childNode,
                        childPath,
                        objectContext,
                        objectTemporalOrder,
                        arrayElement,
                        fields
                );
            }

        } else if (node.isArray()) {

            for (int i = 0;
                 i < node.size();
                 i++) {

                JsonNode childNode =
                        node.get(i);

                String childPath =
                        path + "[" + i + "]";

                collect(
                        childNode,
                        childPath,
                        "",
                        temporalOrder,
                        true,
                        fields
                );
            }

        } else {

            String fieldName =
                    getFieldNameFromPath(path);

            JsonFieldType dataType =
                    determineDataType(node);

            fields.add(
                    new JsonField(
                            fieldName,
                            path,
                            node,
                            dataType,
                            context,
                            temporalOrder
                    )
            );
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

    private boolean isContextField(String fieldName) {

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

    private Long findTemporalOrder(JsonNode node) {

        for (Map.Entry<String, JsonNode> field
                : node.properties()) {

            String fieldName =
                    field.getKey().toLowerCase();

            if (!fieldName.contains("offset")) {
                continue;
            }

            JsonNode value =
                    field.getValue();

            if (value == null) {
                continue;
            }

            try {
                return Long.parseLong(
                        value.asText().trim()
                );
            } catch (NumberFormatException ignored) {
                // Kein numerischer Offset.
            }
        }

        return null;
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

    private String getFieldNameFromPath(String path) {

        int lastDot =
                path.lastIndexOf(".");

        String fieldName =
                lastDot == -1
                        ? path
                        : path.substring(lastDot + 1);

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