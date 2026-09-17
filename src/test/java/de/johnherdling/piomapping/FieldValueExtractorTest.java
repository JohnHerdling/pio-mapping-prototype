package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldValueExtractorTest {

    @Test
    void extractsRepeatedAllergyValuesFromQ2() throws IOException {

        JsonSourceReader reader = new JsonSourceReader();
        JsonNode root = reader.read("data/q2.json");

        JsonFieldCollector collector = new JsonFieldCollector();
        List<JsonField> fields = collector.collect(root);

        FieldMatcher matcher = new FieldMatcher();

        List<JsonField> matches = matcher.findMatches(
                fields,
                "allergy",
                JsonFieldType.TEXT
        );

        FieldValueExtractor extractor = new FieldValueExtractor();

        List<String> values = extractor.extractValues(matches);

        assertEquals(2, values.size());
        assertEquals("Penicillin", values.get(0));
        assertEquals("Latex", values.get(1));
    }
}