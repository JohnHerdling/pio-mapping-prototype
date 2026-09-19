package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.List;

public class TargetStructureBuilder {

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    public JsonNode buildBodyWeight(String value) {

        ObjectNode root =
                objectMapper.createObjectNode();

        ObjectNode valueQuantity =
                root.putObject("valueQuantity");

        valueQuantity.put(
                "value",
                Double.parseDouble(value)
        );

        return root;
    }

    public JsonNode buildDiagnosis(String value) {

        ObjectNode root =
                objectMapper.createObjectNode();

        ObjectNode code =
                root.putObject("code");

        ObjectNode coding =
                code.putArray("coding")
                        .addObject();

        coding.put("code", value);

        return root;
    }

    public JsonNode buildMedicationInformation(String value) {

        ObjectNode root =
                objectMapper.createObjectNode();

        ObjectNode valueCodeableConcept =
                root.putObject("valueCodeableConcept");

        valueCodeableConcept.put("text", value);

        return root;
    }

    public JsonNode buildPain(String value) {

        ObjectNode root =
                objectMapper.createObjectNode();

        ObjectNode valueCodeableConcept =
                root.putObject("valueCodeableConcept");

        ObjectNode coding =
                valueCodeableConcept.putArray("coding")
                        .addObject();

        coding.put("code", value);

        return root;
    }

    public JsonNode buildAllergies(List<String> values) {

        ArrayNode root =
                objectMapper.createArrayNode();

        for (String value : values) {

            ObjectNode allergy =
                    root.addObject();

            ObjectNode code =
                    allergy.putObject("code");

            code.put("text", value);
        }

        return root;
    }

    public JsonNode build(MappingResult result) {

        if (result.getValues().isEmpty()) {
            return objectMapper.createObjectNode();
        }

        return switch (result.getMappingId()) {

            case "M1" ->
                    buildBodyWeight(
                            result.getValues().get(0)
                    );

            case "M2" ->
                    buildDiagnosis(
                            result.getValues().get(0)
                    );

            case "M3" ->
                    buildMedicationInformation(
                            result.getValues().get(0)
                    );

            case "M4" ->
                    buildPain(
                            result.getValues().get(0)
                    );

            case "M5" ->
                    buildAllergies(
                            result.getValues()
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Unknown mapping ID: "
                                    + result.getMappingId()
                    );
        };
    }
}