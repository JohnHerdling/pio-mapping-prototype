package de.johnherdling.piomapping;

import java.util.Map;

public class ValueTransformer {

    public String transform(
            String sourceValue,
            Map<String, String> transformations
    ) {

        return transformations.get(sourceValue);
    }
}