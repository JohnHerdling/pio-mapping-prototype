package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MappingEvaluationTest {

    @Test
    void comparesAllMappingsWithExpectedOutput() throws IOException {

        JsonSourceReader sourceReader =
                new JsonSourceReader();

        MappingConfigReader configReader =
                new MappingConfigReader();

        MappingConfig config =
                configReader.read("config/mapping.yaml");

        ExpectedOutputReader expectedOutputReader =
                new ExpectedOutputReader();

        JsonNode expectedOutput =
                expectedOutputReader.read(
                        "expected/expected-output.json"
                );

        MappingEngine engine =
                new MappingEngine();

        String[] sourceFiles = {
                "data/q1.json",
                "data/q2.json",
                "data/q3.json"
        };

        int total = 0;
        int correctCount = 0;

        for (String sourceFile : sourceFiles) {

            JsonNode source =
                    sourceReader.read(sourceFile);

            System.out.println();
            System.out.println("Quelle: " + sourceFile);

            for (MappingRule rule : config.getMappings()) {

                MappingResult result =
                        engine.execute(source, rule);

                JsonNode expectedValue =
                        expectedOutput
                                .get(rule.getId())
                                .get("expectedValue");

                boolean correct =
                        matchesExpectedValue(
                                result,
                                expectedValue
                        );

                total++;

                if (correct) {
                    correctCount++;
                }

                System.out.println(
                        rule.getId()
                                + " -> "
                                + result.getValues()
                                + " | Soll: "
                                + expectedValue
                                + " | korrekt: "
                                + correct
                );

                assertTrue(
                        correct,
                        "Fehler bei "
                                + sourceFile
                                + " / "
                                + rule.getId()
                                + ": Ist="
                                + result.getValues()
                                + ", Soll="
                                + expectedValue
                );
            }
        }

        System.out.println();
        System.out.println(
                "Gesamtergebnis: "
                        + correctCount
                        + " / "
                        + total
        );
    }

    private boolean matchesExpectedValue(
            MappingResult result,
            JsonNode expectedValue
    ) {

        if (expectedValue.isArray()) {

            if (result.getValues().size()
                    != expectedValue.size()) {
                return false;
            }

            for (int i = 0; i < expectedValue.size(); i++) {

                if (!result.getValues().get(i)
                        .equals(expectedValue.get(i).asText())) {
                    return false;
                }
            }

            return true;
        }

        if (result.getValues().size() != 1) {
            return false;
        }

        return result.getValues().get(0)
                .equals(expectedValue.asText());
    }
}