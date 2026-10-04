package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SyntheaEvaluationExporter {

    private static final Path DATA_DIR = Path.of(
            "evaluation",
            "original",
            "synthea_sample_data_csv_latest"
    );

    private static final Path OUTPUT_DIR = Path.of(
            "evaluation",
            "input"
    );

    /*
     * Für M4 werden nur Beobachtungen berücksichtigt,
     * die unmittelbar die Schmerzintensität beschreiben.
     */
    private static final Set<String> PAIN_DESCRIPTIONS =
            Set.of(
                    "Pain severity - 0-10 verbal numeric rating [Score] - Reported",
                    "Pain severity - Reported",
                    "Pain severity in the past week - 0-10 numeric rating [Reported]"
            );

    /*
     * Dieser Eintrag beschreibt lediglich eine
     * allgemeine allergische Disposition und keinen
     * konkreten Allergieauslöser.
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
                "Synthea-Evaluationsexport"
        );

        System.out.println(
                "========================================"
        );

        /*
         * 1. Geeignete Patient:innen für jeden
         * Mappingfall bestimmen.
         */
        Set<String> m1Patients =
                findM1Patients();

        Set<String> m2Patients =
                findPatientsWithRequiredValues(
                        DATA_DIR.resolve("conditions.csv"),
                        "PATIENT",
                        "CODE",
                        "DESCRIPTION"
                );

        Set<String> m3Patients =
                findPatientsWithRequiredValues(
                        DATA_DIR.resolve("medications.csv"),
                        "PATIENT",
                        "START",
                        "CODE",
                        "DESCRIPTION"
                );

        Set<String> m4Patients =
                findM4Patients();

        Set<String> m5Patients =
                findM5Patients();

        /*
         * 2. Schnittmenge bilden.
         */
        Set<String> evaluationPatients =
                new LinkedHashSet<>(m1Patients);

        evaluationPatients.retainAll(m2Patients);
        evaluationPatients.retainAll(m3Patients);
        evaluationPatients.retainAll(m4Patients);
        evaluationPatients.retainAll(m5Patients);

        System.out.println(
                "Geeignete Patient:innen: "
                        + evaluationPatients.size()
        );

        if (evaluationPatients.isEmpty()) {
            throw new IllegalStateException(
                    "Keine geeigneten Evaluationspatient:innen gefunden."
            );
        }

        /*
         * 3. Originaltabellen vollständig einlesen.
         *
         * Die Feldnamen und Werte werden unverändert
         * aus Synthea übernommen.
         */
        List<Map<String, String>> observations =
                readCsv(
                        DATA_DIR.resolve(
                                "observations.csv"
                        )
                );

        List<Map<String, String>> conditions =
                readCsv(
                        DATA_DIR.resolve(
                                "conditions.csv"
                        )
                );

        List<Map<String, String>> medications =
                readCsv(
                        DATA_DIR.resolve(
                                "medications.csv"
                        )
                );

        List<Map<String, String>> allergies =
                readCsv(
                        DATA_DIR.resolve(
                                "allergies.csv"
                        )
                );

        /*
         * 4. Alten Export entfernen.
         *
         * Es werden ausschließlich patient-*.json
         * gelöscht. Andere Dateien bleiben erhalten.
         */
        Files.createDirectories(OUTPUT_DIR);

        try (var files = Files.list(OUTPUT_DIR)) {

            files.filter(
                            path ->
                                    path.getFileName()
                                            .toString()
                                            .matches(
                                                    "patient-\\d{3}\\.json"
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
         * 5. Für jede geeignete Person einen
         * semantisch unveränderten JSON-Datensatz
         * erzeugen.
         */
        int number = 1;

        for (String patientId :
                evaluationPatients) {

            Map<String, Object> patientData =
                    new LinkedHashMap<>();

            patientData.put(
                    "observations",
                    filterByPatient(
                            observations,
                            patientId
                    )
            );

            patientData.put(
                    "conditions",
                    filterByPatient(
                            conditions,
                            patientId
                    )
            );

            patientData.put(
                    "medications",
                    filterByPatient(
                            medications,
                            patientId
                    )
            );

            /*
             * WICHTIG:
             * Hier werden alle originalen
             * Allergieeinträge des Patienten
             * exportiert.
             *
             * "Allergic disposition (finding)"
             * wird NICHT aus den Quelldaten entfernt.
             *
             * Der Ausschluss betrifft nur die
             * Auswahl der Evaluationspopulation bzw.
             * später die Ground Truth.
             *
             * Dadurch bleiben die externen
             * Quelldaten semantisch unverändert.
             */
            patientData.put(
                    "allergies",
                    filterByPatient(
                            allergies,
                            patientId
                    )
            );

            String fileName =
                    String.format(
                            "patient-%03d.json",
                            number
                    );

            Path outputFile =
                    OUTPUT_DIR.resolve(fileName);

            MAPPER.writeValue(
                    outputFile.toFile(),
                    patientData
            );

            System.out.println(
                    String.format(
                            "[OK] %s <- %s",
                            fileName,
                            patientId
                    )
            );

            number++;
        }

        System.out.println();
        System.out.println(
                "========================================"
        );

        System.out.println(
                "Export abgeschlossen: "
                        + evaluationPatients.size()
                        + " JSON-Dateien"
        );

        System.out.println(
                "========================================"
        );
    }

    /*
     * M1:
     * Mindestens eine geeignete
     * Body-Weight-Beobachtung.
     */
    private static Set<String> findM1Patients()
            throws IOException {

        Set<String> patients =
                new LinkedHashSet<>();

        Path file =
                DATA_DIR.resolve(
                        "observations.csv"
                );

        try (
                Reader reader =
                        Files.newBufferedReader(
                                file,
                                StandardCharsets.UTF_8
                        );

                CSVParser parser =
                        createParser(reader)
        ) {

            for (CSVRecord record : parser) {

                String patient =
                        value(record, "PATIENT");

                String description =
                        value(record, "DESCRIPTION");

                String units =
                        value(record, "UNITS");

                String type =
                        value(record, "TYPE");

                String observationValue =
                        value(record, "VALUE");

                if (!patient.isBlank()
                        && "Body Weight".equals(
                        description
                )
                        && "kg".equals(units)
                        && "numeric".equals(type)
                        && !observationValue.isBlank()) {

                    patients.add(patient);
                }
            }
        }

        return patients;
    }

    /*
     * M4:
     * Mindestens eine direkte
     * Pain-Severity-Beobachtung.
     */
    private static Set<String> findM4Patients()
            throws IOException {

        Set<String> patients =
                new LinkedHashSet<>();

        Path file =
                DATA_DIR.resolve(
                        "observations.csv"
                );

        try (
                Reader reader =
                        Files.newBufferedReader(
                                file,
                                StandardCharsets.UTF_8
                        );

                CSVParser parser =
                        createParser(reader)
        ) {

            for (CSVRecord record : parser) {

                String patient =
                        value(record, "PATIENT");

                String description =
                        value(record, "DESCRIPTION");

                String observationValue =
                        value(record, "VALUE");

                if (!patient.isBlank()
                        && PAIN_DESCRIPTIONS.contains(
                        description
                )
                        && !observationValue.isBlank()) {

                    patients.add(patient);
                }
            }
        }

        return patients;
    }

    /*
     * M5:
     * Mindestens ein konkreter Allergieeintrag.
     *
     * Der allgemeine Eintrag
     * "Allergic disposition (finding)"
     * genügt allein nicht für die Aufnahme.
     */
    private static Set<String> findM5Patients()
            throws IOException {

        Set<String> patients =
                new LinkedHashSet<>();

        Path file =
                DATA_DIR.resolve(
                        "allergies.csv"
                );

        try (
                Reader reader =
                        Files.newBufferedReader(
                                file,
                                StandardCharsets.UTF_8
                        );

                CSVParser parser =
                        createParser(reader)
        ) {

            for (CSVRecord record : parser) {

                String patient =
                        value(record, "PATIENT");

                String code =
                        value(record, "CODE");

                String description =
                        value(record, "DESCRIPTION");

                if (!patient.isBlank()
                        && !code.isBlank()
                        && !description.isBlank()
                        && !GENERIC_ALLERGY.equals(
                        description
                )) {

                    patients.add(patient);
                }
            }
        }

        return patients;
    }

    /*
     * Hilfsmethode für M2 und M3.
     */
    private static Set<String>
    findPatientsWithRequiredValues(
            Path file,
            String... requiredColumns
    ) throws IOException {

        Set<String> patients =
                new LinkedHashSet<>();

        try (
                Reader reader =
                        Files.newBufferedReader(
                                file,
                                StandardCharsets.UTF_8
                        );

                CSVParser parser =
                        createParser(reader)
        ) {

            for (CSVRecord record : parser) {

                boolean complete = true;

                for (String column :
                        requiredColumns) {

                    if (value(
                            record,
                            column
                    ).isBlank()) {

                        complete = false;
                        break;
                    }
                }

                if (complete) {

                    patients.add(
                            value(
                                    record,
                                    "PATIENT"
                            )
                    );
                }
            }
        }

        return patients;
    }

    /*
     * CSV-Datei vollständig einlesen.
     *
     * Header und Werte werden so übernommen,
     * wie sie in Synthea vorliegen.
     */
    private static List<Map<String, String>>
    readCsv(Path file)
            throws IOException {

        List<Map<String, String>> rows =
                new ArrayList<>();

        try (
                Reader reader =
                        Files.newBufferedReader(
                                file,
                                StandardCharsets.UTF_8
                        );

                CSVParser parser =
                        createParser(reader)
        ) {

            List<String> headers =
                    parser.getHeaderNames();

            for (CSVRecord record : parser) {

                Map<String, String> row =
                        new LinkedHashMap<>();

                for (String header : headers) {

                    row.put(
                            header,
                            value(
                                    record,
                                    header
                            )
                    );
                }

                rows.add(row);
            }
        }

        return rows;
    }

    /*
     * Datensätze einer bestimmten Person
     * auswählen.
     */
    private static List<Map<String, String>>
    filterByPatient(
            List<Map<String, String>> rows,
            String patientId
    ) {

        List<Map<String, String>> result =
                new ArrayList<>();

        for (Map<String, String> row :
                rows) {

            if (patientId.equals(
                    row.get("PATIENT")
            )) {

                result.add(row);
            }
        }

        return result;
    }

    /*
     * Apache Commons CSV Parser.
     */
    private static CSVParser createParser(
            Reader reader
    ) throws IOException {

        return CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .get()
                .parse(reader);
    }

    /*
     * Sicheres Auslesen eines CSV-Wertes.
     */
    private static String value(
            CSVRecord record,
            String column
    ) {

        if (!record.isMapped(column)) {
            return "";
        }

        String value =
                record.get(column);

        return value == null
                ? ""
                : value.trim();
    }
}