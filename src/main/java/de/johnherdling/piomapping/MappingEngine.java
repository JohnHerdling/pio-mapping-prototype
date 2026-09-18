package de.johnherdling.piomapping;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

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

        public MappingResult execute(
                        JsonNode source,
                        MappingRule rule
    ) {

        List<JsonField> fields =
                collector.collect(source);

        List<JsonField> matches =
                matcher.findMatches(
                        fields,
                        rule.getConcept(),
                        rule.getDataType()
                );

        List<String> values =
                extractor.extractValues(matches);

        if (rule.getTransformations() != null) {

            List<String> transformedValues =
                    new ArrayList<>();

            for (String value : values) {

                String valueForTransformation = value;

                if (rule.getDataType() == JsonFieldType.BOOLEAN) {

                        Boolean normalizedValue =
                                booleanNormalizer.normalize(value);

                        if (normalizedValue != null) {
                        valueForTransformation =
                                normalizedValue.toString();
                        }
                }

                String transformedValue =
                        transformer.transform(
                                valueForTransformation,
                                rule.getTransformations()
                        );

                transformedValues.add(transformedValue);
                }

            values = transformedValues;
        }

        return new MappingResult(
                rule.getId(),
                rule.getTarget(),
                values
        );
    }
}