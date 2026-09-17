package de.johnherdling.piomapping;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MappingConfigReaderTest {

    @Test
    void readsMappingConfiguration() throws IOException {

        MappingConfigReader reader = new MappingConfigReader();

        MappingConfig config = reader.read("config/mapping.yaml");

        assertEquals(5, config.getMappings().size());

        MappingRule firstRule = config.getMappings().get(0);

        assertEquals("M1", firstRule.getId());
        assertEquals("weight", firstRule.getConcept());
        assertEquals(JsonFieldType.NUMBER, firstRule.getDataType());
        assertEquals("valueQuantity.value", firstRule.getTarget());
        assertFalse(firstRule.isMultiple());
    }
}