package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MappingEngine {

    private final JsonFieldCollector collector =
            new JsonFieldCollector();

    private final FieldMatcher matcher =
            new FieldMatcher();

    private final FieldValueExtractor extractor =
            new FieldValueExtractor();

    private final ValueTransformer transformer =
            new ValueTransformer();

    private final BooleanValueNormalizer booleanNormalizer =
            new BooleanValueNormalizer();

    private final ConceptNormalizer conceptNormalizer =
            new ConceptNormalizer();

    public MappingResult execute(
            JsonNode source,
            MappingRule rule
    ) {

        String structuralState =
                detectStructuralState(
                        source,
                        rule
                );

        if (structuralState != null
                && rule.getTransformations() != null) {

            String transformed =
                    transformer.transform(
                            structuralState,
                            rule.getTransformations()
                    );

            if (transformed != null) {

                return new MappingResult(
                        rule.getId(),
                        rule.getTarget(),
                        List.of(transformed)
                );
            }
        }

        List<JsonField> fields =
                collector.collect(source);

        List<JsonField> matches =
                matcher.findMatches(
                        fields,
                        rule.getConcept(),
                        rule.getDataType()
                );

        /*
         * Für das fachliche Konzept "pain" werden nur
         * gültige numerische Schmerzwerte von 0 bis 10
         * berücksichtigt.
         *
         * Die Prüfung erfolgt vor der zeitlichen Auswahl,
         * damit ungültige spätere Werte einen vorherigen
         * gültigen Messwert nicht verdrängen.
         */
        if (isPainRule(rule)) {

            matches =
                    matches.stream()
                            .filter(
                                    this::isValidPainMatch
                            )
                            .toList();
        }

        if (!rule.isMultiple()
                && matches.size() > 1) {

            List<JsonField> temporalMatches =
                    matches.stream()
                            .filter(field ->
                                    field.getTemporalOrder()
                                            != null)
                            .toList();

            if (!temporalMatches.isEmpty()) {

                long latestOrder =
                        temporalMatches.stream()
                                .mapToLong(
                                        JsonField::getTemporalOrder
                                )
                                .max()
                                .orElseThrow();

                List<JsonField> latestMatches =
                        temporalMatches.stream()
                                .filter(field ->
                                        field.getTemporalOrder()
                                                == latestOrder)
                                .toList();

                JsonField selected =
                        latestMatches.stream()
                                .filter(field ->
                                        field.getFieldName()
                                                .toLowerCase()
                                                .endsWith("value"))
                                .findFirst()
                                .orElse(
                                        latestMatches.get(
                                                latestMatches.size() - 1
                                        )
                                );

                matches =
                        new ArrayList<>(
                                List.of(selected)
                        );

            } else {

                JsonField lastMatch =
                        matches.get(
                                matches.size() - 1
                        );

                matches =
                        new ArrayList<>(
                                List.of(lastMatch)
                        );
            }
        }

        List<String> values =
                extractor.extractValues(matches);

        if (rule.isMultiple()
                && isAllergyRule(rule)) {

            values.removeIf(
                    this::isAllergyDisposition
            );
        }

        if (rule.getTransformations() != null) {

            List<String> transformedValues =
                    new ArrayList<>();

            for (String value : values) {

                String valueForTransformation =
                        value;

                Boolean normalizedValue =
                        booleanNormalizer.normalize(
                                value
                        );

                if (normalizedValue != null) {

                    if (normalizedValue) {

                        if (rule.getTransformations()
                                .containsKey("YES")) {

                            valueForTransformation =
                                    "YES";

                        } else if (rule.getTransformations()
                                .containsKey("ACTIVE")) {

                            valueForTransformation =
                                    "ACTIVE";

                        } else {

                            valueForTransformation =
                                    "true";
                        }

                    } else {

                        if (rule.getTransformations()
                                .containsKey("NO")) {

                            valueForTransformation =
                                    "NO";

                        } else if (rule.getTransformations()
                                .containsKey("INACTIVE")) {

                            valueForTransformation =
                                    "INACTIVE";

                        } else {

                            valueForTransformation =
                                    "false";
                        }
                    }
                }

                String transformedValue =
                        transformer.transform(
                                valueForTransformation,
                                rule.getTransformations()
                        );

                if (transformedValue != null) {

                    transformedValues.add(
                            transformedValue
                    );
                }
            }

            values = transformedValues;
        }

        return new MappingResult(
                rule.getId(),
                rule.getTarget(),
                values
        );
    }

    private boolean isPainRule(
            MappingRule rule
    ) {

        String concept =
                conceptNormalizer.normalize(
                        rule.getConcept()
                );

        return concept.equals("pain");
    }

    private boolean isValidPainMatch(
            JsonField field
    ) {

        String fieldName =
                field.getFieldName()
                        .toLowerCase();

        /*
         * Kontextfelder wie IDs und Offsets bleiben erhalten.
         * Nur eigentliche Werte werden auf den gültigen
         * Wertebereich geprüft.
         */
        if (!fieldName.endsWith("value")) {
            return true;
        }

        String value =
                field.getValue().asText();

        try {

            double number =
                    Double.parseDouble(
                            value.trim()
                    );

            return number >= 0
                    && number <= 10;

        } catch (NumberFormatException exception) {

            return false;
        }
    }

    private boolean isAllergyRule(
            MappingRule rule
    ) {

        String concept =
                conceptNormalizer.normalize(
                        rule.getConcept()
                );

        return concept.contains("allergy");
    }

    private boolean isAllergyDisposition(
            String value
    ) {

        if (value == null) {
            return false;
        }

        String normalized =
                value.trim().toLowerCase();

        return normalized.equals(
                "allergic disposition (finding)"
        );
    }

    private String detectStructuralState(
            JsonNode source,
            MappingRule rule
    ) {

        if (source == null
                || !source.isObject()
                || rule.getTransformations() == null) {

            return null;
        }

        String normalizedConcept =
                conceptNormalizer.normalize(
                        rule.getConcept()
                );

        for (Map.Entry<String, JsonNode> entry
                : source.properties()) {

            String normalizedFieldName =
                    conceptNormalizer.normalize(
                            entry.getKey()
                    );

            JsonNode value =
                    entry.getValue();

            boolean conceptMatches =
                    normalizedFieldName.contains(
                            normalizedConcept
                    );

            if (!conceptMatches) {
                continue;
            }

            if (value.isArray()
                    && !value.isEmpty()) {

                if (rule.getTransformations()
                        .containsKey("ACTIVE")) {

                    return "ACTIVE";
                }

                if (rule.getTransformations()
                        .containsKey("YES")) {

                    return "YES";
                }
            }

            if (value.isObject()
                    && !value.isEmpty()) {

                if (rule.getTransformations()
                        .containsKey("ACTIVE")) {

                    return "ACTIVE";
                }

                if (rule.getTransformations()
                        .containsKey("YES")) {

                    return "YES";
                }
            }
        }

        return null;
    }
}