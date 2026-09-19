package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TargetStructureIntegrationTest {

    @Test
    void mapsBodyWeightFromQ1IntoTargetStructure()
            throws IOException {

        JsonSourceReader sourceReader =
                new JsonSourceReader();

        MappingConfigReader configReader =
                new MappingConfigReader();

        MappingEngine engine =
                new MappingEngine();

        TargetStructureBuilder builder =
                new TargetStructureBuilder();

        JsonNode source =
                sourceReader.read("data/q1.json");

        MappingConfig config =
                configReader.read("config/mapping.yaml");

        MappingRule rule =
                config.getMappings()
                        .stream()
                        .filter(mapping ->
                                mapping.getId().equals("M1"))
                        .findFirst()
                        .orElseThrow();

        MappingResult mappingResult =
                engine.execute(source, rule);

        JsonNode targetStructure =
                builder.build(mappingResult);

        assertEquals(
                82.0,
                targetStructure
                        .get("valueQuantity")
                        .get("value")
                        .asDouble()
        );
    }

    @Test
    void mapsAllCasesFromQ1IntoTargetStructures()
            throws IOException {

        JsonSourceReader sourceReader =
                new JsonSourceReader();

        MappingConfigReader configReader =
                new MappingConfigReader();

        MappingEngine engine =
                new MappingEngine();

        TargetStructureBuilder builder =
                new TargetStructureBuilder();

        JsonNode source =
                sourceReader.read("data/q1.json");

        MappingConfig config =
                configReader.read("config/mapping.yaml");

        for (MappingRule rule : config.getMappings()) {

            MappingResult mappingResult =
                    engine.execute(source, rule);

            JsonNode target =
                    builder.build(mappingResult);

            switch (rule.getId()) {

                case "M1" -> assertEquals(
                        82.0,
                        target.get("valueQuantity")
                                .get("value")
                                .asDouble()
                );

                case "M2" -> assertEquals(
                        "I10",
                        target.get("code")
                                .get("coding")
                                .get(0)
                                .get("code")
                                .asText()
                );

                case "M3" -> assertEquals(
                        "Medikation liegt vor",
                        target.get("valueCodeableConcept")
                                .get("text")
                                .asText()
                );

                case "M4" -> assertEquals(
                        "22253000",
                        target.get("valueCodeableConcept")
                                .get("coding")
                                .get(0)
                                .get("code")
                                .asText()
                );

                case "M5" -> {
                    assertEquals(2, target.size());

                    assertEquals(
                            "Penicillin",
                            target.get(0)
                                    .get("code")
                                    .get("text")
                                    .asText()
                    );

                    assertEquals(
                            "Latex",
                            target.get(1)
                                    .get("code")
                                    .get("text")
                                    .asText()
                    );
                }

                default -> throw new IllegalArgumentException(
                        "Unknown mapping ID: " + rule.getId()
                );
            }
        }
    }

    @Test
    void evaluatesTargetStructuresForAllSourceSchemas()
            throws IOException {

        JsonSourceReader sourceReader =
                new JsonSourceReader();

        MappingConfigReader configReader =
                new MappingConfigReader();

        MappingEngine engine =
                new MappingEngine();

        TargetStructureBuilder builder =
                new TargetStructureBuilder();

        MappingConfig config =
                configReader.read("config/mapping.yaml");

        String[] sourceFiles = {
                "data/q1.json",
                "data/q2.json",
                "data/q3.json"
        };

        for (String sourceFile : sourceFiles) {

            JsonNode source =
                    sourceReader.read(sourceFile);

            System.out.println();
            System.out.println(
                    "Zielstruktur-Evaluation: " + sourceFile
            );

            for (MappingRule rule : config.getMappings()) {

                MappingResult mappingResult =
                        engine.execute(source, rule);

                JsonNode target =
                        builder.build(mappingResult);

                boolean correct =
                        targetContainsExpectedValue(
                                rule.getId(),
                                target
                        );

                System.out.println(
                        rule.getId()
                                + " | korrekt: "
                                + correct
                );
            }
        }
    }

    private boolean targetContainsExpectedValue(
        String mappingId,
        JsonNode target
    ) {

        return switch (mappingId) {

            case "M1" ->
                    target.path("valueQuantity")
                            .path("value")
                            .asDouble(Double.NaN) == 82.0;

            case "M2" ->
                    target.path("code")
                            .path("coding")
                            .path(0)
                            .path("code")
                            .asText()
                            .equals("I10");

            case "M3" ->
                    target.path("valueCodeableConcept")
                            .path("text")
                            .asText()
                            .equals("Medikation liegt vor");

            case "M4" ->
                    target.path("valueCodeableConcept")
                            .path("coding")
                            .path(0)
                            .path("code")
                            .asText()
                            .equals("22253000");

            case "M5" ->
                    target.isArray()
                            && target.size() == 2
                            && target.path(0)
                                    .path("code")
                                    .path("text")
                                    .asText()
                                    .equals("Penicillin")
                            && target.path(1)
                                    .path("code")
                                    .path("text")
                                    .asText()
                                    .equals("Latex");

            default -> false;
        };
    }
}