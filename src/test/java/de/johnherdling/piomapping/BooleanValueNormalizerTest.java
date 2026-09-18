package de.johnherdling.piomapping;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BooleanValueNormalizerTest {

    private final BooleanValueNormalizer normalizer =
            new BooleanValueNormalizer();

    @Test
    void normalizesTrueValues() {
        assertTrue(normalizer.normalize("true"));
        assertTrue(normalizer.normalize("yes"));
        assertTrue(normalizer.normalize("present"));
    }

    @Test
    void normalizesFalseValues() {
        assertFalse(normalizer.normalize("false"));
        assertFalse(normalizer.normalize("no"));
        assertFalse(normalizer.normalize("absent"));
    }

    @Test
    void returnsNullForUnknownValue() {
        assertNull(normalizer.normalize("unknown"));
    }
}