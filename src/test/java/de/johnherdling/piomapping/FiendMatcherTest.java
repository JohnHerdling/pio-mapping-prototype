package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldMatcherTest {

    // M1
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
        assertEquals("weight", matches.get(0).getPath());
        assertEquals(82, matches.get(0).getValue().asInt());
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
        assertEquals(
                "vitals.bodyWeight.value",
                matches.get(0).getPath()
        );
        assertEquals(82, matches.get(0).getValue().asInt());
    }

    @Test
    void findsWeightInQ3() throws IOException {

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

        assertEquals(1, matches.size());
        assertEquals(
                "observations[0].data.numericValue",
                matches.get(0).getPath()
        );
        assertEquals(82, matches.get(0).getValue().asInt());
    }

    // M2
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
        assertEquals(
                "patient.medical.diagnosis.code",
                matches.get(0).getPath()
        );
        assertEquals("I10", matches.get(0).getValue().asText());
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
    void findsDiagnosisInQ3() throws IOException {

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

        assertEquals(1, matches.size());
        assertEquals(
                "clinicalData.problems[0].coding.value",
                matches.get(0).getPath()
        );
        assertEquals("I10", matches.get(0).getValue().asText());
    }

    // M3
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
        assertEquals(
                "medicationStatus",
                matches.get(0).getPath()
        );
        assertEquals("YES", matches.get(0).getValue().asText());
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
        assertEquals(
                "medication.status",
                matches.get(0).getPath()
        );
        assertEquals("ACTIVE", matches.get(0).getValue().asText());
    }

    @Test
    void findsMedicationFieldInQ3() throws IOException {

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

        assertEquals(1, matches.size());
        assertEquals(
                "therapy.medicationFlag",
                matches.get(0).getPath()
        );
        assertEquals(1, matches.get(0).getValue().asInt());
    }

    // M4
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
        assertEquals(
                "pain.present",
                matches.get(0).getPath()
        );
        assertEquals(
                true,
                matches.get(0).getValue().asBoolean()
        );
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
        assertEquals(
                "symptoms.painStatus",
                matches.get(0).getPath()
        );
        assertEquals(
                "present",
                matches.get(0).getValue().asText()
        );
    }

    @Test
        void findsPainStateInQ3() throws IOException {

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

        boolean containsPainState =
                matches.stream().anyMatch(
                        match ->
                                match.getPath().equals(
                                        "assessment.symptoms[0].state"
                                )
                                && match.getValue()
                                        .asText()
                                        .equals("detected")
                );

        assertEquals(true, containsPainState);
        }

    // M5
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
        assertEquals(
                "Penicillin",
                matches.get(0).getValue().asText()
        );
        assertEquals(
                "Latex",
                matches.get(1).getValue().asText()
        );
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
    void findsAllergiesInQ3() throws IOException {

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

        assertEquals(2, matches.size());

        assertEquals(
                "intolerances.items[0].substance.display",
                matches.get(0).getPath()
        );
        assertEquals(
                "Penicillin",
                matches.get(0).getValue().asText()
        );

        assertEquals(
                "intolerances.items[1].substance.display",
                matches.get(1).getPath()
        );
        assertEquals(
                "Latex",
                matches.get(1).getValue().asText()
        );
    }
}