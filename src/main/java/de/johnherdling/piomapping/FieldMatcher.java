package de.johnherdling.piomapping;

import java.util.ArrayList;
import java.util.List;

public class FieldMatcher {

    private final NameNormalizer normalizer = new NameNormalizer();
    private final BooleanValueNormalizer booleanNormalizer = new BooleanValueNormalizer();

    public List<JsonField> findMatches(
            List<JsonField> fields,
            String searchTerm,
            JsonFieldType expectedType
    ) {

        List<JsonField> matches = new ArrayList<>();

        String normalizedSearchTerm = normalizer.normalize(searchTerm);

        for (JsonField field : fields) {

            String normalizedFieldName =
                    normalizer.normalize(field.getFieldName());

            String normalizedPath =
                    normalizer.normalize(field.getPath());

            boolean typeMatches =
                        matchesExpectedType(field, expectedType);

            boolean nameMatches =
                    normalizedFieldName.contains(normalizedSearchTerm);

            boolean pathMatches =
                    normalizedPath.contains(normalizedSearchTerm);

            if (typeMatches && (nameMatches || pathMatches)) {
                matches.add(field);
            }
        }

        return matches;
    }

    private boolean matchesExpectedType(
        JsonField field,
        JsonFieldType expectedType
        ) {

        if (field.getDataType() == expectedType) {
                return true;
        }

        if (expectedType == JsonFieldType.BOOLEAN
                && field.getDataType() == JsonFieldType.TEXT) {

                Boolean normalizedValue =
                        booleanNormalizer.normalize(
                                field.getValue().asText()
                        );

                return normalizedValue != null;
        }

        return false;
        }
}