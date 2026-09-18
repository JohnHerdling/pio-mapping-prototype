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
                    normalizedFieldName.contains(
                            normalizedSearchTerm
                    );

            boolean pathMatches =
                    normalizedPath.contains(
                            normalizedSearchTerm
                    );

            boolean contextMatches =
                    normalizedContext.contains(
                            normalizedSearchTerm
                    );

            if (typeMatches
                    && (nameMatches
                    || pathMatches
                    || contextMatches)) {

                matches.add(field);
            }
        }

        return matches;
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

        if (expectedType == JsonFieldType.BOOLEAN) {

            Boolean normalizedValue =
                    booleanNormalizer.normalize(
                            field.getValue().asText()
                    );

            return normalizedValue != null;
        }

        if (expectedType == JsonFieldType.TEXT
                && field.getDataType() == JsonFieldType.NUMBER) {

            Boolean normalizedValue =
                    booleanNormalizer.normalize(
                            field.getValue().asText()
                    );

            return normalizedValue != null;
        }

        return false;
    }
}