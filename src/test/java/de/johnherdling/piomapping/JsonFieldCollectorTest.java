package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonFieldCollectorTest {

    @Test
    void collectsFieldsFromQ1() throws IOException {

        JsonSourceReader reader = new JsonSourceReader();
        JsonNode root = reader.read("data/q1.json");

        JsonFieldCollector collector = new JsonFieldCollector();
        List<JsonField> fields = collector.collect(root);

        assertEquals(6, fields.size());

        JsonField firstField = fields.get(0);

        assertEquals("weight", firstField.getFieldName());
        assertEquals("weight", firstField.getPath());
        assertEquals(82, firstField.getValue().asInt());

        JsonField allergyField = fields.get(4);

        assertEquals("allergies", allergyField.getFieldName());
        assertEquals("allergies[0]", allergyField.getPath());
        assertEquals("Penicillin", allergyField.getValue().asText());
    }

    @Test
    void collectsFieldsFromQ2() throws IOException {

        JsonSourceReader reader = new JsonSourceReader();
        JsonNode root = reader.read("data/q2.json");

        JsonFieldCollector collector = new JsonFieldCollector();
        List<JsonField> fields = collector.collect(root);

        assertEquals(6, fields.size());

        JsonField allergyField = fields.get(4);

        assertEquals("name", allergyField.getFieldName());
        assertEquals("allergyEntries[0].name", allergyField.getPath());
        assertEquals("Penicillin", allergyField.getValue().asText());
    }
    @Test
    void collectsFieldsFromQ3() throws IOException {

        JsonSourceReader reader = new JsonSourceReader();
        JsonNode root = reader.read("data/q3.json");

        JsonFieldCollector collector = new JsonFieldCollector();
        List<JsonField> fields = collector.collect(root);

        assertEquals(9, fields.size());

        JsonField allergyField = fields.get(7);

        assertEquals("display", allergyField.getFieldName());
        assertEquals(
                "intolerances.items[0].substance.display",
                allergyField.getPath()
        );
        assertEquals("Penicillin", allergyField.getValue().asText());
    }

    @Test
    void determinesDataTypesFromQ1() throws IOException {

        JsonSourceReader reader = new JsonSourceReader();
        JsonNode root = reader.read("data/q1.json");

        JsonFieldCollector collector = new JsonFieldCollector();
        List<JsonField> fields = collector.collect(root);

        assertEquals(JsonFieldType.NUMBER, fields.get(0).getDataType());
        assertEquals(JsonFieldType.TEXT, fields.get(1).getDataType());
        assertEquals(JsonFieldType.BOOLEAN, fields.get(3).getDataType());
    }
}