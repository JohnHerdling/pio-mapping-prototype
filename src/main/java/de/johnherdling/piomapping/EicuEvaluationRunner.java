
package de.johnherdling.piomapping;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class EicuEvaluationRunner {

    private static final Path CASES_DIR =
            Paths.get("data", "eicu-split", "evaluation");

    private static final Path GROUND_TRUTH_FILE =
            Paths.get(
                    "data",
                    "eicu-split",
                    "evaluation-ground-truth.json"
            );

    private static final Path RESULT_FILE =
            Paths.get(
                    "data",
                    "eicu-split",
                    "evaluation-results.json"
            );

    private static final String CONFIG_FILE =
            Paths.get("config", "mapping.yaml").toString();

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper();

    public static void main(String[] args) throws Exception {

        MappingConfigReader configReader =
                new MappingConfigReader();

        MappingConfig config =
                configReader.read(CONFIG_FILE);

        MappingEngine engine =
                new MappingEngine();

        List<Map<String, Object>> groundTruth =
                OBJECT_MAPPER.readValue(
                        GROUND_TRUTH_FILE.toFile(),
                        new TypeReference<>() {}
                );

        Map<String, Map<String, Object>> expectedByStayId =
                new LinkedHashMap<>();

        for (Map<String, Object> expected : groundTruth) {

            String stayId =
                    expected.get("patientUnitStayID").toString();

            expectedByStayId.put(stayId, expected);
        }

        Map<String, MappingRule> rulesById =
                new LinkedHashMap<>();

        for (MappingRule rule : config.getMappings()) {
            rulesById.put(rule.getId(), rule);
        }

        int totalEvaluable = 0;
        int totalCorrect = 0;

        Map<String, Integer> evaluatedPerMapping =
                new LinkedHashMap<>();

        Map<String, Integer> correctPerMapping =
                new LinkedHashMap<>();

        List<String> mappingIds =
                List.of("M1", "M3", "M4", "M5");

        for (String id : mappingIds) {
            evaluatedPerMapping.put(id, 0);
            correctPerMapping.put(id, 0);
        }

        List<Map<String, Object>> detailedResults =
                new ArrayList<>();

        File[] files =
                CASES_DIR.toFile().listFiles(
                        (dir, name) ->
                                name.startsWith("eicu-")
                                        && name.endsWith(".json")
                );

        if (files == null || files.length != 129) {
            throw new IllegalStateException(
                    "Erwartet wurden 129 Evaluationsdateien."
            );
        }

        Arrays.sort(
                files,
                Comparator.comparing(File::getName)
        );

        for (File file : files) {

            String stayId =
                    file.getName()
                            .replace("eicu-", "")
                            .replace(".json", "");

            Map<String, Object> expected =
                    expectedByStayId.get(stayId);

            if (expected == null) {
                throw new IllegalStateException(
                        "Keine Ground Truth für Aufenthalt "
                                + stayId
                );
            }

            JsonNode source =
                    OBJECT_MAPPER.readTree(file);

            Map<String, Object> caseResult =
                    new LinkedHashMap<>();

            caseResult.put("patientUnitStayID", stayId);

            for (String mappingId : mappingIds) {

                MappingRule rule =
                        rulesById.get(mappingId);

                if (rule == null) {
                    throw new IllegalStateException(
                            "Mappingregel " + mappingId
                                    + " nicht gefunden."
                    );
                }

                MappingResult actualResult =
                        engine.execute(source, rule);

                Object expectedValue =
                        expected.get(mappingId);

                List<String> actualValues =
                        actualResult.getValues();

                Object actualValue;

                if (actualValues == null
                        || actualValues.isEmpty()) {

                    actualValue = null;

                } else if (rule.isMultiple()) {

                    actualValue = actualValues;

                } else {

                    actualValue = actualValues.get(0);
                }

                boolean correct =
                        valuesEqual(
                                expectedValue,
                                actualValue
                        );

                totalEvaluable++;

                evaluatedPerMapping.put(
                        mappingId,
                        evaluatedPerMapping.get(mappingId) + 1
                );

                if (correct) {

                    totalCorrect++;

                    correctPerMapping.put(
                            mappingId,
                            correctPerMapping.get(mappingId) + 1
                    );
                }

                Map<String, Object> mappingResult =
                        new LinkedHashMap<>();

                mappingResult.put("expected", expectedValue);
                mappingResult.put("actual", actualValue);
                mappingResult.put("correct", correct);

                caseResult.put(mappingId, mappingResult);
            }

            detailedResults.add(caseResult);
        }

        System.out.println();
        System.out.println("=== eICU UNABHÄNGIGE EVALUATION ===");
        System.out.println("Fälle: " + files.length);
        System.out.println("M2: NOT_EVALUABLE");

        for (String mappingId : mappingIds) {

            int evaluated =
                    evaluatedPerMapping.get(mappingId);

            int correct =
                    correctPerMapping.get(mappingId);

            double percentage =
                    evaluated == 0
                            ? 0.0
                            : correct * 100.0 / evaluated;

            System.out.printf(
                    Locale.US,
                    "%s: %d / %d (%.1f%%)%n",
                    mappingId,
                    correct,
                    evaluated,
                    percentage
            );
        }

        double totalPercentage =
                totalEvaluable == 0
                        ? 0.0
                        : totalCorrect * 100.0 / totalEvaluable;

        System.out.println();

        System.out.println(
                "Korrekt: " + totalCorrect
                        + " / " + totalEvaluable
        );

        System.out.printf(
                Locale.US,
                "Mapping-Korrektheit: %.1f%%%n",
                totalPercentage
        );

        OBJECT_MAPPER
                .writerWithDefaultPrettyPrinter()
                .writeValue(
                        RESULT_FILE.toFile(),
                        detailedResults
                );

        System.out.println(
                "Detailergebnisse: "
                        + RESULT_FILE.toAbsolutePath()
        );
    }

    private static boolean valuesEqual(
            Object expected,
            Object actual
    ) {

        if (expected == null) {
            return actual == null;
        }

        if (actual == null) {
            return false;
        }

        if (expected instanceof Number
                && actual instanceof Number) {

            double expectedNumber =
                    ((Number) expected).doubleValue();

            double actualNumber =
                    ((Number) actual).doubleValue();

            return Math.abs(
                    expectedNumber - actualNumber
            ) < 0.0001;
        }

        if (expected instanceof Number
                && actual instanceof String) {

            try {

                double expectedNumber =
                        ((Number) expected).doubleValue();

                double actualNumber =
                        Double.parseDouble(
                                ((String) actual).trim()
                        );

                return Math.abs(
                        expectedNumber - actualNumber
                ) < 0.0001;

            } catch (NumberFormatException ignored) {
                return false;
            }
        }

        if (expected instanceof List<?> expectedList) {

            List<String> expectedValues =
                    normalizeList(expectedList);

            List<String> actualValues;

            if (actual instanceof List<?> actualList) {

                actualValues =
                        normalizeList(actualList);

            } else {

                actualValues =
                        normalizeList(List.of(actual));
            }

            return expectedValues.equals(actualValues);
        }

        return normalizeScalar(expected)
                .equals(normalizeScalar(actual));
    }

    private static List<String> normalizeList(
            List<?> values
    ) {

        return values.stream()
                .filter(Objects::nonNull)
                .map(EicuEvaluationRunner::normalizeScalar)
                .distinct()
                .sorted()
                .toList();
    }

    private static String normalizeScalar(
            Object value
    ) {

        if (value == null) {
            return "";
        }

        return value.toString()
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}
