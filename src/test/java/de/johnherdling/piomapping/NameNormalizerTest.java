package de.johnherdling.piomapping;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NameNormalizerTest {

    @Test
    void normalizesDifferentNameFormats() {

        NameNormalizer normalizer = new NameNormalizer();

        assertEquals("body weight", normalizer.normalize("bodyWeight"));
        assertEquals("body weight", normalizer.normalize("body-weight"));
        assertEquals("body weight", normalizer.normalize("body_weight"));
        assertEquals("medication status", normalizer.normalize("medicationStatus"));
        assertEquals("diagnosis", normalizer.normalize("diagnoses"));
        assertEquals("allergy", normalizer.normalize("allergies"));
    }
}