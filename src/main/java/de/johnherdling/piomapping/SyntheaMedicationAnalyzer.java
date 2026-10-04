package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

public class SyntheaMedicationAnalyzer {

    private static final Path SYNTHEA_DIR = Path.of(
            "evaluation",
            "original",
            "synthea_sample_data_csv_latest"
    );

    private static final Path INPUT_DIR = Path.of(
            "evaluation",
            "input"
    );

    public static void main(String[] args) throws IOException {

        System.out.println("M3 - Analyse der Medikationszeiträume");
        System.out.println("-------------------------------------");

        Map<String, String> patientLabels =
                readEvaluationPatients();

        List<Medication> medications =
                readMedications();

        int patientsWithActiveMedication = 0;
        int patientsWithoutActiveMedication = 0;

        for (Map.Entry<String, String> entry :
                patientLabels.entrySet()) {

            String patientId = entry.getKey();
            String label = entry.getValue();

            /*
             * Bezugszeitpunkt:
             * jüngster dokumentierter Zeitpunkt innerhalb
             * des exportierten Patientendatensatzes.
             */
            Instant referenceTime =
                    findLatestDocumentedTime(label);

            List<Medication> patientMedications =
                    medications.stream()
                            .filter(m ->
                                    m.patientId.equals(patientId))
                            .toList();

            List<Medication> activeMedications =
                    patientMedications.stream()
                            .filter(m ->
                                    isActiveAt(
                                            m,
                                            referenceTime
                                    ))
                            .toList();

            System.out.println();
            System.out.println(label);
            System.out.println(
                    "Patient-ID: " + patientId
            );
            System.out.println(
                    "Bezugszeitpunkt: " + referenceTime
            );
            System.out.println(
                    "Medikationen insgesamt: "
                            + patientMedications.size()
            );
            System.out.println(
                    "Aktive Medikationen am Bezugszeitpunkt: "
                            + activeMedications.size()
            );

            if (!activeMedications.isEmpty()) {

                patientsWithActiveMedication++;

                System.out.println(
                        "Ground-Truth-Kandidat M3: "
                                + "Medikation liegt vor"
                );

                System.out.println(
                        "Aktive Medikationen:"
                );

                for (Medication medication :
                        activeMedications) {

                    System.out.println(
                            "  - "
                                    + medication.description
                                    + " | START="
                                    + medication.start
                                    + " | STOP="
                                    + (
                                    medication.stop == null
                                            ? "<leer>"
                                            : medication.stop
                            )
                    );
                }

            } else {

                patientsWithoutActiveMedication++;

                System.out.println(
                        "Ground-Truth-Kandidat M3: "
                                + "keine aktive Medikation"
                );
            }
        }

        System.out.println();
        System.out.println("====================================");
        System.out.println("Zusammenfassung");
        System.out.println("====================================");

        System.out.println(
                "Evaluationspatient:innen: "
                        + patientLabels.size()
        );

        System.out.println(
                "Mit aktiver Medikation: "
                        + patientsWithActiveMedication
        );

        System.out.println(
                "Ohne aktive Medikation: "
                        + patientsWithoutActiveMedication
        );
    }

    private static Map<String, String>
    readEvaluationPatients() throws IOException {

        Map<String, String> patients =
                new LinkedHashMap<>();

        ObjectMapper mapper = new ObjectMapper();

        for (int i = 1; i <= 21; i++) {

            String fileName =
                    String.format(
                            "patient-%03d.json",
                            i
                    );

            Path file =
                    INPUT_DIR.resolve(fileName);

            JsonNode root =
                    mapper.readTree(file.toFile());

            String patientId =
                    findPatientId(root);

            if (patientId == null) {
                throw new IllegalStateException(
                        "Keine PATIENT-ID in "
                                + fileName
                                + " gefunden."
                );
            }

            patients.put(
                    patientId,
                    String.format(
                            "patient-%03d",
                            i
                    )
            );
        }

        return patients;
    }

    private static String findPatientId(
            JsonNode root
    ) {

        String[] sections = {
                "observations",
                "conditions",
                "medications",
                "allergies"
        };

        for (String section : sections) {

            JsonNode records =
                    root.get(section);

            if (records == null
                    || !records.isArray()) {
                continue;
            }

            for (JsonNode record : records) {

                JsonNode patient =
                        record.get("PATIENT");

                if (patient != null
                        && !patient.asText().isBlank()) {

                    return patient.asText();
                }
            }
        }

        return null;
    }

    private static Instant findLatestDocumentedTime(
            String patientLabel
    ) throws IOException {

        Path file =
                INPUT_DIR.resolve(
                        patientLabel + ".json"
                );

        ObjectMapper mapper =
                new ObjectMapper();

        JsonNode root =
                mapper.readTree(file.toFile());

        Instant latest = null;

        /*
         * observations.csv verwendet DATE.
         */
        JsonNode observations =
                root.get("observations");

        if (observations != null
                && observations.isArray()) {

            for (JsonNode observation :
                    observations) {

                latest = laterOf(
                        latest,
                        parseInstant(
                                observation.path("DATE")
                                        .asText("")
                        )
                );
            }
        }

        /*
         * conditions, medications und allergies
         * verwenden START und STOP.
         */
        String[] sections = {
                "conditions",
                "medications",
                "allergies"
        };

        for (String section : sections) {

            JsonNode records =
                    root.get(section);

            if (records == null
                    || !records.isArray()) {
                continue;
            }

            for (JsonNode record : records) {

                latest = laterOf(
                        latest,
                        parseInstant(
                                record.path("START")
                                        .asText("")
                        )
                );

                latest = laterOf(
                        latest,
                        parseInstant(
                                record.path("STOP")
                                        .asText("")
                        )
                );
            }
        }

        if (latest == null) {
            throw new IllegalStateException(
                    "Kein Bezugszeitpunkt für "
                            + patientLabel
                            + " bestimmbar."
            );
        }

        return latest;
    }

    private static List<Medication>
    readMedications() throws IOException {

        List<Medication> medications =
                new ArrayList<>();

        Path file =
                SYNTHEA_DIR.resolve(
                        "medications.csv"
                );

        try (
                Reader reader =
                        Files.newBufferedReader(
                                file,
                                StandardCharsets.UTF_8
                        );

                CSVParser parser =
                        CSVFormat.DEFAULT.builder()
                                .setHeader()
                                .setSkipHeaderRecord(true)
                                .get()
                                .parse(reader)
        ) {

            for (CSVRecord record : parser) {

                String patient =
                        record.get("PATIENT");

                Instant start =
                        parseInstant(
                                record.get("START")
                        );

                Instant stop =
                        parseInstant(
                                record.get("STOP")
                        );

                String description =
                        record.get("DESCRIPTION");

                if (patient.isBlank()
                        || start == null) {
                    continue;
                }

                medications.add(
                        new Medication(
                                patient,
                                start,
                                stop,
                                description
                        )
                );
            }
        }

        return medications;
    }

    private static boolean isActiveAt(
            Medication medication,
            Instant referenceTime
    ) {

        if (medication.start.isAfter(
                referenceTime)) {
            return false;
        }

        /*
         * Leeres STOP:
         * kein dokumentiertes Ende.
         */
        if (medication.stop == null) {
            return true;
        }

        /*
         * Die Medikation gilt am Bezugszeitpunkt
         * als aktiv, wenn STOP nicht vor dem
         * Bezugszeitpunkt liegt.
         */
        return !medication.stop.isBefore(
                referenceTime
        );
    }

    private static Instant parseInstant(
            String value
    ) {

        if (value == null
                || value.isBlank()) {
            return null;
        }

        try {
            return Instant.parse(value);
        } catch (Exception e) {
            return null;
        }
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

    private record Medication(
            String patientId,
            Instant start,
            Instant stop,
            String description
    ) {
    }
}