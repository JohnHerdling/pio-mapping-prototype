package de.johnherdling.piomapping;

import java.util.ArrayList;
import java.util.List;

public class FieldValueExtractor {

    public List<String> extractValues(List<JsonField> fields) {

        List<String> values = new ArrayList<>();

        for (JsonField field : fields) {
            values.add(field.getValue().asText());
        }

        return values;
    }
}