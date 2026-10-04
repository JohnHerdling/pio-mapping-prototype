package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class SyntheaGroundTruthGenerator {

    private static final Path INPUT_DIR = Path.of(
            "evaluation",
            "input"
    );

    private static final Path EXPECTED_DIR = Path.of(
            "evaluation",
            "expected"
    );

    /*
     * Dieser Synthea-Eintrag beschreibt lediglich
     * eine allgemeine allergische Disposition und
     * keinen konkreten Allergieauslöser.
     */
    private static final String GENERIC_ALLERGY =
            "Allergic disposition (finding)";

    private static final ObjectMapper MAPPER =
            new ObjectMapper()
                    .enable(
                            SerializationFeature.INDENT_OUTPUT
                    );

    public static void main(String[] args)
            throws IOException {

        System.out.println(
                "Synthea-Ground-Truth-Generator"
        );

        System.out.println(
                "========================================"
        );

        Files.createDirectories(EXPECTED_DIR);

        /*
         * Alte Ground-Truth-Dateien entfernen.
         * Andere Dateien im Ordner bleiben erhalten.
         */
        try (Stream<Path> files =
                     Files.list(EXPECTED_DIR)) {

            files.filter(
                            path ->
                                    path.getFileName()
                                            .toString()
                                            .matches(
                                                    "expected-patient-\\d{3}\\.json"
                                            )
                    )
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException(
                                    "Datei konnte nicht gelöscht werden: "
                                            + path,
                                    e
                            );
                        }
                    });
        }

        /*
         * Vorhandene patient-*.json-Dateien
         * automatisch bestimmen.
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

        if (inputFiles.isEmpty()) {
            throw new IllegalStateException(
                    "Keine Evaluationsdateien gefunden."
            );
        }

        int generated = 0;

        for (Path inputFile : inputFiles) {

            JsonNode root =
                    MAPPER.readTree(
                            inputFile.toFile()
                    );

            Map<String, Object> expected =
                    createGroundTruth(root);

            String inputName =
                    inputFile
                            .getFileName()
                            .toString();

            String outputName =
                    "expected-" + inputName;

            Path outputFile =
                    EXPECTED_DIR.resolve(
                            outputName
                    );

            MAPPER.writeValue(
                    outputFile.toFile(),
                    expected
            );

            System.out.println(
                    "[OK] " + outputName
            );

            generated++;
        }

        System.out.println();
        System.out.println(
                "========================================"
        );

        System.out.println(
                "Ground Truth erzeugt: "
                        + generated
                        + " Dateien"
        );

        System.out.println(
                "========================================"
        );
    }

    private static Map<String, Object>
    createGroundTruth(JsonNode root) {

        Map<String, Object> expected =
                new LinkedHashMap<>();

        expected.put(
                "M1",
                createM1(root)
        );

        expected.put(
                "M2",
                createM2()
        );

        expected.put(
                "M3",
                createM3(root)
        );

        expected.put(
                "M4",
                createM4(root)
        );

        expected.put(
                "M5",
                createM5(root)
        );

        return expected;
    }

    /*
     * ==================================================
     * M1 – Körpergewicht
     * ==================================================
     *
     * Erwartet wird der VALUE der zeitlich jüngsten
     * geeigneten Body-Weight-Beobachtung.
     */
    private static Map<String, Object>
    createM1(JsonNode root) {

        List<JsonNode> candidates =
                new ArrayList<>();

        JsonNode observations =
                root.path("observations");

        for (JsonNode observation :
                observations) {

            if ("Body Weight".equals(
                    observation
                            .path("DESCRIPTION")
                            .asText()
            )
                    && "kg".equals(
                    observation
                            .path("UNITS")
                            .asText()
            )
                    && "numeric".equals(
                    observation
                            .path("TYPE")
                            .asText()
            )
                    && !observation
                    .path("VALUE")
                    .asText()
                    .isBlank()) {

                candidates.add(
                        observation
                );
            }
        }

        JsonNode latest =
                candidates.stream()
                        .max(
                                Comparator.comparing(
                                        node ->
                                                parseInstant(
                                                        node.path(
                                                                        "DATE"
                                                                )
                                                                .asText()
                                                )
                                )
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Keine geeignete "
                                                        + "M1-Beobachtung gefunden."
                                        )
                        );

        double value =
                Double.parseDouble(
                        latest.path("VALUE")
                                .asText()
                );

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put(
                "pioElement",
                "2.11.1.6.2 Wert"
        );

        result.put(
                "target",
                "valueQuantity.value"
        );

        result.put(
                "expectedValue",
                value
        );

        return result;
    }

    /*
     * ==================================================
     * M2 – Diagnose
     * ==================================================
     *
     * Der Mappingfall ist mit den externen
     * Synthea-Daten nicht direkt evaluierbar.
     *
     * Die Quelle verwendet für die betrachteten
     * Diagnosen ein anderes Codesystem als das im
     * Prototyp festgelegte Ziel ICD-10-GM.
     *
     * Eine Terminologieabbildung ist nicht Bestandteil
     * des Prototyps und wird für die externe Evaluation
     * nicht nachträglich ergänzt.
     */
    private static Map<String, Object>
    createM2() {

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put(
                "pioElement",
                "2.1.2.2.1.1.1 Diagnosecode"
        );

        result.put(
                "target",
                "code.coding:ICD-10-GM.code"
        );

        result.put(
                "status",
                "NOT_EVALUABLE"
        );

        result.put(
                "reason",
                "Externes Diagnosencodesystem stimmt "
                        + "nicht mit dem im Prototyp "
                        + "festgelegten Zielcodesystem "
                        + "ICD-10-GM überein."
        );

        return result;
    }

    /*
     * ==================================================
     * M3 – Medikationsinformation
     * ==================================================
     *
     * Bezugszeitpunkt:
     * jüngster dokumentierter Zeitpunkt innerhalb
     * des jeweiligen Patientendatensatzes.
     *
     * Eine Medikation gilt als aktiv, wenn:
     *
     * START <= Bezugszeitpunkt
     *
     * und
     *
     * STOP leer ist
     * oder
     * STOP >= Bezugszeitpunkt.
     */
    private static Map<String, Object>
    createM3(JsonNode root) {

        Instant referenceTime =
                findLatestDocumentedTime(
                        root
                );

        boolean activeMedication =
                false;

        JsonNode medications =
                root.path("medications");

        for (JsonNode medication :
                medications) {

            Instant start =
                    parseInstantNullable(
                            medication
                                    .path("START")
                                    .asText()
                    );

            Instant stop =
                    parseInstantNullable(
                            medication
                                    .path("STOP")
                                    .asText()
                    );

            if (start == null) {
                continue;
            }

            boolean started =
                    !start.isAfter(
                            referenceTime
                    );

            boolean notStopped =
                    stop == null
                            || !stop.isBefore(
                            referenceTime
                    );

            if (started && notStopped) {

                activeMedication =
                        true;

                break;
            }
        }

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put(
                "pioElement",
                "2.9.1 Information zur Einnahme von Medikamenten"
        );

        result.put(
                "target",
                "valueCodeableConcept"
        );

        if (activeMedication) {

            result.put(
                    "expectedValue",
                    "Medikation liegt vor"
            );

        } else {

            result.put(
                    "expectedValue",
                    null
            );
        }

        return result;
    }

    /*
     * ==================================================
     * M4 – Schmerzsymptomatik
     * ==================================================
     *
     * Es wird die zeitlich jüngste geeignete
     * Pain-Severity-Beobachtung verwendet.
     *
     * Ein Wert > 0 bedeutet, dass eine
     * Schmerzsymptomatik vorliegt.
     *
     * Der erwartete Zielwert ist der bereits für
     * M4 festgelegte statische Zielcode 22253000.
     */
    private static Map<String, Object>
    createM4(JsonNode root) {

        List<JsonNode> candidates =
                new ArrayList<>();

        JsonNode observations =
                root.path("observations");

        for (JsonNode observation :
                observations) {

            String description =
                    observation
                            .path("DESCRIPTION")
                            .asText();

            boolean suitable =
                    "Pain severity - 0-10 verbal numeric rating [Score] - Reported"
                            .equals(description)
                    || "Pain severity - Reported"
                            .equals(description)
                    || "Pain severity in the past week - 0-10 numeric rating [Reported]"
                            .equals(description);

            if (suitable
                    && !observation
                    .path("VALUE")
                    .asText()
                    .isBlank()) {

                candidates.add(
                        observation
                );
            }
        }

        JsonNode latest =
                candidates.stream()
                        .max(
                                Comparator.comparing(
                                        node ->
                                                parseInstant(
                                                        node.path(
                                                                        "DATE"
                                                                )
                                                                .asText()
                                                )
                                )
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Keine geeignete "
                                                        + "M4-Beobachtung gefunden."
                                        )
                        );

        double painValue =
                Double.parseDouble(
                        latest
                                .path("VALUE")
                                .asText()
                );

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put(
                "pioElement",
                "2.3.1 Vorliegen einer Schmerzsymptomatik"
        );

        result.put(
                "target",
                "value[x]:valueCodeableConcept"
        );

        if (painValue > 0) {

            result.put(
                    "expectedValue",
                    "22253000"
            );

        } else {

            result.put(
                    "expectedValue",
                    null
            );
        }

        return result;
    }

    /*
     * ==================================================
     * M5 – Allergien / Unverträglichkeiten
     * ==================================================
     *
     * Alle konkreten Allergiebezeichnungen werden
     * als erwartete Wiederholungen übernommen.
     *
     * Der allgemeine Eintrag
     * "Allergic disposition (finding)"
     * wird nicht als konkreter Allergieauslöser
     * gewertet.
     */
    private static Map<String, Object>
    createM5(JsonNode root) {

        List<String> allergies =
                new ArrayList<>();

        JsonNode records =
                root.path("allergies");

        for (JsonNode allergy :
                records) {

            String description =
                    allergy
                            .path("DESCRIPTION")
                            .asText()
                            .trim();

            if (description.isBlank()) {
                continue;
            }

            if (GENERIC_ALLERGY.equals(
                    description
            )) {
                continue;
            }

            allergies.add(
                    description
            );
        }

        if (allergies.isEmpty()) {

            throw new IllegalStateException(
                    "Kein konkreter Allergieauslöser "
                            + "für M5 gefunden."
            );
        }

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put(
                "pioElement",
                "2.8.2.2.2.2 Freitext Anzeigename"
        );

        result.put(
                "target",
                "code.text"
        );

        result.put(
                "expectedValue",
                allergies
        );

        return result;
    }

    /*
     * ==================================================
     * Bezugszeitpunkt für M3
     * ==================================================
     */
    private static Instant
    findLatestDocumentedTime(
            JsonNode root
    ) {

        Instant latest =
                null;

        /*
         * observations.csv:
         * DATE
         */
        for (JsonNode observation :
                root.path("observations")) {

            latest =
                    laterOf(
                            latest,
                            parseInstantNullable(
                                    observation
                                            .path("DATE")
                                            .asText()
                            )
                    );
        }

        /*
         * conditions.csv,
         * medications.csv,
         * allergies.csv:
         * START und STOP
         */
        String[] sections = {
                "conditions",
                "medications",
                "allergies"
        };

        for (String section :
                sections) {

            for (JsonNode record :
                    root.path(section)) {

                latest =
                        laterOf(
                                latest,
                                parseInstantNullable(
                                        record
                                                .path("START")
                                                .asText()
                                )
                        );

                latest =
                        laterOf(
                                latest,
                                parseInstantNullable(
                                        record
                                                .path("STOP")
                                                .asText()
                                )
                        );
            }
        }

        if (latest == null) {

            throw new IllegalStateException(
                    "Kein Bezugszeitpunkt bestimmbar."
            );
        }

        return latest;
    }

    private static Instant laterOf(
            Instant first,
            Instant second
    ) {

        if (second == null) {
            return first;
        }

        if (first == null
                || second.isAfter(first)) {

            return second;
        }

        return first;
    }

    private static Instant parseInstant(
            String value
    ) {

        Instant parsed =
                parseInstantNullable(
                        value
                );

        if (parsed == null) {

            throw new IllegalStateException(
                    "Ungültiger Zeitpunkt: "
                            + value
            );
        }

        return parsed;
    }

    private static Instant
    parseInstantNullable(
            String value
    ) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        try {

            return Instant.parse(
                    value
            );

        } catch (Exception e) {

            return null;
        }
    }
}