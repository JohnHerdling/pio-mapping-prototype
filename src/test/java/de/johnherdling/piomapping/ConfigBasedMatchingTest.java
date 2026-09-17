package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigBasedMatchingTest {

    @Test
    void matchesM1UsingExternalConfiguration() throws IOException {

        MappingConfigReader configReader = new MappingConfigReader();
        MappingConfig config = configReader.read("config/mapping.yaml");

        MappingRule rule = config.getMappings().get(0);

        JsonSourceReader sourceReader = new JsonSourceReader();
        JsonNode root = sourceReader.read("data/q1.json");

        JsonFieldCollector collector = new JsonFieldCollector();
        List<JsonField> fields = collector.collect(root);

        FieldMatcher matcher = new FieldMatcher();

        List<JsonField> matches = matcher.findMatches(
                fields,
                rule.getConcept(),
                rule.getDataType()
        );

        assertEquals("M1", rule.getId());
        assertEquals(1, matches.size());
        assertEquals(82, matches.get(0).getValue().asInt());
    }
}