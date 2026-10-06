package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class EicuEvaluationDataPreparer {

    private static final Path EICU_DIR =
            Paths.get("data", "eicu-collaborative-research-database-demo-2.0.1");

    private static final Path OUTPUT_DIR =
            Paths.get("data", "eicu-evaluation");

    private static final Path CASES_DIR =
            OUTPUT_DIR.resolve("cases");

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static void main(String[] args) throws Exception {

        Files.createDirectories(CASES_DIR);

        Map<String, Map<String, String>> patients =
                readSingleRows(EICU_DIR.resolve("patient.csv.gz"));

        Map<String, List<Map<String, String>>> medications =
                readMultipleRows(EICU_DIR.resolve("medication.csv.gz"));

        Map<String, List<Map<String, String>>> nurseCharting =
                readMultipleRows(EICU_DIR.resolve("nurseCharting.csv.gz"));

        Map<String, List<Map<String, String>>> allergies =
                readMultipleRows(EICU_DIR.resolve("allergy.csv.gz"));

        Map<String, List<Map<String, String>>> diagnoses =
                readMultipleRows(EICU_DIR.resolve("diagnosis.csv.gz"));

        List<Map<String, Object>> groundTruth = new ArrayList<>();

        List<String> eligibleIds = new ArrayList<>();

        for (Map.Entry<String, Map<String, String>> entry : patients.entrySet()) {

            String stayId = entry.getKey();
            Map<String, String> patient = entry.getValue();

            String admissionWeight = patient.get("admissionweight");

            List<Map<String, String>> medicationRows =
                    medications.getOrDefault(stayId, List.of());

            List<Map<String, String>> chartRows =
                    nurseCharting.getOrDefault(stayId, List.of());

            List<Map<String, String>> allergyRows =
                    allergies.getOrDefault(stayId, List.of());

            Optional<Map<String, String>> lastPainScore =
                    findLastValidPainScore(chartRows);

            List<String> allergyNames =
                    extractUniqueAllergyNames(allergyRows);

            boolean validWeight =
                    isNumeric(admissionWeight);

            boolean validMedication =
                    medicationRows.stream()
                            .map(row -> row.get("drugname"))
                            .anyMatch(EicuEvaluationDataPreparer::hasText);

            boolean validPain =
                    lastPainScore.isPresent();

            boolean validAllergy =
                    !allergyNames.isEmpty();

            if (!(validWeight
                    && validMedication
                    && validPain
                    && validAllergy)) {
                continue;
            }

            eligibleIds.add(stayId);

            /*
             * Evaluationsinput:
             * Originale Tabellen- und Feldbezeichnungen bleiben erhalten.
             * Es erfolgt keine semantische Umbenennung.
             */
            Map<String, Object> evaluationCase =
                    new LinkedHashMap<>();

            evaluationCase.put("patient", patient);
            evaluationCase.put("medication", medicationRows);
            evaluationCase.put("nurseCharting", chartRows);
            evaluationCase.put("allergy", allergyRows);
            evaluationCase.put(
                    "diagnosis",
                    diagnoses.getOrDefault(stayId, List.of())
            );

            Path caseFile =
                    CASES_DIR.resolve("eicu-" + stayId + ".json");

            OBJECT_MAPPER.writeValue(
                    caseFile.toFile(),
                    evaluationCase
            );

            /*
             * Ground Truth wird unabhängig vom Matching erstellt.
             */
            Map<String, Object> expected =
                    new LinkedHashMap<>();

            expected.put(
                    "patientUnitStayID",
                    stayId
            );

            expected.put(
                    "M1",
                    Double.parseDouble(admissionWeight)
            );

            expected.put(
                    "M2",
                    "NOT_EVALUABLE"
            );

            expected.put(
                    "M3",
                    "Medikation liegt vor"
            );

            double painValue =
                    Double.parseDouble(
                            lastPainScore.get()
                                    .get("nursingchartvalue")
                    );

            if (painValue > 0) {
                expected.put(
                        "M4",
                        "22253000"
                );
            } else {
                expected.put(
                        "M4",
                        null
                );
            }

            expected.put(
                    "M5",
                    allergyNames
            );

            groundTruth.add(expected);
        }

        eligibleIds.sort(
                Comparator.comparingLong(Long::parseLong)
        );

        groundTruth.sort(
                Comparator.comparingLong(
                        row -> Long.parseLong(
                                row.get("patientUnitStayID").toString()
                        )
                )
        );

        OBJECT_MAPPER.writeValue(
                OUTPUT_DIR.resolve("ground-truth.json").toFile(),
                groundTruth
        );

        OBJECT_MAPPER.writeValue(
                OUTPUT_DIR.resolve("eligible-stay-ids.json").toFile(),
                eligibleIds
        );

        System.out.println();
        System.out.println(
                "Geeignete eICU-Fälle: "
                        + eligibleIds.size()
        );

        System.out.println(
                "Bewertbare Transformationen: "
                        + (eligibleIds.size() * 4)
        );

        System.out.println(
                "JSON-Fälle: "
                        + CASES_DIR.toAbsolutePath()
        );

        System.out.println(
                "Ground Truth: "
                        + OUTPUT_DIR
                        .resolve("ground-truth.json")
                        .toAbsolutePath()
        );
    }

    private static Map<String, Map<String, String>>
    readSingleRows(Path file) throws IOException {

        Map<String, Map<String, String>> result =
                new LinkedHashMap<>();

        for (Map<String, String> row : readCsv(file)) {

            String stayId =
                    row.get("patientunitstayid");

            if (hasText(stayId)) {
                result.put(stayId, row);
            }
        }

        return result;
    }

    private static Map<String, List<Map<String, String>>>
    readMultipleRows(Path file) throws IOException {

        Map<String, List<Map<String, String>>> result =
                new LinkedHashMap<>();

        for (Map<String, String> row : readCsv(file)) {

            String stayId =
                    row.get("patientunitstayid");

            if (!hasText(stayId)) {
                continue;
            }

            result.computeIfAbsent(
                    stayId,
                    key -> new ArrayList<>()
            ).add(row);
        }

        return result;
    }

    private static List<Map<String, String>>
    readCsv(Path file) throws IOException {

        List<Map<String, String>> rows =
                new ArrayList<>();

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        new GZIPInputStream(
                                                Files.newInputStream(file)
                                        ),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String headerLine =
                    reader.readLine();

            if (headerLine == null) {
                return rows;
            }

            List<String> headers =
                    parseCsvLine(headerLine);

            String line;

            while ((line = reader.readLine()) != null) {

                List<String> values =
                        parseCsvLine(line);

                Map<String, String> row =
                        new LinkedHashMap<>();

                for (int i = 0; i < headers.size(); i++) {

                    String value =
                            i < values.size()
                                    ? values.get(i)
                                    : "";

                    row.put(
                            headers.get(i),
                            value
                    );
                }

                rows.add(row);
            }
        }

        return rows;
    }

    private static Optional<Map<String, String>>
    findLastValidPainScore(
            List<Map<String, String>> rows) {

        return rows.stream()

                .filter(
                        row ->
                                "Pain Score".equalsIgnoreCase(
                                        row.getOrDefault(
                                                "nursingchartcelltypevalname",
                                                ""
                                        ).trim()
                                )
                )

                .filter(
                        row ->
                                isPainScore(
                                        row.get(
                                                "nursingchartvalue"
                                        )
                                )
                )

                .max(
                        Comparator.comparingInt(
                                row ->
                                        parseInteger(
                                                row.get(
                                                        "nursingchartoffset"
                                                )
                                        )
                        )
                );
    }

    private static List<String>
    extractUniqueAllergyNames(
            List<Map<String, String>> rows) {

        LinkedHashSet<String> names =
                new LinkedHashSet<>();

        for (Map<String, String> row : rows) {

            String name =
                    row.get("allergyname");

            if (hasText(name)) {
                names.add(name.trim());
            }
        }

        return new ArrayList<>(names);
    }

    private static boolean isPainScore(String value) {

        if (!isNumeric(value)) {
            return false;
        }

        double number =
                Double.parseDouble(value);

        return number >= 0
                && number <= 10;
    }

    private static boolean isNumeric(String value) {

        if (!hasText(value)) {
            return false;
        }

        try {
            Double.parseDouble(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static int parseInteger(String value) {

        if (!hasText(value)) {
            return Integer.MIN_VALUE;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return Integer.MIN_VALUE;
        }
    }

    private static boolean hasText(String value) {

        return value != null
                && !value.trim().isEmpty();
    }

    private static List<String>
    parseCsvLine(String line) {

        List<String> values =
                new ArrayList<>();

        StringBuilder current =
                new StringBuilder();

        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {

            char c = line.charAt(i);

            if (c == '"') {

                if (inQuotes
                        && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {

                    current.append('"');
                    i++;

                } else {
                    inQuotes = !inQuotes;
                }

            } else if (c == ',' && !inQuotes) {

                values.add(
                        current.toString()
                );

                current.setLength(0);

            } else {

                current.append(c);
            }
        }

        values.add(
                current.toString()
        );

        return values;
    }
}