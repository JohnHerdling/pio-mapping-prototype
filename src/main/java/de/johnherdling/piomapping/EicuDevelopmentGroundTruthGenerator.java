package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class EicuDevelopmentGroundTruthGenerator {

    private static final Path DEVELOPMENT_DIR =
            Paths.get("data", "eicu-split", "development");

    private static final Path OUTPUT_FILE =
            Paths.get(
                    "data",
                    "eicu-split",
                    "development-ground-truth.json"
            );

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper()
                    .enable(SerializationFeature.INDENT_OUTPUT);

    public static void main(String[] args) throws Exception {

        List<Path> caseFiles;

        try (var stream = Files.list(DEVELOPMENT_DIR)) {

            caseFiles = stream
                    .filter(path ->
                            path.getFileName()
                                    .toString()
                                    .endsWith(".json"))
                    .sorted()
                    .toList();
        }

        List<Map<String, Object>> groundTruth =
                new ArrayList<>();

        for (Path caseFile : caseFiles) {

            JsonNode source =
                    OBJECT_MAPPER.readTree(
                            caseFile.toFile()
                    );

            Map<String, Object> expected =
                    new LinkedHashMap<>();

            String stayId =
                    source.path("patient")
                            .path("patientunitstayid")
                            .asText();

            expected.put(
                    "patientUnitStayID",
                    stayId
            );

            /*
             * M1 – Körpergewicht
             */
            JsonNode admissionWeight =
                    source.path("patient")
                            .path("admissionweight");

            if (!admissionWeight.isMissingNode()
                    && !admissionWeight.asText().isBlank()) {

                expected.put(
                        "M1",
                        Double.parseDouble(
                                admissionWeight.asText()
                        )
                );

            } else {

                expected.put("M1", null);
            }

            /*
             * M2 – Diagnose
             *
             * Für eICU nicht bewertet, da die notwendige
             * Terminologieumwandlung außerhalb des
             * Untersuchungsumfangs liegt.
             */
            expected.put(
                    "M2",
                    "NOT_EVALUABLE"
            );

            /*
             * M3 – Medikationsinformation
             */
            boolean medicationPresent = false;

            for (JsonNode medication :
                    source.path("medication")) {

                String drugName =
                        medication.path("drugname")
                                .asText()
                                .trim();

                if (!drugName.isEmpty()) {
                    medicationPresent = true;
                    break;
                }
            }

            expected.put(
                    "M3",
                    medicationPresent
                            ? "Medikation liegt vor"
                            : null
            );

            /*
             * M4 – Schmerzsymptomatik
             *
             * Verwendet wird der zeitlich letzte
             * gültige Pain Score.
             */
            JsonNode lastPainScore = null;
            int greatestOffset = Integer.MIN_VALUE;

            for (JsonNode chartEntry :
                    source.path("nurseCharting")) {

                String name =
                        chartEntry
                                .path(
                                        "nursingchartcelltypevalname"
                                )
                                .asText()
                                .trim();

                if (!"Pain Score"
                        .equalsIgnoreCase(name)) {
                    continue;
                }

                String value =
                        chartEntry
                                .path("nursingchartvalue")
                                .asText()
                                .trim();

                if (!isPainScore(value)) {
                    continue;
                }

                int offset =
                        parseInteger(
                                chartEntry
                                        .path(
                                                "nursingchartoffset"
                                        )
                                        .asText()
                        );

                if (offset > greatestOffset) {

                    greatestOffset = offset;
                    lastPainScore = chartEntry;
                }
            }

            if (lastPainScore != null) {

                double painValue =
                        Double.parseDouble(
                                lastPainScore
                                        .path(
                                                "nursingchartvalue"
                                        )
                                        .asText()
                        );

                expected.put(
                        "M4",
                        painValue > 0
                                ? "22253000"
                                : null
                );

            } else {

                expected.put("M4", null);
            }

            /*
             * M5 – Allergien
             */
            LinkedHashSet<String> allergyNames =
                    new LinkedHashSet<>();

            for (JsonNode allergy :
                    source.path("allergy")) {

                String allergyName =
                        allergy.path("allergyname")
                                .asText()
                                .trim();

                if (!allergyName.isEmpty()) {
                    allergyNames.add(allergyName);
                }
            }

            expected.put(
                    "M5",
                    new ArrayList<>(allergyNames)
            );

            groundTruth.add(expected);
        }

        groundTruth.sort(
                Comparator.comparingLong(
                        row ->
                                Long.parseLong(
                                        row.get(
                                                "patientUnitStayID"
                                        ).toString()
                                )
                )
        );

        OBJECT_MAPPER.writeValue(
                OUTPUT_FILE.toFile(),
                groundTruth
        );

        System.out.println();
        System.out.println(
                "Entwicklungsfälle: "
                        + groundTruth.size()
        );

        System.out.println(
                "Bewertbare Mappingfälle: "
                        + (groundTruth.size() * 4)
        );

        System.out.println(
                "Ground Truth: "
                        + OUTPUT_FILE.toAbsolutePath()
        );
    }

    private static boolean isPainScore(
            String value
    ) {

        try {

            double number =
                    Double.parseDouble(value);

            return number >= 0
                    && number <= 10;

        } catch (NumberFormatException e) {

            return false;
        }
    }

    private static int parseInteger(
            String value
    ) {

        try {

            return Integer.parseInt(
                    value.trim()
            );

        } catch (NumberFormatException e) {

            return Integer.MIN_VALUE;
        }
    }
}