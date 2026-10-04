package de.johnherdling.piomapping;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

public class SyntheaEvaluationAnalyzer {

    private static final Path DATA_DIR = Path.of(
            "evaluation",
            "original",
            "synthea_sample_data_csv_latest"
    );

    /*
     * M4:
     * Nur die drei Beobachtungen werden berücksichtigt,
     * die unmittelbar die Schmerzintensität beschreiben.
     */
    private static final Set<String> PAIN_DESCRIPTIONS =
            Set.of(
                    "Pain severity - 0-10 verbal numeric rating [Score] - Reported",
                    "Pain severity - Reported",
                    "Pain severity in the past week - 0-10 numeric rating [Reported]"
            );

    /*
     * M5:
     * Dieser Synthea-Eintrag beschreibt lediglich
     * eine allgemeine allergische Disposition,
     * jedoch keinen konkreten Auslöser.
     */
    private static final String GENERIC_ALLERGY =
            "Allergic disposition (finding)";

    /*
     * Patient-001:
     * Wird ausschließlich für die aktuelle Kontrolle
     * der Allergieeinträge ausgegeben.
     */
    private static final String DEBUG_PATIENT_ID =
            "06476266-59b1-ded7-4138-3c7543fd4981";

    public static void main(String[] args)
            throws IOException {

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
         * Schnittmenge:
         * Nur Patient:innen, für die Daten für
         * alle fünf Mappingfälle vorhanden sind.
         */
        Set<String> intersection =
                new LinkedHashSet<>(m1Patients);

        intersection.retainAll(m2Patients);
        intersection.retainAll(m3Patients);
        intersection.retainAll(m4Patients);
        intersection.retainAll(m5Patients);

        System.out.println();
        System.out.println(
                "========================================"
        );
        System.out.println(
                "Synthea-Evaluationspopulation"
        );
        System.out.println(
                "========================================"
        );

        System.out.println(
                "M1 Körpergewicht: "
                        + m1Patients.size()
        );

        System.out.println(
                "M2 Diagnose: "
                        + m2Patients.size()
        );

        System.out.println(
                "M3 Medikation: "
                        + m3Patients.size()
        );

        System.out.println(
                "M4 Schmerzsymptomatik: "
                        + m4Patients.size()
        );

        System.out.println(
                "M5 konkreter Allergieauslöser: "
                        + m5Patients.size()
        );

        System.out.println(
                "----------------------------------------"
        );

        System.out.println(
                "Patient:innen mit Daten für M1-M5: "
                        + intersection.size()
        );

        System.out.println();
        System.out.println(
                "Patienten-IDs:"
        );

        for (String patientId : intersection) {
            System.out.println(patientId);
        }

        /*
         * Zusätzliche Kontrolle:
         * Alle Allergieeinträge von patient-001
         * werden vollständig ausgegeben.
         */
        debugPatientAllergies(
                DEBUG_PATIENT_ID
        );
    }

    /*
     * M1:
     * Mindestens eine Body-Weight-Beobachtung
     * in kg mit numerischem Wert.
     */
    private static Set<String> findM1Patients()
            throws IOException {

        Set<String> patients =
                new LinkedHashSet<>();

        Path file =
                DATA_DIR.resolve("observations.csv");

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
                        && "Body Weight".equals(description)
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
     * Mindestens eine der vorher festgelegten
     * direkten Pain-Severity-Beobachtungen.
     */
    private static Set<String> findM4Patients()
            throws IOException {

        Set<String> patients =
                new LinkedHashSet<>();

        Path file =
                DATA_DIR.resolve("observations.csv");

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
     * Mindestens ein Allergieeintrag mit einer
     * konkreten Bezeichnung.
     *
     * "Allergic disposition (finding)" wird
     * ausgeschlossen, da dieser Eintrag keinen
     * konkreten Allergieauslöser bezeichnet.
     */
    private static Set<String> findM5Patients()
            throws IOException {

        Set<String> patients =
                new LinkedHashSet<>();

        Path file =
                DATA_DIR.resolve("allergies.csv");

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
     * Hilfsmethode für M2 und M3:
     * Ein Patient wird aufgenommen, wenn alle
     * angegebenen Spalten einen Wert enthalten.
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

                for (String column : requiredColumns) {

                    if (value(record, column).isBlank()) {
                        complete = false;
                        break;
                    }
                }

                if (complete) {

                    patients.add(
                            value(record, "PATIENT")
                    );
                }
            }
        }

        return patients;
    }

    /*
     * Debug-Ausgabe:
     * Zeigt sämtliche Allergieeinträge des
     * angegebenen Patienten direkt aus
     * allergies.csv.
     */
    private static void debugPatientAllergies(
            String patientId
    ) throws IOException {

        Path file =
                DATA_DIR.resolve("allergies.csv");

        System.out.println();
        System.out.println(
                "========================================"
        );

        System.out.println(
                "Allergieeinträge für:"
        );

        System.out.println(patientId);

        System.out.println(
                "========================================"
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

            int count = 0;

            for (CSVRecord record : parser) {

                if (!patientId.equals(
                        value(record, "PATIENT"))) {

                    continue;
                }

                count++;

                String description =
                        value(record, "DESCRIPTION");

                boolean concrete =
                        !description.isBlank()
                                && !GENERIC_ALLERGY.equals(
                                description
                        );

                System.out.println();

                System.out.println(
                        "Eintrag " + count
                );

                System.out.println(
                        "CODE: "
                                + value(record, "CODE")
                );

                System.out.println(
                        "SYSTEM: "
                                + value(record, "SYSTEM")
                );

                System.out.println(
                        "DESCRIPTION: "
                                + description
                );

                System.out.println(
                        "TYPE: "
                                + value(record, "TYPE")
                );

                System.out.println(
                        "CATEGORY: "
                                + value(record, "CATEGORY")
                );

                System.out.println(
                        "REACTION1: "
                                + value(record, "REACTION1")
                );

                System.out.println(
                        "DESCRIPTION1: "
                                + value(record, "DESCRIPTION1")
                );

                System.out.println(
                        "SEVERITY1: "
                                + value(record, "SEVERITY1")
                );

                System.out.println(
                        "REACTION2: "
                                + value(record, "REACTION2")
                );

                System.out.println(
                        "DESCRIPTION2: "
                                + value(record, "DESCRIPTION2")
                );

                System.out.println(
                        "SEVERITY2: "
                                + value(record, "SEVERITY2")
                );

                System.out.println(
                        "Als konkreter M5-Eintrag gewertet: "
                                + concrete
                );
            }

            System.out.println();
            System.out.println(
                    "Anzahl Allergieeinträge: "
                            + count
            );
        }
    }

    /*
     * CSV-Datei mit Header einlesen.
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
     * Sicheres Auslesen eines CSV-Feldes.
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