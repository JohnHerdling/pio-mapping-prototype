package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MappingEngineTest {

    @Test
    void executesM1ForQ1() throws IOException {

        JsonSourceReader sourceReader =
                new JsonSourceReader();

        JsonNode source =
                sourceReader.read("data/q1.json");

        MappingConfigReader configReader =
                new MappingConfigReader();

        MappingConfig config =
                configReader.read("config/mapping.yaml");

        MappingRule rule =
                config.getMappings().get(0);

        MappingEngine engine =
                new MappingEngine();

        MappingResult result =
                engine.execute(source, rule);

        assertEquals("M1", result.getMappingId());

        assertEquals(
                "valueQuantity.value",
                result.getTarget()
        );

        assertEquals(1, result.getValues().size());

        assertEquals(
                "82",
                result.getValues().get(0)
        );
    }
}