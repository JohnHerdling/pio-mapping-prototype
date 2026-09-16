package de.johnherdling.piomapping;

import java.util.ArrayList;
import java.util.List;

public class FieldMatcher {

    private final NameNormalizer normalizer = new NameNormalizer();

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
                    field.getDataType() == expectedType;

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
}