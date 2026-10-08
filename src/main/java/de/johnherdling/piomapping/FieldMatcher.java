
package de.johnherdling.piomapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        List<JsonField> matches = new ArrayList<>();

        String normalizedSearchTerm = normalize(searchTerm);

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
                    matchesExpectedType(field, expectedType);

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

                if (normalizedSearchTerm.equals("pain")
                        && isPainGoal(field.getContext())) {
                    continue;
                }

                matches.add(field);
            }
        }

        return matches;
    }

    private boolean isPainGoal(String context) {

        if (context == null || context.isBlank()) {
            return false;
        }

        /*
         * Die spezifische Name-Angabe hat Vorrang
         * vor einer allgemeineren Label-Angabe.
         *
         * Beispiel:
         * Label = Pain Score/Goal
         * Name  = Pain Goal
         */
        Pattern pattern = Pattern.compile(
                "(?i)(?:^|\\s)"
                        + "[a-z0-9_]*name\\s+"
                        + "(pain\\s+(?:score|goal))"
                        + "(?=\\s|$)"
        );

        Matcher matcher = pattern.matcher(context);

        String specificName = null;

        while (matcher.find()) {
            specificName = matcher.group(1);
        }

        if (specificName != null) {
            return specificName
                    .trim()
                    .equalsIgnoreCase("pain goal");
        }

        return false;
    }

    private boolean isAllergyValueField(JsonField field) {

        String fieldName = normalize(field.getFieldName());

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

        return directValueName || compoundValueName;
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

        String normalized = normalizer.normalize(text);

        return conceptNormalizer.normalize(normalized);
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

            return isNumeric(field.getValue().asText());
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

        if (value == null || value.isBlank()) {
            return false;
        }

        try {
            Double.parseDouble(value.trim());
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}
