package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class SyntheaGroundTruthValidator {

    private static final Path EXPECTED_DIR = Path.of(
            "evaluation",
            "expected"
    );

    private static final ObjectMapper MAPPER =
            new ObjectMapper();

    public static void main(String[] args)
            throws IOException {

        System.out.println(
                "Synthea-Ground-Truth-Prüfung"
        );

        System.out.println(
                "========================================"
        );

        List<Path> files;

        try (Stream<Path> stream =
                     Files.list(EXPECTED_DIR)) {

            files = stream
                    .filter(
                            path ->
                                    path.getFileName()
                                            .toString()
                                            .matches(
                                                    "expected-patient-\\d{3}\\.json"
                                            )
                    )
                    .sorted()
                    .toList();
        }

        int fileCount = files.size();

        int m1Valid = 0;
        int m2NotEvaluable = 0;
        int m3MedicationPresent = 0;

        int m4PainPresent = 0;
        int m4NoPain = 0;

        int m5Valid = 0;

        int minAllergies =
                Integer.MAX_VALUE;

        int maxAllergies =
                0;

        int errors =
                0;

        for (Path file : files) {

            JsonNode root =
                    MAPPER.readTree(
                            file.toFile()
                    );

            String fileName =
                    file.getFileName()
                            .toString();

            /*
             * =====================================
             * M1
             * =====================================
             */
            JsonNode m1 =
                    root.path("M1");

            JsonNode m1Value =
                    m1.path("expectedValue");

            if (m1Value.isNumber()) {

                m1Valid++;

            } else {

                errors++;

                System.out.println(
                        "[FEHLER] "
                                + fileName
                                + " - M1 ohne numerischen Wert"
                );
            }

            /*
             * =====================================
             * M2
             * =====================================
             */
            JsonNode m2 =
                    root.path("M2");

            String m2Status =
                    m2.path("status")
                            .asText();

            if ("NOT_EVALUABLE".equals(
                    m2Status
            )) {

                m2NotEvaluable++;

            } else {

                errors++;

                System.out.println(
                        "[FEHLER] "
                                + fileName
                                + " - M2 Status unerwartet: "
                                + m2Status
                );
            }

            /*
             * =====================================
             * M3
             * =====================================
             */
            JsonNode m3 =
                    root.path("M3");

            JsonNode m3Value =
                    m3.get("expectedValue");

            if (m3Value != null
                    && !m3Value.isNull()
                    && "Medikation liegt vor"
                    .equals(
                            m3Value.asText()
                    )) {

                m3MedicationPresent++;

            } else {

                /*
                 * Ein null-Wert wäre grundsätzlich
                 * möglich und nicht automatisch ein
                 * Fehler.
                 *
                 * Nach unserer vorherigen Analyse
                 * erwarten wir für die aktuelle
                 * Population jedoch bei allen
                 * Patient:innen aktive Medikation.
                 */
                errors++;

                System.out.println(
                        "[FEHLER] "
                                + fileName
                                + " - M3 unerwarteter Wert: "
                                + (
                                m3Value == null
                                        ? "FEHLT"
                                        : m3Value.toString()
                        )
                );
            }

            /*
             * =====================================
             * M4
             * =====================================
             */
            JsonNode m4 =
                    root.path("M4");

            JsonNode m4Value =
                    m4.get("expectedValue");

            if (m4Value == null) {

                errors++;

                System.out.println(
                        "[FEHLER] "
                                + fileName
                                + " - M4 expectedValue fehlt"
                );

            } else if (m4Value.isNull()) {

                m4NoPain++;

            } else if ("22253000".equals(
                    m4Value.asText()
            )) {

                m4PainPresent++;

            } else {

                errors++;

                System.out.println(
                        "[FEHLER] "
                                + fileName
                                + " - M4 unerwarteter Wert: "
                                + m4Value
                );
            }

            /*
             * =====================================
             * M5
             * =====================================
             */
            JsonNode m5 =
                    root.path("M5");

            JsonNode allergyValues =
                    m5.path("expectedValue");

            if (!allergyValues.isArray()
                    || allergyValues.isEmpty()) {

                errors++;

                System.out.println(
                        "[FEHLER] "
                                + fileName
                                + " - M5 ohne konkrete Allergien"
                );

            } else {

                boolean m5EntryValid =
                        true;

                for (JsonNode allergy :
                        allergyValues) {

                    String value =
                            allergy.asText()
                                    .trim();

                    if (value.isBlank()
                            || "Allergic disposition (finding)"
                            .equals(value)) {

                        m5EntryValid =
                                false;

                        errors++;

                        System.out.println(
                                "[FEHLER] "
                                        + fileName
                                        + " - ungültiger M5-Eintrag: "
                                        + value
                        );
                    }
                }

                if (m5EntryValid) {

                    m5Valid++;

                    int allergyCount =
                            allergyValues.size();

                    minAllergies =
                            Math.min(
                                    minAllergies,
                                    allergyCount
                            );

                    maxAllergies =
                            Math.max(
                                    maxAllergies,
                                    allergyCount
                            );
                }
            }
        }

        /*
         * Falls keine gültigen M5-Datensätze
         * vorhanden wären.
         */
        if (minAllergies
                == Integer.MAX_VALUE) {

            minAllergies = 0;
        }

        System.out.println();
        System.out.println(
                "Zusammenfassung"
        );

        System.out.println(
                "========================================"
        );

        System.out.println(
                "Ground-Truth-Dateien:       "
                        + fileCount
        );

        System.out.println();

        System.out.println(
                "M1 mit numerischem Wert:    "
                        + m1Valid
        );

        System.out.println(
                "M2 NOT_EVALUABLE:           "
                        + m2NotEvaluable
        );

        System.out.println(
                "M3 Medikation liegt vor:    "
                        + m3MedicationPresent
        );

        System.out.println(
                "M4 Schmerz vorhanden:       "
                        + m4PainPresent
        );

        System.out.println(
                "M4 kein Schmerz:            "
                        + m4NoPain
        );

        System.out.println(
                "M5 mit konkreten Allergien: "
                        + m5Valid
        );

        System.out.println();

        System.out.println(
                "M5 minimale Anzahl:         "
                        + minAllergies
        );

        System.out.println(
                "M5 maximale Anzahl:         "
                        + maxAllergies
        );

        System.out.println();

        System.out.println(
                "Fehler:                     "
                        + errors
        );

        System.out.println(
                "========================================"
        );

        if (fileCount != 16) {

            System.out.println(
                    "[WARNUNG] Erwartet wurden "
                            + "16 Ground-Truth-Dateien."
            );
        }

        if (errors == 0
                && fileCount == 16) {

            System.out.println(
                    "[OK] Ground Truth ist "
                            + "formal plausibel."
            );

        } else {

            System.out.println(
                    "[ACHTUNG] Ground Truth "
                            + "vor dem Freeze prüfen."
            );
        }
    }
}