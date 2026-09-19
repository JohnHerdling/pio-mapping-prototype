package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

class TargetStructureBuilderTest {

    @Test
    void buildsBodyWeightTargetStructure() {

        TargetStructureBuilder builder =
                new TargetStructureBuilder();

        JsonNode result =
                builder.buildBodyWeight("82");

        assertEquals(
                82.0,
                result.get("valueQuantity")
                        .get("value")
                        .asDouble()
        );
    }

    @Test
    void buildsDiagnosisTargetStructure() {

        TargetStructureBuilder builder =
                new TargetStructureBuilder();

        JsonNode result =
                builder.buildDiagnosis("I10");

        assertEquals(
                "I10",
                result.get("code")
                        .get("coding")
                        .get(0)
                        .get("code")
                        .asText()
        );
    }

    @Test
    void buildsMedicationInformationTargetStructure() {

        TargetStructureBuilder builder =
                new TargetStructureBuilder();

        JsonNode result =
                builder.buildMedicationInformation(
                        "Medikation liegt vor"
                );

        assertEquals(
                "Medikation liegt vor",
                result.get("valueCodeableConcept")
                        .get("text")
                        .asText()
        );
    }

    @Test
    void buildsPainTargetStructure() {

        TargetStructureBuilder builder =
                new TargetStructureBuilder();

        JsonNode result =
                builder.buildPain("22253000");

        assertEquals(
                "22253000",
                result.get("valueCodeableConcept")
                        .get("coding")
                        .get(0)
                        .get("code")
                        .asText()
        );
    }

    @Test
    void buildsAllergyTargetStructures() {

        TargetStructureBuilder builder =
                new TargetStructureBuilder();

        JsonNode result =
                builder.buildAllergies(
                        List.of(
                                "Penicillin",
                                "Latex"
                        )
                );

        assertEquals(2, result.size());

        assertEquals(
                "Penicillin",
                result.get(0)
                        .get("code")
                        .get("text")
                        .asText()
        );

        assertEquals(
                "Latex",
                result.get(1)
                        .get("code")
                        .get("text")
                        .asText()
        );
    }

    @Test
    void buildsTargetStructureFromMappingResult() {

        MappingResult mappingResult =
                new MappingResult(
                        "M1",
                        "valueQuantity.value",
                        List.of("82")
                );

        TargetStructureBuilder builder =
                new TargetStructureBuilder();

        JsonNode result =
                builder.build(mappingResult);

        assertEquals(
                82.0,
                result.get("valueQuantity")
                        .get("value")
                        .asDouble()
        );
    }
}