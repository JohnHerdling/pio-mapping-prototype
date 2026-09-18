package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldMatcherTest {
    //M1
    @Test
    void findsWeightInQ1() throws IOException {

        JsonSourceReader reader = new JsonSourceReader();
        JsonNode root = reader.read("data/q1.json");

        JsonFieldCollector collector = new JsonFieldCollector();
        List<JsonField> fields = collector.collect(root);

        FieldMatcher matcher = new FieldMatcher();

        List<JsonField> matches = matcher.findMatches(
                fields,
                "weight",
                JsonFieldType.NUMBER
        );

        assertEquals(1, matches.size());

        JsonField match = matches.get(0);

        assertEquals("weight", match.getPath());
        assertEquals(82, match.getValue().asInt());
    }

    @Test
    void findsWeightInQ2() throws IOException {

        JsonSourceReader reader = new JsonSourceReader();
        JsonNode root = reader.read("data/q2.json");

        JsonFieldCollector collector = new JsonFieldCollector();
        List<JsonField> fields = collector.collect(root);

        FieldMatcher matcher = new FieldMatcher();

        List<JsonField> matches = matcher.findMatches(
                fields,
                "weight",
                JsonFieldType.NUMBER
        );

        assertEquals(1, matches.size());

        JsonField match = matches.get(0);

        assertEquals("vitals.bodyWeight.value", match.getPath());
        assertEquals(82, match.getValue().asInt());
    }

    @Test
    void doesNotFindWeightInQ3() throws IOException {

        JsonSourceReader reader = new JsonSourceReader();
        JsonNode root = reader.read("data/q3.json");

        JsonFieldCollector collector = new JsonFieldCollector();
        List<JsonField> fields = collector.collect(root);

        FieldMatcher matcher = new FieldMatcher();

        List<JsonField> matches = matcher.findMatches(
                fields,
                "weight",
                JsonFieldType.NUMBER
        );

        assertEquals(0, matches.size());
    }
    
    //M2 
    @Test
    void findsDiagnosisInQ1() throws IOException {

        JsonSourceReader reader = new JsonSourceReader();
        JsonNode root = reader.read("data/q1.json");

        JsonFieldCollector collector = new JsonFieldCollector();
        List<JsonField> fields = collector.collect(root);

        FieldMatcher matcher = new FieldMatcher();

        List<JsonField> matches = matcher.findMatches(
                fields,
                "diagnosis",
                JsonFieldType.TEXT
        );

        assertEquals(1, matches.size());

        JsonField match = matches.get(0);

        assertEquals(
                "patient.medical.diagnosis.code",
                match.getPath()
        );

        assertEquals("I10", match.getValue().asText());
    }

    @Test
    void findsDiagnosisInQ2() throws IOException {

        JsonSourceReader reader = new JsonSourceReader();
        JsonNode root = reader.read("data/q2.json");

        JsonFieldCollector collector = new JsonFieldCollector();
        List<JsonField> fields = collector.collect(root);

        FieldMatcher matcher = new FieldMatcher();

        List<JsonField> matches = matcher.findMatches(
                fields,
                "diagnosis",
                JsonFieldType.TEXT
        );

        assertEquals(1, matches.size());
        assertEquals("I10", matches.get(0).getValue().asText());
    }

    @Test
    void doesNotFindDiagnosisInQ3() throws IOException {

        JsonSourceReader reader = new JsonSourceReader();
        JsonNode root = reader.read("data/q3.json");

        JsonFieldCollector collector = new JsonFieldCollector();
        List<JsonField> fields = collector.collect(root);

        FieldMatcher matcher = new FieldMatcher();

        List<JsonField> matches = matcher.findMatches(
                fields,
                "diagnosis",
                JsonFieldType.TEXT
        );

        assertEquals(0, matches.size());
    }

    //M3
    @Test
    void findsMedicationInQ1() throws IOException {

        JsonSourceReader reader = new JsonSourceReader();
        JsonNode root = reader.read("data/q1.json");

        JsonFieldCollector collector = new JsonFieldCollector();
        List<JsonField> fields = collector.collect(root);

        FieldMatcher matcher = new FieldMatcher();

        List<JsonField> matches = matcher.findMatches(
                fields,
                "medication",
                JsonFieldType.TEXT
        );

        assertEquals(1, matches.size());

        JsonField match = matches.get(0);

        assertEquals("medicationStatus", match.getPath());
        assertEquals("YES", match.getValue().asText());
    }

    @Test
    void findsMedicationInQ2() throws IOException {

        JsonSourceReader reader = new JsonSourceReader();
        JsonNode root = reader.read("data/q2.json");

        JsonFieldCollector collector = new JsonFieldCollector();
        List<JsonField> fields = collector.collect(root);

        FieldMatcher matcher = new FieldMatcher();

        List<JsonField> matches = matcher.findMatches(
                fields,
                "medication",
                JsonFieldType.TEXT
        );

        assertEquals(1, matches.size());

        JsonField match = matches.get(0);

        assertEquals("medication.status", match.getPath());
        assertEquals("ACTIVE", match.getValue().asText());
    }

    @Test
    void doesNotFindMedicationInQ3BecauseOfDifferentType() throws IOException {

        JsonSourceReader reader = new JsonSourceReader();
        JsonNode root = reader.read("data/q3.json");

        JsonFieldCollector collector = new JsonFieldCollector();
        List<JsonField> fields = collector.collect(root);

        FieldMatcher matcher = new FieldMatcher();

        List<JsonField> matches = matcher.findMatches(
                fields,
                "medication",
                JsonFieldType.TEXT
        );

        assertEquals(0, matches.size());
    }

    //M4
    @Test
    void findsPainInQ1() throws IOException {

        JsonSourceReader reader = new JsonSourceReader();
        JsonNode root = reader.read("data/q1.json");

        JsonFieldCollector collector = new JsonFieldCollector();
        List<JsonField> fields = collector.collect(root);

        FieldMatcher matcher = new FieldMatcher();

        List<JsonField> matches = matcher.findMatches(
                fields,
                "pain",
                JsonFieldType.BOOLEAN
        );

        assertEquals(1, matches.size());

        JsonField match = matches.get(0);

        assertEquals("pain.present", match.getPath());
        assertEquals(true, match.getValue().asBoolean());
    }

    @Test
    void findsPainInQ2() throws IOException {

        JsonSourceReader reader = new JsonSourceReader();
        JsonNode root = reader.read("data/q2.json");

        JsonFieldCollector collector = new JsonFieldCollector();
        List<JsonField> fields = collector.collect(root);

        FieldMatcher matcher = new FieldMatcher();

        List<JsonField> matches = matcher.findMatches(
                fields,
                "pain",
                JsonFieldType.TEXT
        );

        assertEquals(1, matches.size());

        JsonField match = matches.get(0);

        assertEquals("symptoms.painStatus", match.getPath());
        assertEquals("present", match.getValue().asText());
    }

   @Test
    void doesNotFindPainStateInQ3() throws IOException {

        JsonSourceReader reader = new JsonSourceReader();
        JsonNode root = reader.read("data/q3.json");

        JsonFieldCollector collector = new JsonFieldCollector();
        List<JsonField> fields = collector.collect(root);

        FieldMatcher matcher = new FieldMatcher();

        List<JsonField> matches = matcher.findMatches(
                fields,
                "pain",
                JsonFieldType.TEXT
        );

        assertEquals(0, matches.size());
    }

    //M5
    @Test
    void findsAllergiesInQ1() throws IOException {

        JsonSourceReader reader = new JsonSourceReader();
        JsonNode root = reader.read("data/q1.json");

        JsonFieldCollector collector = new JsonFieldCollector();
        List<JsonField> fields = collector.collect(root);

        FieldMatcher matcher = new FieldMatcher();

        List<JsonField> matches = matcher.findMatches(
                fields,
                "allergy",
                JsonFieldType.TEXT
        );

        assertEquals(2, matches.size());
        assertEquals("Penicillin", matches.get(0).getValue().asText());
        assertEquals("Latex", matches.get(1).getValue().asText());
    }

    @Test
    void findsAllergiesInQ2() throws IOException {

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

        assertEquals(2, matches.size());

        assertEquals(
                "allergyEntries[0].name",
                matches.get(0).getPath()
        );
        assertEquals(
                "Penicillin",
                matches.get(0).getValue().asText()
        );

        assertEquals(
                "allergyEntries[1].name",
                matches.get(1).getPath()
        );
        assertEquals(
                "Latex",
                matches.get(1).getValue().asText()
        );
    }

    @Test
    void doesNotFindAllergiesInQ3() throws IOException {

        JsonSourceReader reader = new JsonSourceReader();
        JsonNode root = reader.read("data/q3.json");

        JsonFieldCollector collector = new JsonFieldCollector();
        List<JsonField> fields = collector.collect(root);

        FieldMatcher matcher = new FieldMatcher();

        List<JsonField> matches = matcher.findMatches(
                fields,
                "allergy",
                JsonFieldType.TEXT
        );

        assertEquals(0, matches.size());
    }
}