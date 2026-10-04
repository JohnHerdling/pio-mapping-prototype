package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class SyntheaEvaluationRunner {

    private static final Path INPUT_DIR = Path.of(
            "evaluation",
            "input"
    );

    private static final Path EXPECTED_DIR = Path.of(
            "evaluation",
            "expected"
    );

    private static final Path RESULTS_DIR = Path.of(
            "evaluation",
            "results"
    );

    private static final String CONFIG_FILE =
            "config/mapping.yaml";

    private static final ObjectMapper MAPPER =
            new ObjectMapper()
                    .enable(
                            SerializationFeature.INDENT_OUTPUT
                    );

    public static void main(String[] args)
            throws IOException {

        System.out.println(
                "Externe Synthea-Evaluation"
        );

        System.out.println(
                "========================================"
        );

        /*
         * Unveränderte Mapping-Konfiguration laden.
         */
        MappingConfigReader configReader =
                new MappingConfigReader();

        MappingConfig config =
                configReader.read(
                        CONFIG_FILE
                );

        /*
         * Unveränderte Mapping-Engine verwenden.
         */
        MappingEngine engine =
                new MappingEngine();

        Files.createDirectories(
                RESULTS_DIR
        );

        /*
         * Eingefrorene Evaluationsdateien laden.
         */
        List<Path> inputFiles;

        try (Stream<Path> files =
                     Files.list(INPUT_DIR)) {

            inputFiles =
                    files.filter(
                                    path ->
                                            path.getFileName()
                                                    .toString()
                                                    .matches(
                                                            "patient-\\d{3}\\.json"
                                                    )
                            )
                            .sorted()
                            .toList();
        }

        int totalEvaluable = 0;
        int totalCorrect = 0;

        Map<String, Integer> evaluatedPerMapping =
                new LinkedHashMap<>();

        Map<String, Integer> correctPerMapping =
                new LinkedHashMap<>();

        evaluatedPerMapping.put("M1", 0);
        evaluatedPerMapping.put("M3", 0);
        evaluatedPerMapping.put("M4", 0);
        evaluatedPerMapping.put("M5", 0);

        correctPerMapping.put("M1", 0);
        correctPerMapping.put("M3", 0);
        correctPerMapping.put("M4", 0);
        correctPerMapping.put("M5", 0);

        /*
         * Alle 16 Evaluationspatient:innen
         * verarbeiten.
         */
        for (Path inputFile : inputFiles) {

            String inputName =
                    inputFile
                            .getFileName()
                            .toString();

            String patientNumber =
                    inputName.substring(
                            "patient-".length(),
                            "patient-".length() + 3
                    );

            Path expectedFile =
                    EXPECTED_DIR.resolve(
                            "expected-patient-"
                                    + patientNumber
                                    + ".json"
                    );

            if (!Files.exists(expectedFile)) {

                throw new IllegalStateException(
                        "Ground Truth fehlt für "
                                + inputName
                );
            }

            JsonNode source =
                    MAPPER.readTree(
                            inputFile.toFile()
                    );

            JsonNode expected =
                    MAPPER.readTree(
                            expectedFile.toFile()
                    );

            Map<String, Object> patientResult =
                    new LinkedHashMap<>();

            patientResult.put(
                    "input",
                    inputName
            );

            Map<String, Object> mappings =
                    new LinkedHashMap<>();

            System.out.println();
            System.out.println(
                    "----------------------------------------"
            );

            System.out.println(
                    inputName
            );

            System.out.println(
                    "----------------------------------------"
            );

            for (MappingRule rule :
                    config.getMappings()) {

                String mappingId =
                        rule.getId();

                /*
                 * M2 wird bewusst nicht durch die
                 * Engine bewertet, da die externe
                 * Quelle ein anderes Diagnosencodesystem
                 * verwendet und Terminologieabbildung
                 * nicht Bestandteil des Prototyps ist.
                 */
                if ("M2".equals(mappingId)) {

                    Map<String, Object> m2Result =
                            new LinkedHashMap<>();

                    m2Result.put(
                            "status",
                            "NOT_EVALUABLE"
                    );

                    mappings.put(
                            mappingId,
                            m2Result
                    );

                    System.out.println(
                            "M2: NOT_EVALUABLE"
                    );

                    continue;
                }

                /*
                 * Bestehende Mapping-Engine ausführen.
                 */
                MappingResult actualResult =
                        engine.execute(
                                source,
                                rule
                        );

                List<String> actualValues =
                        actualResult.getValues();

                JsonNode expectedMapping =
                        expected.path(
                                mappingId
                        );

                JsonNode expectedValue =
                        expectedMapping.get(
                                "expectedValue"
                        );

                boolean correct =
                        compare(
                                mappingId,
                                expectedValue,
                                actualValues
                        );

                totalEvaluable++;

                evaluatedPerMapping.put(
                        mappingId,
                        evaluatedPerMapping.get(
                                mappingId
                        ) + 1
                );

                if (correct) {

                    totalCorrect++;

                    correctPerMapping.put(
                            mappingId,
                            correctPerMapping.get(
                                    mappingId
                            ) + 1
                    );
                }

                Map<String, Object> mappingResult =
                        new LinkedHashMap<>();

                mappingResult.put(
                        "target",
                        actualResult.getTarget()
                );

                mappingResult.put(
                        "expectedValue",
                        jsonNodeToJava(
                                expectedValue
                        )
                );

                mappingResult.put(
                        "actualValues",
                        actualValues
                );

                mappingResult.put(
                        "correct",
                        correct
                );

                mappings.put(
                        mappingId,
                        mappingResult
                );

                System.out.println(
                        mappingId
                                + ": "
                                + (
                                correct
                                        ? "KORREKT"
                                        : "NICHT KORREKT"
                        )
                );

                System.out.println(
                        "  Erwartet: "
                                + expectedValueToText(
                                expectedValue
                        )
                );

                System.out.println(
                        "  Tatsächlich: "
                                + actualValues
                );
            }

            patientResult.put(
                    "mappings",
                    mappings
            );

            Path resultFile =
                    RESULTS_DIR.resolve(
                            "result-patient-"
                                    + patientNumber
                                    + ".json"
                    );

            MAPPER.writeValue(
                    resultFile.toFile(),
                    patientResult
            );
        }

        /*
         * Gesamtergebnis berechnen.
         */
        double accuracy =
                totalEvaluable == 0
                        ? 0.0
                        : (
                        (double) totalCorrect
                                / totalEvaluable
                ) * 100.0;

        System.out.println();
        System.out.println(
                "========================================"
        );

        System.out.println(
                "Zusammenfassung"
        );

        System.out.println(
                "========================================"
        );

        printMappingSummary(
                "M1 Körpergewicht",
                "M1",
                correctPerMapping,
                evaluatedPerMapping
        );

        System.out.println(
                "M2 Diagnose:             NOT_EVALUABLE"
        );

        printMappingSummary(
                "M3 Medikation",
                "M3",
                correctPerMapping,
                evaluatedPerMapping
        );

        printMappingSummary(
                "M4 Schmerzsymptomatik",
                "M4",
                correctPerMapping,
                evaluatedPerMapping
        );

        printMappingSummary(
                "M5 Allergien",
                "M5",
                correctPerMapping,
                evaluatedPerMapping
        );

        System.out.println(
                "----------------------------------------"
        );

        System.out.println(
                "Korrekt:                 "
                        + totalCorrect
                        + " / "
                        + totalEvaluable
        );

        System.out.printf(
                "Mapping-Korrektheit:      %.1f%%%n",
                accuracy
        );

        System.out.println(
                "========================================"
        );

        /*
         * Zusammenfassung zusätzlich als JSON
         * speichern.
         */
        Map<String, Object> summary =
                new LinkedHashMap<>();

        summary.put(
                "evaluationDataset",
                "Synthea"
        );

        summary.put(
                "patients",
                inputFiles.size()
        );

        summary.put(
                "M2Status",
                "NOT_EVALUABLE"
        );

        summary.put(
                "evaluatedTransformations",
                totalEvaluable
        );

        summary.put(
                "correctTransformations",
                totalCorrect
        );

        summary.put(
                "mappingAccuracyPercent",
                accuracy
        );

        Map<String, Object> mappingSummary =
                new LinkedHashMap<>();

        for (String mappingId :
                evaluatedPerMapping.keySet()) {

            Map<String, Integer> values =
                    new LinkedHashMap<>();

            values.put(
                    "correct",
                    correctPerMapping.get(
                            mappingId
                    )
            );

            values.put(
                    "evaluated",
                    evaluatedPerMapping.get(
                            mappingId
                    )
            );

            mappingSummary.put(
                    mappingId,
                    values
            );
        }

        summary.put(
                "mappings",
                mappingSummary
        );

        MAPPER.writeValue(
                RESULTS_DIR
                        .resolve(
                                "summary.json"
                        )
                        .toFile(),
                summary
        );
    }

    /*
     * Vergleich von Ground Truth und
     * tatsächlichem Mapping-Ergebnis.
     */
    private static boolean compare(
            String mappingId,
            JsonNode expectedValue,
            List<String> actualValues
    ) {

        if ("M1".equals(mappingId)) {

            return compareNumeric(
                    expectedValue,
                    actualValues
            );
        }

        if ("M5".equals(mappingId)) {

            return compareList(
                    expectedValue,
                    actualValues
            );
        }

        return compareSingleValue(
                expectedValue,
                actualValues
        );
    }

    /*
     * M1:
     * Numerischer Vergleich.
     *
     * Keine fachliche Toleranz wird eingeführt.
     * Die Zahlenwerte müssen identisch sein.
     */
    private static boolean compareNumeric(
            JsonNode expectedValue,
            List<String> actualValues
    ) {

        if (expectedValue == null
                || !expectedValue.isNumber()
                || actualValues == null
                || actualValues.size() != 1) {

            return false;
        }

        try {

            double expected =
                    expectedValue.asDouble();

            double actual =
                    Double.parseDouble(
                            actualValues.get(0)
                    );

            return Double.compare(
                    expected,
                    actual
            ) == 0;

        } catch (NumberFormatException e) {

            return false;
        }
    }

    /*
     * M3 und M4:
     * Genau ein erwarteter Zielwert.
     *
     * Falls die Ground Truth null enthält,
     * wird eine leere Ergebnisliste erwartet.
     */
    private static boolean compareSingleValue(
            JsonNode expectedValue,
            List<String> actualValues
    ) {

        if (expectedValue == null
                || expectedValue.isNull()) {

            return actualValues == null
                    || actualValues.isEmpty();
        }

        if (actualValues == null
                || actualValues.size() != 1) {

            return false;
        }

        return expectedValue
                .asText()
                .equals(
                        actualValues.get(0)
                );
    }

    /*
     * M5:
     * Vollständiger Vergleich der Wiederholungen.
     *
     * Die Reihenfolge wird nicht bewertet.
     * Die Häufigkeit identischer Werte bleibt
     * jedoch erhalten.
     */
    private static boolean compareList(
            JsonNode expectedValue,
            List<String> actualValues
    ) {

        if (expectedValue == null
                || !expectedValue.isArray()
                || actualValues == null) {

            return false;
        }

        List<String> expected =
                new ArrayList<>();

        for (JsonNode value :
                expectedValue) {

            expected.add(
                    value.asText()
            );
        }

        if (expected.size()
                != actualValues.size()) {

            return false;
        }

        List<String> expectedSorted =
                new ArrayList<>(
                        expected
                );

        List<String> actualSorted =
                new ArrayList<>(
                        actualValues
                );

        expectedSorted.sort(
                String::compareTo
        );

        actualSorted.sort(
                String::compareTo
        );

        return expectedSorted.equals(
                actualSorted
        );
    }

    private static Object jsonNodeToJava(
            JsonNode node
    ) {

        if (node == null
                || node.isNull()) {

            return null;
        }

        if (node.isNumber()) {

            return node.numberValue();
        }

        if (node.isArray()) {

            List<String> values =
                    new ArrayList<>();

            for (JsonNode value :
                    node) {

                values.add(
                        value.asText()
                );
            }

            return values;
        }

        return node.asText();
    }

    private static String expectedValueToText(
            JsonNode node
    ) {

        if (node == null
                || node.isNull()) {

            return "null";
        }

        return node.toString();
    }

    private static void printMappingSummary(
            String label,
            String mappingId,
            Map<String, Integer> correct,
            Map<String, Integer> evaluated
    ) {

        System.out.printf(
                "%-25s %d / %d%n",
                label + ":",
                correct.get(mappingId),
                evaluated.get(mappingId)
        );
    }
}