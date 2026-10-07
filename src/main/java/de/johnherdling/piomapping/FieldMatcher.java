package de.johnherdling.piomapping;

import java.util.ArrayList;
import java.util.List;

public class FieldMatcher {

    private final NameNormalizer normalizer =
            new NameNormalizer();

    private final ConceptNormalizer conceptNormalizer =
            new ConceptNormalizer();

    private final BooleanValueNormalizer booleanNormalizer =
            new BooleanValueNormalizer();

    public List<JsonField> findMatches(
            List<JsonField> fields,
            String searchTerm,
            JsonFieldType expectedType
    ) {

        List<JsonField> matches =
                new ArrayList<>();

        String normalizedSearchTerm =
                normalize(searchTerm);

        for (JsonField field : fields) {

            if (field.getFieldName().equalsIgnoreCase("system")) {
                continue;
            }

            String normalizedFieldName =
                    normalize(field.getFieldName());

            String normalizedPath =
                    normalize(field.getPath());

            String normalizedContext =
                    normalize(field.getContext());

            boolean typeMatches =
                    matchesExpectedType(
                            field,
                            expectedType
                    );

            boolean nameMatches =
                    containsConcept(
                            normalizedFieldName,
                            normalizedSearchTerm
                    );

            boolean pathMatches =
                    containsConcept(
                            normalizedPath,
                            normalizedSearchTerm
                    );

            boolean contextMatches =
                    containsConcept(
                            normalizedContext,
                            normalizedSearchTerm
                    );

            if (typeMatches
                    && (nameMatches
                    || pathMatches
                    || contextMatches)) {

                if (normalizedSearchTerm.equals("weight")
                        && isDerivedWeightMeasure(
                                normalizedContext
                        )) {
                    continue;
                }

                if (normalizedSearchTerm.equals("allergy")
                        && !isAllergyValueField(field)) {
                    continue;
                }

                /*
                 * Bei Schmerz werden Ziel-/Sollwerte
                 * ausgeschlossen. Ein "goal" beschreibt
                 * keinen tatsächlich beobachteten
                 * Schmerzwert.
                 */
                if (normalizedSearchTerm.equals("pain")
                        && isPainGoal(
                                normalizedContext
                        )) {
                    continue;
                }

                matches.add(field);
            }
        }

        return matches;
    }

    private boolean isAllergyValueField(
            JsonField field
    ) {

        String fieldName =
                normalize(field.getFieldName());

        boolean directValueName =
                fieldName.equals("description")
                        || fieldName.equals("name")
                        || fieldName.equals("display")
                        || fieldName.equals("allergen")
                        || fieldName.equals("allergy");

        boolean compoundValueName =
                fieldName.contains("allergy")
                        && (fieldName.contains("name")
                        || fieldName.contains("description")
                        || fieldName.contains("display")
                        || fieldName.contains("allergen"));

        return directValueName
                || compoundValueName;
    }

    private boolean isPainGoal(
            String context
    ) {

        if (context == null
                || context.isBlank()) {
            return false;
        }

        return context.contains("pain goal")
                && !context.contains("pain score");
    }

    private boolean containsConcept(
            String text,
            String concept
    ) {

        if (text == null
                || concept == null
                || text.isBlank()
                || concept.isBlank()) {

            return false;
        }

        return text.contains(concept);
    }

    private boolean isDerivedWeightMeasure(
            String context
    ) {

        if (context == null) {
            return false;
        }

        return context.contains("weight for length")
                || context.contains("weight for age")
                || context.contains("weight percentile");
    }

    private String normalize(String text) {

        String normalized =
                normalizer.normalize(text);

        return conceptNormalizer.normalize(
                normalized
        );
    }

    private boolean matchesExpectedType(
            JsonField field,
            JsonFieldType expectedType
    ) {

        if (field.getDataType() == expectedType) {
            return true;
        }

        if (expectedType == JsonFieldType.NUMBER
                && field.getDataType()
                == JsonFieldType.TEXT) {

            return isNumeric(
                    field.getValue().asText()
            );
        }

        if (expectedType == JsonFieldType.BOOLEAN) {

            Boolean normalizedValue =
                    booleanNormalizer.normalize(
                            field.getValue().asText()
                    );

            return normalizedValue != null;
        }

        if (expectedType == JsonFieldType.TEXT
                && field.getDataType()
                == JsonFieldType.NUMBER) {

            Boolean normalizedValue =
                    booleanNormalizer.normalize(
                            field.getValue().asText()
                    );

            return normalizedValue != null;
        }

        return false;
    }

    private boolean isNumeric(String value) {

        if (value == null
                || value.isBlank()) {
            return false;
        }

        try {

            Double.parseDouble(
                    value.trim()
            );

            return true;

        } catch (NumberFormatException exception) {

            return false;
        }
    }
}