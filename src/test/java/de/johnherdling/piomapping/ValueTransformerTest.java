package de.johnherdling.piomapping;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValueTransformerTest {

    @Test
    void transformsYesToMedicationPresent() {

        ValueTransformer transformer = new ValueTransformer();

        Map<String, String> transformations = Map.of(
                "YES", "Medikation liegt vor",
                "ACTIVE", "Medikation liegt vor"
        );

        String result = transformer.transform(
                "YES",
                transformations
        );

        assertEquals(
                "Medikation liegt vor",
                result
        );
    }

    @Test
    void transformsActiveToMedicationPresent() {

        ValueTransformer transformer = new ValueTransformer();

        Map<String, String> transformations = Map.of(
                "YES", "Medikation liegt vor",
                "ACTIVE", "Medikation liegt vor"
        );

        String result = transformer.transform(
                "ACTIVE",
                transformations
        );

        assertEquals(
                "Medikation liegt vor",
                result
        );
    }

    @Test
    void transformsTrueToPainPresentCode() {

        ValueTransformer transformer = new ValueTransformer();

        Map<String, String> transformations = Map.of(
                "true", "22253000"
        );

        String result = transformer.transform(
                "true",
                transformations
        );

        assertEquals(
                "22253000",
                result
        );
    }

    @Test
    void transformsPresentToPainPresentCode() {

        ValueTransformer transformer = new ValueTransformer();

        Map<String, String> transformations = Map.of(
                "present", "22253000"
        );

        String result = transformer.transform(
                "present",
                transformations
        );

        assertEquals(
                "22253000",
                result
        );
    }
}